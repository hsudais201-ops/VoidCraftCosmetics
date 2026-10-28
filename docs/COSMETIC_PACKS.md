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
