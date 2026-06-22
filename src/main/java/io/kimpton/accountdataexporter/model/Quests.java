// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class Quests
{
	int notStarted;
	int inProgress;
	int finished;
	int unknown;
	int total;
	List<QuestEntry> entries;
}
