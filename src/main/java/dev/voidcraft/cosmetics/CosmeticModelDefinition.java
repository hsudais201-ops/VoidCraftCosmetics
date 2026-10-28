package dev.voidcraft.cosmetics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser and validator for VoidCraft's simple, documented cuboid model format.
 * This is deliberately not a Blockbench/Bedrock/GeckoLib model importer.
 */
public final class CosmeticModelDefinition {
    private static final int MAX_PARTS = 64;
    private static final int MAX_CUBES = 256;
    private static final int MAX_TEXTURE_DIMENSION = 4096;

    private CosmeticModelDefinition() {}

    public static Definition read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            int version = requiredInt(root, "format_version");
            if (version != 1) {
                throw new IllegalArgumentException("model format_version must be 1");
            }

            int[] textureSize = intVector(required(root, "texture_size"), 2, "texture_size");
            if (textureSize[0] < 1 || textureSize[1] < 1
                    || textureSize[0] > MAX_TEXTURE_DIMENSION
                    || textureSize[1] > MAX_TEXTURE_DIMENSION) {
                throw new IllegalArgumentException("texture_size values must be between 1 and 4096");
            }

            JsonArray partsJson = required(root, "parts").getAsJsonArray();
            if (partsJson.size() < 1 || partsJson.size() > MAX_PARTS) {
                throw new IllegalArgumentException("parts must contain between 1 and 64 entries");
            }

            List<Part> parts = new ArrayList<>();
            int cubeCount = 0;
            for (JsonElement partElement : partsJson) {
                JsonObject part = partElement.getAsJsonObject();
                String name = requiredString(part, "name");
                float[] origin = floatVector(required(part, "origin"), 3, "part.origin");
                float[] rotation = part.has("rotation")
                        ? floatVector(part.get("rotation"), 3, "part.rotation")
                        : new float[] {0, 0, 0};
                JsonArray cubesJson = required(part, "cubes").getAsJsonArray();
                if (cubesJson.size() == 0) {
                    throw new IllegalArgumentException("part " + name + " must have at least one cube");
                }

                List<Cube> cubes = new ArrayList<>();
                for (JsonElement cubeElement : cubesJson) {
                    cubeCount++;
                    if (cubeCount > MAX_CUBES) {
                        throw new IllegalArgumentException("model exceeds 256 cubes");
                    }
                    JsonObject cube = cubeElement.getAsJsonObject();
                    float[] cubeOrigin = floatVector(required(cube, "origin"), 3, "cube.origin");
                    float[] size = floatVector(required(cube, "size"), 3, "cube.size");
                    int[] uv = intVector(required(cube, "uv"), 2, "cube.uv");
                    for (float dimension : size) {
                        if (dimension <= 0 || dimension > 128) {
                            throw new IllegalArgumentException("cube dimensions must be greater than 0 and at most 128");
                        }
                    }
                    if (uv[0] < 0 || uv[1] < 0 || uv[0] >= textureSize[0] || uv[1] >= textureSize[1]) {
                        throw new IllegalArgumentException("cube UV origin must be inside texture_size");
                    }
                    // Approximate standard cuboid UV rectangle: 2*(width+depth) by height+depth.
                    int uvWidth = (int) Math.ceil(2.0 * (size[0] + size[2]));
                    int uvHeight = (int) Math.ceil(size[1] + size[2]);
                    if (uv[0] + uvWidth > textureSize[0] || uv[1] + uvHeight > textureSize[1]) {
                        throw new IllegalArgumentException("cube UV rectangle exceeds texture_size");
                    }
                    cubes.add(new Cube(cubeOrigin, size, uv));
                }
                parts.add(new Part(name, origin, rotation, List.copyOf(cubes)));
            }
            return new Definition(textureSize[0], textureSize[1], List.copyOf(parts), cubeCount);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid VoidCraft cuboid model " + file.getFileName()
                    + ": " + exception.getMessage(), exception);
        }
    }

    private static JsonElement required(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            throw new IllegalArgumentException("missing field: " + key);
        }
        return object.get(key);
    }

    private static String requiredString(JsonObject object, String key) {
        JsonElement value = required(object, key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isBlank()) {
            throw new IllegalArgumentException(key + " must be a non-empty string");
        }
        return value.getAsString();
    }

    private static int requiredInt(JsonObject object, String key) {
        JsonElement value = required(object, key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        double number = value.getAsDouble();
        if (number != Math.rint(number) || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        return (int) number;
    }

    private static int[] intVector(JsonElement element, int length, String field) {
        JsonArray array = element.getAsJsonArray();
        if (array.size() != length) {
            throw new IllegalArgumentException(field + " must contain " + length + " numbers");
        }
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            double number = array.get(i).getAsDouble();
            if (!Double.isFinite(number) || number != Math.rint(number)
                    || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(field + " values must be finite integers");
            }
            result[i] = (int) number;
        }
        return result;
    }

    private static float[] floatVector(JsonElement element, int length, String field) {
        JsonArray array = element.getAsJsonArray();
        if (array.size() != length) {
            throw new IllegalArgumentException(field + " must contain " + length + " numbers");
        }
        float[] result = new float[length];
        for (int i = 0; i < length; i++) {
            double number = array.get(i).getAsDouble();
            if (!Double.isFinite(number) || Math.abs(number) > 1024) {
                throw new IllegalArgumentException(field + " values must be finite and within +/-1024");
            }
            result[i] = (float) number;
        }
        return result;
    }

    public record Definition(int textureWidth, int textureHeight, List<Part> parts, int cubeCount) {}
    public record Part(String name, float[] origin, float[] rotation, List<Cube> cubes) {}
    public record Cube(float[] origin, float[] size, int[] uv) {}
}
