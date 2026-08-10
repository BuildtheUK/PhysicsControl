package org.btuk.pcontrol.util;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public class MinecraftVersion implements Comparable<MinecraftVersion> {
    private final short serverMajorVersion;
    private final short serverMinorVersion;
    private final short serverPatchVersion;

    public MinecraftVersion(int major, int minor, int patch) {
        this.serverMajorVersion = (short) major;
        this.serverMinorVersion = (short) minor;
        this.serverPatchVersion = (short) patch;
    }

    public MinecraftVersion(@Nonnull Plugin plugin) {
        try {
            String[] sections = this.getMinecraftVersion(plugin.getServer()).split("\\.");

            if (sections.length < 2 || sections.length > 3) {
                throw new IllegalArgumentException("Wrong sections amount: " + Arrays.toString(sections));
            }

            this.serverMajorVersion = this.parseVersionSection(sections[0], "major");
            this.serverMinorVersion = this.parseVersionSection(sections[1], "minor");
            this.serverPatchVersion = sections.length == 2 ? 0 : this.parseVersionSection(sections[2], "patch");

            if (this.serverMajorVersion < 26 || (this.serverMajorVersion == 26 && this.serverMinorVersion == 1 && this.serverPatchVersion < 2) || (this.serverMajorVersion == 26 && this.serverMinorVersion < 1)) {
                throw new IllegalArgumentException("Unsupported version: " + this.serverMajorVersion + "." + this.serverMinorVersion + "." + this.serverPatchVersion + ". Only 26.1.2+ is supported.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unsupported server version", e);
        }
    }

    @Nonnull
    private String getMinecraftVersion(@Nonnull Server server) {
        return server.getMinecraftVersion();
    }

    @Nonnull
    private Short parseVersionSection(@Nonnull String in, @Nonnull String sectionName) {
        short result;
        try {
            result = Short.parseShort(in);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to parse server " + sectionName + " section: " + in);
        }
        if (result < 0) {
            throw new IllegalArgumentException("Negative value: " + in);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.serverMajorVersion + "." + this.serverMinorVersion + "." + this.serverPatchVersion;
    }

    @Override
    public int compareTo(@Nonnull MinecraftVersion o) {
        if (this.serverMajorVersion != o.serverMajorVersion) {
            return Short.compare(this.serverMajorVersion, o.serverMajorVersion);
        }
        if (this.serverMinorVersion != o.serverMinorVersion) {
            return Short.compare(this.serverMinorVersion, o.serverMinorVersion);
        }
        return Short.compare(this.serverPatchVersion, o.serverPatchVersion);
    }

    public boolean isAtLeast(int major, int minor, int patch) {
        return this.compareTo(new MinecraftVersion(major, minor, patch)) >= 0;
    }

    public boolean isVersion(int majorVersion, int minorVersion, int patchVersion) {
        return this.serverMajorVersion == majorVersion
            && this.serverMinorVersion == minorVersion
            && this.serverPatchVersion == patchVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return serverMajorVersion == that.serverMajorVersion &&
            serverMinorVersion == that.serverMinorVersion &&
            serverPatchVersion == that.serverPatchVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMajorVersion, serverMinorVersion, serverPatchVersion);
    }
}
