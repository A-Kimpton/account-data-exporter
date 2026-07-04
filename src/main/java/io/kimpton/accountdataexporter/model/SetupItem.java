package io.kimpton.accountdataexporter.model;

import lombok.Value;

/** One item of an inventory setup, with its container slot preserved. */
@Value
public class SetupItem
{
	int slot;
	int id;
	String name;
	int quantity;
	boolean fuzzy;
}
