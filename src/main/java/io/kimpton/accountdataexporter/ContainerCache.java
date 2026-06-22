// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter;

import io.kimpton.accountdataexporter.model.Container;
import java.util.Collections;

/** Retains the last-known-good copy of a single container. Bank/inventory/equipment are only
 *  populated client-side once the player opens them, so when the live container is unavailable we
 *  serve the retained copy flagged {@code fromCache} rather than reporting it empty. */
class ContainerCache
{
	private Container lastGood;
	private long lastSeenTimestamp;

	/** Seeds the cache from a previously persisted snapshot, off the client thread. */
	void seed(Container previous)
	{
		if (previous != null && previous.isLoaded())
		{
			lastGood = previous;
			lastSeenTimestamp = previous.getLastSeenTimestamp() > 0
				? previous.getLastSeenTimestamp()
				: System.currentTimeMillis();
		}
	}

	/** Returns the container to export: the live one if loaded (updating the cache), otherwise the
	 *  last-known-good copy, otherwise an empty unloaded container. */
	Container choose(Container live)
	{
		if (live != null && live.isLoaded())
		{
			lastSeenTimestamp = System.currentTimeMillis();
			lastGood = new Container(true, false, lastSeenTimestamp,
				live.getValue(), live.getItemCount(), live.getItems());
			return lastGood;
		}

		if (lastGood != null)
		{
			return new Container(true, true, lastSeenTimestamp,
				lastGood.getValue(), lastGood.getItemCount(), lastGood.getItems());
		}

		return new Container(false, false, 0, 0, 0, Collections.emptyList());
	}

	void reset()
	{
		lastGood = null;
		lastSeenTimestamp = 0;
	}
}
