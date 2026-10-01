package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class GeOffer
{
	int slot;
	String state;
	int itemId;
	String itemName;
	long listedPrice;
	long marketPrice;
	int totalQuantity;
	int completedQuantity;
	int remainingQuantity;
	long spent;
	long listedValueEstimate;
	long accountValueEstimate;
}
