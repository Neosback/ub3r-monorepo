# Discord Bot

The game server uses Kord for its optional Discord gateway bot. It does not
start when `DISCORD_TOKEN` is blank.

When enabled, configure these environment variables outside version control:

- `DISCORD_TOKEN` — bot token.
- `DISCORD_GUILD_ID` — guild where slash commands are registered.
- `DISCORD_CHANNEL_ID` — public announcement channel.
- `DISCORD_STAFF_ALERT_CHANNEL_ID` — staff/security alert channel.

The bot exposes `/players` and `/status` publicly. `/announce` and
`/alert-test` require Discord's Administrator permission and produce a
structured server audit entry. The bot delivers all outbound messages on an
asynchronous bounded queue; failed Discord delivery never blocks gameplay.
