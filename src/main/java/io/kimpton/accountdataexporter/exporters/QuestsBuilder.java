package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.QuestEntry;
import io.kimpton.accountdataexporter.model.Quests;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

public class QuestsBuilder
{
	@Inject
	private Client client;

	public Quests build()
	{
		int notStarted = 0;
		int inProgress = 0;
		int finished = 0;
		int unknown = 0;
		List<QuestEntry> entries = new ArrayList<>();

		for (Quest quest : Quest.values())
		{
			QuestState state;
			try
			{
				state = quest.getState(client);
			}
			catch (RuntimeException e)
			{
				state = null;
			}

			if (state == QuestState.NOT_STARTED)
			{
				notStarted++;
			}
			else if (state == QuestState.IN_PROGRESS)
			{
				inProgress++;
			}
			else if (state == QuestState.FINISHED)
			{
				finished++;
			}
			else
			{
				unknown++;
			}

			entries.add(new QuestEntry(quest.name(), quest.getName(), state != null ? state.name() : "UNKNOWN"));
		}

		return new Quests(notStarted, inProgress, finished, unknown,
			notStarted + inProgress + finished + unknown, entries);
	}
}
