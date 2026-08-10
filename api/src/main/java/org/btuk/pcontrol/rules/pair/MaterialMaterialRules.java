package org.btuk.pcontrol.rules.pair;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class MaterialMaterialRules extends KeysPairTriggerRules<MaterialMaterialRules, Material, Material> {
    private final @Nonnull String configKey1;
    private final @Nonnull String configKey2;

    public MaterialMaterialRules(@Nonnull PControlData data,
                                 @Nonnull Class<? extends Event> eventClass,
                                 @Nonnull String configKey1,
                                 @Nonnull String configKey2
    ) {
        super(data, eventClass, configKey1, configKey2);
        this.configKey1 = configKey1;
        this.configKey2 = configKey2;
    }

    @Override
    public void parse(@Nonnull ConfigurationSection section) {
        PControlTrigger trigger = this.parseTrigger(section);
        this.regPair(
            trigger,
            this.parseBlockTypes(trigger, section, this.configKey1, true),
            this.parseBlockTypes(trigger, section, this.configKey2, true)
        );
    }
}
