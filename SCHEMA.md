# Account Data Exporter — JSON schema

This document describes the JSON written by the Account Data Exporter RuneLite plugin.
[`example.json`](example.json) is a complete, illustrative sample.

## Output files

Written to `.runelite/account-data-exporter/`:

| File | Contents |
|------|----------|
| `latest.json` | The most recent snapshot, for any account. |
| `<RSN>.json` | The most recent snapshot for that account (RSN sanitised: `[^a-zA-Z0-9 _-]` → `_`). |

Files are written atomically (temp file + rename), so a reader never sees a half-written file.
Real output is minified; `example.json` is pretty-printed for readability.

## When it's written

A snapshot is produced on a game-tick heartbeat: starting 5 ticks after login, then every
`exportIntervalTicks` (default 5 ticks ≈ 3s). Each snapshot is a full document — there is no diffing.

## Versioning

| Field | Type | Meaning |
|-------|------|---------|
| `schemaVersion` | string | Semantic version of the data shape (e.g. `"1.0.1"`). Currently `1.0.1`. |
| `exportVersion` | string | Plugin/export build version (e.g. `"0.5.0"`). Informational. |

`schemaVersion` is semver: **major** = breaking shape change (rename/remove/retype a field, restructure a block); **minor** = a new top-level content block added; **patch** = additive fields on an existing block. `1.0.0` was the initial launch. Consumers should branch on the **major** component — minor and patch changes are backward-compatible (treat unknown fields/blocks as optional).

> Note: versions `1` (integer) predate this scheme and are equivalent to `1.0.0`. Consumers should accept either an integer or a semver string for `schemaVersion`.

## Section toggles & omission contract

Each data section has a config toggle (all default **on**). **A disabled section's key is omitted
entirely** — treat an absent key as "not exported". Null is never emitted (Gson drops nulls), so
absent and null are equivalent: "no data".

| Toggle (config key) | Section key(s) |
|---------------------|----------------|
| `exportCharacterMeta` | `status`, `location`, `animation` |
| `exportSkills` | `skills` |
| `exportInventory` | `inventory` |
| `exportEquipment` | `equipment` |
| `exportBank` | `bank` |
| `exportGrandExchange` | `grandExchange` |
| `exportQuests` | `quests` |
| `exportAchievementDiaries` | `achievementDiaries` |
| `exportCombatAchievements` | `combatAchievements` |
| `exportSlayer` | `slayer` |
| `exportHunterRumours` | `hunterRumours` |
| `exportCombat` | `combat` |

The identity/meta header and value totals (below) are always present.

## Units & conventions

- **gp values** (`value`, `*Value`, `*Estimate`): integer coins.
- **xp**: raw experience (e.g. `13034431`).
- **timestamps**: `timestamp` is epoch milliseconds; `timestampIso` is ISO-8601 UTC.
- **percentages** (`runEnergyPercent`, `specialAttackPercent`, `healthPercent`, `completionPercent`):
  `0.0`–`100.0` doubles.
- **durations** (`durationMs`): milliseconds.

---

## Top-level: identity & meta (always present)

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | string | Semver; see Versioning. |
| `exportVersion` | string | See Versioning. |
| `timestamp` | long | Epoch ms when the snapshot was built. |
| `timestampIso` | string | ISO-8601 UTC equivalent. |
| `rsn` | string | Display name of the logged-in account. |
| `world` | int | Current world number. |
| `gameState` | string | RuneLite `GameState` name (e.g. `LOGGED_IN`). |
| `combatLevel` | int | |
| `totalLevel` | int | Sum of real skill levels (excludes the OVERALL pseudo-skill). |
| `totalXp` | long | Sum of skill experience. |
| `accountType` | int | Raw IRONMAN varbit value. |
| `accountTypeName` | string | Slug: `normal`, `ironman`, `ultimate_ironman`, `hardcore_ironman`, `group_ironman`, `hardcore_group_ironman`, `unranked_group_ironman`. |
| `fps` | int | Client FPS at snapshot time. |
| `exportIntervalTicks` | int | The configured export interval. |
| `exportDirectory` | string | Absolute path the files were written to. |

