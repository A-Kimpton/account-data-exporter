package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/** One saved Inventory Setups layout. Slots are container positions (inventory
 *  0-27 row-major 4 wide; equipment uses RuneLite EquipmentInventorySlot ids). */
@Value
public class InventorySetupExport
{
	String name;
	String notes;
	boolean favorite;
	int iconItemId;
	int spellbook;
	List<SetupItem> inventory;
	List<SetupItem> equipment;
	List<SetupItem> runePouch;
	List<SetupItem> boltPouch;
	List<SetupItem> quiver;
	List<Integer> additionalItemIds;
}
