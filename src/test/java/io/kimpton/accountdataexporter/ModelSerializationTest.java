package io.kimpton.accountdataexporter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kimpton.accountdataexporter.model.Container;
import io.kimpton.accountdataexporter.model.GeOffer;
import io.kimpton.accountdataexporter.model.ItemStack;
import io.kimpton.accountdataexporter.model.MortimerState;
import io.kimpton.accountdataexporter.model.SlayerBlockSlot;
import io.kimpton.accountdataexporter.model.SlayerMasterBlocks;
import io.kimpton.accountdataexporter.model.SlayerModifier;
import io.kimpton.accountdataexporter.model.SlayerModifierUnlock;
import io.kimpton.accountdataexporter.model.SlayerTaskOffer;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.util.Arrays;
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
	public void mortimerBlockListOmitsTheDiarySlotAndKeepsItsPointCost()
	{
		// Mortimer has two slots bought with slayer points and no diary slot, so the
		// diary key must be absent rather than present-and-empty: consumers key off
		// "no diarySlot" to mean "this master has none".
		SlayerMasterBlocks mortimer = new SlayerMasterBlocks(
			Arrays.asList(
				new SlayerBlockSlot(1, true, 131, "Venators", 120),
				new SlayerBlockSlot(2, false, 0, null, 120)),
			null);

		JsonObject json = gson.toJsonTree(mortimer).getAsJsonObject();

		Assert.assertFalse(json.has("diarySlot"));
		Assert.assertEquals(120, json.getAsJsonArray("slots").get(0).getAsJsonObject().get("pointCost").getAsInt());
		Assert.assertEquals(mortimer, gson.fromJson(json, SlayerMasterBlocks.class));

		// A classic master still carries its diary slot, at pointCost 0.
		SlayerMasterBlocks duradel = new SlayerMasterBlocks(
			Collections.singletonList(new SlayerBlockSlot(1, false, 0, null, 0)),
			new SlayerBlockSlot(0, false, 0, null, 0));
		Assert.assertTrue(gson.toJsonTree(duradel).getAsJsonObject().has("diarySlot"));
	}

	@Test
	public void mortimerTaskChoicesRoundTripWithTheirModifiers()
	{
		MortimerState state = new MortimerState(37, 0b11011, 10, Arrays.asList(
			new SlayerTaskOffer(1, true, 131, "Venators", 0, null,
				new SlayerModifier(2, "Task Quantity", 40, false)),
			new SlayerTaskOffer(2, true, 95, "Hydras", 0, null,
				new SlayerModifier(5, "Slayer XP", 15, true)),
			// The third choice is locked until 50 tasks, and reads as an empty offer.
			new SlayerTaskOffer(3, false, 0, null, 0, null, null)),
			Collections.emptyList());

		JsonObject json = gson.toJsonTree(state).getAsJsonObject();

		Assert.assertEquals(37, json.get("tasksCompleted").getAsInt());
		JsonObject third = json.getAsJsonArray("taskChoices").get(2).getAsJsonObject();
		Assert.assertFalse(third.get("offered").getAsBoolean());
		Assert.assertFalse("an absent modifier must not serialise as a key", third.has("modifier"));
		// negative is what distinguishes a penalty from a bonus - it must survive.
		Assert.assertTrue(json.getAsJsonArray("taskChoices").get(1).getAsJsonObject()
			.getAsJsonObject("modifier").get("negative").getAsBoolean());
		Assert.assertEquals(state, gson.fromJson(json, MortimerState.class));
	}

	@Test
	public void mortimerModifierUnlocksTrackHisTaskCount()
	{
		// His modifiers unlock as his own task count rises, so the same ladder reads
		// differently at 20 tasks than at 50 — that progression is the point of
		// exporting his counter separately from the shared streak.
		int tasksCompleted = 20;
		SlayerModifierUnlock earned = new SlayerModifierUnlock(
			3, "Clue Scrolls", "Increased clue drop rate", 15, tasksCompleted >= 15);
		SlayerModifierUnlock pending = new SlayerModifierUnlock(
			5, "Slayer XP", "More slayer experience", 40, tasksCompleted >= 40);

		MortimerState state = new MortimerState(tasksCompleted, 0, 10,
			Collections.emptyList(), Arrays.asList(earned, pending));

		JsonObject json = gson.toJsonTree(state).getAsJsonObject();
		JsonObject first = json.getAsJsonArray("modifierUnlocks").get(0).getAsJsonObject();
		JsonObject second = json.getAsJsonArray("modifierUnlocks").get(1).getAsJsonObject();

		Assert.assertTrue("15 <= 20, so this rung is unlocked", first.get("unlocked").getAsBoolean());
		Assert.assertFalse("40 > 20, so this rung is not", second.get("unlocked").getAsBoolean());
		Assert.assertEquals(40, second.get("tasksRequired").getAsInt());
		Assert.assertEquals(state, gson.fromJson(json, MortimerState.class));
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
