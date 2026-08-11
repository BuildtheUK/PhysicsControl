package org.btuk.pcontrol.util.update.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.btuk.pcontrol.util.FileUtils;
import org.btuk.pcontrol.util.LocaleUtils;
import org.btuk.pcontrol.util.SneakyThrowsSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

public class PluginDataUpdater {
    @SuppressWarnings("unused")
    @Nullable
    private String updateAndReturnNewVersion(@Nonnull String previousVersion) {
        if (previousVersion.toLowerCase().contains("beta")) return "1.0.0";

        String currentVersion = this.getCurrentVersion();
        int[] argsPrevious = parseVersion(previousVersion);
        int[] argsCurrent = parseVersion(currentVersion);
        for (int i = 0; i < argsPrevious.length; i++) {
            if (argsPrevious[i] < argsCurrent[i]) break;
            if (argsPrevious[i] > argsCurrent[i]) return null;
        }

        int majorVersion = argsPrevious[0];
        int minorVersion = argsPrevious[1];
        int patchVersion = argsPrevious[2];

        if (majorVersion == 1 && minorVersion == 0) return this.updateTo_1_1_0();
        if (majorVersion == 1 && minorVersion == 1) return this.updateTo_1_2_0();
        if (majorVersion == 1 && minorVersion == 2) return this.updateTo_1_3_0();

        return currentVersion;
    }

    @Nonnull
    private String updateTo_1_1_0() {
        File oldFile = new File(this.plugin.getDataFolder(), "config.yml");
        if (oldFile.isFile()) {
            ConfigurationSection oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            File newDir = new File(this.plugin.getDataFolder(), "triggers");
            //noinspection ResultOfMethodCallIgnored
            newDir.mkdirs();
            for (String worldName : oldConfig.getKeys(false)) {
                ConfigurationSection worldConfig = oldConfig.getConfigurationSection(worldName);
                if (worldConfig == null) continue;
                FileConfiguration newConfig = new YamlConfiguration();
                for (String key : worldConfig.getKeys(false)) {
                    if (!worldConfig.isBoolean(key)) continue;
                    newConfig.set(key, worldConfig.getBoolean(key));
                }
                File newFile = new File(newDir, worldName + ".yml");
                try {
                    newConfig.save(newFile);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to save config file " + newFile, e);
                }
            }
            //noinspection ResultOfMethodCallIgnored
            oldFile.delete();
        }
        return "1.1.0";
    }