## Value totals (always present)

| Field | Type | Notes |
|-------|------|-------|
| `carriedValue` | long | `inventory.value + equipment.value` (0 for sections that are off/unloaded). |
| `knownAccountValue` | long | `bank.value + carriedValue`. |
| `grandExchangeAccountValueEstimate` | long | Estimated gp tied up in active GE offers (see `grandExchange`). |
| `knownAccountValueWithGeEstimate` | long | `knownAccountValue + grandExchangeAccountValueEstimate`. |

---

## `status` (object)

| Field | Type | Notes |
|-------|------|-------|
| `runEnergyPercent` | double | 0–100. |
| `weight` | int | kg. |
| `specialAttackPercent` | double | 0–100 (normalised from the raw 0–1000 varp). |
| `specialAttackEnabled` | boolean | Whether special attack is toggled on. |

## `location` (object)

| Field | Type | Notes |
|-------|------|-------|
| `loaded` | boolean | False when the world location isn't available (e.g. mid-load). |
| `worldX`,`worldY` | int | World tile coordinates. |
| `plane` | int | 0–3. |
| `regionId` | int | Region id. |
| `regionX`,`regionY` | int | Local coordinates within the region. |

## `animation` (object)

| Field | Type | Notes |
|-------|------|-------|
| `current` | int | Current animation id (`-1` = none). |
| `pose`,`idlePose` | int | Movement/idle pose animation ids. |
| `orientation`,`currentOrientation` | int | Facing direction (0–2047). |

## `combat` (object)

Event-driven (from hitsplats), not just sampled. A fight ends after 10s with no hit involving you.

| Field | Type | Notes |
|-------|------|-------|
| `inCombat` | boolean | True while a hit you dealt or took landed within the last 10s. |
| `target` | object \| absent | The actor you're interacting with (omitted when none). See below. |
| `durationMs` | long | Length of the current continuous fight. `0` when not in combat. |
| `damageDealt` | int | Sum of your hitsplats this fight. |
| `damageTaken` | int | Sum of hitsplats on you this fight. |

When `inCombat` is false, `target` is omitted and `durationMs`/`damageDealt`/`damageTaken` are `0`.

### `combat.target` (object)

| Field | Type | Notes |
|-------|------|-------|
| `type` | string | `"npc"` or `"player"`. |
| `id` | int | NPC id. **Omitted for player targets.** |
| `name` | string | NPC name. **Omitted for player targets.** |
| `combatLevel` | int | **Omitted for player targets.** |
| `healthPercent` | double | 0–100, from the target's health bar. Omitted when no bar is shown, or for players. |
| `healthPresent` | boolean | Whether a health bar was visible. **Omitted for player targets.** |

