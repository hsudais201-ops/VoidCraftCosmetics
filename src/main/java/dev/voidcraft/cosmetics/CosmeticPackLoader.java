package dev.voidcraft.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovers user-installed cosmetic pack definitions.
 *
 * This class validates metadata and referenced files only. It does not yet
 * parse arbitrary 3D models or render cosmetics.
 */
public final class CosmeticPackLoader {
    private static final Logger LOGGER = Logger.getLogger("VoidCraftCosmetics");
    private static final String CONFIG_DIRECTORY = "voidcraft-cosmetics";

    private CosmeticPackLoader() {}

    public static List<CosmeticPack> discover(Path gameDirectory) {
        Path root = gameDirectory.resolve("config").resolve(CONFIG_DIRECTORY);
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not create cosmetics folder: " + root, exception);
            return Collections.emptyList();
        }

        List<CosmeticPack> packs = new ArrayList<>();
        try (var paths = Files.list(root)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> readDefinition(root, path, packs));
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not scan cosmetics folder: " + root, exception);
        }
        return Collections.unmodifiableList(packs);
    }

    private static void readDefinition(Path root, Path definition, List<CosmeticPack> packs) {
        try (Reader reader = Files.newBufferedReader(definition)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = requiredString(json, "id").toLowerCase(Locale.ROOT);
            String name = requiredString(json, "name");
            String slot = requiredString(json, "slot").toLowerCase(Locale.ROOT);
            String model = requiredString(json, "model");
            String texture = requiredString(json, "texture");

            if (!id.matches("[a-z0-9_.-]+")) {
                throw new IllegalArgumentException("id may contain only a-z, 0-9, _, . and -");
            }
            if (!List.of("head", "body", "back", "legs", "feet").contains(slot)) {
                throw new IllegalArgumentException("unsupported slot: " + slot);
            }

            Path modelPath = safeResolve(root, model);
            Path texturePath = safeResolve(root, texture);
            if (!Files.isRegularFile(modelPath)) {
                throw new IllegalArgumentException("model file not found: " + model);
            }
            if (!Files.isRegularFile(texturePath)) {
                throw new IllegalArgumentException("texture file not found: " + texture);
            }

            String animation = json.has("animation") && !json.get("animation").isJsonNull()
                    ? json.get("animation").getAsString() : "";
            packs.add(new CosmeticPack(id, name, slot, modelPath, texturePath, animation));
            LOGGER.info("Found cosmetic pack definition: " + id + " (" + name + ")");
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Skipping invalid cosmetic definition " + definition.getFileName()
                    + ": " + exception.getMessage());
        }
    }

    private static String requiredString(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonPrimitive()
                || !json.getAsJsonPrimitive(key).isString()
                || json.get(key).getAsString().isBlank()) {
            throw new IllegalArgumentException("missing required string: " + key);
        }
        return json.get(key).getAsString();
    }

    private static Path safeResolve(Path root, String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root.normalize())) {
            throw new IllegalArgumentException("path must stay inside the cosmetics folder");
        }
        return resolved;
    }

    public record CosmeticPack(
            String id,
            String name,
            String slot,
            Path modelPath,
            Path texturePath,
            String animation
    ) {}
}
