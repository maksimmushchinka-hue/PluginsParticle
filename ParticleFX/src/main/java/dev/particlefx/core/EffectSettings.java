package dev.particlefx.core;

/** Параметры запуска эффекта. */
public final class EffectSettings {
    public double scale = 1.0;
    public double duration = 10.0;
    public double speed = 1.0;
    public double density = 1.0;
    public double radius = 6.0;
    public double height = 6.0;
    public LaunchMode mode = LaunchMode.FRONT;

    public EffectSettings copy() {
        EffectSettings s = new EffectSettings();
        s.scale = scale;
        s.duration = duration;
        s.speed = speed;
        s.density = density;
        s.radius = radius;
        s.height = height;
        s.mode = mode;
        return s;
    }

    /** Приводит все значения к допустимым; неприменимые параметры возвращает к значениям по умолчанию. */
    public void sanitize(PluginSettings cfg, AnimationInfo info) {
        scale = cfg.scale.fix(scale, 1.0);
        duration = cfg.duration.fix(duration, info.duration());
        speed = cfg.speed.fix(speed, 1.0);
        density = cfg.density.fix(density, 1.0);
        radius = info.hasRadius() ? cfg.radius.fix(radius, info.radius()) : info.radius();
        height = info.hasHeight() ? cfg.height.fix(height, info.height()) : info.height();
        if (mode == null) mode = cfg.defaultMode;
    }
}
