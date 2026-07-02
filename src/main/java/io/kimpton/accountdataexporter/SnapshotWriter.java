package io.kimpton.accountdataexporter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

/** Reads and writes snapshot JSON under {@code .runelite/account-data-exporter/}. All writes happen
 *  on the shared executor, never the client thread, and land atomically via a temp-file rename.
 *  Unchanged snapshots are skipped (compared with volatile fields excluded), with a periodic
 *  heartbeat write so consumers that judge freshness by the embedded timestamp still see the
 *  account as live while the player idles. */
@Slf4j
@Singleton
class SnapshotWriter
{
	private static final String EXPORT_DIR_NAME = "account-data-exporter";
	private static final String LATEST_FILE = "latest.json";

	/** Fields that change every export regardless of game state; excluded from the dirty check. */
	private static final String[] VOLATILE_FIELDS = {"timestamp", "timestampIso", "fps"};

	/** Maximum age of the on-disk snapshot while logged in, even when nothing changed. Must stay
	 *  well under consumers' live-vs-stale threshold (the companion server uses 10 minutes). */
	private static final long HEARTBEAT_MS = 2 * 60 * 1000L;

	@Inject
	private Gson gson;

	@Inject
	private ScheduledExecutorService executor;

	// Dirty-check state; only touched inside the synchronized executor task.
	private String lastRsn;
	private String lastContent;
	private long lastWriteMs;

	File getExportDirectory()
	{
		return new File(RuneLite.RUNELITE_DIR, EXPORT_DIR_NAME);
	}

	/** Queues an asynchronous write of {@code latest.json} and {@code <rsn>.json}. Skips the write
	 *  when nothing but the volatile fields changed since the last one (unless the heartbeat is due),
	 *  which silences the downstream file-watch pipeline while the player idles. */
	void write(String rsn, Snapshot snapshot)
	{
		executor.execute(() ->
		{
			synchronized (this)
			{
				String content = contentKey(snapshot);
				long now = System.currentTimeMillis();
				boolean unchanged = content != null && content.equals(lastContent)
					&& rsn != null && rsn.equals(lastRsn);
				if (unchanged && now - lastWriteMs < HEARTBEAT_MS)
				{
					return;
				}

				File dir = getExportDirectory();
				if (!dir.exists() && !dir.mkdirs())
				{
					log.warn("Could not create export directory: {}", dir.getAbsolutePath());
					return;
				}

				try
				{
					writeAtomically(new File(dir, LATEST_FILE), snapshot);
					writeAtomically(accountFile(dir, rsn), snapshot);
					lastRsn = rsn;
					lastContent = content;
					lastWriteMs = now;
				}
				catch (IOException e)
				{
					log.warn("Failed to write account data export", e);
				}
			}
		});
	}

	/** Serializes the snapshot with the always-changing fields removed, for change detection.
	 *  Returns null if serialization fails (treated as "changed"). */
	private String contentKey(Snapshot snapshot)
	{
		try
		{
			JsonObject tree = gson.toJsonTree(snapshot).getAsJsonObject();
			for (String field : VOLATILE_FIELDS)
			{
				tree.remove(field);
			}
			// Containers refresh lastSeenTimestamp on every export even when their
			// items are identical — ignore it, or nothing would ever look unchanged.
			for (String container : new String[]{"inventory", "equipment", "bank"})
			{
				if (tree.get(container) != null && tree.get(container).isJsonObject())
				{
					tree.getAsJsonObject(container).remove("lastSeenTimestamp");
				}
			}
			return tree.toString();
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	/** Reads the previous on-disk snapshot for an account, or null if absent/unreadable.
	 *  Intended to seed in-memory caches off the client thread. */
	Snapshot loadPrevious(String rsn)
	{
		File file = accountFile(getExportDirectory(), rsn);
		if (!file.exists())
		{
			return null;
		}

		try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))
		{
			return gson.fromJson(reader, Snapshot.class);
		}
		catch (IOException | RuntimeException e)
		{
			log.debug("Could not read previous snapshot {}", file.getAbsolutePath(), e);
			return null;
		}
	}

	private void writeAtomically(File target, Snapshot snapshot) throws IOException
	{
		File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
		try (Writer writer = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8))
		{
			gson.toJson(snapshot, writer);
		}

		try
		{
			Files.move(tmp.toPath(), target.toPath(),
				StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		}
		catch (IOException e)
		{
			// Some filesystems don't support atomic moves; fall back to a plain replace.
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private File accountFile(File dir, String rsn)
	{
		return new File(dir, safeFileName(rsn) + ".json");
	}

	private String safeFileName(String rsn)
	{
		return rsn.replaceAll("[^a-zA-Z0-9 _-]", "_");
	}
}
