// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.model;

import lombok.Value;

@Value
public class Location
{
	boolean loaded;
	int worldX;
	int worldY;
	int plane;
	int regionId;
	int regionX;
	int regionY;
}
