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
