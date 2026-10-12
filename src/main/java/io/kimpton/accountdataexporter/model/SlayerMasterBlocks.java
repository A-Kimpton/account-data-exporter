package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class SlayerMasterBlocks
{
	List<SlayerBlockSlot> slots;
	/** The Elite Lumbridge & Draynor bonus slot, or null for a master who has no
	 *  diary slot at all (Mortimer). */
	SlayerBlockSlot diarySlot;
}
