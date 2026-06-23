package io.kimpton.accountdataexporter.model;

import lombok.Value;

/** Pity progress for the active rumour. The game guarantees completion by {@code threshold} catches
 *  ({@code thresholdWithOutfit} with the full hunter outfit); {@code catchesUntilPity} uses the
 *  no-outfit threshold as the upper bound. */
@Value
public class Pity
{
	int threshold;
	int thresholdWithOutfit;
	int catchesUntilPity;
}
