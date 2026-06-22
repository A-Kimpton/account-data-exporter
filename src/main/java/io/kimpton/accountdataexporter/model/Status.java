package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class Status
{
	int runEnergy;
	double runEnergyPercent;
	int weight;
	int specialAttackPercent;
	boolean specialAttackEnabled;
}
