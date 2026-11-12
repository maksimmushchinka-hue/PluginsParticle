package dev.particlefx.fx;

import static dev.particlefx.fx.Shapes.*;
import static java.lang.Math.*;

import java.util.Map;
import java.util.Random;
import org.bukkit.Particle;

/** Космические анимации (1–10). */
public final class SpaceFx {
    private SpaceFx() {}

    private static final Pen CORE = Pen.of(Particle.END_ROD);
    private static final Pen FLM = Pen.of(Particle.FLAME);
    private static final Pen HOT = Pen.dust(0xFFC040, 1.5);
    private static final Pen FADE = Pen.fade(0xFF8020, 0x40302A, 1.6);
    private static final Pen[] SHELL = {Pen.dust(0xFFFFFF, 1.6), Pen.dust(0xFFAA33, 1.8), Pen.dust(0xFF4422, 2.0)};
    private static final Pen VOID = Pen.dust(0x050508, 3.0);
    private static final Pen ACC = Pen.dust(0xFFD080, 1.6);
    private static final Pen JET = Pen.fade(0xAACCFF, 0x6030C0, 1.6);
    private static final Pen ARC_A = Pen.fade(0x66AAFF, 0xFFFFFF, 1.6);
    private static final Pen ARC_B = Pen.fade(0xFF66CC, 0xFFFFFF, 1.6);
    private static final Pen PLANET = Pen.dust(0xE8B860, 2.0);
    private static final Pen BAND = Pen.dust(0xB07A30, 1.4);
    private static final Pen RING_A = Pen.dust(0xE8D8A8, 1.3);
    private static final Pen RING_B = Pen.dust(0xB89868, 1.3);
    private static final Pen SUN = Pen.dust(0xFFE040, 2.4);
    private static final Pen ORBIT = Pen.dust(0x6070A0, 0.6);
    private static final Pen ORB = Pen.dust(0x8090C0, 0.6);
    private static final Pen NUC = Pen.of(Particle.END_ROD);
    private static final Pen METEOR_TAIL = Pen.fade(0xFFA030, 0x504040, 1.4);

    public static void register(Map<String, Renderer> m) {
        m.put("supernova", SpaceFx::supernova);
        m.put("galactic_vortex", SpaceFx::galacticVortex);
        m.put("black_hole", SpaceFx::blackHole);
        m.put("galaxy_collision", SpaceFx::galaxyCollision);
        m.put("nebula", SpaceFx::nebula);
        m.put("saturn_rings", SpaceFx::saturn);
        m.put("meteor_shower", SpaceFx::meteorShower);
        m.put("comet", SpaceFx::comet);
        m.put("planetary_system", SpaceFx::planetary);
        m.put("atom", SpaceFx::atom);
    }

    // 1. Сверхновая
    static void supernova(Frame f) {
        double R = f.r, c = f.h * 0.45, t = f.t;
        if (t < 0.42) {
            double k = f.ph(0, 0.42);
            sphere(f, CORE, 0, c, 0, R * (0.05 + 0.03 * sin(f.tick * 0.9) + 0.05 * k), f.n(18), f.spin(1.5));
            double rr = R * (0.7 - 0.55 * easeIn(k));
            ringT(f, HOT, 0, c, 0, rr, f.n(36), f.spin(-2), 0.6, f.spin(0.3));
            ringT(f, HOT, 0, c, 0, rr * 0.8, f.n(30), f.spin(2), -0.9, f.spin(0.5));
        } else if (t < 0.75) {
            double k = f.ph(0.42, 0.75);
            sphere(f, CORE, 0, c, 0, R * 0.11 * (1 - 0.4 * k), f.n(14), f.spin(1));
            for (int i = 0; i < 3; i++) {
                double kk = easeOut(f.ph(0.42 + i * 0.07, 0.75));
                if (kk > 0.001) sphere(f, SHELL[i], 0, c, 0, R * (0.15 + 0.65 * kk), f.n(52), f.spin(0.4 * (i + 1)));
            }
        } else {
            if (f.at(0.75)) {
                burst(f, Particle.FIREWORK, 0, c, 0, f.n(140), 1.3);
                burst(f, Particle.FLAME, 0, c, 0, f.n(80), 0.9);
                f.p(Particle.EXPLOSION_EMITTER, 0, c, 0);
            }
            double k = easeOut(f.ph(0.75, 1.0));
            sphere(f, FADE, 0, c, 0, R * (0.8 + 0.9 * k), f.n(80), f.spin(0.2));
            for (int j = 0; j < 10; j++) {
                double[] d = fib(j, 10, 0.7);
                ray(f, CORE, 0, c, 0, d[0], d[1], d[2], R * 0.8 * k, R * (0.9 + 1.6 * k), f.n(6));
            }
        }
    }

