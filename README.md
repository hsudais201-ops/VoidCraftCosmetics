# VoidCraft Cosmetics

Fabric client mod prototype for Minecraft Java 1.21.1.

## Build
The GitHub Actions workflow builds the project with Java 21 and uploads the JAR as an artifact when the build succeeds. Open the repository's **Actions** tab and select the successful **Build VoidCraft Cosmetics** run to download the artifact.

## Implemented prototype features
- Fabric client entrypoint.
- A 3D cap mesh is attached to the player head through Fabric's feature-renderer registration callback.
- Press **O** to cycle through **Off**, **Purple**, **Red**, **Blue**, and **Black** cap styles.
- Cap styles use built-in Minecraft wool textures; no extra texture files are required for these previews.
- Cosmetic pack definitions are discovered recursively under `config/voidcraft-cosmetics/` and validated for supported fields, duplicate IDs, file extensions, and safe paths.

## Not implemented yet
Custom pack models and textures are not rendered; the pack loader currently validates their metadata and file references only. A full cosmetics selection screen, user-provided texture loading, capes/wings/tails/horns, animated models, and network synchronization remain future work. Other players will only see cosmetics when their client has compatible rendering/data support; this prototype does not synchronize equipped cosmetics.

Target: Fabric / Minecraft 1.21.1 / Java 21.
