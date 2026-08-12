package org.btuk.pcontrol.util.update.data;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class PluginDataUpdater {
    @SuppressWarnings("unused")
    @Nullable
    private String updateAndReturnNewVersion(@Nonnull String previousVersion) {
        if (previousVersion.toLowerCase().contains("beta")) return "1.0.0";

        String currentVersion = this.getCurrentVersion();
        int[] argsPrevious = parseVersion(previousVersion);
        int[] argsCurrent = parseVersion(currentVersion);
        for (int i = 0; i < argsPrevious.length; i++) {
            if (argsPrevious[i] < argsCurrent[i]) break;
            if (argsPrevious[i] > argsCurrent[i]) return null;
        }

        int majorVersion = argsPrevious[0];
        int minorVersion = argsPrevious[1];
        int patchVersion = argsPrevious[2];

        return currentVersion;
    }

    private static int[] parseVersion(@Nonnull String versionName) {
        String[] args = versionName.split("\\.");
        if (args.length != 3) throw new IllegalArgumentException("Unable to parse version: " + versionName);
        int majorVersion = Integer.parseInt(args[0]);
        if (majorVersion < 1 || majorVersion > 26) {
            throw new IllegalArgumentException("Wrong major version of " + versionName + ": " + majorVersion);
        }
        int[] result = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = Integer.parseInt(args[i]);
        }
        return result;
    }

    private final Plugin plugin;

    public PluginDataUpdater(@Nonnull Plugin plugin) {
        this.plugin = plugin;
        String currentVersion = this.getCurrentVersion();
        File versionFile = this.getVersionFile();
        if (!versionFile.isFile()) {
            this.writeVersionFile();
            return;
        }
        String previousVersion;
        try (java.util.stream.Stream<String> lines = Files.lines(versionFile.toPath(), StandardCharsets.UTF_8)) {
            previousVersion = lines.findFirst().orElseThrow(() -> new RuntimeException("Version file is empty"));
        } catch (Exception e) {
            throw new RuntimeException("Could not read " + versionFile.getName() + " file", e);
        }
        if (currentVersion.equals(previousVersion)) return;
        String sourceVersion = previousVersion;
        while (true) {
            String nextVersion = this.updateAndReturnNewVersion(previousVersion);
            if (nextVersion == null) return; // config is newer than plugin
            if (nextVersion.equals(currentVersion)) break;
            previousVersion = nextVersion;
        }
        this.writeVersionFile();
        this.plugin.getLogger().info("Plugin updated successfully: " + sourceVersion + " -> " + currentVersion);
    }

    @Nonnull
    private String getCurrentVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Nonnull
    private File getVersionFile() {
        return new File(this.plugin.getDataFolder(), "plugin-version");
    }

    private void writeVersionFile() {
        File f = getVersionFile();
        if (f.exists() && !f.delete()) {
            throw new RuntimeException("Could not delete " + f.getName() + " file");
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
            if (f.createNewFile()) {
                Files.write(f.toPath(), Arrays.asList(
                    getCurrentVersion(),
                    "^ Do NOT change this! This is necessary for the plugin to work correctly."
                ), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create " + f.getName() + " file", e);
        }
    }
}
