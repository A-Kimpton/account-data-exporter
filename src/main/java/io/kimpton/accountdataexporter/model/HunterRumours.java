package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** Hunter Rumour state, sourced from the Hunter Rumours plugin's saved config (no game var exposes
 *  it). {@code available} is false when that plugin has stored nothing for this account. */
@Value
public class HunterRumours
{
	boolean available;
	String backToBack;
	HunterRef activeHunter;
	RumourRef currentRumour;
	int caught;
	boolean finished;
	Pity pity;
	List<RumourAssignment> assignments;
}
