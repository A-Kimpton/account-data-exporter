package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.CaTask;
import io.kimpton.accountdataexporter.model.CaTier;
import io.kimpton.accountdataexporter.model.CombatAchievements;
import java.util.ArrayList;
import java.util.HashMap;
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

	// Task definitions (id + name) are static game data — read the enum/struct
	// tables once per tier and only re-check the completion varps per export,
	// instead of walking ~550 struct compositions on the client thread each time.
	private final Map<Integer, List<TaskDef>> tierDefCache = new HashMap<>();
	private boolean warnedEmpty;

	private static final class TaskDef
	{
		final int id;
		final String name;

		TaskDef(int id, String name)
		{
			this.id = id;
			this.name = name;
		}
	}

	/** Builds the CA section. When {@code includeTasks} is false only tier totals are exported
	 *  (the per-task list dominates snapshot size). */
	public CombatAchievements build(boolean includeTasks)
	{
		List<CaTier> tiers = new ArrayList<>();
		int totalCompleted = 0;
		int totalTasks = 0;

		for (Map.Entry<Integer, String> tier : TIER_ENUMS.entrySet())
		{
			List<TaskDef> defs = tierDefs(tier.getKey(), tier.getValue());
			List<CaTask> tasks = includeTasks ? new ArrayList<>(defs.size()) : null;
			int tierCompleted = 0;

			for (TaskDef def : defs)
			{
				boolean completed = isCompleted(def.id);
				if (completed)
				{
					tierCompleted++;
				}
				if (tasks != null)
				{
					tasks.add(new CaTask(def.id, def.name, completed));
				}
			}

			totalCompleted += tierCompleted;
			totalTasks += defs.size();
			tiers.add(new CaTier(tier.getValue(), tierCompleted, defs.size(), tasks));
		}

		if (totalTasks == 0 && !warnedEmpty)
		{
			warnedEmpty = true;
			log.warn("Combat achievements resolved 0 tasks — the tier enum/param ids may have shifted in a game update.");
		}

		return new CombatAchievements(totalCompleted, totalTasks, tiers);
	}

	/** Returns the (cached) task definitions for a tier. A failed/empty read is not cached, so a
	 *  tier that wasn't loaded yet is retried on the next export. */
	private List<TaskDef> tierDefs(int enumId, String tierName)
	{
		List<TaskDef> cached = tierDefCache.get(enumId);
		if (cached != null)
		{
			return cached;
		}

		List<TaskDef> defs = new ArrayList<>();
		try
		{
			EnumComposition tierEnum = client.getEnum(enumId);
			for (int structId : tierEnum.getIntVals())
			{
				StructComposition struct = client.getStructComposition(structId);
				defs.add(new TaskDef(struct.getIntValue(PARAM_TASK_ID), struct.getStringValue(PARAM_TASK_NAME)));
			}
		}
		catch (RuntimeException e)
		{
			log.debug("Could not read combat achievement tier {}", tierName, e);
			return defs;
		}
		if (!defs.isEmpty())
		{
			tierDefCache.put(enumId, defs);
		}
		return defs;
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
