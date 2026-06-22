package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class SlayerMasterBlocks
{
	List<SlayerBlockSlot> slots;
	SlayerBlockSlot diarySlot;
}