> **Player targets are deliberately redacted**: only `{"type":"player"}` is recorded — no name,
> level, or health — to avoid logging data about other players.
> Health is a **health-bar percentage, not exact HP** (the server doesn't send exact NPC HP).

---

## `skills` (object map)

Map of skill name → stat object. Excludes the OVERALL pseudo-skill; includes all real skills
(e.g. Sailing).

```json
"skills": { "Attack": { "level": 87, "boostedLevel": 87, "xp": 4347005 }, ... }
```

| Field | Type | Notes |
|-------|------|-------|
| `level` | int | Real (unboosted) level. |
| `boostedLevel` | int | Current boosted level. |
| `xp` | int | Experience. |

## `inventory` / `equipment` / `bank` (Container objects)

Same shape. Containers are only populated client-side once opened (notably the **bank**), so a
last-known-good copy is retained and served when the live container is unavailable.

| Field | Type | Notes |
|-------|------|-------|
| `loaded` | boolean | True if live or cached data is available; false if never seen this account. |
| `fromCache` | boolean | True when serving a retained copy rather than a live read. |
| `lastSeenTimestamp` | long | Epoch ms the data was last read live (`0` if never). |
| `value` | long | Total GE value of contents. |
| `itemCount` | int | Number of distinct item stacks. |
| `items` | array | List of item stacks (below). |

### item stack

| Field | Type | Notes |
|-------|------|-------|
| `slot` | int | Container slot index. |
| `id` | int | Item id. |
| `name` | string | Item name. |
| `quantity` | int | |
| `price` | int | Per-item GE price. |
| `value` | long | `price * quantity`. |

## `grandExchange` (object)

| Field | Type | Notes |
|-------|------|-------|
| `loaded` | boolean | Whether GE offer data was available. |
| `activeCount` | int | Number of non-empty offer slots. |
| `listedValueEstimate` | long | Σ `listedPrice * totalQuantity`. |
| `accountValueEstimate` | long | Estimated gp value held across offers (cash spent + market value of items owed). |
| `offers` | array | Active offers (below). |

### offer

| Field | Type | Notes |
|-------|------|-------|
| `slot` | int | GE slot (0–7). |
| `state` | string | RuneLite offer state name (e.g. `BUYING`, `SELLING`, `BOUGHT`). |
| `itemId` | int | |
| `itemName` | string | |
| `listedPrice` | int | Price per item the offer was listed at. |
| `marketPrice` | int | Current GE price. |
| `totalQuantity` | int | |
| `completedQuantity` | int | Bought/sold so far. |
| `remainingQuantity` | int | |
| `spent` | int | gp spent so far. |
| `listedValueEstimate` | long | `listedPrice * totalQuantity`. |
| `accountValueEstimate` | long | Per-offer contribution to the account value estimate. |

## `quests` (object)

| Field | Type | Notes |
|-------|------|-------|
| `notStarted`,`inProgress`,`finished`,`unknown` | int | Counts by state. |
| `total` | int | All quests. |
| `entries` | array | Per-quest entries. |

### quest entry

| Field | Type | Notes |
|-------|------|-------|
| `id` | string | RuneLite `Quest` enum name (stable id). |
| `name` | string | Display name. |
| `state` | string | `NOT_STARTED` \| `IN_PROGRESS` \| `FINISHED` \| `UNKNOWN`. |

## `achievementDiaries` (object)

| Field | Type | Notes |
|-------|------|-------|
| `completedTierCount` | int | Completed tiers across all regions. |
| `totalTierCount` | int | `regions * 4`. |
| `completionPercent` | double | 0–100. |
| `regions` | array | Per-region (below). |

### diary region

| Field | Type | Notes |
|-------|------|-------|
| `key` | string | Slug (e.g. `lumbridge_draynor`). |
| `name` | string | Display name. |
| `easy`,`medium`,`hard`,`elite` | object | `{ "complete": bool, "value": int }` (raw reward varbit value). |
| `completedTierCount` | int | 0–4. |
| `totalTierCount` | int | 4. |
| `allComplete` | boolean | |

## `combatAchievements` (object)

| Field | Type | Notes |
|-------|------|-------|
| `completed` | int | Total completed tasks. |
| `total` | int | Total tasks. |
| `tiers` | array | Per-tier (Easy → Grandmaster). |

### CA tier

| Field | Type | Notes |
|-------|------|-------|
| `name` | string | `Easy`,`Medium`,`Hard`,`Elite`,`Master`,`Grandmaster`. |
| `completed` | int | Completed in this tier. |
| `total` | int | Tasks in this tier. |
| `tasks` | array | `{ "id": int, "name": string, "completed": bool }`. |

## `slayer` (object)

| Field | Type | Notes |
|-------|------|-------|
| `points` | int | Slayer reward points. |
| `tasksCompletedStreak` | int | Current streak. |
| `wildernessTasksCompleted` | int | |
| `currentTask` | object | Current assignment (below). |
| `unlocks` | object map | Reward-shop unlock name → varbit value (non-zero = purchased). |
| `taskExtensions` | object map | Extend-unlock name → varbit value. |
| `autoKill` | object map | Auto-kill toggle name → varbit value. |
| `blockLists` | object map | Per-master block lists (below). |

### `slayer.currentTask`

| Field | Type | Notes |
|-------|------|-------|
| `hasTask` | boolean | `taskId > 0 && amountRemaining > 0`. |
| `taskId` | int | |
| `name` | string \| null | Resolved from the SlayerTask DB table; null if unknown. |
| `amountRemaining` | int | |
| `initialAmount` | int | Originally assigned amount. |
| `areaId` | int | Konar slayer-area varp (0 = no area). |
| `areaName` | string \| null | Konar's required location, decoded from the SlayerArea DB table (the in-game slayer-helper name, e.g. `Taverley Dungeon`); null when `areaId` is 0 or unknown. |
| `assignedMasterId` | int | |
| `assignedMasterName` | string \| null | e.g. `Nieve`. |
| `bossId` | int | Boss-slayer-task id (0 if none). |
| `bossName` | string \| null | Boss name for a boss task, decoded via the SlayerTaskSublist → SlayerTask DB tables; null when `bossId` is 0 or unknown. |

### `slayer.blockLists` (map of master key → object)

Masters: `turael`, `mazchna`, `vannaka`, `chaeldar`, `konar`, `nieve`, `duradel`, `krystilia`.

| Field | Type | Notes |
|-------|------|-------|
| `slots` | array | 6 block slots: `{ "slot": 1–6, "blocked": bool, "taskId": int, "name": string\|null }`. |
| `diarySlot` | object | The diary-granted block slot (`slot: 0`), same shape. |

## `hunterRumours` (object)

Hunter Rumour state. **The game exposes no var for this**, so it is read from the Hunter Rumours
plugin's saved config (`geel9/runelite-hunter-rumours`). If that plugin isn't installed or hasn't
tracked anything for the account, `available` is `false` and the other fields are omitted.

| Field | Type | Notes |
|-------|------|-------|
| `available` | boolean | False when no Hunter Rumours data exists for the account. |
| `backToBack` | string | Back-to-back mode state (e.g. `DISABLED`). |
| `activeHunter` | object | The currently-active master (below). |
| `currentRumour` | object | The active master's assigned rumour (below). |
| `caught` | int | Creatures caught toward the current rumour. |
| `finished` | boolean | Whether the current rumour is complete and ready to hand in. |
| `pity` | object | Pity progress for the current rumour (below). |
| `assignments` | array | One entry per master (below). |

### hunter ref (`activeHunter`, `assignments[].hunter`)

| Field | Type | Notes |
|-------|------|-------|
| `npcId` | int | Master NPC id (13121–13126). |
| `name` | string | e.g. `Aco`. |
| `tier` | string | `NOVICE` \| `ADEPT` \| `EXPERT` \| `MASTER`. |

### rumour ref (`currentRumour`, `assignments[].rumour`)

| Field | Type | Notes |
|-------|------|-------|
| `key` | string | Stored enum key, e.g. `ORANGE_SALAMANDER`. |
| `name` | string | Creature display name. |
| `trap` | object | `{ "name": string, "pityThreshold": int, "pityThresholdWithOutfit": int }`. |

### `hunterRumours.pity`

| Field | Type | Notes |
|-------|------|-------|
| `threshold` | int | Catches that guarantee completion (no outfit). |
| `thresholdWithOutfit` | int | With the full hunter outfit equipped. |
| `catchesUntilPity` | int | `max(0, threshold - caught)` — upper bound; the outfit lowers it. |

> Pity is **not** an exact "catches remaining" — the real requirement is randomised per assignment.
> `threshold`/`thresholdWithOutfit` are the guaranteed-by bounds; treat `catchesUntilPity` as a ceiling.

### assignment (`assignments[]`)

| Field | Type | Notes |
|-------|------|-------|
| `hunter` | object | Hunter ref. |
| `rumour` | object \| null | Rumour ref, or null if not yet discovered for this master. |
| `known` | boolean | Whether this master's rumour has been discovered. |
| `active` | boolean | Whether this is the currently-active master. |

---

## Notes for consumers

- **Absent = no data.** A missing section key means it's toggled off (or, for `combat.target`, not
  applicable). Don't distinguish absent from null.
- **`fromCache` matters for bank/inventory/equipment.** If `fromCache` is true, the data is as of
  `lastSeenTimestamp`, not "now".
- **Health is a percentage, not exact HP.**
- **Player combat targets carry no identifying data** by design.
