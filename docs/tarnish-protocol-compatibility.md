# Tarnish protocol compatibility

The game client is the protocol authority and remains unchanged. The server uses
the original Tarnish packet-size table, with outbound sizes reconciled to the
current client's `SizeConstants` table.

## Intentionally ignored control packets

- `45`: client mouse movement telemetry
- `86`: camera movement telemetry
- `120`: selected tab notification
- `202`: client idle notification

## Intentionally unsupported gameplay packets

These packets are framed and rejected server-side with `opcode_disabled`, so
they cannot desynchronize the stream. They can be enabled later by adding a
server gameplay service; no client handler changes are required.

- `23`, `79`, `156`, `181`, `253`: currently unimplemented alternate entity or ground actions
- `136`: player gambling request
- `149`: custom item/value setting action
- `187`: custom color-setting update
- `218`: report-abuse submission

## Removed Mystic compatibility

- inbound opcode `11` appearance alias
- inbound opcode `216` Mystic bank-tab creation packet
- inbound opcode `230` legacy NPC-click compatibility packet
- inbound opcodes `77` and `229` legacy no-op bindings
- unused outbound opcode `55` Mystic bank-tab selection writer
- unused Mystic song-setting wrapper that incorrectly reused sound opcode `174`

Actively handled server bindings now all correspond to opcodes emitted by the
unchanged Tarnish client. Fixed outbound packet sizes are validated at encode
time; variable-byte and variable-short packets are validated against the same
client table.
