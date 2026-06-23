package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.HunterRef;
import io.kimpton.accountdataexporter.model.RumourRef;
import io.kimpton.accountdataexporter.model.TrapRef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Static reference data for Hunter Rumours, derived from the open-source Hunter Rumours plugin
 *  (geel9/runelite-hunter-rumours). The game exposes no var for rumour state, so these tables let us
 *  enrich the raw enum keys that plugin persists to config. */
final class HunterRumourReference
{
	private HunterRumourReference()
	{
	}

	// Guild hunters in tier order. Keyed by NPC id; also resolvable from the stored Hunter enum name.
	private static final List<HunterRef> MASTERS = new ArrayList<>();
	private static final Map<Integer, HunterRef> MASTER_BY_NPC = new LinkedHashMap<>();
	private static final Map<String, Integer> NPC_BY_HUNTER_ENUM = new LinkedHashMap<>();

	private static final Map<String, TrapRef> TRAPS = new LinkedHashMap<>();
	private static final Map<String, RumourRef> RUMOURS = new LinkedHashMap<>();

	static
	{
		master(13121, "Gilman", "NOVICE", "NOVICE_GILMAN");
		master(13122, "Ornus", "ADEPT", "ADEPT_ORNUS");
		master(13123, "Cervus", "ADEPT", "ADEPT_CERVUS");
		master(13124, "Aco", "EXPERT", "EXPERT_ACO");
		master(13125, "Teco", "EXPERT", "EXPERT_TECO");
		master(13126, "Wolf", "MASTER", "MASTER_WOLF");

		// trap key -> display name, pity threshold, pity threshold with full hunter outfit
		trap("SNARE", "Bird snare", 40, 38);
		trap("DEADFALL", "Deadfall", 30, 28);
		trap("NET_TRAP", "Net trap", 50, 46);
		trap("PIT", "Pit Trap", 30, 28);
		trap("BOX_TRAP", "Box Trap", 100, 94);
		trap("FALCONRY", "Falconry", 20, 18);
		trap("BUTTERFLY", "Butterfly Net", 150, 142);
		trap("NOOSE", "Tracking", 30, 28);
		trap("NOOSE_HERBIBOAR", "Tracking", 14, 12);

		// rumour key (as stored in config) -> creature display name, trap key
		rumour("TROPICAL_WAGTAIL", "Tropical Wagtail", "SNARE");
		rumour("WILD_KEBBIT", "Wild Kebbit", "DEADFALL");
		rumour("SAPPHIRE_GLACIALIS", "Sapphire Glacialis", "BUTTERFLY");
		rumour("SWAMP_LIZARD", "Swamp Lizard", "NET_TRAP");
		rumour("SPINED_LARUPIA", "Spined Larupia", "PIT");
		rumour("BARB_TAILED_KEBBIT", "Barb-tailed Kebbit", "DEADFALL");
		rumour("SNOWY_KNIGHT", "Snowy Knight", "BUTTERFLY");
		rumour("PRICKLY_KEBBIT", "Prickly Kebbit", "DEADFALL");
		rumour("EMBERTAILED_JERBOA", "Embertailed Jerboa", "BOX_TRAP");
		rumour("HORNED_GRAAHK", "Horned Graahk", "PIT");
		rumour("SPOTTED_KEBBIT", "Spotted Kebbit", "FALCONRY");
		rumour("BLACK_WARLOCK", "Black Warlock", "BUTTERFLY");
		rumour("ORANGE_SALAMANDER", "Orange Salamander", "NET_TRAP");
		rumour("RAZOR_BACKED_KEBBIT", "Razor-backed Kebbit", "NOOSE");
		rumour("SABRE_TOOTHED_KEBBIT", "Sabre-toothed Kebbit", "DEADFALL");
		rumour("GREY_CHINCHOMPA", "Grey Chinchompa", "BOX_TRAP");
		rumour("SABRE_TOOTHED_KYATT", "Sabre-toothed Kyatt", "PIT");
		rumour("DARK_KEBBIT", "Dark Kebbit", "FALCONRY");
		rumour("PYRE_FOX", "Pyre Fox", "DEADFALL");
		rumour("RED_SALAMANDER", "Red Salamander", "NET_TRAP");
		rumour("RED_CHINCHOMPA", "Carnivorous Chinchompa", "BOX_TRAP");
		rumour("RED_CHINCHOMPA_2", "Red Chinchompa", "BOX_TRAP");
		rumour("SUNLIGHT_MOTH", "Sunlight Moth", "BUTTERFLY");
		rumour("DASHING_KEBBIT", "Dashing Kebbit", "FALCONRY");
		rumour("SUNLIGHT_ANTELOPE", "Sunlight Antelope", "PIT");
		rumour("MOONLIGHT_MOTH", "Moonlight Moth", "BUTTERFLY");
		rumour("TECU_SALAMANDER", "Tecu Salamander", "NET_TRAP");
		rumour("HERBIBOAR", "Herbiboar", "NOOSE_HERBIBOAR");
		rumour("MOONLIGHT_ANTELOPE", "Moonlight Antelope", "PIT");
	}

	private static void master(int npcId, String name, String tier, String hunterEnum)
	{
		HunterRef ref = new HunterRef(npcId, name, tier);
		MASTERS.add(ref);
		MASTER_BY_NPC.put(npcId, ref);
		NPC_BY_HUNTER_ENUM.put(hunterEnum, npcId);
	}

	private static void trap(String key, String name, int pity, int pityWithOutfit)
	{
		TRAPS.put(key, new TrapRef(name, pity, pityWithOutfit));
	}

	private static void rumour(String key, String name, String trapKey)
	{
		RUMOURS.put(key, new RumourRef(key, name, TRAPS.get(trapKey)));
	}

	static List<HunterRef> masters()
	{
		return MASTERS;
	}

	static HunterRef masterByNpcId(int npcId)
	{
		return MASTER_BY_NPC.get(npcId);
	}

	static int npcIdForHunterEnum(String hunterEnum)
	{
		return hunterEnum == null ? 0 : NPC_BY_HUNTER_ENUM.getOrDefault(hunterEnum, 0);
	}

	/** Resolves a stored rumour enum key to its enriched reference, or null if unknown/none. */
	static RumourRef rumour(String key)
	{
		if (key == null || key.isEmpty() || "NONE".equals(key))
		{
			return null;
		}
		return RUMOURS.get(key);
	}
}
