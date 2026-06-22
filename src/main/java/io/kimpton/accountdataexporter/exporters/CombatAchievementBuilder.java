// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.CaTask;
import io.kimpton.accountdataexporter.model.CaTier;
import io.kimpton.accountdataexporter.model.CombatAchievements;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.StructComposition;
import net.runelite.api.gameval.VarPlayerID;

/** Combat achievement completion. Each tier is an enum of CA-task structs; per struct, param 1308 is
 *  the task name and 1306 is the task id. Completion is a bitfield spread across 20 varps, indexed by
 *  {@code id / 32} with the bit at {@code id % 32}. */
@Slf4j
public class CombatAchievementBuilder
{
	// Struct param ids on each CA-task struct.
	private static final int PARAM_TASK_NAME = 1308;
	private static final int PARAM_TASK_ID = 1306;

	// Enum ids for the six CA tiers, in order.
	private static final Map<Integer, String> TIER_ENUMS = new LinkedHashMap<>();

	static
	{
		TIER_ENUMS.put(3981, "Easy");
		TIER_ENUMS.put(3982, "Medium");
		TIER_ENUMS.put(3983, "Hard");
		TIER_ENUMS.put(3984, "Elite");
		TIER_ENUMS.put(3985, "Master");
		TIER_ENUMS.put(3986, "Grandmaster");
	}

	private static final int[] COMPLETED_VARPS = {
		VarPlayerID.CA_TASK_COMPLETED_0, VarPlayerID.CA_TASK_COMPLETED_1, VarPlayerID.CA_TASK_COMPLETED_2,
		VarPlayerID.CA_TASK_COMPLETED_3, VarPlayerID.CA_TASK_COMPLETED_4, VarPlayerID.CA_TASK_COMPLETED_5,
		VarPlayerID.CA_TASK_COMPLETED_6, VarPlayerID.CA_TASK_COMPLETED_7, VarPlayerID.CA_TASK_COMPLETED_8,
		VarPlayerID.CA_TASK_COMPLETED_9, VarPlayerID.CA_TASK_COMPLETED_10, VarPlayerID.CA_TASK_COMPLETED_11,
		VarPlayerID.CA_TASK_COMPLETED_12, VarPlayerID.CA_TASK_COMPLETED_13, VarPlayerID.CA_TASK_COMPLETED_14,
		VarPlayerID.CA_TASK_COMPLETED_15, VarPlayerID.CA_TASK_COMPLETED_16, VarPlayerID.CA_TASK_COMPLETED_17,
		VarPlayerID.CA_TASK_COMPLETED_18, VarPlayerID.CA_TASK_COMPLETED_19,
	};

	@Inject
	private Client client;

	public CombatAchievements build()
	{
		List<CaTier> tiers = new ArrayList<>();
		int totalCompleted = 0;
		int totalTasks = 0;

		for (Map.Entry<Integer, String> tier : TIER_ENUMS.entrySet())
		{
			List<CaTask> tasks = new ArrayList<>();
			int tierCompleted = 0;

			try
			{
				EnumComposition tierEnum = client.getEnum(tier.getKey());
				for (int structId : tierEnum.getIntVals())
				{
					StructComposition struct = client.getStructComposition(structId);
					String name = struct.getStringValue(PARAM_TASK_NAME);
					int id = struct.getIntValue(PARAM_TASK_ID);
					boolean completed = isCompleted(id);

					if (completed)
					{
						tierCompleted++;
					}
					tasks.add(new CaTask(id, name, completed));
				}
			}
			catch (RuntimeException e)
			{
				log.debug("Could not read combat achievement tier {}", tier.getValue(), e);
			}

			totalCompleted += tierCompleted;
			totalTasks += tasks.size();
			tiers.add(new CaTier(tier.getValue(), tierCompleted, tasks.size(), tasks));
		}

		return new CombatAchievements(totalCompleted, totalTasks, tiers);
	}

	private boolean isCompleted(int taskId)
	{
		int word = taskId / 32;
		if (word < 0 || word >= COMPLETED_VARPS.length)
		{
			return false;
		}
		return (client.getVarpValue(COMPLETED_VARPS[word]) & (1 << (taskId % 32))) != 0;
	}
}
