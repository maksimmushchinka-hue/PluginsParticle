package dev.particlefx.fx;

import org.bukkit.Material;
import org.bukkit.Particle;

/** «Кисть»: способ нарисовать одну точку фигуры (координаты относительно точки запуска). */
@FunctionalInterface
public interface Pen {
    void draw(Frame f, double x, double y, double z);

    static Pen of(Particle p) {
        return (f, x, y, z) -> f.p(p, x, y, z);
    }

    static Pen dust(int rgb, double size) {
        return (f, x, y, z) -> f.d(x, y, z, rgb, size);
    }

    static Pen fade(int from, int to, double size) {
        return (f, x, y, z) -> f.dt(x, y, z, from, to, size);
    }

    static Pen block(Material m) {
        return (f, x, y, z) -> f.blk(m, x, y, z);
    }

    static Pen hue(double h, double size) {
        return dust(Colors.hue(h), size);
    }
}
