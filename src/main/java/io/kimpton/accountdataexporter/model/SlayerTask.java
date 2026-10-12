package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class SlayerTask
{
	boolean hasTask;
	int taskId;
	String name;
	int amountRemaining;
	int initialAmount;
	int areaId;
	String areaName;
	int assignedMasterId;
	String assignedMasterName;
	int bossId;
	String bossName;
	/** The modifier on this task, or null when it has none (every master but Mortimer). */
	SlayerModifier modifier;
}
