package io.kimpton.accountdataexporter.model;

import lombok.Value;

/**
 * One rung of Mortimer's progression. His modifiers are not all available from
 * the start: each unlocks at a number of tasks completed for him, so the same
 * account gets steadily better offers from him over time.
 *
 * `tasksRequired` is the SlayerModifiers table's requirement column, read live
 * rather than hardcoded, and `unlocked` compares it against his own task counter.
 */
@Value
public class SlayerModifierUnlock
{
	int id;
	String name;
	String description;
	int tasksRequired;
	boolean unlocked;
}