    // 2. Галактический вихрь
    static void galacticVortex(Frame f) {
        double R = f.r, c = f.h * 0.5, t = f.t;
        double open = smooth(f.ph(0.5, 0.64));
        double close = easeIn(f.ph(0.85, 1));
        double wind = lerp(3.2, 0.9, open);
        double grow = (0.55 + 0.65 * open) * (1 - 0.92 * close);
        int arms = 5, m = f.n(34);
        for (int a = 0; a < arms; a++) {
            for (int j = 1; j <= m; j++) {
                double s = (double) j / m;
                double rad = R * s * grow;
                double ang = a * TWO_PI / arms + wind * s * TWO_PI + f.spin(0.35) * (1.4 - s);
                f.d(cos(ang) * rad, c + sin(s * 9 + a) * R * 0.04 * s, sin(ang) * rad,
                        Colors.hue(0.55 + 0.25 * s + 0.02 * a), 1.1 + 0.5 * (1 - s));
            }
        }
        sphere(f, CORE, 0, c, 0, R * 0.1 * grow, f.n(14), f.spin(2));
        if (open > 0 && open < 1) ring(f, Pen.dust(0xFFFFFF, 1.4), 0, c, 0, R * 1.8 * open, f.n(60), 0);
        if (f.at(0.5)) burst(f, Particle.END_ROD, 0, c, 0, f.n(90), 0.7);
        if (f.at(0.85)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(100), 1.0);
    }

    // 3. Чёрная дыра
    static void blackHole(Frame f) {
        double R = f.r, c = f.h * 0.5, t = f.t;
        if (t < 0.8) {
            double scale = 1 - 0.95 * easeIn(f.ph(0.7, 0.8));
            int m = f.n(26);
            for (int a = 0; a < 4; a++) {
                for (int j = 0; j < m; j++) {
                    double u = ((double) j / m + t * 1.6 + a * 0.25) % 1.0;
                    double rad = R * pow(1 - u, 1.6) * scale + 0.15;
                    double ang = a * TWO_PI / 4 + 2.2 / (0.2 + rad / R) + f.spin(0.3);
                    double k = clamp01(1 - rad / R);
                    f.d(cos(ang) * rad, c + (rad / R) * 0.2 * R * sin(ang * 2 + a), sin(ang) * rad,
                            Colors.mix(0xFF6A00, 0xB040FF, k), 1.0 + 1.2 * k);
                }
            }
            ring(f, ACC, 0, c, 0, R * 0.16 * scale + 0.3, f.n(30), f.spin(2));
            sphere(f, VOID, 0, c, 0, R * 0.09 * scale + 0.1, f.n(22), 0);
        } else {
            if (f.at(0.8)) {
                burst(f, Particle.END_ROD, 0, c, 0, f.n(140), 1.4);
                burst(f, Particle.FIREWORK, 0, c, 0, f.n(60), 1.0);
            }
            double k = easeOut(f.ph(0.8, 1));
            for (int s = -1; s <= 1; s += 2) line(f, JET, 0, c, 0, 0, c + s * R * 1.6 * k, 0, f.n(30));
            ring(f, JET, 0, c, 0, R * 1.6 * k, f.n(70), 0);
            sphere(f, VOID, 0, c, 0, R * 0.1 * (1 - k) + 0.05, f.n(12), 0);
        }
    }

