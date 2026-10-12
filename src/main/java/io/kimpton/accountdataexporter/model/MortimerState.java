package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

/**
 * Mortimer-specific slayer state. He does not fit the shape the other masters
 * share: he keeps his own task counter, offers a choice of assignments instead of
 * one, attaches a modifier to every task, and sells two block slots rather than
 * earning six through quest points.
 *
 * `tasksCompleted` is his own streak — his tasks leave the shared
 * SLAYER_TASKS_COMPLETED counter untouched — and it is also what gates his
 * unlocks (a third task choice, and the modifiers themselves).
 *
 * `modifiersRaw` is the unlocked-modifier varp exported verbatim; its bit layout
 * isn't documented anywhere we can verify, so it is passed through rather than
 * decoded into booleans we'd be guessing at.
 */
@Value
public class MortimerState
{
	int tasksCompleted;
	int modifiersRaw;
	int rolledMasterId;
	List<SlayerTaskOffer> taskChoices;
}
