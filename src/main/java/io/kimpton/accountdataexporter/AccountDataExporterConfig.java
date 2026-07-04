package io.kimpton.accountdataexporter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(AccountDataExporterConfig.GROUP)
public interface AccountDataExporterConfig extends Config
{
	String GROUP = "accountdataexporter";

	@ConfigSection(
		name = "Sections",
		description = "<p width='250'>Choose which data sections are written to the export.</p>",
		position = 1
	)
	String sectionsSection = "sections";

	@ConfigSection(
		name = "Integrations",
		description = "<p width='250'>Data read from other plugins' saved settings (those plugins must be installed and have stored data).</p>",
		position = 2
	)
	String integrationsSection = "integrations";

	@ConfigItem(
		keyName = "exportIntervalTicks",
		name = "Export interval",
		description = "<p width='250'>Number of game ticks between snapshot exports. Lower values export more often.</p>",
		position = 0
	)
	default int exportIntervalTicks()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "showSidePanel",
		name = "Side panel",
		description = "<p width='250'>Show the side panel with export status, slayer block lists, and hunter rumours.</p>",
		position = 1
	)
	default boolean showSidePanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportCharacterMeta",
		name = "Character meta",
		description = "<p width='250'>Export character location, run energy, special attack, weight, and animation.</p>",
		section = sectionsSection,
		position = 1
	)
	default boolean exportCharacterMeta()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportSkills",
		name = "Skills",
		description = "<p width='250'>Export skill levels and XP.</p>",
		section = sectionsSection,
		position = 2
	)
	default boolean exportSkills()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportInventory",
		name = "Inventory",
		description = "<p width='250'>Export inventory contents.</p>",
		section = sectionsSection,
		position = 3
	)
	default boolean exportInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportEquipment",
		name = "Worn equipment",
		description = "<p width='250'>Export worn equipment.</p>",
		section = sectionsSection,
		position = 4
	)
	default boolean exportEquipment()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportBank",
		name = "Bank",
		description = "<p width='250'>Export bank contents. The bank must be opened at least once per session to load.</p>",
		section = sectionsSection,
		position = 5
	)
	default boolean exportBank()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportGrandExchange",
		name = "Grand Exchange",
		description = "<p width='250'>Export Grand Exchange offers.</p>",
		section = sectionsSection,
		position = 6
	)
	default boolean exportGrandExchange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportQuests",
		name = "Quests",
		description = "<p width='250'>Export quest states.</p>",
		section = sectionsSection,
		position = 7
	)
	default boolean exportQuests()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportAchievementDiaries",
		name = "Achievement diaries",
		description = "<p width='250'>Export achievement diary completion.</p>",
		section = sectionsSection,
		position = 8
	)
	default boolean exportAchievementDiaries()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportCombatAchievements",
		name = "Combat achievements",
		description = "<p width='250'>Export combat achievement task completion.</p>",
		section = sectionsSection,
		position = 9
	)
	default boolean exportCombatAchievements()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportSlayer",
		name = "Slayer",
		description = "<p width='250'>Export slayer task, points, unlocks, and block lists.</p>",
		section = sectionsSection,
		position = 10
	)
	default boolean exportSlayer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportCombat",
		name = "Combat",
		description = "<p width='250'>Export current combat target, duration, and damage dealt/taken. Player targets are logged as a target only, with no other data.</p>",
		section = sectionsSection,
		position = 11
	)
	default boolean exportCombat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportHunterRumours",
		name = "Hunter rumours",
		description = "<p width='250'>Export Hunter Rumour assignments and progress. Reads the Hunter Rumours plugin's saved state.</p>",
		section = integrationsSection,
		position = 1
	)
	default boolean exportHunterRumours()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportBankTags",
		name = "Bank tags",
		description = "<p width='250'>Export bank tag tabs (name, icon, tagged items). Reads RuneLite's built-in Bank Tags plugin config.</p>",
		section = integrationsSection,
		position = 2
	)
	default boolean exportBankTags()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportInventorySetups",
		name = "Inventory setups",
		description = "<p width='250'>Export saved Inventory Setups layouts (items in their positions, equipment, pouches). Reads the Inventory Setups plugin's config.</p>",
		section = integrationsSection,
		position = 3
	)
	default boolean exportInventorySetups()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportSeedVault",
		name = "Seed vault",
		description = "<p width='250'>Export seed vault contents. The vault must be opened at least once per session to load.</p>",
		section = sectionsSection,
		position = 14
	)
	default boolean exportSeedVault()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportLootingBag",
		name = "Looting bag",
		description = "<p width='250'>Export looting bag contents (loads when checked/opened).</p>",
		section = sectionsSection,
		position = 15
	)
	default boolean exportLootingBag()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportExtraContainers",
		name = "Storage containers",
		description = "<p width='250'>Export the seed box, tackle box, forestry kit and huntsman's kit (each loads when opened/checked).</p>",
		section = sectionsSection,
		position = 16
	)
	default boolean exportExtraContainers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "exportCombatAchievementTasks",
		name = "Combat achievement task list",
		description = "<p width='250'>Include the full per-task list in the combat achievements section. Off exports tier totals only, which keeps the snapshot much smaller.</p>",
		section = sectionsSection,
		position = 13
	)
	default boolean exportCombatAchievementTasks()
	{
		return true;
	}
}
