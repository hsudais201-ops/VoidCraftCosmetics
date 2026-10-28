# Custom cosmetic pack format (prototype)

Put packs in the game directory under:

`config/voidcraft-cosmetics/`

Each definition is a JSON file, for example `starter_cap.json`:

```json
{
  "format_version": 1,
  "id": "starter_cap",
  "name": "Starter Cap",
  "slot": "head",
  "model": "models/starter_cap.json",
  "texture": "textures/starter_cap.png",
  "animation": ""
}
```

Place the referenced files relative to the same directory:

```text
config/voidcraft-cosmetics/
├── starter_cap.json
├── models/
│   └── starter_cap.json
└── textures/
    └── starter_cap.png
```

Allowed slots in this prototype are `head`, `body`, `back`, `legs`, and `feet`. Paths must stay inside the cosmetics directory. The loader discovers definitions and checks that referenced files exist. **Custom model JSON and textures are not yet connected to the renderer.**

The currently rendered prototype cap is a separate built-in 3D mesh using Minecraft's purple wool texture. It can be toggled with **O**. Do not assume the custom model JSON is compatible with Blockbench, GeckoLib, or Minecraft's vanilla model format until a format adapter is implemented.