    // 4. Столкновение галактик
    static void galaxyCollision(Frame f) {
        double R = f.r, c = f.h * 0.5, t = f.t;
        if (t < 0.6) {
            double approach = easeIn(f.ph(0, 0.6));
            double D = R * (1.5 - 1.3 * approach);
            for (int g = 0; g < 2; g++) {
                double sx = g == 0 ? -D : D;
                double dir = g == 0 ? 1 : -1;
                int m = f.n(22);
                for (int arm = 0; arm < 2; arm++) {
                    for (int j = 1; j <= m; j++) {
                        double s = (double) j / m, rad = R * 0.55 * s;
                        double ang = arm * PI + s * TWO_PI * 1.1 + f.spin(0.45) * dir;
                        f.d(sx + cos(ang) * rad, c + (g == 0 ? 0.15 : -0.15) * R * s * sin(ang), sin(ang) * rad,
                                Colors.hue((g == 0 ? 0.58 : 0.92) + 0.1 * s), 1.3);
                    }
                }
                sphere(f, CORE, sx, c, 0, R * 0.07, f.n(8), 0);
            }
        } else {
            if (f.at(0.6)) {
                burst(f, Particle.FIREWORK, 0, c, 0, f.n(150), 1.2);
                burst(f, Particle.END_ROD, 0, c, 0, f.n(60), 0.7);
            }
            double k = easeOut(f.ph(0.6, 1));
            for (int i = 0; i < 8; i++) {
                double yaw = i * PI / 8 + f.spin(0.1);
                double rad = R * (0.3 + 1.5 * k);
                double a0 = (i % 2 == 0) ? 0.1 : PI + 0.1;
                arcV(f, (i % 2 == 0) ? ARC_A : ARC_B, 0, c, 0, rad, f.n(26), a0, a0 + PI - 0.2, yaw);
            }
            sphere(f, CORE, 0, c, 0, R * 0.12 * (1 - k), f.n(10), 0);
        }
    }

    // 5. Космическая туманность
    static void nebula(Frame f) {
        double R = f.r, c = f.h * 0.5, t = f.t;
        double[] hues = {0.85, 0.6, 0.08, 0.45};
        for (int i = 0; i < 4; i++) {
            double ang = i * TWO_PI / 4 + f.spin(0.05), dist = R * 0.55;
            double cx = cos(ang) * dist, cz = sin(ang) * dist;
            double cy = c + sin(f.tick * 0.03 + i * 1.7) * R * 0.15;
            double ti = 0.18 + i * 0.18;
            double fl = exp(-abs(t - ti) * 28);
            Random rr = new Random(500 + i);
            int m = f.n(50);
            for (int j = 0; j < m; j++) {
                double gx = rr.nextGaussian() * R * 0.28, gy = rr.nextGaussian() * R * 0.2,
                        gz = rr.nextGaussian() * R * 0.28;
                double wob = sin(f.tick * 0.05 + j) * 0.15;
                f.d(cx + gx + wob, cy + gy, cz + gz, Colors.hue(hues[i] + 0.06 * rr.nextDouble()),
                        1.2 + 1.6 * fl + rr.nextDouble() * 0.8);
            }
            if (fl > 0.4) sphere(f, CORE, cx, cy, cz, R * 0.25 * fl, f.n(8), 0);
            if (f.at(ti)) burst(f, Particle.END_ROD, cx, cy, cz, f.n(40), 0.5);
        }
        for (int s = 0; s < f.n(10); s++) {
            f.p(Particle.END_ROD, (f.rnd.nextDouble() - 0.5) * R * 2.4, c + (f.rnd.nextDouble() - 0.5) * R * 1.6,
                    (f.rnd.nextDouble() - 0.5) * R * 2.4);
        }
        if (f.at(0.92)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(140), 0.9);
    }

    // 6. Кольца Сатурна
    static void saturn(Frame f) {
        double R = f.r, c = f.h * 0.5;
        double grow = easeOut(f.ph(0, 0.5));
        double out = easeIn(f.ph(0.88, 1));
        double tilt = lerp(0.3, 1.2, smooth(f.ph(0.55, 0.63))) + 0.08 * sin(f.spin(0.5));
        double az = f.spin(0.08);
        double pr = R * 0.32 * (0.3 + 0.7 * grow);
        sphere(f, PLANET, 0, c, 0, pr, f.n(50), f.spin(0.4));
        ringT(f, BAND, 0, c, 0, pr, f.n(30), f.spin(0.4), 0.0, 0);
        double[] rr = {0.62, 0.74, 0.88, 1.0};
        for (int i = 0; i < rr.length; i++) {
            double rad = R * rr[i] * (0.2 + 0.8 * grow) * (1 + 1.2 * out);
            ringT(f, i % 2 == 0 ? RING_A : RING_B, 0, c, 0, rad, f.n(54), f.spin(i % 2 == 0 ? 0.2 : -0.2), tilt, az);
        }
        if (f.at(0.55)) burst(f, Particle.END_ROD, 0, c, 0, f.n(70), 0.8);
        if (f.at(0.88)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(90), 0.9);
    }

