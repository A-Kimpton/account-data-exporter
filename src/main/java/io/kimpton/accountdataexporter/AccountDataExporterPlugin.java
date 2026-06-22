package io.kimpton.accountdataexporter;

import com.google.inject.Provides;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Account Data Exporter",
	description = "Exports local account snapshots to JSON for spreadsheets, account tracking, and personal analytics.",
	tags = {"data", "export", "json", "account", "tracking"}
)
public class AccountDataExporterPlugin extends Plugin
{
	private static final int LOGIN_SETTLE_TICKS = 5;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private AccountDataExporterConfig config;

	@Inject
	private SnapshotService snapshotService;

	@Inject
	private SnapshotWriter snapshotWriter;

	private int ticksLoggedIn;
	private int ticksSinceExport;
	private String lastRsn = "";

	@Provides
	AccountDataExporterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AccountDataExporterConfig.class);
	}

	@Override
	protected void startUp()
	{
		log.info("Account Data Exporter v{} started.", SnapshotService.EXPORT_VERSION);
		resetSessionState();
	}

	@Override
	protected void shutDown()
	{
		log.info("Account Data Exporter stopped.");
		resetSessionState();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player localPlayer = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN || localPlayer == null)
		{
			ticksLoggedIn = 0;
			ticksSinceExport = 0;
			return;
		}

		String rsn = localPlayer.getName();
		if (rsn != null && !rsn.equals(lastRsn))
		{
			onAccountChanged(rsn);
		}

		ticksLoggedIn++;
		ticksSinceExport++;

		if (ticksLoggedIn < LOGIN_SETTLE_TICKS || ticksSinceExport < exportIntervalTicks())
		{
			return;
		}

		ticksSinceExport = 0;
		exportSnapshot();
	}

	private void exportSnapshot()
	{
		String exportDir = snapshotWriter.getExportDirectory().getAbsolutePath();
		Snapshot snapshot = snapshotService.build(config, exportDir);
		if (snapshot == null)
		{
			return;
		}

		snapshotWriter.write(snapshot.getRsn(), snapshot);
		log.debug("Exported snapshot for {} (world {}, total level {})",
			snapshot.getRsn(), snapshot.getWorld(), snapshot.getTotalLevel());
	}

	private void onAccountChanged(String rsn)
	{
		lastRsn = rsn;
		snapshotService.resetCaches();

		// Load the previous on-disk snapshot off-thread, then seed caches back on the client thread.
		executor.execute(() ->
		{
			Snapshot previous = snapshotWriter.loadPrevious(rsn);
			if (previous != null)
			{
				clientThread.invokeLater(() -> snapshotService.seedFromPrevious(previous));
			}
		});
	}

	private void resetSessionState()
	{
		ticksLoggedIn = 0;
		ticksSinceExport = 0;
		lastRsn = "";
		snapshotService.resetCaches();
	}

	private int exportIntervalTicks()
	{
		return Math.max(1, config.exportIntervalTicks());
	}
}
