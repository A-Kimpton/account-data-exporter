package io.kimpton.accountdataexporter.exporters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kimpton.accountdataexporter.model.InventorySetupExport;
import io.kimpton.accountdataexporter.model.InventorySetups;
import io.kimpton.accountdataexporter.model.SetupItem;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

/** Builds the Inventory Setups section from the Inventory Setups hub plugin's
 *  config (group "inventorysetups", V3 storage: "setupsOrderV3_" = JSON array of
 *  setup hashes, each setup at "setupsV3_&lt;hash&gt;" as JSON). Item names are
 *  resolved at export time (the plugin doesn't persist them). */
@Slf4j
public class InventorySetupsBuilder
{
	private static final String GROUP = "inventorysetups";
	private static final String ORDER_KEY = "setupsOrderV3_";
	private static final String SETUP_PREFIX = "setupsV3_";

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Gson gson;

	// Serialized shapes of the Inventory Setups plugin (unknown fields ignored).
	private static class SItem
	{
		int id;
		Integer q;
		Boolean f;
	}

	private static class SSetup
	{
		List<SItem> inv;
		List<SItem> eq;
		List<SItem> rp;
		List<SItem> bp;
		List<SItem> qv;
		Map<String, SItem> afi;
		String name;
		String notes;
		Boolean fv;
		Integer iId;
		Integer sb;
	}

	public InventorySetups build()
	{
		Set<String> keys = new LinkedHashSet<>();
		String orderJson = configManager.getConfiguration(GROUP, ORDER_KEY);
		if (orderJson != null && !orderJson.trim().isEmpty())
		{
			try
			{
				Type t = new TypeToken<List<String>>() { }.getType();
				List<String> order = gson.fromJson(orderJson, t);
				if (order != null)
				{
					for (String hash : order)
					{
						keys.add(SETUP_PREFIX + hash);
					}
				}
			}
			catch (RuntimeException e)
			{
				log.debug("Could not parse inventory setups order", e);
			}
		}
		// Fallback / catch-all: any setup keys not covered by the order list.
		String wholePrefix = GROUP + "." + SETUP_PREFIX;
		for (String wholeKey : configManager.getConfigurationKeys(wholePrefix))
		{
			keys.add(wholeKey.substring(wholeKey.lastIndexOf(SETUP_PREFIX)));
		}

		List<InventorySetupExport> setups = new ArrayList<>();
		for (String key : keys)
		{
			String json = configManager.getConfiguration(GROUP, key);
			if (json == null || json.trim().isEmpty())
			{
				continue;
			}
			try
			{
				SSetup s = gson.fromJson(json, SSetup.class);
				if (s == null || s.name == null)
				{
					continue;
				}
				List<Integer> extra = new ArrayList<>();
				if (s.afi != null)
				{
					for (SItem it : s.afi.values())
					{
						if (it != null)
						{
							extra.add(it.id);
						}
					}
				}
				setups.add(new InventorySetupExport(
					s.name,
					s.notes,
					Boolean.TRUE.equals(s.fv),
					s.iId != null ? s.iId : 0,
					s.sb != null ? s.sb : 0,
					items(s.inv), items(s.eq), items(s.rp), items(s.bp), items(s.qv),
					extra));
			}
			catch (RuntimeException e)
			{
				log.debug("Could not parse inventory setup {}", key, e);
			}
		}
		return new InventorySetups(!setups.isEmpty(), setups);
	}

	/** Converts a serialized item list to exported items, keeping list indexes as
	 *  slots and skipping null (empty) entries. */
	private List<SetupItem> items(List<SItem> list)
	{
		List<SetupItem> out = new ArrayList<>();
		if (list == null)
		{
			return out;
		}
		for (int slot = 0; slot < list.size(); slot++)
		{
			SItem it = list.get(slot);
			if (it == null || it.id <= 0)
			{
				continue;
			}
			out.add(new SetupItem(slot, it.id, itemName(it.id),
				it.q != null ? it.q : 1, Boolean.TRUE.equals(it.f)));
		}
		return out;
	}

	private String itemName(int id)
	{
		try
		{
			return itemManager.getItemComposition(id).getName();
		}
		catch (RuntimeException e)
		{
			return "";
		}
	}
}
