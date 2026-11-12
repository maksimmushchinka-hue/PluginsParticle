package dev.particlefx.core;

public enum LaunchMode {
    SELF("На месте игрока"),
    FRONT("Перед игроком");

    private final String label;

    LaunchMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public LaunchMode next() {
        return this == SELF ? FRONT : SELF;
    }

    public static LaunchMode parse(String s, LaunchMode def) {
        if (s == null) return def;
        try {
            return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }
}