    @Nonnull
    private String updateTo_1_2_0() {
        File oldMessagesFile = new File(this.plugin.getDataFolder(), "messages.yml");
        File newMessagesFile = this.getLangFile("messages.yml");
        if (oldMessagesFile.isFile() && !newMessagesFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            newMessagesFile.getParentFile().mkdirs();
            try {
                Files.move(oldMessagesFile.toPath(), newMessagesFile.toPath());
                //noinspection ResultOfMethodCallIgnored
                oldMessagesFile.delete();
            } catch (Exception e) {
                throw new RuntimeException("Unable to move messages file " + oldMessagesFile + " to " + newMessagesFile, e);
            }
        }

        try {
            YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(newMessagesFile);

            boolean someUpdated = false;
            //noinspection ConstantValue
            someUpdated = updateValue(messagesConfig,
                "trigger-unsupported-state", String.class, false,
                msg -> msg.replace("%min_version%", "?")
            ) || someUpdated;
            someUpdated = updateValue(messagesConfig,
                "debug-message", String.class, false,
                msg -> msg.contains("%pos%") ? msg : msg + "%pos%"
            ) || someUpdated;

            if (someUpdated) {
                messagesConfig.save(newMessagesFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to patch messages file " + newMessagesFile, e);
        }

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        if (configFile.isFile()) {
            try {
                YamlConfiguration configData = YamlConfiguration.loadConfiguration(configFile);
                if (!configData.isBoolean("language")) {
                    configData.set("language", "auto");
                }
                configData.save(configFile);

                Map<String, String> defaultComments = FileUtils.readCommentsFromYml(
                    this.plugin.getResource("config.yml"));

                FileUtils.writeCommentsToYmlFile(
                    Files.newInputStream(configFile.toPath()),
                    (SneakyThrowsSupplier<OutputStream>) () -> Files.newOutputStream(configFile.toPath()),
                    defaultComments,
                    null
                );
            } catch (Exception e) {
                throw new RuntimeException("Unable to patch config file " + configFile, e);
            }
        }
        return "1.2.0";
    }

    @Nonnull
    private String updateTo_1_3_0() {
        File categoriesFile = this.getLangFile("categories.yml");
        if (categoriesFile.isFile()) {
            try {
                YamlConfiguration categoriesConfig = YamlConfiguration.loadConfiguration(categoriesFile);

                {
                    String oldValue1 = categoriesConfig.getString("GRAVITY_BLOCKS", "").trim();
                    categoriesConfig.set("GRAVITY_BLOCKS", null);

                    String oldValue2 = categoriesConfig.getString("LIQUIDS", "").trim();
                    categoriesConfig.set("LIQUIDS", null);

                    if (!oldValue1.isEmpty() && !oldValue2.isEmpty()) {
                        String newValue = oldValue1 + " & " + oldValue2;
                        categoriesConfig.set("GRAVITY_AND_LIQUIDS", newValue);
                    }
                }

                if (categoriesConfig.getString("PLAYERS_INTERACTIONS", null) == null) {
                    File configFile = this.getLangFile("config.yml");
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

                    String langKey = LocaleUtils.prepareLangKey(this.getClass(), null, config.getString("language"));

                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(this.plugin.getResource(
                            "lang/" + langKey + "/" + "categories.yml"))));

                    categoriesConfig.set("PLAYERS_INTERACTIONS", defaultConfig.getString("PLAYERS_INTERACTIONS"));
                }

                categoriesConfig.save(categoriesFile);
            } catch (Exception e) {
                throw new RuntimeException("Unable to patch file " + categoriesFile.getAbsolutePath(), e);
            }
        }

        this.renameTriggers(
            "BONE_MEAL_USAGE", "PLAYERS_BONE_MEAL_USAGE"
        );

        return "1.3.0";
    }

    private void renameTriggers(@Nonnull String... oldToNewNames) {
        if (oldToNewNames.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong arguments amount");
        }

        Map<String, String> oldToNewNamesMap = new HashMap<>();
        for (int i = 0; i < oldToNewNames.length; ) {
            oldToNewNamesMap.put(oldToNewNames[i++], oldToNewNames[i++]);
        }
        if (oldToNewNamesMap.isEmpty()) {
            return;
        }

        Set<String> renamedTriggers = new HashSet<>();

        {
            for (File triggersFile : getAllTriggersFiles()) {
                try {
                    YamlConfiguration triggersConfig = YamlConfiguration.loadConfiguration(triggersFile);

                    boolean someUpdated = false;
                    for (Map.Entry<String, String> entry : oldToNewNamesMap.entrySet()) {
                        String oldKey = entry.getKey();
                        String newKey = entry.getValue();
                        if (!triggersConfig.contains(oldKey)) continue;
                        Object value = triggersConfig.get(oldKey);
                        triggersConfig.set(oldKey, null);
                        triggersConfig.set(newKey, value);
                        renamedTriggers.add(oldKey);
                        someUpdated = true;
                    }

                    if (someUpdated) triggersConfig.save(triggersFile);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to patch file " + triggersFile.getAbsolutePath(), e);
                }
            }
        }

        this.renameTriggers0(this.getLangFile("triggers.yml"), oldToNewNamesMap, renamedTriggers);

        for (String oldKey : renamedTriggers) {
            this.plugin.getLogger().warning("Trigger renamed: " + oldKey + " -> " + oldToNewNamesMap.get(oldKey));
        }
    }

    @Nonnull
    private File getLangFile(@Nonnull String name) {
        return new File(new File(this.plugin.getDataFolder(), "lang"), name);
    }

