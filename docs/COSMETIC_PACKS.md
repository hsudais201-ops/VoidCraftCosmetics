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
- `slot` is one of `head`, `horns`, `body`, `back`, `cape`, `wings`, `tail`, `legs` or `feet`.
- The model path ends in `.json`, the texture path ends in `.png`, and both files exist.
- Asset paths must be relative and stay inside the cosmetics directory, including after resolving symbolic links.
- Definition files linked from outside the cosmetics directory are skipped.

**Current renderer status:** the prototype attempts to render custom packs for all nine declared slots. Press **P** to cycle through discovered packs and then **Off**; this shortcut selects one pack at a time, while the menu supports multiple equipped packs. Press **O** to cycle the separate built-in cap styles. Head/horns packs attach to the head; body/back/cape/wings/tail packs attach to the torso; leg/feet packs are drawn at both leg pivots. The new `cape`, `wings`, `tail`, and `horns` names are slot aliases, not separate physics rigs: cape/wings/tail still use the torso pivot and horns use the head pivot. This is an early attachment strategy, not a polished anatomical fitting system—models may need slot-specific coordinates and may overlap. Basic named animation presets are supported: `""` (none), `bob`, `spin`, `sway`, `flap`, `tail`, and `pulse`. The `tail` preset adds gentle yaw and side-to-side rotation; it is only a basic motion preset and depends on positioning the model correctly. The `flap` preset rotates the model around its X axis and can be used as a basic wing/appendage motion when the model origin is positioned appropriately. These are simple renderer transforms, not a full keyframe animation system. Blockbench, GeckoLib and other model formats are not supported until a format adapter is implemented.

## Supported model JSON schema (version 1)

The loader parses and validates a simple VoidCraft cuboid format. This is a small custom format, **not** a Blockbench export format. Example model file:

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


### Named moving model parts

Model parts are rendered as separate named pieces. For independently moving wings, name the pieces `left_wing` and `right_wing` (or `leftwing` and `rightwing`). For a moving tail, include `tail` in the part name, such as `tail_base` or `dragon_tail`. The renderer adds a simple mirrored flap to left/right wing pieces and a small sway to tail-named pieces while preserving each part's configured base rotation. This is still a basic preset animation: it does not add skeletal hierarchy, physics, or keyframes.

## In-game selection menu

Press `M` to open the cosmetic selection screen. Use **Reload Packs** after adding, removing, or editing pack files while the game is running; the loader rescans definitions and refreshes cached custom models/textures. Use **Previous Cosmetic** and **Next Cosmetic** to highlight an installed pack. Press **Enter** to equip the highlighted pack from the keyboard, or use **Equip Selected** to add it without removing other equipped packs. **Unequip Selected** removes only the highlighted pack. **Turn Custom Cosmetic Off** clears all equipped custom packs. The menu lists the currently equipped pack names and the total number of discovered packs. The built-in cap button cycles the separate built-in cap styles. `P` and `O` remain available as keyboard shortcuts.

The menu now attempts to show a live 3D player preview of the highlighted pack; drag over the preview area to rotate the player. This preview compiles, but it has not yet been verified in an actual Minecraft session; if it fails, you can still equip the selected pack. Custom pack rendering and menu interaction still need in-game testing. This repository builds a Fabric mod JAR, not an Android APK. Other players will not necessarily see your cosmetics in multiplayer unless they have compatible client-side support or a compatible server/network synchronization system is added.


## Quick start: install your first custom cosmetic

1. Start Minecraft once with VoidCraft Cosmetics installed, then close the game.
2. Open the Minecraft game directory and create `config/voidcraft-cosmetics/starter/models/` and `config/voidcraft-cosmetics/starter/textures/`.
3. Save the pack definition below as `config/voidcraft-cosmetics/starter/starter_cap.json`.
4. Save the model JSON below as `config/voidcraft-cosmetics/starter/models/starter_cap.json`.
5. Put a **64×64 PNG** named `starter_cap.png` in `config/voidcraft-cosmetics/starter/textures/`. The PNG must be a real texture image; the mod does not generate missing textures.
6. Launch the game and press **M**. Select **Starter Cap** and choose **Equip selected**. If it does not appear, check `logs/latest.log` for a VoidCraft Cosmetics warning.

Pack definition (`starter_cap.json`):

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

Model (`models/starter_cap.json`):

```json
{
  "format_version": 1,
  "texture_size": [64, 64],
  "parts": [
    {
      "name": "cap_crown",
      "origin": [0, -8, 0],
      "rotation": [0, 0, 0],
      "cubes": [
        {
          "origin": [-4, -4, -4],
          "size": [8, 4, 8],
          "uv": [0, 0]
        }
      ]
    },
    {
      "name": "cap_brim",
      "origin": [0, -5, 0],
      "rotation": [0, 0, 0],
      "cubes": [
        {
          "origin": [-4.5, -0.5, -4.5],
          "size": [9, 1, 9],
          "uv": [0, 16]
        }
      ]
    }
  ]
}
```

This starter model is only a geometry template. You must supply a matching PNG texture, and you may need to adjust the model's origins and texture UVs for the look you want. If you change the texture dimensions, update `texture_size` to match. Pack assets are loaded from paths relative to the main `config/voidcraft-cosmetics/` directory, even when the definition is in a subfolder.
