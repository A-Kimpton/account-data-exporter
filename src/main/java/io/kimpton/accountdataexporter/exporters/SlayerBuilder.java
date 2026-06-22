// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.SlayerBlockSlot;
import io.kimpton.accountdataexporter.model.SlayerMasterBlocks;
import io.kimpton.accountdataexporter.model.SlayerState;
import io.kimpton.accountdataexporter.model.SlayerTask;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/** Slayer task, reward unlocks, task extensions, auto-kill toggles, and per-master block lists.
 *  Task/creature names are resolved from the SlayerTask DB table. */
@Slf4j
public class SlayerBuilder
{
	// SLAYER_MASTER varbit value -> assigning master. 0 = no master assigned.
	private static final String[] SLAYER_MASTER_NAMES = {
		null, "Turael", "Mazchna", "Vannaka", "Chaeldar", "Nieve", "Duradel", "Krystilia", "Konar quo Maten"
	};

	@Inject
	private Client client;

	public SlayerState build()
	{
		return new SlayerState(
			client.getVarbitValue(VarbitID.SLAYER_POINTS),
			client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED),
			client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED),
			buildCurrentTask(),
			buildUnlocks(),
			buildTaskExtensions(),
			buildAutoKill(),
			buildBlockLists());
	}

	private SlayerTask buildCurrentTask()
	{
		int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
		int amountRemaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		int initialAmount = client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL);
		int areaId = client.getVarpValue(VarPlayerID.SLAYER_AREA);
		int masterId = client.getVarbitValue(VarbitID.SLAYER_MASTER);
		int bossId = client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID);

		return new SlayerTask(
			taskId > 0 && amountRemaining > 0,
			taskId,
			lookupTaskName(taskId),
			amountRemaining,
			initialAmount,
			areaId,
			masterId,
			masterName(masterId),
			bossId);
	}

	private String masterName(int masterId)
	{
		if (masterId > 0 && masterId < SLAYER_MASTER_NAMES.length)
		{
			return SLAYER_MASTER_NAMES[masterId];
		}
		return null;
	}

	// Reward-shop unlocks (the "Unlock" tab). Each varbit is non-zero once purchased.
	private Map<String, Integer> buildUnlocks()
	{
		Map<String, Integer> u = new LinkedHashMap<>();
		u.put("redDragons", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_REDDRAGONS));
		u.put("mithrilDragons", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_MITHRILDRAGONS));
		u.put("aviansies", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_AVIANSIES));
		u.put("tzhaar", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_TZHAAR));
		u.put("lizardmen", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_LIZARDMEN));
		u.put("basilisks", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_BASILISK));
		u.put("vampyres", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_VAMPYRES));
		u.put("warpedCreatures", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_WARPED_CREATURES));
		u.put("aquanites", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_AQUANITES));
		u.put("gryphons", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_GRYPHONS));
		u.put("bosses", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_BOSSES));
		u.put("superiorMonsters", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_SUPERIORMOBS));
		u.put("grotesqueGuardiansKills", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_GROTESQUEKILLS));
		u.put("notedMithrilBars", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_NOTEDMITHRILBARS));
		u.put("fossilIslandWyvernBlock", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_FOSSILWYVERNBLOCK));
		u.put("wildernessExtraTasks", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_WILDY_EXTRATASKS));
		u.put("slayerHelmet", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_HOODED));
		u.put("taskStorage", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_STORAGE));
		u.put("slayerHelmetBlack", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_BLACK));
		u.put("slayerHelmetGreen", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_GREEN));
		u.put("slayerHelmetRed", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_RED));
		u.put("slayerHelmetPurple", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_PURPLE));
		u.put("slayerHelmetTurquoise", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_TURQUOISE));
		u.put("slayerHelmetHydra", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_HYDRA));
		u.put("slayerHelmetTwisted", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_TWISTED));
		u.put("slayerHelmetAraxyte", client.getVarbitValue(VarbitID.SLAYER_UNLOCK_HELM_ARAXYTE));
		return u;
	}

	// Task extensions (the "Extend" unlocks). Each varbit is non-zero once the longer assignment is purchased.
	private Map<String, Integer> buildTaskExtensions()
	{
		Map<String, Integer> e = new LinkedHashMap<>();
		e.put("aberrantSpectres", client.getVarbitValue(VarbitID.SLAYER_LONGER_ABERRANTSPECTRES));
		e.put("abyssalDemons", client.getVarbitValue(VarbitID.SLAYER_LONGER_ABYSSALDEMONS));
		e.put("adamantDragons", client.getVarbitValue(VarbitID.SLAYER_LONGER_ADAMANTDRAGONS));
		e.put("ankou", client.getVarbitValue(VarbitID.SLAYER_LONGER_ANKOU));
		e.put("aquanites", client.getVarbitValue(VarbitID.SLAYER_LONGER_AQUANITES));
		e.put("araxytes", client.getVarbitValue(VarbitID.SLAYER_LONGER_ARAXYTES));
		e.put("aviansies", client.getVarbitValue(VarbitID.SLAYER_LONGER_AVIANSIES));
		e.put("basilisks", client.getVarbitValue(VarbitID.SLAYER_LONGER_BASILISK));
		e.put("blackDemons", client.getVarbitValue(VarbitID.SLAYER_LONGER_BLACKDEMONS));
		e.put("blackDragons", client.getVarbitValue(VarbitID.SLAYER_LONGER_BLACKDRAGONS));
		e.put("bloodveld", client.getVarbitValue(VarbitID.SLAYER_LONGER_BLOODVELD));
		e.put("caveHorrors", client.getVarbitValue(VarbitID.SLAYER_LONGER_CAVEHORRORS));
		e.put("caveKraken", client.getVarbitValue(VarbitID.SLAYER_LONGER_CAVEKRAKEN));
		e.put("custodians", client.getVarbitValue(VarbitID.SLAYER_LONGER_CUSTODIANS));
		e.put("darkBeasts", client.getVarbitValue(VarbitID.SLAYER_LONGER_DARKBEASTS));
		e.put("dustDevils", client.getVarbitValue(VarbitID.SLAYER_LONGER_DUSTDEVILS));
		e.put("fossilIslandWyverns", client.getVarbitValue(VarbitID.SLAYER_LONGER_FOSSILWYVERNS));
		e.put("gargoyles", client.getVarbitValue(VarbitID.SLAYER_LONGER_GARGOYLES));
		e.put("greaterDemons", client.getVarbitValue(VarbitID.SLAYER_LONGER_GREATERDEMONS));
		e.put("metalDragons", client.getVarbitValue(VarbitID.SLAYER_LONGER_METALDRAGONS));
		e.put("mithrilDragons", client.getVarbitValue(VarbitID.SLAYER_LONGER_MITHRILDRAGONS));
		e.put("nechryael", client.getVarbitValue(VarbitID.SLAYER_LONGER_NECHRYAEL));
		e.put("revenants", client.getVarbitValue(VarbitID.SLAYER_LONGER_REVENANTS));
		e.put("runeDragons", client.getVarbitValue(VarbitID.SLAYER_LONGER_RUNEDRAGONS));
		e.put("scabarites", client.getVarbitValue(VarbitID.SLAYER_LONGER_SCABARITES));
		e.put("skeletalWyverns", client.getVarbitValue(VarbitID.SLAYER_LONGER_SKELETALWYVERNS));
		e.put("spiritualCreatures", client.getVarbitValue(VarbitID.SLAYER_LONGER_SPIRITUALGWD));
		e.put("suqahs", client.getVarbitValue(VarbitID.SLAYER_LONGER_SUQAH));
		e.put("vampyres", client.getVarbitValue(VarbitID.SLAYER_LONGER_VAMPYRES));
		e.put("wyrms", client.getVarbitValue(VarbitID.SLAYER_LONGER_WYRMS));
		return e;
	}

	// Auto-kill toggles for tasks with a "do not return" reward unlock.
	private Map<String, Integer> buildAutoKill()
	{
		Map<String, Integer> a = new LinkedHashMap<>();
		a.put("desertLizards", client.getVarbitValue(VarbitID.SLAYER_AUTOKILL_DESERTLIZARDS));
		a.put("gargoyles", client.getVarbitValue(VarbitID.SLAYER_AUTOKILL_GARGOYLES));
		a.put("rockslugs", client.getVarbitValue(VarbitID.SLAYER_AUTOKILL_ROCKSLUGS));
		a.put("zygomites", client.getVarbitValue(VarbitID.SLAYER_AUTOKILL_ZYGOMITES));
		return a;
	}

	private Map<String, SlayerMasterBlocks> buildBlockLists()
	{
		Map<String, SlayerMasterBlocks> masters = new LinkedHashMap<>();

		masters.put("turael", masterBlocks(
			VarbitID.SLAYER_BLOCKED_TURAEL_1, VarbitID.SLAYER_BLOCKED_TURAEL_2, VarbitID.SLAYER_BLOCKED_TURAEL_3,
			VarbitID.SLAYER_BLOCKED_TURAEL_4, VarbitID.SLAYER_BLOCKED_TURAEL_5, VarbitID.SLAYER_BLOCKED_TURAEL_6,
			VarbitID.SLAYER_BLOCKED_TURAEL_DIARY));
		masters.put("mazchna", masterBlocks(
			VarbitID.SLAYER_BLOCKED_MAZCHNA_1, VarbitID.SLAYER_BLOCKED_MAZCHNA_2, VarbitID.SLAYER_BLOCKED_MAZCHNA_3,
			VarbitID.SLAYER_BLOCKED_MAZCHNA_4, VarbitID.SLAYER_BLOCKED_MAZCHNA_5, VarbitID.SLAYER_BLOCKED_MAZCHNA_6,
			VarbitID.SLAYER_BLOCKED_MAZCHNA_DIARY));
		masters.put("vannaka", masterBlocks(
			VarbitID.SLAYER_BLOCKED_VANNAKA_1, VarbitID.SLAYER_BLOCKED_VANNAKA_2, VarbitID.SLAYER_BLOCKED_VANNAKA_3,
			VarbitID.SLAYER_BLOCKED_VANNAKA_4, VarbitID.SLAYER_BLOCKED_VANNAKA_5, VarbitID.SLAYER_BLOCKED_VANNAKA_6,
			VarbitID.SLAYER_BLOCKED_VANNAKA_DIARY));
		masters.put("chaeldar", masterBlocks(
			VarbitID.SLAYER_BLOCKED_CHAELDAR_1, VarbitID.SLAYER_BLOCKED_CHAELDAR_2, VarbitID.SLAYER_BLOCKED_CHAELDAR_3,
			VarbitID.SLAYER_BLOCKED_CHAELDAR_4, VarbitID.SLAYER_BLOCKED_CHAELDAR_5, VarbitID.SLAYER_BLOCKED_CHAELDAR_6,
			VarbitID.SLAYER_BLOCKED_CHAELDAR_DIARY));
		masters.put("konar", masterBlocks(
			VarbitID.SLAYER_BLOCKED_KONAR_1, VarbitID.SLAYER_BLOCKED_KONAR_2, VarbitID.SLAYER_BLOCKED_KONAR_3,
			VarbitID.SLAYER_BLOCKED_KONAR_4, VarbitID.SLAYER_BLOCKED_KONAR_5, VarbitID.SLAYER_BLOCKED_KONAR_6,
			VarbitID.SLAYER_BLOCKED_KONAR_DIARY));
		masters.put("nieve", masterBlocks(
			VarbitID.SLAYER_BLOCKED_NIEVE_1, VarbitID.SLAYER_BLOCKED_NIEVE_2, VarbitID.SLAYER_BLOCKED_NIEVE_3,
			VarbitID.SLAYER_BLOCKED_NIEVE_4, VarbitID.SLAYER_BLOCKED_NIEVE_5, VarbitID.SLAYER_BLOCKED_NIEVE_6,
			VarbitID.SLAYER_BLOCKED_NIEVE_DIARY));
		masters.put("duradel", masterBlocks(
			VarbitID.SLAYER_BLOCKED_DURADEL_1, VarbitID.SLAYER_BLOCKED_DURADEL_2, VarbitID.SLAYER_BLOCKED_DURADEL_3,
			VarbitID.SLAYER_BLOCKED_DURADEL_4, VarbitID.SLAYER_BLOCKED_DURADEL_5, VarbitID.SLAYER_BLOCKED_DURADEL_6,
			VarbitID.SLAYER_BLOCKED_DURADEL_DIARY));
		masters.put("krystilia", masterBlocks(
			VarbitID.SLAYER_BLOCKED_KRYSTILIA_1, VarbitID.SLAYER_BLOCKED_KRYSTILIA_2, VarbitID.SLAYER_BLOCKED_KRYSTILIA_3,
			VarbitID.SLAYER_BLOCKED_KRYSTILIA_4, VarbitID.SLAYER_BLOCKED_KRYSTILIA_5, VarbitID.SLAYER_BLOCKED_KRYSTILIA_6,
			VarbitID.SLAYER_BLOCKED_KRYSTILIA_DIARY));

		return masters;
	}

	private SlayerMasterBlocks masterBlocks(int s1, int s2, int s3, int s4, int s5, int s6, int diary)
	{
		List<SlayerBlockSlot> slots = new ArrayList<>();
		int[] slotVarbits = {s1, s2, s3, s4, s5, s6};
		for (int i = 0; i < slotVarbits.length; i++)
		{
			slots.add(blockSlot(i + 1, slotVarbits[i]));
		}
		return new SlayerMasterBlocks(slots, blockSlot(0, diary));
	}

	private SlayerBlockSlot blockSlot(int slot, int varbitId)
	{
		int taskId = client.getVarbitValue(varbitId);
		return new SlayerBlockSlot(slot, taskId > 0, taskId, taskId > 0 ? lookupTaskName(taskId) : null);
	}

	// Resolves the task/creature name for a SlayerTask id via RuneLite's DB-table API; null if unknown.
	private String lookupTaskName(int taskId)
	{
		if (taskId <= 0)
		{
			return null;
		}

		try
		{
			List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
			if (rows == null || rows.isEmpty())
			{
				return null;
			}

			Object[] nameField = client.getDBTableField(rows.get(0), DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
			if (nameField == null || nameField.length == 0 || nameField[0] == null)
			{
				return null;
			}

			return String.valueOf(nameField[0]);
		}
		catch (Exception e)
		{
			log.debug("Could not resolve slayer task name for id {}", taskId, e);
			return null;
		}
	}
}
