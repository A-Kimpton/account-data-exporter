package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class AchievementDiaries
{
	int completedTierCount;
	int totalTierCount;
	double completionPercent;
	List<DiaryRegion> regions;
}
