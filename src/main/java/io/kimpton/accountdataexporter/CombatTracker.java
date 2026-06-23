package io.kimpton.accountdataexporter;

import io.kimpton.accountdataexporter.model.CombatState;
import io.kimpton.accountdataexporter.model.CombatTarget;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.HitsplatApplied;

/** Tracks the local player's current fight from hitsplat events. Combat is considered active while a
 *  hitsplat involving the local player has landed within {@link #COMBAT_TIMEOUT_MS}; damage and
 *  duration are reset whenever a new fight begins after that gap. */
@Singleton
class CombatTracker
{
	private static final long COMBAT_TIMEOUT_MS = 10_000;

	@Inject
	private Client client;

	private long fightStartMs;
	private long lastCombatMs;
	private int damageDealt;
	private int damageTaken;

	void onHitsplat(HitsplatApplied event)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		Actor actor = event.getActor();
		boolean mine = event.getHitsplat().isMine();
		boolean onMe = actor == localPlayer;

		// Only hits we dealt or hits we took count as our combat.
		if (!mine && !onMe)
		{
			return;
		}

		long now = System.currentTimeMillis();
		if (lastCombatMs == 0 || now - lastCombatMs > COMBAT_TIMEOUT_MS)
		{
			fightStartMs = now;
			damageDealt = 0;
			damageTaken = 0;
		}
		lastCombatMs = now;

		int amount = event.getHitsplat().getAmount();
		if (onMe)
		{
			damageTaken += amount;
		}
		if (mine)
		{
			damageDealt += amount;
		}
	}

	CombatState snapshot()
	{
		long now = System.currentTimeMillis();
		boolean inCombat = lastCombatMs > 0 && now - lastCombatMs <= COMBAT_TIMEOUT_MS;

		if (!inCombat)
		{
			return new CombatState(false, null, 0, 0, 0);
		}

		Player localPlayer = client.getLocalPlayer();
		CombatTarget target = localPlayer != null ? resolveTarget(localPlayer.getInteracting()) : null;
		return new CombatState(true, target, now - fightStartMs, damageDealt, damageTaken);
	}

	void reset()
	{
		fightStartMs = 0;
		lastCombatMs = 0;
		damageDealt = 0;
		damageTaken = 0;
	}

	private CombatTarget resolveTarget(Actor actor)
	{
		if (actor instanceof NPC)
		{
			NPC npc = (NPC) actor;
			int ratio = npc.getHealthRatio();
			int scale = npc.getHealthScale();
			boolean healthPresent = ratio >= 0 && scale > 0;
			Double healthPercent = healthPresent ? Math.round((ratio * 1000.0) / scale) / 10.0 : null;
			return new CombatTarget("npc", npc.getId(), npc.getName(), npc.getCombatLevel(), healthPercent, healthPresent);
		}

		if (actor instanceof Player)
		{
			// Deliberately log no other data about other players.
			return new CombatTarget("player", null, null, null, null, null);
		}

		return null;
	}
}
