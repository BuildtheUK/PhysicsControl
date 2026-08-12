package org.btuk.pcontrol;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import org.btuk.pcontrol.data.trigger.EventsListenerParser;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.inventory.PControlCategoryInventory;
import org.btuk.pcontrol.inventory.PControlInventory;
import org.btuk.pcontrol.listener.block.*;
import org.btuk.pcontrol.listener.custom.BoneMealUsageListener;
import org.btuk.pcontrol.listener.entity.EntityChangeBlockEventListener;
import org.btuk.pcontrol.listener.entity.EntityInteractEventListener;
import org.btuk.pcontrol.listener.entity.ProjectileHitEventListener;
import org.btuk.pcontrol.listener.entity.ProjectileLaunchEventListener;
import org.btuk.pcontrol.listener.player.PlayerInteractEventListener;
import org.btuk.pcontrol.listener.world.StructureGrowEventListener;
import org.btuk.pcontrol.rules.TriggerRules;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.StringJoiner;

public final class PhysicsControl extends JavaPlugin implements Listener, BasicCommand {
    private PControlDataBukkit data;

    @Override
    public void onEnable() {
        this.data = new PControlDataBukkit(this);

        this.data.getTriggersRegisty().getIgnoredState().markAvailable();
        for (PControlTrigger trigger : this.data.getCategoriesRegistry().getSettingsCategory().getTriggers()) {
            trigger.markAvailable();
        }

        if (TriggerRules.LOG_TRIGGERS_REGISTRATIONS) {
            this.getLogger().info("Total rules registered: " + TriggerRules.getTotalRulesRegistered());
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        EventsListenerParser parser = new EventsListenerParser(this.data);
        this.registerListeners(parser);
        parser.parseAllEvents();

        LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("physicscontrol", "Basic PhysicsControl plugin command", List.of("pcontrol", "physicsc", "pc"), this);
        });

        this.data.reloadConfigs();
    }

    private void registerListeners(@Nonnull EventsListenerParser parser) {
        this.reg(new BlockBurnEventListener(this.data, parser));
        this.reg(new BlockFadeEventListener(this.data, parser));
        this.reg(new BlockFromToEventListener(this.data, parser));
        this.reg(new BlockGrowEventListener(this.data, parser));
        this.reg(new BlockIgniteEventListener(this.data, parser));
        this.reg(new BlockPhysicsEventListener(this.data, parser));
        this.reg(new BlockSpreadEventListener(this.data, parser));
        this.reg(new EntityBlockFormEventListener(this.data, parser));
        this.reg(new LeavesDecayEventListener(this.data, parser));
        this.reg(new MoistureChangeEventListener(this.data, parser));
        this.reg(new BoneMealUsageListener(this.data, parser));
        this.reg(new EntityChangeBlockEventListener(this.data, parser));
        this.reg(new EntityInteractEventListener(this.data, parser));
        this.reg(new ProjectileHitEventListener(this.data, parser));
        this.reg(new PlayerInteractEventListener(this.data, parser));
        this.reg(new StructureGrowEventListener(this.data, parser));
        this.reg(new ProjectileLaunchEventListener(this.data, parser));
    }

    private void reg(@Nonnull PhysicsListener listener) {
        listener.unregisterUnavailableTriggers();
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        if (this.data == null) return; // Plugin was not loaded
        HandlerList.unregisterAll((Plugin) this);
        this.data.unloadData();
        this.data = null;
    }

    private void openGui(@Nonnull CommandSender sender) {
        if (!(sender instanceof Player)) {
            this.data.getMessage("only-players-menu").send(sender);
            return;
        }
        if (!sender.isOp() && !sender.hasPermission("physicscontrol.open-menu")) {
            this.data.getMessage("bad-perms-inventory").send(sender);
            return;
        }
        ((Player) sender).openInventory(new PControlCategoryInventory(this.data, ((Player) sender).getWorld()).getInventory());
    }

    private void reload(@Nonnull CommandSender sender) {
        if (!sender.isOp() && !sender.hasPermission("physicscontrol.reload")) {
            this.data.getMessage("bad-perms-reload").send(sender);
            return;
        }
        this.data.reloadConfigs();
        this.data.getMessage("config-reloaded").send(sender);
    }

    private void teleport(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!sender.isOp() && !sender.hasPermission("minecraft.command.tp")) {
            this.data.getMessage("bad-perms-reload").send(sender);
            return;
        }
        if (!(sender instanceof Player)) {
            this.data.getMessage("only-players-menu").send(sender);
            return;
        }
        if (args.length != 5) return;

        World world = this.data.server().getWorld(args[1]);
        if (world == null) return;

        int x, y, z;
        try {
            x = Integer.parseInt(args[2]);
            y = Integer.parseInt(args[3]);
            z = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            return;
        }

        ((Player) sender).teleport(new Location(world, x, y, z));
    }

    private void switchTrigger(@Nonnull CommandSender sender, @Nonnull String[] args) {
        World world;
        if (sender instanceof Player && args.length < 2) {
            world = ((Player) sender).getWorld();
        } else {
            if (args.length < 2) {
                this.data.getMessage("world-or-key-not-specified").send(sender);
                return;
            }
            world = this.data.server().getWorld(args[0]);
            if (world == null) {
                this.data.getMessage("world-not-found", "%world%", args[0]).send(sender);
                return;
            }
        }
        String key = join("_", 1, args).toUpperCase();
        try {
            PControlTrigger trigger = this.data.getTriggersRegisty().valueOf(key, false);
            if (trigger == this.data.getTriggersRegisty().getIgnoredState()) throw new IllegalArgumentException();
            this.data.getInventory(trigger.getCategory(), world).switchTrigger(sender, trigger);
        } catch (IllegalArgumentException e) {
            this.data.getMessage("key-not-found", "%key%", key).send(sender);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String join(@Nonnull CharSequence delimiter, int firstElementIndex, @Nonnull String[] elements) {
        StringJoiner joiner = new StringJoiner(delimiter);
        while (firstElementIndex < elements.length) {
            joiner.add(elements[firstElementIndex++]);
        }
        return joiner.toString();
    }

    @EventHandler(ignoreCancelled = true)
    private void on(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof PControlInventory) {
            ((PControlInventory) event.getClickedInventory().getHolder()).handle(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void on(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PControlInventory) {
            ((PControlInventory) event.getInventory().getHolder()).handle(event);
        }
    }

    @EventHandler
    private void on(PluginDisableEvent event) {
        if (event.getPlugin() != this) return;
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof PControlInventory) {
                player.closeInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void on(WorldLoadEvent event) {
        this.data.updateWorldData(event.getWorld(), true);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(WorldUnloadEvent event) {
        this.data.unloadWorldData(event.getWorld());
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (args.length == 0) {
            this.openGui(sender);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> this.reload(sender);
            case "tp" -> this.teleport(sender, args);
            default -> this.switchTrigger(sender, args);
        }
        return;
    }
}
