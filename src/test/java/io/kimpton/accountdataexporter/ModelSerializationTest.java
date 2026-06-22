/*
 * Copyright (c) 2026, Antony Kimpton
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.kimpton.accountdataexporter;

import com.google.gson.Gson;
import io.kimpton.accountdataexporter.model.Container;
import io.kimpton.accountdataexporter.model.ItemStack;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/** Guards the assumption the cache-seeding path relies on: Lombok @Value models (no no-arg
 *  constructor, final fields) round-trip through Gson without data loss. */
public class ModelSerializationTest
{
	private final Gson gson = new Gson();

	@Test
	public void containerRoundTrips()
	{
		Container original = new Container(true, false, 123L, 4567L, 1,
			Collections.singletonList(new ItemStack(0, 995, "Coins", 4567, 1, 4567L)));

		Container restored = gson.fromJson(gson.toJson(original), Container.class);

		Assert.assertEquals(original, restored);
		Assert.assertTrue(restored.isLoaded());
		Assert.assertEquals(4567L, restored.getValue());
		Assert.assertEquals("Coins", restored.getItems().get(0).getName());
	}

	@Test
	public void disabledSectionsAreOmitted()
	{
		Snapshot snapshot = Snapshot.builder()
			.schemaVersion(1)
			.rsn("Zezima")
			.build();

		String json = gson.toJson(snapshot);

		// Null sections must not appear as keys, so consumers can treat absent as "not exported".
		Assert.assertFalse(json.contains("\"slayer\""));
		Assert.assertFalse(json.contains("\"bank\""));
		Assert.assertTrue(json.contains("\"rsn\":\"Zezima\""));
	}
}
