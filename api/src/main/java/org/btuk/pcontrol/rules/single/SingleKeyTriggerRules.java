package org.btuk.pcontrol.rules.single;

import org.bukkit.event.Event;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.TriggerRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SingleKeyTriggerRules<T, K1> extends TriggerRules<T> {

    private final Map<K1, PControlTrigger> singleRules = new HashMap<>();

    protected SingleKeyTriggerRules(@Nonnull PControlData data,
                                    @Nonnull Class<? extends Event> eventClass,
                                    @Nonnull String configKey
    ) {
        super(data, eventClass, configKey);
    }

    @Nonnull
    protected T regSingle(@Nonnull PControlTrigger trigger,
                          @Nonnull Iterable<K1> keysSet
    ) {
        int rulesAdded = 0;
        PControlTrigger previousTrigger;
        for (K1 key : keysSet) {
            previousTrigger = this.singleRules.put(key, trigger);
            if (previousTrigger != null) {
                this.data.log().warning("Changed trigger for key "
                    + key + ": "
                    + previousTrigger + " -> " + trigger);
            }
            rulesAdded++;
        }
        return this.completeRegistration(trigger, rulesAdded);
    }

    @Nullable
    public PControlTrigger findTrigger(@Nonnull K1 key) {
        return this.singleRules.get(key);
    }

    @Override
    public void unregisterAll() {
        this.singleRules.clear();
    }
}
