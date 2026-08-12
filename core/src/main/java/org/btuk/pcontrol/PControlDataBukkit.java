package org.btuk.pcontrol;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.btuk.pcontrol.data.CustomTags;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.category.CategoriesRegistry;
import org.btuk.pcontrol.data.category.PControlCategory;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.data.trigger.TriggersRegistry;
import org.btuk.pcontrol.inventory.PControlInventory;
import org.btuk.pcontrol.inventory.PControlTriggerInventory;
import org.btuk.pcontrol.set.parser.TypesSetsParser;
import org.btuk.pcontrol.text.CommonColor;
import org.btuk.pcontrol.text.NullText;
import org.btuk.pcontrol.text.Text;
import org.btuk.pcontrol.text.TextHelper;
import org.btuk.pcontrol.text.adventure.AdventureTextHelper;
import org.btuk.pcontrol.util.EntityTypeUtils;
import org.btuk.pcontrol.util.FileUtils;
import org.btuk.pcontrol.util.LocaleUtils;
import org.btuk.pcontrol.util.MinecraftVersion;
import org.btuk.pcontrol.util.update.data.PluginDataUpdater;
import org.btuk.pcontrol.versionsadapter.VersionsAdapter_Modern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public final class PControlDataBukkit implements PControlData {
    private final JavaPlugin plugin;
    private final MinecraftVersion serverVersion;
    private final Set<EntityType> removableProjectileTypes;

    private final Map<String, String> messages = new HashMap<>();
    private final Map<PControlCategory, String> categoriesNames = new HashMap<>();
    private final Map<PControlTrigger, String> triggersNames = new HashMap<>();

    private final Map<World, Map<PControlTrigger, Boolean>> states = new HashMap<>();
    private final Map<World, Map<PControlCategory, PControlTriggerInventory>> inventories = new HashMap<>();
    private String langKey = null;
    private final TypesSetsParser typesSetsParser;
    private final CustomTags customTags;
    private final CategoriesRegistry categories;
    private final TriggersRegistry triggers;
    private final VersionsAdapter versionsAdapter;
    private final TextHelper textHelper;

    PControlDataBukkit(@Nonnull JavaPlugin plugin) {
        this.plugin = plugin;

        try {
            new PluginDataUpdater(plugin);
        } catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to update config from previous plugin version", t);
        }

        this.serverVersion = new MinecraftVersion(plugin);
        this.validateServerVersions();

        this.removableProjectileTypes = EntityTypeUtils.matchEntityTypes(null,
            "ARROW",
            "SPECTRAL_ARROW",
            "TIPPED_ARROW",
            "TRIDENT"
        );

        this.customTags = new CustomTags(this);
        this.typesSetsParser = new TypesSetsParser(this);
        this.customTags.parseTags();
        this.categories = new CategoriesRegistry(this);
        this.triggers = new TriggersRegistry(this);
        this.versionsAdapter = this.createVersionsAdapter();
        this.textHelper = new AdventureTextHelper();
    }

    @Nonnull
    private VersionsAdapter createVersionsAdapter() {
        return new VersionsAdapter_Modern(this);
    }

    private void validateServerVersions() {
        if (!serverVersion.isAtLeast(26, 1, 2)) {
            throw new RuntimeException("Unsupported server version (" + serverVersion + "). " +
                "Only Minecraft 26.1.2 and newer are supported.");
        }
    }

    @Override
    @Nonnull
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    @Nonnull
    public Set<EntityType> getRemovableProjectileTypes() {
        return this.removableProjectileTypes;
    }

    public void reloadConfigs() {
        this.unloadData();

        File configFile = FileUtils.createConfigFileIfNotExist(this.plugin,
            "config.yml", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.initLangKey(config.getString("language"));

        this.reloadLocale();
        this.reloadTriggers();
    }

    private void initLangKey(@Nullable String rawLangKey) {
        this.langKey = LocaleUtils.prepareLangKey(this.getClass(), this.plugin.getLogger(), rawLangKey);
    }

    private void reloadLocale() {

        Function<String, String> messageProcessor = msg ->
            msg.replace("%plugin%", this.plugin.getName());

        LocaleUtils.reloadLocale(this.plugin, this.langKey,
            this.categories::valueOf, messageProcessor,
            "categories.yml", this.categoriesNames);

        LocaleUtils.reloadLocale(this.plugin, this.langKey,
            key -> key, messageProcessor,
            "messages.yml", this.messages);

        LocaleUtils.reloadLocale(this.plugin, this.langKey,
            key -> this.triggers.valueOf(key, false), messageProcessor,
            "triggers.yml", this.triggersNames);

        for (PControlCategory category : this.categories.values()) {
            if (this.categoriesNames.containsKey(category)) continue;
            this.categoriesNames.put(category, category.name());
            this.plugin.getLogger().warning("Unable to load name of category " + category);
        }
        for (PControlTrigger trigger : this.triggers.values()) {
            if (this.triggersNames.containsKey(trigger)) continue;
            this.triggersNames.put(trigger, trigger.name());
            this.plugin.getLogger().warning("Unable to load name of trigger " + trigger);
        }
    }

    private void reloadTriggers() {
        for (World world : this.plugin.getServer().getWorlds()) {
            this.updateWorldData(world, true);
        }
    }

    void unloadData() {
        this.messages.clear();
        this.states.clear();
        this.plugin.getServer().getOnlinePlayers().forEach(player -> {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (!(holder instanceof PControlInventory)) return;
            player.closeInventory();
        });
        this.inventories.clear();
    }

    @Override
    @Nonnull
    public Text getMessage(@Nonnull String key, @Nonnull String... placeholders) {
        String result = this.messages.get(key);
        if (result == null) {
            if (this.messages.containsKey(key)) return NullText.INSTANCE;
            return this.textHelper.create(key + " " + Arrays.toString(placeholders), CommonColor.RED);
        }
        for (int i = 0; i < placeholders.length; i++) {
            result = result.replace(placeholders[i], placeholders[++i]);
        }
        return this.textHelper.fromAmpersandFormat(result);
    }

    @Override
    @Nonnull
    public String getTriggerName(@Nonnull PControlTrigger trigger) {
        return this.triggersNames.get(trigger);
    }

    @Override
    @Nonnull
    public String getCategoryName(@Nonnull PControlCategory category) {
        return this.categoriesNames.get(category);
    }

    @Nonnull
    public PControlTriggerInventory getInventory(@Nonnull PControlCategory category, @Nonnull World world) {
        Map<PControlCategory, PControlTriggerInventory> worldInventories = this.inventories.computeIfAbsent(world, k -> new HashMap<>());
        return worldInventories.computeIfAbsent(category, k -> {
            category.prepareIcon(this);
            return new PControlTriggerInventory(this, category, world);
        });
    }

    void updateWorldData(@Nonnull World world, boolean configPriority) {
        File file = FileUtils.createConfigFileIfNotExist(this.plugin,
            "triggers" + File.separator + world.getName() + ".yml",
            null
        );
        YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(file);

        Map<PControlTrigger, Boolean> configTriggers = new HashMap<>();
        for (String key : worldConfig.getKeys(false)) {
            try {
                PControlTrigger trigger;
                try {
                    trigger = this.triggers.valueOf(key.toUpperCase().replace(" ", "_"), false);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown trigger type");
                }
                if (!worldConfig.isBoolean(key)) {
                    throw new IllegalArgumentException("Is not a boolean value, "
                        + "but \"" + worldConfig.get(key).getClass().getSimpleName() + "\"");
                }
                boolean value = worldConfig.getBoolean(key);
                configTriggers.put(trigger, value);
            } catch (Exception ex) {
                if (configPriority) {
                    this.plugin.getLogger().warning("Unable to load trigger \"" + key + "\" "
                        + "of world \"" + world.getName() + "\": " + ex.getMessage());
                }
            }
        }

        Map<PControlTrigger, Boolean> memoryTriggers = this.states.computeIfAbsent(world, k -> new HashMap<>());
        Map<PControlCategory, PControlTriggerInventory> worldInventories = this.inventories.computeIfAbsent(world, k -> new HashMap<>());

        boolean firstInit = configTriggers.isEmpty();
        boolean changed = false;
        for (PControlTrigger trigger : this.triggers.values()) {
            Boolean memoryValue = memoryTriggers.get(trigger);
            Boolean configValue = configTriggers.get(trigger);
            boolean currentValue;
            if (configPriority) {
                currentValue = configValue == null ? trigger.getDefaultValue() : configValue;
            } else {
                currentValue = memoryValue == null ? trigger.getDefaultValue() : memoryValue;
            }
            if ((configValue == null || configValue != currentValue)) {
                worldConfig.set(trigger.name(), currentValue);
                changed = true;
                if (!firstInit) {
                    this.plugin.getLogger().info("Added trigger \"" + this.getTriggerName(trigger) + "\" "
                        + "(" + currentValue + ") for world \"" + world.getName() + "\"");
                }
            }
            if (memoryValue == null || memoryValue != currentValue) {
                memoryTriggers.put(trigger, currentValue);
                PControlTriggerInventory inventory = worldInventories.get(trigger.getCategory());
                if (inventory != null) {
                    inventory.updateTriggerStack(trigger);
                }
            }
        }
        if (!changed) return;
        try {
            worldConfig.save(file);
        } catch (Exception e) {
            this.plugin.getLogger().severe("Unable to save config file " + file);
        }
    }

    void unloadWorldData(@Nonnull World world) {
        this.states.remove(world);
        this.inventories.remove(world);
    }

    @Override
    public boolean hasVersion(int majorVersion, int minorVersion, int patchVersion) {
        return this.serverVersion.isAtLeast(majorVersion, minorVersion, patchVersion);
    }

    @Override
    public boolean isVersion(int majorVersion, int minorVersion, int patchVersion) {
        return this.serverVersion.isVersion(majorVersion, minorVersion, patchVersion);
    }

    @Override
    public void cancelIfDisabled(@Nonnull Cancellable event, @Nonnull World world, @Nonnull PControlTrigger trigger) {
        if (!this.isActionAllowed(world, trigger)) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean isActionAllowed(@Nonnull World world, @Nonnull PControlTrigger trigger) {
        return this.getWorldTriggers(world).getOrDefault(trigger, false);
    }

    public void switchTrigger(@Nonnull World world, @Nonnull PControlTrigger trigger) {
        Map<PControlTrigger, Boolean> worldTriggers = this.getWorldTriggers(world);
        worldTriggers.put(trigger, !worldTriggers.get(trigger));
        this.updateWorldData(world, false);
    }

    @Nonnull
    private Map<PControlTrigger, Boolean> getWorldTriggers(@Nonnull World world) {
        Map<PControlTrigger, Boolean> worldTriggers = this.states.get(world);
        if (worldTriggers == null) {
            this.updateWorldData(world, true);
            worldTriggers = this.states.get(world);
        }
        return worldTriggers;
    }

    @Override
    public void announce(@Nullable World world, @Nonnull Text text) {
        text.send(this.plugin.getServer().getConsoleSender());
        this.plugin.getServer().getOnlinePlayers().stream()
            .filter(player -> player.isOp() || player.hasPermission("physicscontrol.announce"))
            .filter(player -> world == null || player.getWorld() == world)
            .forEach(text::send);
    }

    @Nonnull
    @Override
    public TypesSetsParser getTypesSetsParser() {
        return Objects.requireNonNull(this.typesSetsParser, "Type sets parser not initialized yet");
    }

    @Nonnull
    @Override
    public CustomTags getCustomTags() {
        return Objects.requireNonNull(this.customTags, "Custom tags not initialized yet");
    }

    @Nonnull
    @Override
    public CategoriesRegistry getCategoriesRegistry() {
        return Objects.requireNonNull(this.categories, "Categories registry not initialized yet");
    }

    @Nonnull
    @Override
    public TriggersRegistry getTriggersRegisty() {
        return Objects.requireNonNull(this.triggers, "Triggers registry not initialized yet");
    }

    @Nonnull
    @Override
    public VersionsAdapter getVersionsAdapter() {
        return Objects.requireNonNull(this.versionsAdapter, "Versions adapter not initialized yet");
    }

    @Nonnull
    @Override
    public TextHelper getTextHelper() {
        return Objects.requireNonNull(this.textHelper, "Text helper adapter not initialized yet");
    }
}
