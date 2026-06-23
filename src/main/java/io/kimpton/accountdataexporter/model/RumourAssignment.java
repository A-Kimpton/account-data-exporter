package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class RumourAssignment
{
	HunterRef hunter;
	RumourRef rumour;  // null when not yet discovered for this master
	boolean known;
	boolean active;
}
