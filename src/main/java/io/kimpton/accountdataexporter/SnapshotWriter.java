package io.kimpton.accountdataexporter;

import com.google.gson.Gson;
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
 *  on the shared executor, never the client thread, and land atomically via a temp-file rename. */
@Slf4j
@Singleton
class SnapshotWriter
{
	private static final String EXPORT_DIR_NAME = "account-data-exporter";
	private static final String LATEST_FILE = "latest.json";

	@Inject
	private Gson gson;

	@Inject
	private ScheduledExecutorService executor;

	File getExportDirectory()
	{
		return new File(RuneLite.RUNELITE_DIR, EXPORT_DIR_NAME);
	}

	/** Queues an asynchronous write of {@code latest.json} and {@code <rsn>.json}. */
	void write(String rsn, Snapshot snapshot)
	{
		executor.execute(() ->
		{
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
			}
			catch (IOException e)
			{
				log.warn("Failed to write account data export", e);
			}
		});
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
