package io.kimpton.accountdataexporter.model;

import lombok.Value;

/** The actor the local player is fighting. For NPCs the full set of fields is populated; for player
 *  targets only {@code type} is set (we deliberately log no other data about other players). */
@Value
public class CombatTarget
{
	String type;          // "npc" or "player"
	Integer id;           // npc only
	String name;          // npc only
	Integer combatLevel;  // npc only
	Double healthPercent; // npc only, null when no health bar is shown
	Boolean healthPresent; // npc only
}
