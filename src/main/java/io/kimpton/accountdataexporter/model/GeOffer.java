package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class GeOffer
{
	int slot;
	String state;
	int itemId;
	String itemName;
	int listedPrice;
	int marketPrice;
	int totalQuantity;
	int completedQuantity;
	int remainingQuantity;
	int spent;
	long listedValueEstimate;
	long accountValueEstimate;
}
