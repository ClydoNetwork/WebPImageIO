package com.luciad.imageio.webp;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
class NativeLibraryUtils {

    public static void automaticallyLoad() throws IOException {
        val nativesPathStr = System.getProperty("java.library.path");

        if (nativesPathStr == null || nativesPathStr.isBlank()) {
            throw new IllegalStateException("Native library path is not set.");
        }

        val nativesDirectory = Path.of(nativesPathStr);
        if (!Files.exists(nativesDirectory)) {
            try {
                Files.createDirectories(nativesDirectory);
            } catch (IOException e) {
                throw new IOException("Failed to create native library folder: " + nativesDirectory, e);
            }
        }

        val platform = PlatformUtil.getPlatform();
        val archBits = PlatformUtil.getArchBits();

        val nativeFileName = getNativeFileName();

        val nativeFile = nativesDirectory.resolve(nativeFileName);
        if (!Files.exists(nativeFile)) {
            try (val in = NativeLibraryUtils.class.getResourceAsStream(String.format("/native/%s/%s/%s", platform.getTelemetryName(), archBits, nativeFileName))) {
                if (in == null) {
                    throw new IOException("Native library " + nativeFileName + " not found in resources.");
                }

                Files.copy(in, nativeFile);
            } catch (IOException e) {
                throw new IOException("Failed to copy " + nativeFileName + " to " + nativesDirectory, e);
            }
        }

        try {
            System.loadLibrary(getNativeName());
        } catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError("Failed to load native library: " + nativeFileName);
        }
    }

    @NotNull
    @Contract(pure = true)
    private static String getNativeFileName() {
        val libraryName = NativeLibraryUtils.getNativeName();
        val platform = PlatformUtil.getPlatform();

        return switch (platform) {
            case WINDOWS -> "%s.dll".formatted(libraryName);
            case LINUX -> "lib%s.so".formatted(libraryName);
            case OSX -> "lib%s.dylib".formatted(libraryName);
            default -> throw new IllegalArgumentException("Unknown platform: " + platform);
        };
    }

    @NotNull
    private static String getNativeName() {
        return "webp-imageio";
    }

}
