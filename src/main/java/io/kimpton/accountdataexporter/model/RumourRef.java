package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class RumourRef
{
	String key;   // stored enum name, e.g. "ORANGE_SALAMANDER"
	String name;  // creature display name
	TrapRef trap;
}
