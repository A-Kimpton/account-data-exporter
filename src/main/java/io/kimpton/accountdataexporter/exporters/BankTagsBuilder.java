package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.BankTagTab;
import io.kimpton.accountdataexporter.model.BankTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/** Builds the Bank Tags section from RuneLite's built-in Bank Tags plugin config
 *  (group "banktags"): the tag-tab list ("tagtabs" CSV), each tab's icon item
 *  ("icon_&lt;tag&gt;"), and the tagged item ids ("item_&lt;id&gt;" = CSV of tags).
 *  available=false when the player has no tag tabs. */
@Slf4j
public class BankTagsBuilder
{
	private static final String GROUP = "banktags";
	private static final String TAG_TABS_KEY = "tagtabs";
	private static final String ICON_PREFIX = "icon_";
	private static final String ITEM_PREFIX = "item_";

	@Inject
	private ConfigManager configManager;

	public BankTags build()
	{
		String tabsCsv = configManager.getConfiguration(GROUP, TAG_TABS_KEY);
		if (tabsCsv == null || tabsCsv.trim().isEmpty())
		{
			return new BankTags(false, new ArrayList<>());
		}

		// tag (standardized lowercase) -> tagged item ids, from every item_<id> key.
		Map<String, List<Integer>> tagItems = new HashMap<>();
		String wholePrefix = GROUP + "." + ITEM_PREFIX;
		for (String wholeKey : configManager.getConfigurationKeys(wholePrefix))
		{
			String idPart = wholeKey.substring(wholeKey.lastIndexOf(ITEM_PREFIX) + ITEM_PREFIX.length());
			int itemId;
			try
			{
				itemId = Integer.parseInt(idPart);
			}
			catch (NumberFormatException e)
			{
				continue;
			}
			String tagsCsv = configManager.getConfiguration(GROUP, ITEM_PREFIX + idPart);
			if (tagsCsv == null)
			{
				continue;
			}
			for (String tag : tagsCsv.split(","))
			{
				String std = standardize(tag);
				if (!std.isEmpty())
				{
					tagItems.computeIfAbsent(std, k -> new ArrayList<>()).add(itemId);
				}
			}
		}

		List<BankTagTab> tabs = new ArrayList<>();
		for (String tab : tabsCsv.split(","))
		{
			String std = standardize(tab);
			if (std.isEmpty())
			{
				continue;
			}
			int icon = parseInt(configManager.getConfiguration(GROUP, ICON_PREFIX + std));
			List<Integer> ids = tagItems.getOrDefault(std, new ArrayList<>());
			tabs.add(new BankTagTab(std, icon, ids));
		}
		return new BankTags(!tabs.isEmpty(), tabs);
	}

	// Mirrors RuneLite Text.standardize: lowercase, trimmed.
	private static String standardize(String s)
	{
		return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
	}

	private static int parseInt(String s)
	{
		try
		{
			return s == null ? 0 : Integer.parseInt(s.trim());
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
}
