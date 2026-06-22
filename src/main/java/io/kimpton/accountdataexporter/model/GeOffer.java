// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
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
