package org.btuk.pcontrol;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.text.CommonColor;
import org.btuk.pcontrol.text.Text;

import javax.annotation.Nonnull;

public abstract class PhysicsListener implements Listener {
    protected final PControlData data;
    protected final VersionsAdapter versionsAdapter;

    private final PControlTrigger triggerAllowUnrecognizedActions;
    private final PControlTrigger triggerDebugMessages;

    protected PhysicsListener(@Nonnull PControlData data) {
        this.data = data;
        this.versionsAdapter = data.getVersionsAdapter();

        this.triggerAllowUnrecognizedActions = data.getTriggersRegisty().valueOf("ALLOW_UNRECOGNIZED_ACTIONS");
        this.triggerDebugMessages = data.getTriggersRegisty().valueOf("DEBUG_MESSAGES");
    }

    protected void unregisterUnavailableTriggers() {
    }

    protected void unrecognizedAction(@Nonnull Cancellable event, @Nonnull Location l, @Nonnull Object content) {
        if (l.getWorld() == null) throw new IllegalArgumentException("World cannot be null");
        if (!this.data.isActionAllowed(l.getWorld(), this.triggerAllowUnrecognizedActions)) {
            event.setCancelled(true);
        }
        if (this.data.isActionAllowed(l.getWorld(), this.triggerDebugMessages)) {
            this.debugAction((Event) event, l, content);
        }
    }

    protected void debugAction(@Nonnull Event event, @Nonnull Location l, @Nonnull Object content) {
        if (l.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        String pos = l.getWorld().getName() + " " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();

        Text text = this.data.getMessage("debug-message",
            "%action%", event.getClass().getSimpleName().replace("Event", ""),
            "%content%", content.toString(),
            "%pos%", pos
        );

        String command = "/pc tp " + pos;

        Text hover = this.data.getTextHelper().create("✔", CommonColor.YELLOW);

        this.data.announce(l.getWorld(), text.setClickCommand(command).setHoverText(hover));
    }

    @SuppressWarnings("unused")
    @Nonnull
    protected String genOffsetMsg(@Nonnull Block first, @Nonnull Block second) {
        return genOffsetMsg(
            first.getX() - second.getX(),
            first.getY() - second.getY(),
            first.getZ() - second.getZ());
    }

    @Nonnull
    private String genOffsetMsg(int oX, int oY, int oZ) {
        return "offset=" + (oX < 0 ? oX : " " + oX) + " " + (oY < 0 ? oY : " " + oY) + " " + (oZ < 0 ? oZ : " " + oZ) + ";";
    }
}
