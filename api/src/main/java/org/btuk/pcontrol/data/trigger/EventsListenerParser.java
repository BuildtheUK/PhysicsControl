package org.btuk.pcontrol.data.trigger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.btuk.pcontrol.data.PControlData;
import org.btuk.pcontrol.rules.TriggerRules;
import org.btuk.pcontrol.util.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class EventsListenerParser {
    private final PControlData data;
    private final Map<Class<? extends Event>, List<TriggerRules<?>>> registeredRules = new HashMap<>();

    public EventsListenerParser(@Nonnull PControlData data) {
        this.data = data;
    }

    public void registerParser(@Nonnull TriggerRules<?> trigger) {
        List<String> parameterNames = trigger.getParameterNames();
        if (parameterNames.isEmpty()) throw new IllegalArgumentException("No parameter names provided");

        Class<? extends Event> eventClass = trigger.getEventClass();
        List<TriggerRules<?>> listeners = this.registeredRules
            .computeIfAbsent(eventClass, aClass -> new ArrayList<>());
        if (this.getTrigger(listeners, parameterNames) != null) {
            throw new IllegalArgumentException(
                "Duplicate parameters for event " + eventClass.getName() + ": " + parameterNames);
        }
        listeners.add(trigger);
    }

    public void parseAllEvents() {
        File configFile = FileUtils.createConfigFileIfNotExist(this.data.getPlugin(),
            "logics/events.yml", "logics/events.yml");
        YamlConfiguration rootSection = YamlConfiguration.loadConfiguration(configFile);

        for (String eventName : rootSection.getKeys(false)) {
            ConfigurationSection eventSection = rootSection.getConfigurationSection(eventName);
            if (eventSection == null) {
                throw new IllegalArgumentException("Not a section: " + eventName);
            }
            String eventClassName = eventSection.getName().replace("/", ".");
            Class<? extends Event> eventClass;
            try {
                //noinspection unchecked
                eventClass = (Class<? extends Event>) Class.forName(eventClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unknown event class: " + eventClassName);
            }
            for (String ruleKey : eventSection.getKeys(false)) {
                ConfigurationSection ruleConfig = eventSection.getConfigurationSection(ruleKey);
                if (ruleConfig == null) {
                    throw new IllegalArgumentException("Not a section: " + ruleKey);
                }

                String[] minVersion = ruleConfig.getString("min-version", "1.0.0").split("\\.");
                int majorVersion = Integer.parseInt(minVersion[0]);
                int minorVersion = Integer.parseInt(minVersion[1]);
                int patchVersion = minVersion.length < 3 ? 0 : Integer.parseInt(minVersion[2]);
                if (!this.data.hasVersion(majorVersion, minorVersion, patchVersion)) continue;

                this.parse(eventClass, ruleConfig);
            }
        }
    }

    private void parse(@Nonnull Class<? extends Event> eventClass, @Nonnull ConfigurationSection section) {
        List<TriggerRules<?>> listeners = this.registeredRules.get(eventClass);
        if (listeners == null) {
            throw new IllegalArgumentException("Unable to find listeners of event " + eventClass.getName());
        }

        Set<String> keys = section.getKeys(false);
        keys.remove("trigger");
        keys.remove("min-version");
        TriggerRules<?> trigger = this.getTrigger(listeners, keys);
        if (trigger == null) {
            throw new IllegalArgumentException("Unable to find rules of " + eventClass.getName() + " for parameters " + keys);
        }

        trigger.parse(section);
    }

    @Nullable
    private TriggerRules<?> getTrigger(@Nonnull List<TriggerRules<?>> listeners, @Nonnull Collection<String> parameters) {
        listener:
        for (TriggerRules<?> trigger : listeners) {
            List<String> parameterNames = trigger.getParameterNames();
            if (parameterNames.size() != parameters.size()) continue;
            for (String parameter : parameterNames) {
                if (!parameters.contains(parameter)) continue listener;
            }
            return trigger;
        }
        return null;
    }
}