    // 7. Метеоритный дождь
    static void meteorShower(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        for (int i = 0; i < 8; i++) {
            double s0 = 0.04 + i * 0.075, len = 0.3;
            double u = (t - s0) / len;
            Random rr = new Random(900 + i);
            double az = rr.nextDouble() * TWO_PI, d1 = R * (0.3 + 0.7 * rr.nextDouble());
            double sx = cos(az + 0.8) * R * 1.5, sz = sin(az + 0.8) * R * 1.5, sy = H * 1.2;
            double ex = cos(az) * d1, ez = sin(az) * d1;
            if (u >= 0 && u <= 1) {
                for (int k = 0; k < 12; k++) {
                    double v = u - k * 0.04;
                    if (v < 0) break;
                    double x = lerp(sx, ex, v), y = lerp(sy, 0, v), z = lerp(sz, ez, v);
                    if (k < 2) {
                        f.p(Particle.FLAME, x, y, z);
                        f.p(Particle.END_ROD, x, y, z);
                    } else {
                        f.dt(x, y, z, 0xFFA030, 0x504040, 1.6 - k * 0.08);
                    }
                }
            }
            if (f.at(s0 + len)) burst(f, Particle.FLAME, ex, 0.2, ez, f.n(24), 0.35);
        }
        if (t >= 0.7 && t < 0.9) {
            double big = f.ph(0.7, 0.9);
            double y = H * 1.6 * (1 - easeIn(big));
            sphere(f, FLM, 0, y, 0, R * 0.12, f.n(18), f.spin(2));
            for (int k = 1; k < 20; k++) {
                double yy = y + k * H * 0.05, rad = R * 0.03 * k, ang = k * 0.9 + f.spin(2);
                f.p(Particle.FLAME, cos(ang) * rad, yy, sin(ang) * rad);
                if (k % 2 == 0) f.p(Particle.SMOKE, cos(ang + PI) * rad, yy, sin(ang + PI) * rad);
            }
        }
        if (f.at(0.9)) {
            burst(f, Particle.FLAME, 0, 0.5, 0, f.n(160), 1.1);
            f.p(Particle.EXPLOSION_EMITTER, 0, 0.5, 0);
        }
        if (t >= 0.9) {
            double k = easeOut(f.ph(0.9, 1));
            ring(f, METEOR_TAIL, 0, 0.3, 0, R * 2 * k, f.n(90), 0);
        }
    }

    // 8. Комета
    private static double[] cometPos(double R, double H, double a) {
        return new double[]{R * 1.2 * cos(a), H * 0.5 + H * 0.18 * sin(2 * a), R * 0.6 * sin(a)};
    }

    static void comet(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double open = smooth(f.ph(0.6, 0.7));
        double fade = 1 - easeIn(f.ph(0.93, 1));
        double a = -2.4 + t * TWO_PI * 0.9;
        double[] hd = cometPos(R, H, a);
        sphere(f, CORE, hd[0], hd[1], hd[2], R * 0.04 * fade + 0.05, f.n(10), f.spin(3));
        f.p(Particle.FLAME, hd[0], hd[1], hd[2]);
        int tail = 34;
        for (int k = 1; k <= tail; k++) {
            double ak = a - k * 0.045;
            double[] p = cometPos(R, H, ak);
            double rad = (k / (double) tail) * R * 0.14 * (1 + 4 * open);
            for (int s = 0; s < 2; s++) {
                double ph = k * 0.55 + f.spin(1.5) + s * PI;
                double ox = cos(ph) * rad * cos(ak), oy = sin(ph) * rad, oz = cos(ph) * rad * sin(ak);
                f.d(p[0] + ox, p[1] + oy, p[2] + oz, Colors.mix(0xFFFFFF, 0x3060FF, k / (double) tail),
                        max(0.5, 1.5 - k * 0.025));
            }
        }
        if (f.at(0.6)) burst(f, Particle.END_ROD, hd[0], hd[1], hd[2], f.n(70), 0.5);
        if (f.at(0.95)) burst(f, Particle.FIREWORK, hd[0], hd[1], hd[2], f.n(60), 0.6);
    }

