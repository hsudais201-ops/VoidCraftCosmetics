# VoidCraft Cosmetics

Fabric client mod prototype for Minecraft Java 1.21.1.

## Build and download

The GitHub Actions workflow builds with Java 21 and uploads a Fabric mod JAR when successful. Open the [Actions page](https://github.com/hsudais201-ops/VoidCraftCosmetics/actions), select a successful **Build VoidCraft Cosmetics** run, and download the artifact named `VoidCraftCosmetics-fabric-1.21.1`.

This repository produces a **Fabric JAR, not an Android APK**.

## Controls

- **M** — open the cosmetic selection menu.
- **Left / Right arrow** — highlight the previous or next installed pack.
- **Enter** or **Equip selected** — equip the highlighted pack without unequipping other packs.
- **Unequip selected** — remove only the highlighted pack.
- **Turn custom cosmetic off** — clear all equipped custom packs.
- **Reload packs** — rescan cosmetic definitions and refresh the renderer cache.
- **O** — cycle built-in cap styles: Off, Purple, Red, Blue, Black.
- **P** — cycle through installed custom packs one at a time, then Off.

The selection menu shows the highlighted pack, its slot and animation preset, equipped status, equipped pack names, and installed pack count. It also attempts to render a live 3D player preview; dragging over the preview area rotates it. The preview compiles but still needs testing in an actual Minecraft session.

## Custom cosmetic packs

Put pack definition JSON files anywhere under `config/voidcraft-cosmetics/`. Referenced model and PNG paths are relative to that root. See [the complete pack format and quick-start guide](docs/COSMETIC_PACKS.md).

Supported declared slots:
- `head`, `horns`
- `body`, `back`, `cape`, `wings`, `tail`
- `legs`, `feet`

The loader validates unique IDs, paths, supported slots and animation presets, model structure, UV bounds, texture dimensions, and size limits. Model geometry uses VoidCraft's own version-1 cuboid JSON format; Blockbench, Bedrock and GeckoLib model formats are **not** directly supported.

Basic animation presets include `bob`, `spin`, `sway`, `flap`, `tail` and `pulse`. Named model parts containing `left_wing`, `right_wing` or `tail` receive simple rotation animation.

## Built-in cap

A 3D cap mesh is attached to the player head. The built-in cap uses Minecraft wool textures and does not require custom PNG files. The selected cap style and equipped custom pack IDs are saved in `config/voidcraft-cosmetics/settings.properties`.

## Current limitations

- This is a prototype; successful CI builds verify compilation and packaging, **not** that every cosmetic renders correctly in-game.
- Cape, wing and tail slots currently use the torso attachment; horns use the head attachment; legs and feet use the leg pivots. These are basic attachment points, not separate physics rigs or fully fitted anatomy.
- Animations are simple transforms, not a full skeletal/keyframe system.
- Cosmetics are client-side. Other players will not necessarily see them unless they have compatible client-side support or a compatible server/network synchronization system is added.
- Rendering and draggable preview behavior still need real in-game testing.

## Target

Fabric / Minecraft Java 1.21.1 / Java 21.