    private void renameTriggers0(@Nonnull File triggersFile,
                                 @Nonnull Map<String, String> oldToNewNamesMap,
                                 @Nonnull Set<String> renamedTriggers
    ) {
        if (!triggersFile.isFile()) return;
        try {
            YamlConfiguration triggersConfig = YamlConfiguration.loadConfiguration(triggersFile);

            boolean someUpdated = false;
            for (Map.Entry<String, String> entry : oldToNewNamesMap.entrySet()) {
                String oldKey = entry.getKey();
                String newKey = entry.getValue();
                if (!triggersConfig.contains(oldKey)) continue;
                Object value = triggersConfig.get(oldKey);
                triggersConfig.set(oldKey, null);
                triggersConfig.set(newKey, value);
                renamedTriggers.add(oldKey);
                someUpdated = true;
            }

            if (someUpdated) triggersConfig.save(triggersFile);
        } catch (Exception e) {
            throw new RuntimeException("Unable to patch file " + triggersFile.getAbsolutePath(), e);
        }
    }

    @Nonnull
    private List<File> getAllTriggersFiles() {
        List<File> result = new ArrayList<>();
        File worldsDir = new File(this.plugin.getDataFolder(), "triggers");
        File[] files = worldsDir.listFiles();
        if (files == null) {
            this.plugin.getLogger().warning("Unable to read directory files list: " + worldsDir.getAbsolutePath());
        } else {
            for (File triggersFile : files) {
                if (!triggersFile.isFile()) continue;
                if (!triggersFile.getName().endsWith(".yml")) continue;
                result.add(triggersFile);
            }
        }
        return result;
    }

    private static <T> boolean updateValue(@Nonnull ConfigurationSection section,
                                           @Nonnull String path,
                                           @Nonnull Class<T> valueClass,
                                           boolean addIfNotExist,
                                           @Nonnull Function<T, T> mapper
    ) {
        Object previous = section.get(path);
        if (previous == null && !addIfNotExist) return false;
        if (!valueClass.isInstance(previous)) return false;
        T actual = mapper.apply(valueClass.cast(previous));
        if (Objects.equals(previous, actual)) return false;
        section.set(path, actual);
        return true;
    }

    private static int[] parseVersion(@Nonnull String versionName) {
        String[] args = versionName.split("\\.");
        if (args.length != 3) throw new IllegalArgumentException("Unable to parse version: " + versionName);
        int majorVersion = Integer.parseInt(args[0]);
        if (majorVersion < 1 || majorVersion > 26) {
            throw new IllegalArgumentException("Wrong major version of " + versionName + ": " + majorVersion);
        }
        int[] result = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = Integer.parseInt(args[i]);
        }
        return result;
    }

    private final Plugin plugin;

    public PluginDataUpdater(@Nonnull Plugin plugin) {
        this.plugin = plugin;
        String currentVersion = this.getCurrentVersion();
        File versionFile = this.getVersionFile();
        if (!versionFile.isFile()) {
            this.writeVersionFile();
            return;
        }
        String previousVersion;
        try {
            previousVersion = Files.lines(versionFile.toPath(), StandardCharsets.UTF_8).iterator().next();
        } catch (Exception e) {
            throw new RuntimeException("Could not read " + versionFile.getName() + " file", e);
        }
        if (currentVersion.equals(previousVersion)) return;
        String sourceVersion = previousVersion;
        while (true) {
            String nextVersion = this.updateAndReturnNewVersion(previousVersion);
            if (nextVersion == null) return; // config is newer than plugin
            if (nextVersion.equals(currentVersion)) break;
            previousVersion = nextVersion;
        }
        this.writeVersionFile();
        this.plugin.getLogger().info("Plugin updated successfully: " + sourceVersion + " -> " + currentVersion);
    }

    @Nonnull
    private String getCurrentVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Nonnull
    private File getVersionFile() {
        return new File(this.plugin.getDataFolder(), "plugin-version");
    }

    private void writeVersionFile() {
        File f = getVersionFile();
        if (f.exists() && !f.delete()) {
            throw new RuntimeException("Could not delete " + f.getName() + " file");
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
            if (f.createNewFile()) {
                Files.write(f.toPath(), Arrays.asList(
                    getCurrentVersion(),
                    "^ Do NOT change this! This is necessary for the plugin to work correctly."
                ), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create " + f.getName() + " file", e);
        }
    }
}
