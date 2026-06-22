// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
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
