// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class DiaryRegion
{
	String key;
	String name;
	DiaryTier easy;
	DiaryTier medium;
	DiaryTier hard;
	DiaryTier elite;
	int completedTierCount;
	int totalTierCount;
	boolean allComplete;
}
