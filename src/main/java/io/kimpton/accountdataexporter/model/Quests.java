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
