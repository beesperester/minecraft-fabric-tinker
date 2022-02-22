package beesperester.callofthewild;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beesperester.callofthewild.classes.BlockProperties;

public class Config {

    public boolean allowHungerEffects = true;
    public boolean allowDeathFromStarvation = false;

    public boolean allowExposureEffects = true;
    public boolean allowDeathFromExposure = false;

    public BlockProperties[] blockProperties;

    public Config() {
        blockProperties = new BlockProperties[] {
                new BlockProperties("block.minecraft.lantern", 50f),
                new BlockProperties("block.minecraft.torch", 50f),
                new BlockProperties("block.minecraft.campfire", 200f)
        };
    }

    public static void attemptLoadConfig() {
        Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("call_of_the_wild_config.json");
        File configFile = configFilePath.toFile();

        if (configFile.exists()) {
            try {
                Config config = Config.loadConfigFile(configFile);
                CallOfTheWildMod.CONFIG = config;
                Config.writeConfigFile(configFile, config);
            } catch (IOException ex) {
                CallOfTheWildMod.LOGGER
                        .error("Something went wrong while loading the config file, using default config file");
            }
        } else {
            try {
                Config.writeConfigFile(configFile, new Config());
            } catch (IOException ex) {
                CallOfTheWildMod.LOGGER.error(
                        "Something went wrong while creating a default config. Please report this to the mod author");
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
