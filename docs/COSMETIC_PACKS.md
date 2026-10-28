# Custom cosmetic pack format (prototype)

Put cosmetic definitions anywhere inside the game directory's:

`config/voidcraft-cosmetics/`

The loader searches subfolders recursively for JSON definitions. Example `starter/starter_cap.json`:

```json
{
  "format_version": 1,
  "id": "starter_cap",
  "name": "Starter Cap",
  "slot": "head",
  "model": "starter/models/starter_cap.json",
  "texture": "starter/textures/starter_cap.png",
  "animation": "bob"
}
```

Referenced asset paths are relative to the main `config/voidcraft-cosmetics/` directory, not the definition's subfolder. For the example above, place the assets at:

```text
config/voidcraft-cosmetics/
└── starter/
    ├── starter_cap.json
    ├── models/
    │   └── starter_cap.json
    └── textures/
        └── starter_cap.png
```

Validation currently checks:
- `format_version` is 1 when supplied.
- `id` uses lowercase letters, digits, underscores, dots or hyphens and is unique across discovered packs.
- `slot` is one of `head`, `body`, `back`, `legs` or `feet`.
- The model path ends in `.json`, the texture path ends in `.png`, and both files exist.
- Asset paths must be relative and stay inside the cosmetics directory, including after resolving symbolic links.
- Definition files linked from outside the cosmetics directory are skipped.

**Current renderer status:** the prototype attempts to render the selected pack for all five declared slots. Press **P** to cycle through discovered packs and then **Off**. Press **O** to cycle the separate built-in cap styles. Head packs attach to the head; body/back attach to the torso; leg/feet packs are drawn at both leg pivots. This is an early attachment strategy, not a polished anatomical fitting system—models may need slot-specific coordinates and may overlap. Basic named animation presets are supported: `""` (none), `bob`, `spin`, `sway`, and `pulse`. These are simple renderer transforms, not a full keyframe animation system. Blockbench, GeckoLib and other model formats are not supported until a format adapter is implemented.

## Supported model JSON schema (version 1)

The loader now parses and validates a simple VoidCraft cuboid format. This is a small custom format, **not** a Blockbench export format. Example model file:

```json
{
  "format_version": 1,
  "texture_size": [64, 64],
  "parts": [
    {
      "name": "cap",
      "origin": [0, 0, 0],
      "rotation": [0, 0, 0],
      "cubes": [
        {
          "origin": [-4, -8, -4],
          "size": [8, 4, 8],
          "uv": [0, 0]
        }
      ]
    }
  ]
}
```

Each part requires a name, three-number origin, and one or more cubes. Rotation is optional and defaults to zero. Each cube requires a three-number origin, positive three-number size, and two-number UV origin. Texture dimensions must be between 1 and 4096; models are limited to 64 parts and 256 cubes. Coordinates must be finite and within +/-1024.

**Texture rules:** PNG files must be at most 8 MiB, have dimensions no larger than 4096×4096, and exactly match `texture_size`. Each cube's UV origin and estimated UV rectangle must fit within the declared texture. Invalid packs are skipped and logged. Rendering has not yet been verified in an actual Minecraft session. This is prototype support for the declared slots, not a guarantee that arbitrary models will fit every player skin or animation.


## In-game selection menu

Press `M` to open the cosmetic selection screen. Use Previous/Next Cosmetic to choose an installed pack, then select Equip Selected. Turn Custom Cosmetic Off clears the custom pack selection. The built-in cap button cycles the built-in cap styles. `P` and `O` remain available as keyboard shortcuts.
