package dev.voidcraft.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovers user-installed cosmetic pack definitions.
 *
 * Discovers and validates metadata, model definitions, and safe asset paths.
 * The initial renderer currently supports custom head-slot packs only.
 */
public final class CosmeticPackLoader {
    private static final Logger LOGGER = Logger.getLogger("VoidCraftCosmetics");
    private static final String CONFIG_DIRECTORY = "voidcraft-cosmetics";
    private static final int SUPPORTED_FORMAT_VERSION = 1;

    private CosmeticPackLoader() {}

    public static List<CosmeticPack> discover(Path gameDirectory) {
        Path root = gameDirectory.resolve("config").resolve(CONFIG_DIRECTORY).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not create cosmetics folder: " + root, exception);
            return Collections.emptyList();
        }

        List<CosmeticPack> packs = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .sorted()
                    .forEach(path -> readDefinition(root, path, packs, ids));
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not scan cosmetics folder: " + root, exception);
        }
        return Collections.unmodifiableList(packs);
    }

    private static void readDefinition(Path root, Path definition, List<CosmeticPack> packs, Set<String> ids) {
        try {
            if (!definition.toRealPath().startsWith(root.toRealPath())) {
                throw new IllegalArgumentException("definition symlink must stay inside the cosmetics folder");
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not resolve cosmetic definition: " + definition, exception);
            return;
        }

        try (Reader reader = Files.newBufferedReader(definition)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("format_version") && (!json.get("format_version").isJsonPrimitive()
                    || !json.getAsJsonPrimitive("format_version").isNumber()
                    || json.get("format_version").getAsInt() != SUPPORTED_FORMAT_VERSION)) {
                throw new IllegalArgumentException("unsupported format_version; expected "
                        + SUPPORTED_FORMAT_VERSION);
            }

            String id = requiredString(json, "id").toLowerCase(Locale.ROOT);
            String name = requiredString(json, "name");
            String slot = requiredString(json, "slot").toLowerCase(Locale.ROOT);
            String model = requiredString(json, "model");
            String texture = requiredString(json, "texture");

            if (!id.matches("[a-z0-9_.-]+")) {
                throw new IllegalArgumentException("id may contain only a-z, 0-9, _, . and -");
            }
            if (ids.contains(id)) {
                throw new IllegalArgumentException("duplicate cosmetic id: " + id);
            }
            if (!List.of("head", "body", "back", "legs", "feet").contains(slot)) {
                throw new IllegalArgumentException("unsupported slot: " + slot);
            }
            if (!model.toLowerCase(Locale.ROOT).endsWith(".json")) {
                throw new IllegalArgumentException("model reference must end in .json");
            }
            if (!texture.toLowerCase(Locale.ROOT).endsWith(".png")) {
                throw new IllegalArgumentException("texture reference must end in .png");
            }

            Path modelPath = safeResolve(root, model);
            Path texturePath = safeResolve(root, texture);
            if (!Files.isRegularFile(modelPath)) {
                throw new IllegalArgumentException("model file not found: " + model);
            }
            if (!Files.isRegularFile(texturePath)) {
                throw new IllegalArgumentException("texture file not found: " + texture);
            }

            CosmeticModelDefinition.Definition parsedModel = CosmeticModelDefinition.read(modelPath);
            LOGGER.info("Validated model for " + id + ": " + parsedModel.parts().size()
                    + " parts, " + parsedModel.cubeCount() + " cubes");

            String animation = json.has("animation") && !json.get("animation").isJsonNull()
                    ? json.get("animation").getAsString() : "";
            packs.add(new CosmeticPack(id, name, slot, modelPath, texturePath, animation));
            ids.add(id);
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

    private static Path safeResolve(Path root, String relative) throws IOException {
        Path supplied = Path.of(relative);
        if (supplied.isAbsolute()) {
            throw new IllegalArgumentException("asset paths must be relative");
        }
        Path resolved = root.resolve(supplied).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("path must stay inside the cosmetics folder");
        }
        if (Files.exists(resolved)) {
            Path realRoot = root.toRealPath();
            Path realResolved = resolved.toRealPath();
            if (!realResolved.startsWith(realRoot)) {
                throw new IllegalArgumentException("asset symlink must stay inside the cosmetics folder");
            }
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