    // 9. Планетарная система
    static void planetary(Frame f) {
        double R = f.r, c = f.h * 0.5, t = f.t;
        double m = 1 + 0.6 * smooth(f.ph(0.75, 0.8)) - 0.92 * easeIn(f.ph(0.9, 1));
        sphere(f, SUN, 0, c, 0, R * 0.12 * (1 + 0.08 * sin(f.tick * 0.5)), f.n(30), f.spin(0.3));
        sphere(f, FLM, 0, c, 0, R * 0.15, f.n(8), f.spin(1));
        double[] fr = {0.25, 0.4, 0.55, 0.7, 0.85, 1.0};
        int[] cols = {0xB0B0B0, 0xE8C070, 0x3070FF, 0xFF5030, 0xE0A060, 0x80D0FF};
        for (int i = 0; i < fr.length; i++) {
            double rad = R * fr[i] * m;
            double tilt = (i % 3) * 0.18 - 0.1, az = i * 1.1;
            ringT(f, ORBIT, 0, c, 0, rad, f.n(30), 0, tilt, az);
            double ang = f.spin(0.18 / pow(fr[i], 1.5));
            double[] p = orbit(rad, ang, tilt, az);
            sphere(f, Pen.dust(cols[i], 1.8), p[0], c + p[1], p[2], R * (i == 5 ? 0.06 : 0.04), f.n(8), f.spin(1));
            if (i == 2) {
                double ma = f.spin(1.2);
                f.d(p[0] + cos(ma) * R * 0.09, c + p[1] + sin(ma * 1.3) * R * 0.02, p[2] + sin(ma) * R * 0.09,
                        0xFFFFFF, 1.4);
            }
            if (i == 4) ringT(f, RING_A, p[0], c + p[1], p[2], R * 0.09, f.n(14), f.spin(0.5), 0.5, 0.2);
            if (t >= 0.75 && t < 0.95) {
                for (int k = 1; k <= 5; k++) {
                    double[] q = orbit(rad, ang - k * 0.1, tilt, az);
                    f.d(q[0], c + q[1], q[2], cols[i], 1.4 - k * 0.2);
                }
            }
        }
        if (f.at(0.75)) burst(f, Particle.END_ROD, 0, c, 0, f.n(90), 0.9);
        if (f.at(0.98)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(50), 0.6);
    }

    // 10. Атом
    static void atom(Frame f) {
        double R = f.r, c = f.h * 0.5;
        double pulse = 1 + 0.4 * sin(PI * f.ph(0.7, 0.82));
        double collapse = 1 - 0.88 * easeIn(f.ph(0.92, 1));
        double mult = pulse * collapse;
        for (int i = 0; i < 10; i++) {
            double[] d = fib(i, 10, f.spin(0.8));
            f.d(d[0] * R * 0.1, c + d[1] * R * 0.1, d[2] * R * 0.1, (i % 2 == 0) ? 0xFF3030 : 0x3080FF, 2.2);
        }
        sphere(f, NUC, 0, c, 0, R * 0.17, f.n(10), f.spin(-1.2));
        double[] rad = {0.75, 0.9, 1.0};
        for (int o = 0; o < 3; o++) {
            double tilt = 1.05, az = o * TWO_PI / 3, rr = R * rad[o] * mult;
            ringT(f, ORB, 0, c, 0, rr, f.n(34), 0, tilt, az);
            for (int e = 0; e < 2; e++) {
                double w = 0.45 + 0.12 * o;
                double base = f.spin(w) + e * PI + o * 1.3 + TWO_PI * 3 * easeIn(f.ph(0.7, 0.9));
                for (int k = 0; k < 14; k++) {
                    double[] p = orbit(rr, base - k * 0.075, tilt, az);
                    if (k == 0) {
                        f.d(p[0], c + p[1], p[2], 0x66E0FF, 2.6);
                        f.p(Particle.END_ROD, p[0], c + p[1], p[2]);
                    } else {
                        f.dt(p[0], c + p[1], p[2], 0x80F0FF, 0x2040FF, max(0.5, 1.7 - k * 0.1));
                    }
                }
            }
        }
        if (f.at(0.7)) burst(f, Particle.END_ROD, 0, c, 0, f.n(80), 0.8);
        if (f.at(0.98)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(80), 0.9);
    }
}
