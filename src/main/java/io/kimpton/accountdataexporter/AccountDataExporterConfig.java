/*
 * Copyright (c) 2026, Antony Kimpton
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		keyName = "exportSkills",
		name = "Skills",
		description = "Export skill levels and XP.",
		section = sectionsSection,
		position = 1
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
		position = 2
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
		position = 3
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
		position = 4
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
		position = 5
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
		position = 6
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
		position = 7
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
		position = 8
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
		position = 9
	)
	default boolean exportSlayer()
	{
		return true;
	}
}
