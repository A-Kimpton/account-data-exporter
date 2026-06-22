// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
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
