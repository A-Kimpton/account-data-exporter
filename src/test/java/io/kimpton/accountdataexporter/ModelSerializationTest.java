package io.kimpton.accountdataexporter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kimpton.accountdataexporter.model.Container;
import io.kimpton.accountdataexporter.model.GeOffer;
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
			Collections.singletonList(new ItemStack(0, 995, "Coins", 4567, 1, 4567L, 1, 4567L)));

		Container restored = gson.fromJson(gson.toJson(original), Container.class);

		Assert.assertEquals(original, restored);
		Assert.assertTrue(restored.isLoaded());
		Assert.assertEquals(4567L, restored.getValue());
		Assert.assertEquals("Coins", restored.getItems().get(0).getName());
	}

	@Test
	public void largeGpValuesArePreservedAsJsonNumbers()
	{
		long gp = 3_000_000_000L;
		ItemStack stack = new ItemStack(0, 1, "Example", 2, gp, gp * 2, 0, 0);
		JsonObject itemJson = gson.toJsonTree(stack).getAsJsonObject();
		Assert.assertTrue(itemJson.get("price").getAsJsonPrimitive().isNumber());
		Assert.assertEquals(gp, itemJson.get("price").getAsLong());
		Assert.assertEquals(stack, gson.fromJson(itemJson, ItemStack.class));

		GeOffer offer = new GeOffer(0, "BUYING", 1, "Example", gp, gp,
			2, 1, 1, gp, gp * 2, gp * 2);
		JsonObject offerJson = gson.toJsonTree(offer).getAsJsonObject();
		for (String field : new String[]{"listedPrice", "marketPrice", "spent"})
		{
			Assert.assertTrue(offerJson.get(field).getAsJsonPrimitive().isNumber());
			Assert.assertEquals(gp, offerJson.get(field).getAsLong());
		}
		Assert.assertEquals(offer, gson.fromJson(offerJson, GeOffer.class));
	}

	@Test
	public void legacyIntGpValuesStillLoad()
	{
		String oldItem = "{\"slot\":0,\"id\":995,\"name\":\"Coins\",\"quantity\":42,"
			+ "\"price\":1,\"value\":42,\"haPrice\":1,\"haValue\":42}";
		ItemStack restored = gson.fromJson(oldItem, ItemStack.class);
		Assert.assertEquals(1L, restored.getPrice());
		Assert.assertEquals(42L, restored.getValue());
		Assert.assertEquals(oldItem, gson.toJson(restored));
	}

	@Test
	public void disabledSectionsAreOmitted()
	{
		Snapshot snapshot = Snapshot.builder()
			.schemaVersion("1.0.1")
			.rsn("Zezima")
			.build();

		String json = gson.toJson(snapshot);

		// Null sections must not appear as keys, so consumers can treat absent as "not exported".
		Assert.assertFalse(json.contains("\"slayer\""));
		Assert.assertFalse(json.contains("\"bank\""));
		Assert.assertTrue(json.contains("\"rsn\":\"Zezima\""));
	}
}
