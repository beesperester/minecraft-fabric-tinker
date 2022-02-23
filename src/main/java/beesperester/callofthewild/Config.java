package beesperester.callofthewild;

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

import beesperester.callofthewild.classes.BlockProperties;

public class Config {

    public boolean allowHungerEffects = true;
    public boolean allowDeathFromStarvation = false;

    public boolean allowExposureEffects = true;
    public boolean allowDeathFromExposure = false;

    public List<BlockProperties> blockProperties;

    public Config() {
        BlockProperties[] defaultBlockProperties = {
                new BlockProperties("block.minecraft.lantern", 50f),
                new BlockProperties("block.minecraft.torch", 50f),
                new BlockProperties("block.minecraft.campfire", 200f)
        };

        blockProperties = Arrays.asList(defaultBlockProperties);
    }

    public static Config attemptLoadConfig() {
        Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("call_of_the_wild_config.json");
        File configFile = configFilePath.toFile();

        if (configFile.exists()) {
            try {
                Config config = Config.loadConfigFile(configFile);
                Config.writeConfigFile(configFile, config);

                return config;
            } catch (IOException ex) {
                CallOfTheWildMod.LOGGER
                        .error("Something went wrong while loading the config file, using default config file");

                return new Config();
            }
        } else {
            try {
                Config config = new Config();
                Config.writeConfigFile(configFile, config);

                return config;
            } catch (IOException ex) {
                CallOfTheWildMod.LOGGER.error(
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
