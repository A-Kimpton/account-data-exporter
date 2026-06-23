package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.HunterRef;
import io.kimpton.accountdataexporter.model.HunterRumours;
import io.kimpton.accountdataexporter.model.Pity;
import io.kimpton.accountdataexporter.model.RumourAssignment;
import io.kimpton.accountdataexporter.model.RumourRef;
import io.kimpton.accountdataexporter.model.TrapRef;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;

/** Builds the Hunter Rumour section by reading the Hunter Rumours plugin's RS-profile config (group
 *  {@value #HR_GROUP}). Returns an {@code available=false} section when that plugin has stored
 *  nothing for the current account. */
public class HunterRumourBuilder
{
	private static final String HR_GROUP = "hunterrumours";

	@Inject
	private ConfigManager configManager;

	public HunterRumours build()
	{
		String activeHunterEnum = cfg("current.hunter");

		List<RumourAssignment> assignments = new ArrayList<>();
		int activeNpcId = HunterRumourReference.npcIdForHunterEnum(activeHunterEnum);
		boolean anyAssignment = false;

		for (HunterRef master : HunterRumourReference.masters())
		{
			String rumourKey = cfg("hunter." + master.getNpcId());
			RumourRef rumour = HunterRumourReference.rumour(rumourKey);
			boolean known = rumour != null;
			anyAssignment |= known;
			assignments.add(new RumourAssignment(master, rumour, known, master.getNpcId() == activeNpcId));
		}

		// No data at all => the Hunter Rumours plugin hasn't tracked anything for this account.
		if (activeHunterEnum == null && !anyAssignment)
		{
			return new HunterRumours(false, null, null, null, 0, false, null, new ArrayList<>());
		}

		HunterRef activeHunter = HunterRumourReference.masterByNpcId(activeNpcId);
		RumourRef currentRumour = activeNpcId != 0 ? HunterRumourReference.rumour(cfg("hunter." + activeNpcId)) : null;
		int caught = parseInt(cfg("current.rumour.caught"));
		boolean finished = "true".equalsIgnoreCase(cfg("current.rumour.finished"));
		Pity pity = buildPity(currentRumour, caught);

		return new HunterRumours(true, cfg("backtoback"), activeHunter, currentRumour, caught, finished, pity, assignments);
	}

	private Pity buildPity(RumourRef rumour, int caught)
	{
		if (rumour == null || rumour.getTrap() == null)
		{
			return null;
		}
		TrapRef trap = rumour.getTrap();
		int untilPity = Math.max(0, trap.getPityThreshold() - caught);
		return new Pity(trap.getPityThreshold(), trap.getPityThresholdWithOutfit(), untilPity);
	}

	private String cfg(String key)
	{
		return configManager.getRSProfileConfiguration(HR_GROUP, key);
	}

	private int parseInt(String value)
	{
		try
		{
			return value == null ? 0 : Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
}
