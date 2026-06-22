package io.kimpton.accountdataexporter.model;

import java.util.Map;
import lombok.Value;

@Value
public class SlayerState
{
	int points;
	int tasksCompletedStreak;
	int wildernessTasksCompleted;
	SlayerTask currentTask;
	Map<String, Integer> unlocks;
	Map<String, Integer> taskExtensions;
	Map<String, Integer> autoKill;
	Map<String, SlayerMasterBlocks> blockLists;
}
