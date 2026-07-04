package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** Inventory Setups integration (read from the Inventory Setups hub plugin's config). */
@Value
public class InventorySetups
{
	boolean available;
	List<InventorySetupExport> setups;
}
