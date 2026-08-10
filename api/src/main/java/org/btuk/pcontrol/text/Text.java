package org.btuk.pcontrol.text;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

public interface Text {
    void send(@Nonnull CommandSender receiver);

    @Nonnull
    List<Text> split(@Nonnull String regex);

    @Nonnull
    Text setClickCommand(@Nonnull String command);

    @Nonnull
    Text setHoverText(@Nonnull Text text);
}
