package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class CombatAchievements
{
	int completed;
	int total;
	List<CaTier> tiers;
}
