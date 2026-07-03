package com.binaris.wizardry.datagen.provider;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base data provider for generating a Markdown table with all the artifacts registered in the mod, including item sprite,
 * artifact name and description. This is made specially to be used for VitePress documentation.
 */
public abstract class ArtifactDocsProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;
    private final String modId;
    private final String iconBasePath;
    private final List<Item> artifacts = new ArrayList<>();
    private final Map<String, String> translations = new HashMap<>();

    protected ArtifactDocsProvider(PackOutput output, String modId, String iconBasePath) {
        this.output = output;
        this.modId = modId;
        this.iconBasePath = iconBasePath;
        loadTranslations();
    }

    protected abstract void buildArtifacts(@NotNull Consumer<Item> consumer);

    /** Load en_us.json translations. Used to get the artifacts descriptions */
    private void loadTranslations() {
        String langPath = "/assets/" + modId + "/lang/en_us.json";
        try (InputStream is = getClass().getResourceAsStream(langPath)) {
            if (is == null) {
                LOGGER.error("Could not find language file at {}", langPath);
                return;
            }
            JsonObject langJson = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            langJson.entrySet().forEach(entry ->
                    translations.put(entry.getKey(), entry.getValue().getAsString())
            );
            LOGGER.info("Loaded {} translations for {}", translations.size(), modId);
        } catch (IOException e) {
            LOGGER.error("Error loading translations: {}", e.getMessage());
        }
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        artifacts.clear();
        buildArtifacts(artifacts::add);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        StringBuilder md = new StringBuilder();


        md.append("# Artifacts\n\n");


        md.append("| Icon | Name | Description |\n");
        md.append("|------|------|-------------|\n");

        for (Item artifact : artifacts) {
            ResourceLocation location = BuiltInRegistries.ITEM.getKey(artifact);

            String displayName = translate(artifact.getDescriptionId());
            String iconPath = iconBasePath + location.getPath() + ".png";
            String description = translate(artifact.getDescriptionId() + ".desc");


            md.append("| ![](").append(iconPath).append(") ");
            md.append("| ").append(displayName);
            md.append("| ").append(description).append(" ");
            md.append("|\n");
        }

        md.append("\n");


        // Save the spells.md table file
        Path tablePath = output.getOutputFolder().resolve("docs/artifacts.md");
        futures.add(saveStringToFile(cachedOutput, md.toString(), tablePath));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Translates a key to its localized value. If the key is not found, returns the key itself.
     *
     * @param key The translation key
     * @return The translated string, or the key if not found
     */
    protected String translate(String key) {
        return translations.getOrDefault(key, key);
    }

    /**
     * Helper method to save string content to a file. This method creates the necessary directories, converts the string
     * to bytes, and uses the CachedOutput to write the file.
     *
     * @param cache   The CachedOutput to use for writing the file
     * @param content The string content to save
     * @param path    The path where the file should be saved
     * @return A CompletableFuture representing the asynchronous file writing operation
     */
    @SuppressWarnings("deprecation")
    private CompletableFuture<?> saveStringToFile(CachedOutput cache, String content, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(path.getParent());
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file: " + path, e);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "Artifact Docs: " + modId;
    }
}
