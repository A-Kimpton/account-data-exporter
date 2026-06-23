package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class HunterRef
{
	int npcId;
	String name;
	String tier;  // NOVICE | ADEPT | EXPERT | MASTER
}
