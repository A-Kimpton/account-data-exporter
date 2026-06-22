// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.AchievementDiaries;
import io.kimpton.accountdataexporter.model.DiaryRegion;
import io.kimpton.accountdataexporter.model.DiaryTier;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/** Diary completion is read from the per-region reward varbits, which become non-zero once a tier's
 *  reward has been claimed. */
public class AchievementDiaryBuilder
{
	@Inject
	private Client client;

	public AchievementDiaries build()
	{
		List<DiaryRegion> regions = new ArrayList<>();

		regions.add(region("ardougne", "Ardougne", VarbitID.ARDOUGNE_EASY_REWARD, VarbitID.ARDOUGNE_MEDIUM_REWARD, VarbitID.ARDOUGNE_HARD_REWARD, VarbitID.ARDOUGNE_ELITE_REWARD));
		regions.add(region("desert", "Desert", VarbitID.DESERT_EASY_REWARD, VarbitID.DESERT_MEDIUM_REWARD, VarbitID.DESERT_HARD_REWARD, VarbitID.DESERT_ELITE_REWARD));
		regions.add(region("falador", "Falador", VarbitID.FALADOR_EASY_REWARD, VarbitID.FALADOR_MEDIUM_REWARD, VarbitID.FALADOR_HARD_REWARD, VarbitID.FALADOR_ELITE_REWARD));
		regions.add(region("fremennik", "Fremennik", VarbitID.FREMENNIK_EASY_REWARD, VarbitID.FREMENNIK_MEDIUM_REWARD, VarbitID.FREMENNIK_HARD_REWARD, VarbitID.FREMENNIK_ELITE_REWARD));
		regions.add(region("kandarin", "Kandarin", VarbitID.KANDARIN_EASY_REWARD, VarbitID.KANDARIN_MEDIUM_REWARD, VarbitID.KANDARIN_HARD_REWARD, VarbitID.KANDARIN_ELITE_REWARD));
		regions.add(region("karamja", "Karamja", VarbitID.ATJUN_EASY_REWARD, VarbitID.ATJUN_MED_REWARD, VarbitID.ATJUN_HARD_REWARD, VarbitID.KARAMJA_ELITE_REWARD));
		regions.add(region("kourend_kebos", "Kourend & Kebos", VarbitID.KOUREND_EASY_REWARD, VarbitID.KOUREND_MEDIUM_REWARD, VarbitID.KOUREND_HARD_REWARD, VarbitID.KOUREND_ELITE_REWARD));
		regions.add(region("lumbridge_draynor", "Lumbridge & Draynor", VarbitID.LUMBRIDGE_EASY_REWARD, VarbitID.LUMBRIDGE_MEDIUM_REWARD, VarbitID.LUMBRIDGE_HARD_REWARD, VarbitID.LUMBRIDGE_ELITE_REWARD));
		regions.add(region("morytania", "Morytania", VarbitID.MORYTANIA_EASY_REWARD, VarbitID.MORYTANIA_MEDIUM_REWARD, VarbitID.MORYTANIA_HARD_REWARD, VarbitID.MORYTANIA_ELITE_REWARD));
		regions.add(region("varrock", "Varrock", VarbitID.VARROCK_EASY_REWARD, VarbitID.VARROCK_MEDIUM_REWARD, VarbitID.VARROCK_HARD_REWARD, VarbitID.VARROCK_ELITE_REWARD));
		regions.add(region("western_provinces", "Western Provinces", VarbitID.WESTERN_EASY_REWARD, VarbitID.WESTERN_MEDIUM_REWARD, VarbitID.WESTERN_HARD_REWARD, VarbitID.WESTERN_ELITE_REWARD));
		regions.add(region("wilderness", "Wilderness", VarbitID.WILDERNESS_EASY_REWARD, VarbitID.WILDERNESS_MEDIUM_REWARD, VarbitID.WILDERNESS_HARD_REWARD, VarbitID.WILDERNESS_ELITE_REWARD));

		int completedTiers = 0;
		for (DiaryRegion region : regions)
		{
			completedTiers += region.getCompletedTierCount();
		}
		int totalTiers = regions.size() * 4;
		double completionPercent = totalTiers > 0 ? (completedTiers * 100.0) / totalTiers : 0.0;

		return new AchievementDiaries(completedTiers, totalTiers, completionPercent, regions);
	}

	private DiaryRegion region(String key, String name, int easy, int medium, int hard, int elite)
	{
		DiaryTier easyTier = tier(easy);
		DiaryTier mediumTier = tier(medium);
		DiaryTier hardTier = tier(hard);
		DiaryTier eliteTier = tier(elite);

		int completed = (easyTier.isComplete() ? 1 : 0)
			+ (mediumTier.isComplete() ? 1 : 0)
			+ (hardTier.isComplete() ? 1 : 0)
			+ (eliteTier.isComplete() ? 1 : 0);

		return new DiaryRegion(key, name, easyTier, mediumTier, hardTier, eliteTier, completed, 4, completed == 4);
	}

	private DiaryTier tier(int varbitId)
	{
		int value = client.getVarbitValue(varbitId);
		return new DiaryTier(value > 0, value);
	}
}
