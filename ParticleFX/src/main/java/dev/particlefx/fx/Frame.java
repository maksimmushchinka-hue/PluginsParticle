package dev.particlefx.fx;

import java.util.Random;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Контекст одного кадра анимации. Все координаты, передаваемые в методы рисования,
 * задаются относительно точки запуска. Класс следит за бюджетом частиц на тик.
 */
public final class Frame {
    public final World world;
    public final double ox, oy, oz;
    /** Горизонтальное направление «вперёд» (единичный вектор) и «вправо». */
    public final double fx, fz, rx, rz;
    public final double t;
    public final int tick, total;
    /** Эффективные радиус и высота (уже умножены на масштаб). */
    public final double r, h;
    public final double sp, density;
    public final Random rnd;
    private final Player viewer;
    private final boolean force;
    private final int startBudget;
    private int budget;

    public Frame(World world, double ox, double oy, double oz, double fx, double fz, int tick, int total,
                 double r, double h, double sp, double density, Random rnd, Player viewer, boolean force, int budget) {
        this.world = world;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.fx = fx;
        this.fz = fz;
        this.rx = -fz;
        this.rz = fx;
        this.tick = tick;
        this.total = total;
        this.t = total <= 0 ? 1.0 : Math.min(1.0, (double) tick / total);
        this.r = r;
        this.h = h;
        this.sp = sp;
        this.density = density;
        this.rnd = rnd;
        this.viewer = viewer;
        this.force = force;
        this.startBudget = budget;
        this.budget = budget;
    }

    public int used() {
        return startBudget - budget;
    }

    /** Число частиц с учётом плотности (минимум 1). */
    public int n(double base) {
        return Math.max(1, (int) Math.round(base * density));
    }

    /** Прогресс внутри окна [a, b] общего времени, 0..1. */
    public double ph(double a, double b) {
        if (b <= a) return t >= b ? 1.0 : 0.0;
        double x = (t - a) / (b - a);
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }

    /** true ровно в одном тике — первом, где прогресс достиг x. */
    public boolean at(double x) {
        return tick == (int) Math.ceil(x * total - 1.0e-9);
    }

    /** Секунды, прошедшие с момента прогресса from. */
    public double sec(double from) {
        return Math.max(0.0, t - from) * total / 20.0;
    }

    /** Угол вращения (радианы) при заданном числе оборотов в секунду с учётом скорости. */
    public double spin(double turnsPerSecond) {
        return Shapes.TWO_PI * turnsPerSecond * (tick / 20.0) * sp;
    }

    /** Мировая X/Z-координата (относительно точки запуска) из боковой и передней компонент. */
    public double px(double lat, double fwd) {
        return lat * rx + fwd * fx;
    }

    public double pz(double lat, double fwd) {
        return lat * rz + fwd * fz;
    }

    private void emit(Particle pt, double x, double y, double z, int count, double a, double b, double c,
                      double extra, Object data) {
        if (budget <= 0) return;
        double wx = ox + x, wy = oy + y, wz = oz + z;
        if (!Double.isFinite(wx) || !Double.isFinite(wy) || !Double.isFinite(wz)) return;
        budget -= Math.max(1, count);
        if (viewer != null) {
            viewer.spawnParticle(pt, wx, wy, wz, count, a, b, c, extra, data);
        } else {
            world.spawnParticle(pt, wx, wy, wz, count, a, b, c, extra, data, force);
        }
    }

    /** Одна частица без данных. */
    public void p(Particle pt, double x, double y, double z) {
        emit(pt, x, y, z, 1, 0, 0, 0, 0, null);
    }

    /** Частица, летящая в заданном направлении (клиент сам анимирует полёт). */
    public void v(Particle pt, double x, double y, double z, double dx, double dy, double dz, double speed) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) return;
        emit(pt, x, y, z, 0, dx, dy, dz, speed, null);
    }

    public void d(double x, double y, double z, int rgb, double size) {
        emit(Particle.DUST, x, y, z, 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(rgb & 0xFFFFFF), (float) Math.max(0.2, Math.min(4.0, size))));
    }

    public void dt(double x, double y, double z, int from, int to, double size) {
        emit(Particle.DUST_COLOR_TRANSITION, x, y, z, 1, 0, 0, 0, 0,
                new Particle.DustTransition(Color.fromRGB(from & 0xFFFFFF), Color.fromRGB(to & 0xFFFFFF),
                        (float) Math.max(0.2, Math.min(4.0, size))));
    }

    public void blk(Material m, double x, double y, double z) {
        emit(Particle.BLOCK, x, y, z, 1, 0, 0, 0, 0, m.createBlockData());
    }
}
