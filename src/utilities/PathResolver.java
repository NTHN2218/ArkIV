package utilities;

import java.io.File;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class PathResolver {

    private static String cachedAssetsPath = null;
    private static String cachedDataFilePath = null;

    private static Font cachedRegularFont = null;
    private static Font cachedBoldFont = null;
    private static Font cachedItalicFont = null;

    /**
     * Returns the absolute path to the assets directory.
     */
    public static String getAssetsPath() {
        if (cachedAssetsPath != null) {
            return cachedAssetsPath;
        }

        String appRoot = getApplicationRoot();
        File assetsDir = new File(appRoot, "assets");

        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            throw new RuntimeException(
                    "Assets directory not found at: " + assetsDir.getAbsolutePath()
            );
        }

        cachedAssetsPath = assetsDir.getAbsolutePath();
        return cachedAssetsPath;
    }

    public static String getDataDirPath() {
        File dataDir = new File(getAssetsPath(), "data");

        if (!dataDir.exists() || !dataDir.isDirectory()) {
            throw new RuntimeException(
                    "Data directory not found at: " + dataDir.getAbsolutePath()
            );
        }

        return dataDir.getAbsolutePath();
    }



    /**
     * Returns the absolute path to assets/data.json
     */
    public static String getDataFilePath() {
        if (cachedDataFilePath != null) {
            return cachedDataFilePath;
        }

        File dataFile = new File(getAssetsPath(), "data.json");

        if (!dataFile.exists() || !dataFile.isFile()) {
            throw new RuntimeException(
                    "data.json not found at: " + dataFile.getAbsolutePath()
            );
        }

        cachedDataFilePath = dataFile.getAbsolutePath();
        return cachedDataFilePath;
    }

    /**
     * Finds the application root directory by backtracking.
     */
    private static String getApplicationRoot() {
        try {
            String path = PathResolver.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            File location = new File(path);

            // Running from JAR
            if (location.isFile()) {
                return location.getParent();
            }

            // Running from IDE: backtrack until assets folder is found
            File current = location;

            while (current != null) {
                File assetsDir = new File(current, "assets");
                if (assetsDir.exists() && assetsDir.isDirectory()) {
                    return current.getAbsolutePath();
                }
                current = current.getParentFile();
            }

            // Fallback
            return System.getProperty("user.dir");

        } catch (Exception e) {
            e.printStackTrace();
            return System.getProperty("user.dir");
        }
    }

    /**
     * Optional sanity check
     */
    public static void validateAssets() {
        try {
            System.out.println("Validating assets...");
            System.out.println("Assets path: " + getAssetsPath());
            System.out.println("✓ data.json found at: " + getDataFilePath());
        } catch (RuntimeException e) {
            System.err.println("✗ Asset validation failed: " + e.getMessage());
            throw e;
        }
    }

    public static String getFontDirPath() {
        File fontDir = new File(getAssetsPath(), "fonts");

        if (!fontDir.exists() || !fontDir.isDirectory()) {
            throw new RuntimeException(
                    "Fonts directory not found at: " + fontDir.getAbsolutePath()
            );
        }

        return fontDir.getAbsolutePath();
    }

    private static Font loadFontFile(String filename) {
        File fontFile = new File(getFontDirPath(), filename);
        try {
            Font loaded = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loaded);
            return loaded;
        } catch (Exception e) {
            System.err.println("Could not load font file: " + filename + " -- " + e.getMessage());
            return null;
        }
    }

    public static Font getRegularBaseFont() {
        if (cachedRegularFont == null) {
            Font loaded = loadFontFile("JetBrainsMono-Regular.ttf");
            cachedRegularFont = (loaded != null) ? loaded : new Font("Segoe UI", Font.PLAIN, 12);
        }
        return cachedRegularFont;
    }

    public static Font getBoldBaseFont() {
        if (cachedBoldFont == null) {
            Font loaded = loadFontFile("JetBrainsMono-Bold.ttf");
            cachedBoldFont = (loaded != null) ? loaded : new Font("Segoe UI", Font.BOLD, 12);
        }
        return cachedBoldFont;
    }

    public static Font getItalicBaseFont() {
        if (cachedItalicFont == null) {
            Font loaded = loadFontFile("JetBrainsMono-Italic.ttf");
            cachedItalicFont = (loaded != null) ? loaded : new Font("Segoe UI", Font.ITALIC, 12);
        }
        return cachedItalicFont;
    }
}
