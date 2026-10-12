package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class ItemStack
{
	int slot;
	int id;
	String name;
	int quantity;
	long price;
	long value;
	int haPrice;
	long haValue;
}
