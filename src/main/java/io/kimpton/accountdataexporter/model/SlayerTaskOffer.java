package io.kimpton.accountdataexporter.model;

import lombok.Value;

/**
 * One of the assignments Mortimer is currently offering. He hands out a choice
 * rather than a single task: two options, rising to three once enough tasks have
 * been completed for him. `offered` is false for a choice slot that is empty —
 * the third before it unlocks, or all of them when no roll is pending.
 */
@Value
public class SlayerTaskOffer
{
	int choice;
	boolean offered;
	int taskId;
	String name;
	int bossId;
	String bossName;
	SlayerModifier modifier;
}
