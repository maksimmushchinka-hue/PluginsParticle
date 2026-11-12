package dev.particlefx.core;

import dev.particlefx.ParticleFXPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;

/** Загруженная и проверенная конфигурация плагина. */
public final class PluginSettings {
    public record EffectConfig(boolean enabled, double scale, double duration, double speed, double density,
                               double radius, double height) {
    }

    private final ParticleFXPlugin plugin;
    public Range scale, duration, speed, density, radius, height;
    public int maxParticlesEffect, maxParticlesGlobal, maxActiveServer, maxActivePlayer;
    public double frontDistance;
    public boolean longRange, adminBypass, previewPrivate;
    public LaunchMode defaultMode = LaunchMode.FRONT;
    public double previewMaxSeconds, previewScale, previewDensity;
    public double stepScale, stepDuration, stepSpeed, stepDensity, stepRadius, stepHeight;
    private final Map<String, EffectConfig> effects = new HashMap<>();

    public PluginSettings(ParticleFXPlugin plugin) {
        this.plugin = plugin;
    }

    private static double num(double v, double def) {
        return Double.isNaN(v) || Double.isInfinite(v) ? def : v;
    }

    private Range range(FileConfiguration c, String key, double dMin, double dMax) {
        double a = num(c.getDouble("limits." + key + ".min", dMin), dMin);
        double b = num(c.getDouble("limits." + key + ".max", dMax), dMax);
        a = Math.max(a, 0.01);
        return new Range(a, Math.max(a, b));
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();
        defaultMode = LaunchMode.parse(c.getString("defaults.mode", "FRONT"), LaunchMode.FRONT);
        frontDistance = Math.max(0, Math.min(60, num(c.getDouble("defaults.front-distance", 8.0), 8.0)));
        longRange = c.getBoolean("defaults.long-distance-render", true);
        scale = range(c, "scale", 0.25, 4.0);
        duration = range(c, "duration", 3.0, 60.0);
        speed = range(c, "speed", 0.25, 3.0);
        density = range(c, "density", 0.25, 3.0);
        radius = range(c, "radius", 1.0, 30.0);
        height = range(c, "height", 1.0, 40.0);
        maxParticlesEffect = Math.max(10, c.getInt("limits.max-particles-per-tick-per-effect", 600));
        maxParticlesGlobal = Math.max(maxParticlesEffect, c.getInt("limits.max-particles-per-tick-global", 2500));
        maxActiveServer = Math.max(1, c.getInt("limits.max-active-effects-server", 20));
        maxActivePlayer = Math.max(1, c.getInt("limits.max-active-effects-per-player", 3));
        adminBypass = c.getBoolean("limits.admin-bypass-limits", true);
        previewMaxSeconds = Math.max(1.0, num(c.getDouble("preview.max-duration-seconds", 6.0), 6.0));
        previewScale = Math.max(0.1, num(c.getDouble("preview.scale-multiplier", 0.5), 0.5));
        previewDensity = Math.max(0.1, num(c.getDouble("preview.density-multiplier", 0.6), 0.6));
        previewPrivate = c.getBoolean("preview.private", true);
        stepScale = Math.max(0.01, num(c.getDouble("gui.step.scale", 0.25), 0.25));
        stepDuration = Math.max(0.1, num(c.getDouble("gui.step.duration", 1.0), 1.0));
        stepSpeed = Math.max(0.01, num(c.getDouble("gui.step.speed", 0.25), 0.25));
        stepDensity = Math.max(0.01, num(c.getDouble("gui.step.density", 0.25), 0.25));
        stepRadius = Math.max(0.1, num(c.getDouble("gui.step.radius", 1.0), 1.0));
        stepHeight = Math.max(0.1, num(c.getDouble("gui.step.height", 1.0), 1.0));
        effects.clear();
        for (AnimationInfo i : AnimationCatalog.ALL) {
            String p = "animations." + i.id() + ".";
            effects.put(i.id(), new EffectConfig(c.getBoolean(p + "enabled", true),
                    c.getDouble(p + "scale", 1.0), c.getDouble(p + "duration", i.duration()),
                    c.getDouble(p + "speed", 1.0), c.getDouble(p + "density", 1.0),
                    c.getDouble(p + "radius", i.radius()), c.getDouble(p + "height", i.height())));
        }
    }

    public boolean isEnabled(AnimationInfo info) {
        EffectConfig e = effects.get(info.id());
        return e == null || e.enabled();
    }

    /** Настройки по умолчанию для анимации (проверенные и ограниченные). */
    public EffectSettings defaultsFor(AnimationInfo info) {
        EffectConfig e = effects.get(info.id());
        EffectSettings s = new EffectSettings();
        if (e != null) {
            s.scale = e.scale();
            s.duration = e.duration();
            s.speed = e.speed();
            s.density = e.density();
            s.radius = e.radius();
            s.height = e.height();
        } else {
            s.duration = info.duration();
            s.radius = info.radius();
            s.height = info.height();
        }
        s.mode = defaultMode;
        s.sanitize(this, info);
        return s;
    }
}
