package beesperester.intothewild;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beesperester.intothewild.classes.BiomeTemperature;
import beesperester.intothewild.classes.EntityTemperature;

public class Config {

    public boolean allowHungerEffects = true;
    public boolean allowDeathFromStarvation = false;

    public float saturationDepletionRate = (20f * 60f) / 4f;
    public float hungerDepletionRate = (20f * 60f) / 2f;
    public float starvationRate = (20f * 60f) * 3f;

    public boolean allowExposureEffects = true;
    public boolean allowDeathFromExposure = false;

    public float bodyTemperatureHarmlesssDeviation = 10f;
    public float bodyTemperatureHarmfullDeviation = 30f;
    public float bodyDefaultTemperature = 36.5f;

    public List<EntityTemperature> entityProperties;
    public List<BiomeTemperature> biomeTemperatures;

    public Config() {
        EntityTemperature[] defaultEntityProperties = {
                // blocks
                new EntityTemperature("block.minecraft.lantern", 25f),
                new EntityTemperature("block.minecraft.*torch", 50f),
                new EntityTemperature("block.minecraft.campfire", 100f),
                new EntityTemperature("block.minecraft.lava", 200f),
                // leather armor
                new EntityTemperature("item.minecraft.leather_boots", 2.5f),
                new EntityTemperature("item.minecraft.leather_chestplate", 5f),
                new EntityTemperature("item.minecraft.leather_leggings", 5f),
                new EntityTemperature("item.minecraft.leather_helmet", 2.5f),
                // iron armor
                new EntityTemperature("item.minecraft.iron_boots", 5f),
                new EntityTemperature("item.minecraft.iron_chestplate", 10f),
                new EntityTemperature("item.minecraft.iron_leggings", 10f),
                new EntityTemperature("item.minecraft.iron_helmet", 5f),
                // gold armor
                new EntityTemperature("item.minecraft.gold_boots", 10f),
                new EntityTemperature("item.minecraft.gold_chestplate", 20f),
                new EntityTemperature("item.minecraft.gold_leggings", 20f),
                new EntityTemperature("item.minecraft.gold_helmet", 10f),
                // diamond armor
                new EntityTemperature("item.minecraft.diamond_boots", 15f),
                new EntityTemperature("item.minecraft.diamond_chestplate", 25f),
                new EntityTemperature("item.minecraft.diamond_leggings", 25f),
                new EntityTemperature("item.minecraft.diamond_helmet", 15f),
        };

        BiomeTemperature[] defaultBiomeTemperatures = {
                new BiomeTemperature(0f, -5f, -10f),
                new BiomeTemperature(0.8f, 20f, 10f),
                new BiomeTemperature(1.5f, 30f, 15f),
                new BiomeTemperature(2.0f, 40f, 20f),
        };

        entityProperties = Arrays.asList(defaultEntityProperties);

        biomeTemperatures = Arrays.asList(defaultBiomeTemperatures);
    }

    public static Config attemptLoadConfig() {
        Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("into_the_wild_config.json");
        File configFile = configFilePath.toFile();

        if (configFile.exists()) {
            try {
                Config config = Config.loadConfigFile(configFile);
                Config.writeConfigFile(configFile, config);

                return config;
            } catch (IOException ex) {
                IntoTheWild.LOGGER
                        .error("Something went wrong while loading the config file, using default config file");

                return new Config();
            }
        } else {
            try {
                Config config = new Config();
                Config.writeConfigFile(configFile, config);

                return config;
            } catch (IOException ex) {
                IntoTheWild.LOGGER.error(
                        "Something went wrong while creating a default config. Please report this to the mod author");

                return new Config();
            }
        }
    }

    public static Config loadConfigFile(File configFile) throws IOException {
        FileReader reader = null;
        try {
            Gson gson = new Gson();
            reader = new FileReader(configFile);
            return gson.fromJson(reader, Config.class);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void writeConfigFile(File configFile, Config config) throws IOException {
        FileWriter writer = null;

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer = new FileWriter(configFile);
            writer.write(gson.toJson(config));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
