package org.btuk.pcontrol.rules.pair;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;

import javax.annotation.Nonnull;

public class EntityMaterialRules extends KeysPairTriggerRules<EntityMaterialRules, EntityType, Material> {
    private final @Nonnull String configKey1;
    private final @Nonnull String configKey2;

    public EntityMaterialRules(@Nonnull PControlData data,
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
            this.parseEntityTypes(trigger, section, this.configKey1),
            this.parseBlockTypes(trigger, section, this.configKey2, true)
        );
    }
}
