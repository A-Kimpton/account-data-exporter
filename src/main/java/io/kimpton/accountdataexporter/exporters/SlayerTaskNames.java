package io.kimpton.accountdataexporter.exporters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Bundled slayer task id → name mapping (dumped from the game cache's
 *  slayer_task DB table). Used as a fallback when the live DB lookup yields
 *  nothing — which is what happens for block-list ids in practice. */
final class SlayerTaskNames
{
	private static final Properties NAMES = load();

	private SlayerTaskNames()
	{
	}

	static String name(int taskId)
	{
		return taskId > 0 ? NAMES.getProperty(String.valueOf(taskId)) : null;
	}

	private static Properties load()
	{
		Properties p = new Properties();
		try (InputStream in = SlayerTaskNames.class.getResourceAsStream(
			"/io/kimpton/accountdataexporter/slayer-task-names.properties"))
		{
			if (in != null)
			{
				p.load(in);
			}
		}
		catch (IOException e)
		{
			// Names then resolve to null; consumers fall back to ids.
		}
		return p;
	}
}
