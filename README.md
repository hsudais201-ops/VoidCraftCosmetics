# VoidCraft Cosmetics

Fabric client mod prototype for Minecraft Java 1.21.1.

## Build
The GitHub Actions workflow builds the project with Java 21 and uploads the JAR as an artifact when the build succeeds. Open the repository's **Actions** tab and select the successful **Build VoidCraft Cosmetics** run to download the artifact.

## Prototype status
- Fabric client entrypoint is in place.
- Press **O** in game to toggle a preview flag and see its status message.

## Not implemented yet
The current prototype does not render a cosmetic. Actual 3D accessory rendering, custom model/texture pack loading, cape and wing animations, and multiplayer synchronization remain future work. Multiplayer visibility requires a compatible system on other clients or the server.

Target: Fabric / Minecraft 1.21.1 / Java 21.
