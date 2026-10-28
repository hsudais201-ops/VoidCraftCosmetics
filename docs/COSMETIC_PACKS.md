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
  "animation": ""
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

**Important:** this is still pack metadata validation only. The model JSON is not parsed and custom pack models/textures are not yet connected to the in-game renderer. The visible prototype cap is a separate built-in 3D mesh using Minecraft's purple wool texture, toggled with **O**. Blockbench, GeckoLib and other model formats are not supported until a format adapter is implemented.

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

**Current status:** this schema is parsed and validated during pack discovery, but the parsed geometry is not yet rendered. Texture PNG dimensions/content are not yet loaded by the mod.
