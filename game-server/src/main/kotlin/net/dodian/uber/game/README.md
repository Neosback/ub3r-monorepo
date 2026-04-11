## Game

This package contains all **gameplay content** — the actual game features that players
interact with. Everything here defines *what happens* in the game world.

- **`content/`** — All gameplay modules
  - **`combat/`** — Combat styles, mechanics
  - **`commands/`** — Player, admin, dev, and beta commands
  - **`dialogue/`** — NPC dialogue system
  - **`events/`** — Content-specific events (party room, etc.)
  - **`items/`** — Item interactions (consumables, equipment, combinations, cosmetics)
  - **`minigames/`** — Casino and other minigames
  - **`npcs/`** — NPC definitions and interaction handlers
  - **`objects/`** — Object interactions (banking, doors, travel)
  - **`shop/`** — Shop system and shop plugin definitions
  - **`skills/`** — All skill implementations (mining, fishing, woodcutting, etc.)
  - **`social/`** — Chat, dialogue, social features
  - **`ui/`** — Interface buttons and UI handlers

### Design Philosophy

Content modules import from `api` for model types and plugin DSLs, and occasionally
from `engine.systems` for runtime services. Content should **never** import from
`engine.sync`, `engine.net`, or `engine.phases` — those are internal engine details.

