// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
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
	int assignedMasterId;
	String assignedMasterName;
	int bossId;
}
