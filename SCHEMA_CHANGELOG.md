# Schema changelog

Shape changes to the export JSON, tracked by **`schemaVersion`** (semver — see
[SCHEMA.md](SCHEMA.md) for the rules and the full current schema). This logs only
changes to the data *shape*; plugin builds that don't change the shape bump
`exportVersion` instead and aren't listed here.

`1.0.0` is the initial launch (the baseline schema in SCHEMA.md) and isn't
enumerated; entries below cover everything since.

Bump rules: **major** = breaking change · **minor** = new top-level content block ·
**patch** = additive fields on an existing block.

---

## 1.0.1 — decoded slayer area + boss names

**Patch** · additive fields on the existing `slayer.currentTask` block · backward-compatible.

Konar tasks carry a raw `areaId` and boss tasks a raw `bossId` that previously had
to be decoded against the game cache by the consumer. The exporter now decodes
both at export time (the same DB-table lookups RuneLite's Slayer plugin uses) and
ships the human-readable name beside each id.

| Field | Type | Notes |
|-------|------|-------|
| `slayer.currentTask.areaName` | string \| null | Konar's required kill location, decoded from `areaId`. `null` when `areaId` is `0`. |
| `slayer.currentTask.bossName` | string \| null | Boss for a boss-slayer task, decoded from `bossId`. `null` when `bossId` is `0`. |

Example — `slayer.currentTask` for a Konar "Blue dragons in Taverley Dungeon"
assignment. (The `areaId`/`areaName` pairing is illustrative: the name is read
from the game cache at export time, so it always matches whatever `areaId` holds.)

```json
{
  "hasTask": true,
  "taskId": 28,
  "name": "Blue dragons",
  "amountRemaining": 122,
  "initialAmount": 150,
  "areaId": 51,
  "areaName": "Taverley Dungeon",
  "assignedMasterId": 8,
  "assignedMasterName": "Konar quo Maten",
  "bossId": 0,
  "bossName": null
}
```

Non-Konar, non-boss tasks keep `areaId: 0` / `bossId: 0` with `areaName` and
`bossName` set to `null` (as in [example.json](example.json)):

```json
{
  "hasTask": false,
  "taskId": 18,
  "name": "Trolls",
  "amountRemaining": 0,
  "initialAmount": 180,
  "areaId": 0,
  "areaName": null,
  "assignedMasterId": 5,
  "assignedMasterName": "Nieve",
  "bossId": 0,
  "bossName": null
}
```
