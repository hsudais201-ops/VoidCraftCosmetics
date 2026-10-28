# VoidCraft Cosmetics

Fabric client mod prototype for Minecraft Java 1.21.1.

## Build
The GitHub Actions workflow builds the project with Java 21 and uploads the JAR as an artifact when the build succeeds. Open the repository's **Actions** tab and select the successful **Build VoidCraft Cosmetics** run to download the artifact.

## Implemented prototype features
- Fabric client entrypoint.
- A basic 3D cap mesh is attached to the player head through Fabric's feature-renderer registration callback.
- Press **O** in game to toggle the cap on or off.
- The prototype cap uses Minecraft's built-in purple wool texture; it is not yet a user-selectable cosmetic.

## Not implemented yet
Custom pack models and textures are discovered and validated but not rendered. A cosmetics selection screen, cap texture customization, wings/capes, animations, and network synchronization remain future work. Other players will only see a cosmetic when their client has compatible rendering/data support; this prototype does not synchronize equipped cosmetics.

Target: Fabric / Minecraft 1.21.1 / Java 21.
