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
