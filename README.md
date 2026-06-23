# Account Data Exporter

A RuneLite plugin that writes local account snapshots to JSON for spreadsheets, account tracking,
and personal analytics. See the schema for the full data output and format.

This plugin is largely designed for me for a personal project, but I can see this type of tool is likely useful to others as well. If you have any suggestions, please open an issue or PR.

I initially tried to extend [local-data-exporter](https://github.com/GoblinTek/local-data-exporter/pull/1) by [GoblinTek](https://github.com/GoblinTek) but my PR has been open and inactive for 2 weeks now and I suspect his plugin was largly one-shot and vibe coded anyway due to its structure; so I decided to make my own plugin from the ground up with a more maintainable approach.

## Output

JSON is written to `.runelite/account-data-exporter/`:

- `latest.json` — the most recent snapshot
- `<RSN>.json` — the most recent snapshot for that account

Each section can be toggled in the plugin config (all on by default).

See [`SCHEMA.md`](SCHEMA.md) for the full field-by-field format and [`example.json`](example.json)
for a complete sample export.

- The [hunter rumours plugin](https://runelite.net/plugin-hub/show/hunter-rumours) is needed if you want to export hunter rumours data; this is because the capture mechanism is via dialogue and thats not something I want to support. I suspect any user that wants to export this data will have the plugin installed anyway.

## Building

```sh
./gradlew run        # launch a development client with the plugin loaded
./gradlew shadowJar  # build a side-loadable jar
```

## AI Use

This tool was absolutely built with AI (claude) however I am also a software engineer by day.

- I have tried to guide the AI to produce code that is correct, readable, and maintainable in a way that I /think/ I would have written it myself, but I cannot guarantee that the code is perfect or free of bugs - "works for me".


## Contributors

- If you would like to submit a PR and you use AI, please be transparent about it in your PR description. I will not reject PRs that use AI, but I do want to know about it, as it helps me review the code.
- But please write your own PR or issue descriptions, and do not use AI to write it for you. I want to know your thoughts and reasoning, not an AI's; its too easy to throw up a bunch of waffle with AI.
- Any breaking changes to the schema will be considered a major version bump, and I will not accept PRs that break the schema without a good reason. If you want to add new fields, please do so in a backwards compatible way.
- Always update the [`example.json`](example.json) file with any new fields you add, and update the [`SCHEMA.md`](SCHEMA.md) file with any changes to the schema.
