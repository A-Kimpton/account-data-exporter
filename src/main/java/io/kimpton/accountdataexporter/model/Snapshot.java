package io.kimpton.accountdataexporter.model;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** Top-level export document written to disk. Sections are nullable: a section disabled in config
 *  is omitted entirely (the consumer should treat an absent key as "not exported"). */
@Value
@Builder
public class Snapshot
{
	int schemaVersion;
	String exportVersion;
	long timestamp;
	String timestampIso;

	String rsn;
	int world;
	String gameState;
	int combatLevel;
	int totalLevel;
	long totalXp;
	int accountType;
	String accountTypeName;

	int fps;
	int exportIntervalTicks;
	String exportDirectory;

	Status status;
	Location location;
	Animation animation;
	CombatState combat;

	long carriedValue;
	long knownAccountValue;
	long grandExchangeAccountValueEstimate;
	long knownAccountValueWithGeEstimate;

	Map<String, SkillStat> skills;
	Container inventory;
	Container equipment;
	Container bank;
	GrandExchange grandExchange;
	Quests quests;
	AchievementDiaries achievementDiaries;
	CombatAchievements combatAchievements;
	SlayerState slayer;
	HunterRumours hunterRumours;
}
