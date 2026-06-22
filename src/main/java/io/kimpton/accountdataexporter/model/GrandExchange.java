package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class GrandExchange
{
	boolean loaded;
	int activeCount;
	long listedValueEstimate;
	long accountValueEstimate;
	List<GeOffer> offers;
}
