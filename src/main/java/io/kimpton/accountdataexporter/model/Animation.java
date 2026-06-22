// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
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
