package kor.toxicity.toxicitylibs.api.manager;

import kor.toxicity.toxicitylibs.api.ToxicityPlugin;

public interface ToxicityPluginManager {
    void start(ToxicityPlugin plugin);
    void reload(ToxicityPlugin plugin);
    void end(ToxicityPlugin plugin);
}
