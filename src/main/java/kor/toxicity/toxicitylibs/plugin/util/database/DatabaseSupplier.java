package kor.toxicity.toxicitylibs.plugin.util.database;

import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import kor.toxicity.toxicitylibs.plugin.util.ConfigUtil;
import kor.toxicity.toxicitylibs.plugin.util.FunctionUtil;
import kor.toxicity.toxicitylibs.plugin.util.data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
public enum DatabaseSupplier {
    YML {
        @Override
        public Database supply(ConfigurationSection section) {
            return new Database() {
                @Override
                public void close() {

                }

                @Override
                public @NotNull PlayerData load(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player) {
                    var file = getUserFile(plugin,player);
                    if (file != null) {
                        var yaml = new YamlConfiguration();
                        try {
                            yaml.load(file);
                            var list = ConfigUtil.getAsStringList(yaml,"storage").orElse(null);
                            if (list != null) return new PlayerData(player,list);
                        } catch (Exception ex) {
                            ToxicityLibs.warn("error has occurred: " + player.getName());
                        }
                    }
                    ToxicityLibs.warn("unable to read this user's data: " + player.getName());
                    return new PlayerData(player,Collections.emptyList());
                }

                @Override
                public boolean save(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player, @NotNull PlayerData data) {
                    var file = getUserFile(plugin,player);
                    if (file != null) {
                        var yaml = new YamlConfiguration();
                        try {
                            yaml.set("storage", data.getSerializedStorageItem());
                            yaml.save(file);
                            return true;
                        } catch (Exception ex) {
                            ToxicityLibs.warn("error has occurred: " + player.getName());
                        }
                    }
                    return false;
                }

                private @Nullable File getUserFile(JavaPlugin plugin, OfflinePlayer player) {
                    var dataFolder = plugin.getDataFolder();
                    if (!dataFolder.exists()) dataFolder.mkdir();
                    var userFolder = new File(dataFolder,"users");
                    if (!userFolder.exists()) userFolder.mkdir();
                    var user = new File(userFolder,player.getUniqueId() + ".yml");
                    try {
                        if (!user.exists()) user.createNewFile();
                        return user;
                    } catch (IOException exception) {
                        return null;
                    }
                }
            };
        }
    },
    MYSQL {
        @Override
        public @Nullable Database supply(ConfigurationSection section) {
            var host = ConfigUtil.getAsString(section,"host").orElse(null);
            if (host == null) {
                ToxicityLibs.warn("host not found!");
                return null;
            }
            var database = ConfigUtil.getAsString(section,"database").orElse(null);
            if (database == null) {
                ToxicityLibs.warn("database not found!");
                return null;
            }
            var name = ConfigUtil.getAsString(section,"name").orElse(null);
            if (name == null) {
                ToxicityLibs.warn("name not found!");
                return null;
            }
            var password = ConfigUtil.getAsString(section,"password").orElse(null);
            if (password == null) {
                ToxicityLibs.warn("name not found!");
                return null;
            }
            try {
                var mysql = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true&useSSL=false", name, password);

                try (Statement statement = mysql.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS storage(uuid CHAR(36) NOT NULL, number BIGINT UNSIGNED NOT NULL, value TEXT(65535) NOT NULL, PRIMARY KEY(uuid,number));");
                } catch (Exception e) {
                    ToxicityLibs.warn("unable to make a default table.");
                }
                return new Database() {
                    @Override
                    public void close() {
                        try {
                            mysql.close();
                        } catch (SQLException e) {
                            var message = e.getMessage();
                            ToxicityLibs.warn("unable to close mysql: " + (message != null ? message : "<none>"));
                        }
                    }

                    @Override
                    public @NotNull PlayerData load(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player) {
                        var list = new ArrayList<String>();
                        var uuid = "'" + player.getUniqueId() + "';";
                        executeQuery("SELECT value FROM storage WHERE uuid = " + uuid, r -> {
                            try {
                                list.add(r.getString("value"));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return new PlayerData(player,list);
                    }

                    @Override
                    public boolean save(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player, @NotNull PlayerData data) {
                        var uuid = player.getUniqueId().toString();
                        executeUpdate("DELETE FROM storage WHERE uuid = '" + uuid + "';", s -> {});
                        FunctionUtil.forEachIndexed(data.getSerializedStorageItem(),(i,s) -> executeUpdate("INSERT INTO storage(uuid, number, value) VALUES(?,?,?);", p -> {
                            try {
                                p.setString(0,uuid);
                                p.setInt(1, i);
                                p.setString(2, s);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }));
                        return true;
                    }

                    private void executeQuery(String query, Consumer<ResultSet> consumer) {
                        try (PreparedStatement statement = mysql.prepareStatement(query)) {
                            var set = statement.executeQuery();
                            while (set.next()) {
                                consumer.accept(set);
                            }
                        } catch (Exception e) {
                            var message = e.getMessage();
                            ToxicityLibs.warn("unable to execute query: " + (message != null ? message : "<none>"));
                        }
                    }
                    private void executeUpdate(String query, Consumer<PreparedStatement> statementConsumer) {
                        try (PreparedStatement statement = mysql.prepareStatement(query)) {
                            statementConsumer.accept(statement);
                            statement.executeUpdate();
                        } catch (Exception e) {
                            var message = e.getMessage();
                            ToxicityLibs.warn("unable to execute query: " + (message != null ? message : "<none>"));
                        }
                    }

                };
            } catch (Exception ex) {
                var message = ex.getMessage();
                ToxicityLibs.warn("sql exception occurred: " + (message != null ? message : "<none>"));
                return null;
            }
        }
    }
    ;
    public abstract @Nullable Database supply(ConfigurationSection section);
}
