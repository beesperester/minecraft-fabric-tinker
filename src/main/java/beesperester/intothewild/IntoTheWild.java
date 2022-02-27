package beesperester.intothewild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class IntoTheWild implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("intothewild");
    public static Config CONFIG = Config.attemptLoadConfig();

    @Override
    public void onInitialize() {

    }
}