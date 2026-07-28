package Registers;

import com.google.gson.*;
import utilities.PathResolver;

import java.io.*;
import java.util.*;

import java.nio.charset.StandardCharsets;

public class RegisterManager {

    private static final String HEADER_FILE = "header.json";

    private final String assetsPath;
    private final File headerFile;

    private int defaultRegisterId = -1;
    private List<RegisterEntry> registers = new ArrayList<>();

    public static class RegisterEntry {
        public int id;
        public String name;
        public String file;
        public int order;

        public RegisterEntry(int id, String name, String file, int order) {
            this.id = id;
            this.name = name;
            this.file = file;
            this.order = order;
        }
    }

    public RegisterManager() {
        assetsPath = PathResolver.getDataDirPath();
        headerFile = new File(assetsPath, HEADER_FILE);
        loadOrInitHeader();
    }

    // ── Header load / init ──────────────────────────────────────────
    private void loadOrInitHeader() {
        if (headerFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(headerFile), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");

                JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
                defaultRegisterId = root.get("defaultRegisterId").getAsInt();

                registers.clear();
                for (JsonElement el : root.getAsJsonArray("registers")) {
                    JsonObject obj = el.getAsJsonObject();
                    registers.add(new RegisterEntry(
                            obj.get("id").getAsInt(),
                            obj.get("name").getAsString(),
                            obj.get("file").getAsString(),
                            obj.get("order").getAsInt()
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
                createFreshHeaderWithDefaultRegister();
            }
        } else {
            createFreshHeaderWithDefaultRegister();
        }
    }

    private void createFreshHeaderWithDefaultRegister() {
        registers.clear();

        // Migration: adopt existing v7/v8 data.json as Register 1 instead of
        // creating a new empty file, so nothing gets "lost" on first launch.
        File legacyDataFile = new File(assetsPath, "data.json");
        String filename = legacyDataFile.exists() ? "data.json" : "register_1.json";

        RegisterEntry defaultEntry = new RegisterEntry(1, "Register 1", filename, 0);
        registers.add(defaultEntry);
        defaultRegisterId = 1;

        if (!legacyDataFile.exists()) {
            writeEmptyRegisterFile(filename);
        }
        saveHeader();
    }

    private void writeEmptyRegisterFile(String filename) {
        File f = new File(assetsPath, filename);
        if (!f.exists()) {
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                writer.print("[]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveHeader() {
        JsonObject root = new JsonObject();
        root.addProperty("defaultRegisterId", defaultRegisterId);

        JsonArray arr = new JsonArray();
        for (RegisterEntry r : getRegisters()) { // sorted by order
            JsonObject obj = new JsonObject();
            obj.addProperty("id", r.id);
            obj.addProperty("name", r.name);
            obj.addProperty("file", r.file);
            obj.addProperty("order", r.order);
            arr.add(obj);
        }
        root.add("registers", arr);

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(headerFile), StandardCharsets.UTF_8))) {
            writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Accessors ────────────────────────────────────────────────────
    public List<RegisterEntry> getRegisters() {
        List<RegisterEntry> sorted = new ArrayList<>(registers);
        sorted.sort(Comparator.comparingInt(r -> r.order));
        return sorted;
    }

    public RegisterEntry getRegisterById(int id) {
        for (RegisterEntry r : registers) if (r.id == id) return r;
        return null;
    }

    public RegisterEntry getDefaultRegister() {
        return getRegisterById(defaultRegisterId);
    }

    public int getDefaultRegisterId() {
        return defaultRegisterId;
    }

    public String getRegisterFilePath(RegisterEntry entry) {
        return new File(assetsPath, entry.file).getAbsolutePath();
    }

    // ── CRUD — wired up starting Phase 3/4, dormant for now ─────────
    public RegisterEntry createRegister(String name) {
        int newId = nextId();
        String finalName = (name == null || name.trim().isEmpty())
                ? "Register " + (registers.size() + 1)
                : name.trim();
        String filename = "register_" + newId + ".json";
        writeEmptyRegisterFile(filename);

        RegisterEntry entry = new RegisterEntry(newId, finalName, filename, registers.size());
        registers.add(entry);
        saveHeader();
        return entry;
    }

    public void renameRegister(int id, String newName) {
        RegisterEntry entry = getRegisterById(id);
        if (entry != null && newName != null && !newName.trim().isEmpty()) {
            entry.name = newName.trim();
            saveHeader();
        }
    }

    public void setDefault(int id) {
        if (getRegisterById(id) != null) {
            defaultRegisterId = id;
            saveHeader();
        }
    }

    public void reorder(int id, int direction) {
        List<RegisterEntry> sorted = getRegisters();
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).id == id) { index = i; break; }
        }
        int swapIndex = index + direction;
        if (index == -1 || swapIndex < 0 || swapIndex >= sorted.size()) return;

        RegisterEntry a = sorted.get(index);
        RegisterEntry b = sorted.get(swapIndex);
        int tmp = a.order;
        a.order = b.order;
        b.order = tmp;
        saveHeader();
    }

