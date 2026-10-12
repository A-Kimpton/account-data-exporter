package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class SlayerBlockSlot
{
	int slot;
	boolean blocked;
	int taskId;
	String name;
	/** Slayer points this slot is bought for (Mortimer), or 0 when the slot is
	 *  earned with quest points instead, as it is for every other master. */
	int pointCost;
}
