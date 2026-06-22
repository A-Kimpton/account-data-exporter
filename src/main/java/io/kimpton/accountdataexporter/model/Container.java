package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** A bank/inventory/equipment container. Containers are only populated client-side once opened,
 *  so {@code loaded}/{@code fromCache} flag whether this is live or a retained last-known copy. */
@Value
public class Container
{
	boolean loaded;
	boolean fromCache;
	long lastSeenTimestamp;
	long value;
	int itemCount;
	List<ItemStack> items;
}
