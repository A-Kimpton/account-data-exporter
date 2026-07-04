package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** Bank Tags integration (read from RuneLite's built-in Bank Tags plugin config). */
@Value
public class BankTags
{
	boolean available;
	List<BankTagTab> tabs;
}
