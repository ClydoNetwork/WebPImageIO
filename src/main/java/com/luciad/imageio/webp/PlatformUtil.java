package com.luciad.imageio.webp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Locale;

@UtilityClass
public class PlatformUtil {

    private Platform platform;

    public Platform getPlatform() {
        if (platform != null) {
            return platform;
        }

        val os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            platform = Platform.WINDOWS;
        } else if (os.contains("mac")) {
            platform = Platform.OSX;
        } else if (os.contains("solaris") || os.contains("sunos")) {
            platform = Platform.SOLARIS;
        } else if (os.contains("linux") || os.contains("unix"))
            platform = Platform.LINUX;
        else {
            platform = Platform.UNKNOWN;
        }

        return platform;
    }

    public boolean is64bit() {
        val archBits = System.getenv("PROCESSOR_ARCHITECTURE");
        if (archBits != null && !archBits.isBlank()) {
            return archBits.contains("64");
        }

        val properties = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (String property : properties) {
            String value = System.getProperty(property);
            if (value != null && value.contains("64")) {
                return true;
            }
        }

        return false;
    }

    public String getArchBits() {
        return is64bit() ? "64" : "32";
    }

    @Getter
    @RequiredArgsConstructor
    public enum Platform {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"),
        OSX("mac"),
        UNKNOWN("unknown");

        private final String telemetryName;
    }
}
