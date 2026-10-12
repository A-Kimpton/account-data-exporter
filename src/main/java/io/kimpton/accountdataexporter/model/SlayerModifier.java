package io.kimpton.accountdataexporter.model;

import lombok.Value;

/**
 * A Mortimer task modifier ("Mortifier") — the bonus attached to one of his
 * assignments. Only Mortimer's tasks carry one; for every other master the
 * modifier varbits read 0 and this is omitted.
 *
 * `value` is the raw magnitude the game stores, whose unit depends on which
 * modifier it is (points, extra kills, a percentage), and `negative` flips its
 * sign — a modifier can make a task worse, not just better. Both are exported
 * raw rather than pre-interpreted, like the rest of this schema.
 */
@Value
public class SlayerModifier
{
	int id;
	String name;
	int value;
	boolean negative;
}
