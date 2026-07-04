package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** One RuneLite Bank Tags tag tab: its name, icon item, and tagged item ids. */
@Value
public class BankTagTab
{
	String tag;
	int iconItemId;
	List<Integer> itemIds;
}
