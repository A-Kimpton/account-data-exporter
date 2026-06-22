package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class Animation
{
	int current;
	int pose;
	int idlePose;
	int orientation;
	int currentOrientation;
}
