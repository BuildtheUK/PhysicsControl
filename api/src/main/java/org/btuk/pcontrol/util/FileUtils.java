package org.btuk.pcontrol.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings({"unused", "SameParameterValue"})
public class FileUtils {
    /**
     * <a href="https://www.uofr.net/~greg/java/get-resource-listing.html">Source</a>
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @author Greg Briggs
     */
    public static String[] getResourceFolderFiles(@Nonnull Class<?> clazz, @Nonnull String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);

            if (dirURL == null) {
                throw new IllegalArgumentException("Specified resource not found");
            }
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    private static void removeDirToRootIfEmpty(@Nonnull File dir) {
        if (!dir.isDirectory()) return;
        final File[] files = dir.listFiles();
        if (files == null) return;
        if (files.length > 0) return;
        if (!dir.delete()) return;
        removeDirToRootIfEmpty(dir.getParentFile());
    }

    @Nonnull
    public static YamlConfiguration loadYamlConfig(@Nonnull Plugin plugin, @Nullable String resourcePath) {
        if (resourcePath != null) {
            InputStream stream = plugin.getResource(resourcePath);
            if (stream != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }
        return new YamlConfiguration();
    }

    @Nonnull
    public static File createConfigFileIfNotExist(@Nonnull Plugin plugin, @Nonnull String name, @Nullable String resourceFile) {
        File configFile = new File(plugin.getDataFolder(), name);
        if (configFile.isFile()) return configFile;
        if (resourceFile != null) {
            plugin.saveResource(resourceFile, false);
            if (!name.equals(resourceFile)) {
                File from = new File(plugin.getDataFolder(), resourceFile);
                try {
                    Files.move(from.toPath(), configFile.toPath());
                    FileUtils.removeDirToRootIfEmpty(from.getParentFile());
                } catch (Exception e) {
                    throw new RuntimeException("Error moving created config file from " + from + " to " + configFile, e);
                }
            }
            return configFile;
        }
        if (!configFile.getParentFile().isDirectory() && !configFile.getParentFile().mkdirs()) {
            throw new RuntimeException("Could not create plugin config directory");
        }
        try {
            if (!configFile.createNewFile()) {
                throw new RuntimeException("Unable to create config file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating config file " + name, e);
        }
        return configFile;
    }

    @Nonnull
    public static Map<String, String> readCommentsFromYml(@Nonnull InputStream stream) throws IOException {
        Map<String, String> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String key = getFirstSubstring(line, ":");
                if (key == null) continue;

                String comment = getLastSubstring(line, "#");
                if (comment == null) continue;

                result.put(key, comment);
            }
        }
        return result;
    }

    public static void writeCommentsToYmlFile(@Nonnull InputStream in,
                                              @Nonnull Supplier<OutputStream> out,
                                              @Nullable Map<String, String> mandatoryComments,
                                              @Nullable Map<String, String> optionalComments
    ) throws IOException {
        if (mandatoryComments == null && optionalComments == null) return;

        List<String> lines = readLines(in);

        boolean contentChanged = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String key = getFirstSubstring(line, ":");
            if (key == null) continue;

            String oldComment = getLastSubstring(line, "#");

            String newComment;
            if (mandatoryComments != null && mandatoryComments.containsKey(key)) {
                newComment = mandatoryComments.get(key);
            } else if (optionalComments != null && optionalComments.containsKey(key) && oldComment == null) {
                newComment = optionalComments.get(key);
            } else {
                continue;
            }

            if (Objects.equals(oldComment, newComment)) continue;

            if (oldComment != null) {
                line = line.substring(0, oldComment.length() - "#".length()).trim();
            }
            if (newComment != null) {
                line = line + " #" + newComment;
            }

            lines.set(i, line);
            contentChanged = true;
        }
        if (!contentChanged) return;

        writeLines(out.get(), lines);
    }

    @Nonnull
    public static List<String> readLines(@Nonnull InputStream in) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        in.close();
        return result;
    }

    public static void writeLines(@Nonnull OutputStream out, @Nonnull List<String> lines) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        for (String line : lines) {
            writer.write(line + "\n");
        }
        writer.close();
        out.close();
    }

    @Nullable
    private static String getFirstSubstring(@Nonnull String string, @Nonnull String splitter) {
        int index = string.indexOf(splitter);
        if (index < 0) return null;
        return string.substring(0, index);
    }

    @Nullable
    private static String getLastSubstring(@Nonnull String string, @Nonnull String splitter) {
        int index = string.indexOf(splitter);
        if (index < 0) return null;
        return string.substring(index + splitter.length());
    }

    public static void appendDataToFile(@Nonnull File file,
                                        @Nonnull String data
    ) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(data);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to append data to file " + file);
        }
    }
}
