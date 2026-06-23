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
		description = "Choose which data sections are written to the export.",
		position = 1
	)
	String sectionsSection = "sections";

	@ConfigItem(
		keyName = "exportIntervalTicks",
		name = "Export interval",
		description = "Number of game ticks between snapshot exports. Lower values export more often.",
		position = 0
	)
	default int exportIntervalTicks()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "exportCharacterMeta",
		name = "Character meta",
		description = "Export character location, run energy, special attack, weight, and animation.",
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
		description = "Export skill levels and XP.",
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
		description = "Export inventory contents.",
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
		description = "Export worn equipment.",
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
		description = "Export bank contents. The bank must be opened at least once per session to load.",
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
		description = "Export Grand Exchange offers.",
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
		description = "Export quest states.",
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
		description = "Export achievement diary completion.",
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
		description = "Export combat achievement task completion.",
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
		description = "Export slayer task, points, unlocks, and block lists.",
		section = sectionsSection,
		position = 10
	)
	default boolean exportSlayer()
	{
		return true;
	}
}