    public boolean deleteRegister(int id, boolean deleteFile) {
        RegisterEntry entry = getRegisterById(id);
        if (entry == null) return false;

        registers.remove(entry);

        if (deleteFile) {
            File f = new File(assetsPath, entry.file);
            if (f.exists()) f.delete();
        }

        List<RegisterEntry> sorted = getRegisters();
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).order = i;

        if (defaultRegisterId == id) {
            defaultRegisterId = sorted.isEmpty() ? -1 : sorted.get(0).id;
        }

        saveHeader();
        return true;
    }

    // ── Unrecognized register detection ─────────────────────────────
    public List<String> scanUnrecognizedFiles() {
        List<String> result = new ArrayList<>();
        File dir = new File(assetsPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.equals(HEADER_FILE));
        if (files == null) return result;

        Set<String> knownFiles = new HashSet<>();
        for (RegisterEntry r : registers) knownFiles.add(r.file);

        for (File f : files) {
            if (knownFiles.contains(f.getName())) continue;
            if (isValidRegisterSchema(f)) result.add(f.getName());
        }
        return result;
    }

    private boolean isValidRegisterSchema(File f) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");

            JsonArray arr = JsonParser.parseString(sb.toString()).getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) return false;
                JsonObject obj = el.getAsJsonObject();
                if (!obj.has("ID") || !obj.has("parentID") || !obj.has("TXT")
                        || !obj.has("isDone") || !obj.has("isSub") || !obj.has("isCollapsed")) {
                    return false;
                }
            }
            return true; // empty array [] also counts as a valid register
        } catch (Exception e) {
            return false;
        }
    }

    public RegisterEntry recognizeFile(String filename, String name) {
        int newId = nextId();
        String finalName = (name == null || name.trim().isEmpty())
                ? "Register " + (registers.size() + 1)
                : name.trim();

        RegisterEntry entry = new RegisterEntry(newId, finalName, filename, registers.size());
        registers.add(entry);
        saveHeader();
        return entry;
    }

    private int nextId() {
        int max = 0;
        for (RegisterEntry r : registers) max = Math.max(max, r.id);
        return max + 1;
    }

    public static class UnrecognizedEntry {
        public final String filename;
        public final String displayName;

        public UnrecognizedEntry(String filename, String displayName) {
            this.filename = filename;
            this.displayName = displayName;
        }
    }

    public List<UnrecognizedEntry> getUnrecognizedEntries() {
        List<UnrecognizedEntry> result = new ArrayList<>();
        List<String> filenames = scanUnrecognizedFiles();
        for (int i = 0; i < filenames.size(); i++) {
            result.add(new UnrecognizedEntry(filenames.get(i), "Unrecognized Reg" + (i + 1)));
        }
        return result;
    }

    public String getAssetsPathPublic() {
        return assetsPath;
    }
}

