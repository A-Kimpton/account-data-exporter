// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter;

import io.kimpton.accountdataexporter.exporters.GrandExchangeBuilder;
import io.kimpton.accountdataexporter.model.Animation;
import io.kimpton.accountdataexporter.model.Container;
import io.kimpton.accountdataexporter.model.GrandExchange;
import io.kimpton.accountdataexporter.model.ItemStack;
import io.kimpton.accountdataexporter.model.Location;
import io.kimpton.accountdataexporter.model.SkillStat;
import io.kimpton.accountdataexporter.model.Snapshot;
import io.kimpton.accountdataexporter.model.Status;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ItemManager;

/** Reads the current game state on the client thread and assembles a typed {@link Snapshot}.
 *  Section builders honour the config toggles; a disabled section is left null and omitted. */
@Singleton
class SnapshotService
{
	static final int SCHEMA_VERSION = 1;
	static final String EXPORT_VERSION = "0.1.0";

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private GrandExchangeBuilder grandExchangeBuilder;

	private final ContainerCache inventoryCache = new ContainerCache();
	private final ContainerCache equipmentCache = new ContainerCache();
	private final ContainerCache bankCache = new ContainerCache();

	void resetCaches()
	{
		inventoryCache.reset();
		equipmentCache.reset();
		bankCache.reset();
	}

	/** Seeds the container caches from a previously persisted snapshot (call off the client thread). */
	void seedFromPrevious(Snapshot previous)
	{
		if (previous == null)
		{
			return;
		}
		inventoryCache.seed(previous.getInventory());
		equipmentCache.seed(previous.getEquipment());
		bankCache.seed(previous.getBank());
	}

	Snapshot build(AccountDataExporterConfig config, String exportDirectory)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}

		Snapshot.SnapshotBuilder b = Snapshot.builder()
			.schemaVersion(SCHEMA_VERSION)
			.exportVersion(EXPORT_VERSION)
			.timestamp(System.currentTimeMillis())
			.timestampIso(Instant.now().toString())
			.rsn(localPlayer.getName())
			.world(client.getWorld())
			.gameState(client.getGameState().name())
			.combatLevel(localPlayer.getCombatLevel())
			.totalLevel(calculateTotalLevel())
			.totalXp(calculateTotalXp())
			.fps(client.getFPS())
			.exportIntervalTicks(config.exportIntervalTicks())
			.exportDirectory(exportDirectory)
			.status(buildStatus())
			.location(buildLocation(localPlayer))
			.animation(buildAnimation(localPlayer));

		int accountType = client.getVarbitValue(VarbitID.IRONMAN);
		b.accountType(accountType).accountTypeName(accountTypeName(accountType));

		if (config.exportSkills())
		{
			b.skills(buildSkills());
		}

		Container inventory = config.exportInventory() ? inventoryCache.choose(buildContainer(InventoryID.INVENTORY)) : null;
		Container equipment = config.exportEquipment() ? equipmentCache.choose(buildContainer(InventoryID.EQUIPMENT)) : null;
		Container bank = config.exportBank() ? bankCache.choose(buildContainer(InventoryID.BANK)) : null;
		b.inventory(inventory).equipment(equipment).bank(bank);

		long inventoryValue = containerValue(inventory);
		long equipmentValue = containerValue(equipment);
		long bankValue = containerValue(bank);
		long carriedValue = inventoryValue + equipmentValue;
		long knownAccountValue = bankValue + carriedValue;
		b.carriedValue(carriedValue).knownAccountValue(knownAccountValue);

		long geEstimate = 0;
		if (config.exportGrandExchange())
		{
			GrandExchange grandExchange = grandExchangeBuilder.build();
			geEstimate = grandExchange.getAccountValueEstimate();
			b.grandExchange(grandExchange);
		}
		b.grandExchangeAccountValueEstimate(geEstimate)
			.knownAccountValueWithGeEstimate(knownAccountValue + geEstimate);

		return b.build();
	}

	private static long containerValue(Container container)
	{
		return container != null && container.isLoaded() ? container.getValue() : 0;
	}

	// accountTypeName maps the IRONMAN account-type varbit to a stable slug.
	private String accountTypeName(int v)
	{
		switch (v)
		{
			case 0: return "normal";
			case 1: return "ironman";
			case 2: return "ultimate_ironman";
			case 3: return "hardcore_ironman";
			case 4: return "group_ironman";
			case 5: return "hardcore_group_ironman";
			case 6: return "unranked_group_ironman";
			default: return "";
		}
	}

	private Map<String, SkillStat> buildSkills()
	{
		Map<String, SkillStat> skills = new LinkedHashMap<>();
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			skills.put(skill.getName(), new SkillStat(
				client.getRealSkillLevel(skill),
				client.getBoostedSkillLevel(skill),
				client.getSkillExperience(skill)));
		}
		return skills;
	}

	private int calculateTotalLevel()
	{
		int total = 0;
		for (Skill skill : Skill.values())
		{
			if (skill != Skill.OVERALL)
			{
				total += client.getRealSkillLevel(skill);
			}
		}
		return total;
	}

	private long calculateTotalXp()
	{
		long total = 0;
		for (Skill skill : Skill.values())
		{
			if (skill != Skill.OVERALL)
			{
				total += client.getSkillExperience(skill);
			}
		}
		return total;
	}

	private Container buildContainer(InventoryID inventoryId)
	{
		ItemContainer container = client.getItemContainer(inventoryId);
		if (container == null)
		{
			return new Container(false, false, 0, 0, 0, new ArrayList<>());
		}

		long totalValue = 0;
		int itemCount = 0;
		List<ItemStack> items = new ArrayList<>();
		Item[] containerItems = container.getItems();

		for (int slot = 0; slot < containerItems.length; slot++)
		{
			Item item = containerItems[slot];
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			int id = item.getId();
			int quantity = item.getQuantity();
			int price = itemManager.getItemPrice(id);
			long value = (long) price * quantity;

			totalValue += value;
			itemCount++;
			items.add(new ItemStack(slot, id, itemManager.getItemComposition(id).getName(), quantity, price, value));
		}

		return new Container(true, false, System.currentTimeMillis(), totalValue, itemCount, items);
	}

	private Status buildStatus()
	{
		int runEnergy = client.getEnergy();
		return new Status(
			runEnergy,
			runEnergy / 100.0,
			client.getWeight(),
			client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT),
			client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1);
	}

	private Location buildLocation(Player localPlayer)
	{
		WorldPoint wp = localPlayer.getWorldLocation();
		if (wp == null)
		{
			return new Location(false, 0, 0, 0, 0, 0, 0);
		}
		return new Location(true, wp.getX(), wp.getY(), wp.getPlane(),
			wp.getRegionID(), wp.getRegionX(), wp.getRegionY());
	}

	private Animation buildAnimation(Player localPlayer)
	{
		return new Animation(
			localPlayer.getAnimation(),
			localPlayer.getPoseAnimation(),
			localPlayer.getIdlePoseAnimation(),
			localPlayer.getOrientation(),
			localPlayer.getCurrentOrientation());
	}
}
