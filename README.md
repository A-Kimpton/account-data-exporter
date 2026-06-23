# Account Data Exporter

A RuneLite plugin that writes local account snapshots to JSON for spreadsheets, account tracking,
and personal analytics.

## Output

JSON is written to `.runelite/account-data-exporter/`:

- `latest.json` — the most recent snapshot
- `<RSN>.json` — the most recent snapshot for that account

Each section can be toggled in the plugin config (all on by default).

See [`SCHEMA.md`](SCHEMA.md) for the full field-by-field format and [`example.json`](example.json)
for a complete sample export.

## Building

```sh
./gradlew run        # launch a development client with the plugin loaded
./gradlew shadowJar  # build a side-loadable jar
```
