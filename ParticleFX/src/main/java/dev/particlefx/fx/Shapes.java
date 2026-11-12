package dev.particlefx.fx;

import java.util.Random;
import org.bukkit.Particle;

/** Геометрические примитивы, рисуемые через {@link Pen}. Все координаты относительны точки запуска. */
public final class Shapes {
    public static final double TWO_PI = Math.PI * 2.0;
    private static final double GOLDEN = Math.PI * (3.0 - Math.sqrt(5.0));

    private Shapes() {}

    // ---------- плавность ----------
    public static double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }
    public static double smooth(double x) { x = clamp01(x); return x * x * (3 - 2 * x); }
    public static double easeOut(double x) { x = clamp01(x); double i = 1 - x; return 1 - i * i * i; }
    public static double easeIn(double x) { x = clamp01(x); return x * x * x; }
    public static double lerp(double a, double b, double x) { return a + (b - a) * x; }

    // ---------- повороты ----------
    /** Поворот вокруг оси X на tilt, затем вокруг оси Y на az. */
    public static double[] tilt(double x, double y, double z, double tilt, double az) {
        double y1 = y * Math.cos(tilt) - z * Math.sin(tilt);
        double z1 = y * Math.sin(tilt) + z * Math.cos(tilt);
        double x2 = x * Math.cos(az) + z1 * Math.sin(az);
        double z2 = -x * Math.sin(az) + z1 * Math.cos(az);
        return new double[]{x2, y1, z2};
    }

    public static double[] rot(double x, double y, double z, double ax, double ay) {
        return tilt(x, y, z, ax, ay);
    }

    /** Точка на наклонной орбите (центр в начале координат). */
    public static double[] orbit(double rad, double ang, double tilt, double az) {
        return tilt(Math.cos(ang) * rad, 0, Math.sin(ang) * rad, tilt, az);
    }

    /** i-я из n равномерно распределённых точек единичной сферы (спираль Фибоначчи). */
    public static double[] fib(int i, int n, double spin) {
        double y = 1 - 2 * (i + 0.5) / n;
        double rr = Math.sqrt(Math.max(0, 1 - y * y));
        double a = GOLDEN * i + spin;
        return new double[]{Math.cos(a) * rr, y, Math.sin(a) * rr};
    }

    // ---------- кольца и дуги ----------
    public static void ring(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double phase) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double a = phase + TWO_PI * i / n;
            pen.draw(f, cx + Math.cos(a) * rad, cy, cz + Math.sin(a) * rad);
        }
    }

    public static void ringT(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double phase,
                             double tilt, double az) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double a = phase + TWO_PI * i / n;
            double[] p = tilt(Math.cos(a) * rad, 0, Math.sin(a) * rad, tilt, az);
            pen.draw(f, cx + p[0], cy + p[1], cz + p[2]);
        }
    }

    /** Дуга в вертикальной плоскости, повёрнутой на yaw вокруг оси Y. */
    public static void arcV(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double a0,
                            double a1, double yaw) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double a = n == 1 ? a0 : a0 + (a1 - a0) * i / (n - 1.0);
            double hh = Math.cos(a) * rad;
            pen.draw(f, cx + hh * Math.cos(yaw), cy + Math.sin(a) * rad, cz + hh * Math.sin(yaw));
        }
    }

    public static void ringV(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double phase,
                             double yaw) {
        if (n <= 0) return;
        arcV(f, pen, cx, cy, cz, rad, n, phase, phase + TWO_PI * (n - 1.0) / n, yaw);
    }

    /** Кольцо в вертикальной плоскости, обращённой к «взгляду» (перпендикулярной направлению вперёд). */
    public static void ringF(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double phase) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double a = phase + TWO_PI * i / n;
            double l = Math.cos(a) * rad;
            pen.draw(f, cx + l * f.rx, cy + Math.sin(a) * rad, cz + l * f.rz);
        }
    }

    // ---------- сферы и диски ----------
    public static void sphere(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double spin) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double[] d = fib(i, n, spin);
            pen.draw(f, cx + d[0] * rad, cy + d[1] * rad, cz + d[2] * rad);
        }
    }

    public static void dome(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double spin) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double y = (i + 0.5) / n;
            double rr = Math.sqrt(Math.max(0, 1 - y * y));
            double a = GOLDEN * i + spin;
            pen.draw(f, cx + Math.cos(a) * rr * rad, cy + y * rad, cz + Math.sin(a) * rr * rad);
        }
    }

    public static void disc(Frame f, Pen pen, double cx, double cy, double cz, double rad, int n, double spin) {
        if (rad < 0.02 || n <= 0) return;
        for (int i = 0; i < n; i++) {
            double rr = rad * Math.sqrt((i + 0.5) / n);
            double a = GOLDEN * i + spin;
            pen.draw(f, cx + Math.cos(a) * rr, cy, cz + Math.sin(a) * rr);
        }
    }

    // ---------- линии ----------
    public static void line(Frame f, Pen pen, double x1, double y1, double z1, double x2, double y2, double z2, int n) {
        if (n <= 0) return;
        for (int i = 0; i < n; i++) {
            double u = n == 1 ? 0.5 : i / (n - 1.0);
            pen.draw(f, x1 + (x2 - x1) * u, y1 + (y2 - y1) * u, z1 + (z2 - z1) * u);
        }
    }

    /** Линия в локальных координатах (боковая, высота, вперёд) относительно точки запуска. */
    public static void lineL(Frame f, Pen pen, double lat1, double y1, double fw1, double lat2, double y2,
                             double fw2, int n) {
        line(f, pen, f.px(lat1, fw1), y1, f.pz(lat1, fw1), f.px(lat2, fw2), y2, f.pz(lat2, fw2), n);
    }

    /** Отрезок луча: от c+d*a до c+d*b. */
    public static void ray(Frame f, Pen pen, double cx, double cy, double cz, double dx, double dy, double dz,
                           double a, double b, int n) {
        line(f, pen, cx + dx * a, cy + dy * a, cz + dz * a, cx + dx * b, cy + dy * b, cz + dz * b, n);
    }

    // ---------- направленные выбросы (анимирует клиент) ----------
    public static void burst(Frame f, Particle pt, double cx, double cy, double cz, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double x = f.rnd.nextGaussian(), y = f.rnd.nextGaussian(), z = f.rnd.nextGaussian();
            double l = Math.sqrt(x * x + y * y + z * z);
            if (l < 1.0e-6) continue;
            f.v(pt, cx, cy, cz, x / l, y / l, z / l, speed * (0.6 + 0.4 * f.rnd.nextDouble()));
        }
    }

    /** Веер, смещённый вверх (up — добавка к вертикальной составляющей). */
    public static void fan(Frame f, Particle pt, double cx, double cy, double cz, int count, double speed, double up) {
        for (int i = 0; i < count; i++) {
            double x = f.rnd.nextGaussian(), y = Math.abs(f.rnd.nextGaussian()) + up, z = f.rnd.nextGaussian();
            double l = Math.sqrt(x * x + y * y + z * z);
            if (l < 1.0e-6) continue;
            f.v(pt, cx, cy, cz, x / l, y / l, z / l, speed * (0.6 + 0.4 * f.rnd.nextDouble()));
        }
    }

    public static void flatBurst(Frame f, Particle pt, double cx, double cy, double cz, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double a = f.rnd.nextDouble() * TWO_PI;
            f.v(pt, cx, cy, cz, Math.cos(a), 0.02, Math.sin(a), speed * (0.6 + 0.4 * f.rnd.nextDouble()));
        }
    }

    // ---------- звёзды и многоугольники ----------
    /** Звезда/многоугольник в горизонтальной плоскости; prog — доля уже прорисованных рёбер (0..1). */
    public static void starP(Frame f, Pen pen, double cx, double cy, double cz, double rad, int points, int skip,
                             double rot, int per, double prog) {
        double lim = clamp01(prog) * points;
        for (int i = 0; i < points; i++) {
            double fr = clamp01(lim - i);
            if (fr <= 0) break;
            int j = (i + skip) % points;
            double a0 = rot + TWO_PI * i / points, a1 = rot + TWO_PI * j / points;
            double x0 = cx + Math.cos(a0) * rad, z0 = cz + Math.sin(a0) * rad;
            double x1 = cx + Math.cos(a1) * rad, z1 = cz + Math.sin(a1) * rad;
            line(f, pen, x0, cy, z0, x0 + (x1 - x0) * fr, cy, z0 + (z1 - z0) * fr,
                    Math.max(2, (int) Math.round(per * fr)));
        }
    }

    public static void star(Frame f, Pen pen, double cx, double cy, double cz, double rad, int points, int skip,
                            double rot, int per) {
        starP(f, pen, cx, cy, cz, rad, points, skip, rot, per, 1.0);
    }

    /** Звезда/многоугольник в вертикальной плоскости «лицом к зрителю» на расстоянии fw вперёд. */
    public static void starF(Frame f, Pen pen, double cy, double rad, int points, int skip, double rot, int per,
                             double fw) {
        for (int i = 0; i < points; i++) {
            int j = (i + skip) % points;
            double a0 = rot + TWO_PI * i / points, a1 = rot + TWO_PI * j / points;
            lineL(f, pen, Math.cos(a0) * rad, cy + Math.sin(a0) * rad, fw, Math.cos(a1) * rad,
                    cy + Math.sin(a1) * rad, fw, per);
        }
    }

    // ---------- куб ----------
    /** Рёбра куба, вращаемого на ax (вокруг X) и ay (вокруг Y); prog — доля уже вычерченных рёбер. */
    public static void cube(Frame f, Pen pen, double cx, double cy, double cz, double half, double ay, double ax,
                            int per, double prog) {
        double[][] v = new double[8][];
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? -half : half;
            double y = ((i >> 1) & 1) == 0 ? -half : half;
            double z = ((i >> 2) & 1) == 0 ? -half : half;
            v[i] = rot(x, y, z, ax, ay);
        }
        double lim = clamp01(prog) * 12.0;
        int e = 0;
        for (int i = 0; i < 8; i++) {
            for (int b = 0; b < 3; b++) {
                int j = i ^ (1 << b);
                if (j < i) continue;
                double fr = clamp01(lim - e);
                e++;
                if (fr <= 0) continue;
                double[] a = v[i], c = v[j];
                line(f, pen, cx + a[0], cy + a[1], cz + a[2],
                        cx + a[0] + (c[0] - a[0]) * fr, cy + a[1] + (c[1] - a[1]) * fr,
                        cz + a[2] + (c[2] - a[2]) * fr, Math.max(2, (int) Math.round(per * fr)));
            }
        }
    }

    // ---------- молния ----------
    /** Зубчатая линия с ответвлениями; reveal — доля сегментов, уже «проросших» сверху вниз. */
    public static void bolt(Frame f, Pen pen, double x0, double y0, double z0, double x1, double y1, double z1,
                            int segs, double jit, long seed, int branches, double reveal) {
        Random r = new Random(seed);
        int draw = (int) Math.ceil(segs * clamp01(reveal));
        double px = x0, py = y0, pz = z0;
        for (int i = 1; i <= draw; i++) {
            double u = (double) i / segs;
            double nx = lerp(x0, x1, u), ny = lerp(y0, y1, u), nz = lerp(z0, z1, u);
            if (i < segs) {
                nx += (r.nextDouble() - 0.5) * 2 * jit;
                nz += (r.nextDouble() - 0.5) * 2 * jit;
            }
            double len = Math.sqrt((nx - px) * (nx - px) + (ny - py) * (ny - py) + (nz - pz) * (nz - pz));
            line(f, pen, px, py, pz, nx, ny, nz, Math.max(2, (int) Math.round(len * 1.5 * f.density)));
            if (branches > 0 && i < segs - 1 && r.nextDouble() < 0.4) {
                double bx = nx + (r.nextDouble() - 0.5) * jit * 5;
                double by = ny + (y1 - y0) / segs * (1.5 + r.nextDouble() * 2);
                double bz = nz + (r.nextDouble() - 0.5) * jit * 5;
                bolt(f, pen, nx, ny, nz, bx, by, bz, 4, jit * 0.5, r.nextLong(), branches - 1, 1.0);
            }
            px = nx;
            py = ny;
            pz = nz;
        }
    }
}
