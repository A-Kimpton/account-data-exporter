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
