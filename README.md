# VoidCraft Cosmetics

Fabric client mod prototype for Minecraft Java 1.21.1.

## Build
The GitHub Actions workflow builds the project with Java 21 and uploads the JAR as an artifact when the build succeeds. Open the repository's **Actions** tab and select the successful **Build VoidCraft Cosmetics** run to download the artifact.

## Implemented prototype features
- Fabric client entrypoint.
- A 3D cap mesh is attached to the player head through Fabric's feature-renderer registration callback.
- Press **O** to cycle through **Off**, **Purple**, **Red**, **Blue**, and **Black** built-in cap styles.
- Press **P** to cycle through installed custom head-slot packs and **Off**; the selected pack ID is saved in settings.
- The selected style is saved in `config/voidcraft-cosmetics/settings.properties` and restored on the next launch.
- Cap styles use built-in Minecraft wool textures; no extra texture files are required for these previews.
- Cosmetic pack definitions are discovered recursively under `config/voidcraft-cosmetics/` and validated for supported fields, duplicate IDs, file extensions, and safe paths.
- A custom version-1 cuboid JSON parser validates part origins/rotations, cube sizes/UV origins, estimated UV rectangle bounds, texture dimensions, and model complexity limits.
- The first discovered valid head-slot pack is converted to Minecraft model parts and rendered with its PNG texture. PNGs are limited to 8 MiB and 4096×4096 pixels, and must match the model's declared texture_size.

## Not implemented yet
Custom head-slot cuboid geometry and PNG texture loading have an initial renderer implementation. A valid head pack renders with its own PNG texture; if no valid head pack loads, no custom-pack model is rendered. The separate built-in cap preview remains available via the O key. Body/back/legs/feet slots are validated but not rendered. The latest GitHub Actions build has succeeded, but in-game rendering has not yet been verified. A graphical cosmetics selection screen, capes/wings/tails/horns, animated models, and network synchronization remain future work. Custom pack PNG textures are loaded from the local cosmetics folder. Other players will only see cosmetics when their client has compatible rendering/data support; this prototype does not synchronize equipped cosmetics.

## Settings file
The mod creates `config/voidcraft-cosmetics/settings.properties`. The `capStyle` value is 0 for Off, 1 for Purple, 2 for Red, 3 for Blue, and 4 for Black. Press **O** in game to change it; the mod saves the new choice automatically.

Target: Fabric / Minecraft 1.21.1 / Java 21.
