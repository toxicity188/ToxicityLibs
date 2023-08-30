package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import org.jetbrains.annotations.NotNull;

public class ToxicityAPI {
    private ToxicityAPI() {
        throw new RuntimeException();
    }
    public static ToxicityPlugin getInstance() {
        return ToxicityLibs.getInstance();
    }
    public static void setInstance(@NotNull ToxicityPlugin plugin) {
        ToxicityLibs.setInstance(plugin);
    }
}
