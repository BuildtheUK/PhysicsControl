package org.btuk.pcontrol.rules.pair;

import org.bukkit.event.Event;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.data.trigger.PControlTrigger;
import org.btuk.pcontrol.rules.TriggerRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class KeysPairTriggerRules<T, K1, K2> extends TriggerRules<T> {

    private final Map<K1, Map<K2, PControlTrigger>> pairRules = new HashMap<>();

    protected KeysPairTriggerRules(@Nonnull PControlData data,
                                   @Nonnull Class<? extends Event> eventClass,
                                   @Nonnull String configKey1,
                                   @Nonnull String configKey2
    ) {
        super(data, eventClass, configKey1, configKey2);
    }

    @Nonnull
    protected T regPair(@Nonnull PControlTrigger trigger,
                        @Nonnull Iterable<K1> firstKeysSet,
                        @Nonnull Iterable<K2> secondKeysSet
    ) {
        int rulesAdded = 0;
        PControlTrigger previousTrigger;
        for (K1 firstKey : firstKeysSet) {
            Map<K2, PControlTrigger> map = this.pairRules.computeIfAbsent(firstKey, material1 -> new HashMap<>());
            for (K2 secondKey : secondKeysSet) {
                previousTrigger = map.put(secondKey, trigger);
                if (previousTrigger != null) {
                    this.data.log().warning("Changed trigger for keys pair "
                        + firstKey + " and " + secondKey + ": "
                        + previousTrigger + " -> " + trigger);
                }
                rulesAdded++;
            }
        }
        return this.completeRegistration(trigger, rulesAdded);
    }

    @Nullable
    public PControlTrigger findTrigger(@Nonnull K1 firstKey, @Nonnull K2 secondKey) {
        Map<K2, PControlTrigger> map = this.pairRules.get(firstKey);
        return map == null ? null : map.get(secondKey);
    }

    @Override
    public void unregisterAll() {
        this.pairRules.clear();
    }
}
