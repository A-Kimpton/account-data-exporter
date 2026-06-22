# Account Data Exporter

A RuneLite plugin that writes local account snapshots to JSON for spreadsheets, account tracking,
and personal analytics.

## Output

JSON is written to `.runelite/account-data-exporter/`:

- `latest.json` — the most recent snapshot
- `<RSN>.json` — the most recent snapshot for that account

## Building

```sh
./gradlew run        # launch a development client with the plugin loaded
./gradlew shadowJar  # build a side-loadable jar
```
