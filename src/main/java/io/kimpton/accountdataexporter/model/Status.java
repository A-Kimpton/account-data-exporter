package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class Status
{
	double runEnergyPercent;
	int weight;
	double specialAttackPercent;
	boolean specialAttackEnabled;
}
