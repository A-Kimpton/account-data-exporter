package io.kimpton.accountdataexporter.model;

import lombok.Value;

/** Live combat state, accumulated from hitsplat events. {@code inCombat} is true while a hitsplat
 *  involving the local player has landed within the combat timeout; duration and damage cover the
 *  current continuous fight only. */
@Value
public class CombatState
{
	boolean inCombat;
	CombatTarget target;
	long durationMs;
	int damageDealt;
	int damageTaken;
}
