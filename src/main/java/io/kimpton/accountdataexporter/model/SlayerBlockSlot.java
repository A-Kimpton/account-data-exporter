package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class SlayerBlockSlot
{
	int slot;
	boolean blocked;
	int taskId;
	String name;
}
