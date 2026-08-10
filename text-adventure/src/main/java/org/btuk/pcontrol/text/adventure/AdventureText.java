package org.btuk.pcontrol.text.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.intellij.lang.annotations.RegExp;
import org.btuk.pcontrol.text.Text;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AdventureText implements Text {

    private final Component component;

    AdventureText(@Nonnull Component component) {
        this.component = fixItalic(component);
    }

    @Nonnull
    public Component component() {
        return this.component;
    }

    @Override
    public void send(@Nonnull CommandSender receiver) {
        receiver.sendMessage(this.component);
    }

    @Nonnull
    @Override
    public List<Text> split(@Nonnull @RegExp String regex) {
        List<Text> result = new ArrayList<>();
        for (Component element : ComponentSplit.split(this.component, regex)) {
            result.add(new AdventureText(element));
        }
        return result;
    }

    @Nonnull
    @Override
    public AdventureText setClickCommand(@Nonnull String command) {
        return new AdventureText(this.component.clickEvent(ClickEvent.runCommand(command)));
    }

    @Nonnull
    @Override
    public AdventureText setHoverText(@Nonnull Text text) {
        return new AdventureText(this.component.hoverEvent(HoverEvent.showText(((AdventureText) text).component)));
    }

    @Nonnull
    private static Component fixItalic(@Nonnull Component component) {
        if (component.hasDecoration(TextDecoration.ITALIC)) return component;
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
