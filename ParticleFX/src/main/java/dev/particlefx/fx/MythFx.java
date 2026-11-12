package dev.particlefx.fx;

import static dev.particlefx.fx.Shapes.*;
import static java.lang.Math.*;

import java.util.Map;
import java.util.Random;
import org.bukkit.Particle;

/** Мифические и магические анимации (21–30). */
public final class MythFx {
    private MythFx() {}

    private static final Pen FLM = Pen.of(Particle.FLAME);
    private static final Pen CORE = Pen.of(Particle.END_ROD);
    private static final Pen FA = Pen.dust(0xFF5A10, 1.5);
    private static final Pen FB = Pen.dust(0xFFC030, 1.5);
    private static final Pen GA = Pen.dust(0xFFE070, 1.6);
    private static final Pen GB = Pen.dust(0xFFFFFF, 1.5);
    private static final Pen ICEP = Pen.dust(0xAAE8FF, 1.7);
    private static final Pen BLACK = Pen.dust(0x050505, 3.2);
    private static final Pen GOLD = Pen.dust(0xFFD060, 1.5);
    private static final Pen PALE = Pen.dust(0xFFF4C8, 1.4);
    private static final Pen SILV = Pen.dust(0xDDE8FF, 1.5);
    private static final Pen BLUE = Pen.dust(0x7090FF, 1.5);
    private static final Pen MAG = Pen.dust(0xC040FF, 1.5);
    private static final Pen RUNE = Pen.dust(0x66CCFF, 1.5);
    private static final Pen[] ESH = {Pen.dust(0x66EEFF, 1.6), Pen.dust(0xFFFFFF, 1.5), Pen.dust(0xAA66FF, 1.7)};

    public static void register(Map<String, Renderer> m) {
        m.put("phoenix", MythFx::phoenix);
        m.put("fire_meteor", MythFx::fireMeteor);
        m.put("ice_crown", MythFx::iceCrown);
        m.put("solar_eclipse", MythFx::eclipse);
        m.put("moon_portal", MythFx::moonPortal);
        m.put("magic_portal", MythFx::magicPortal);
        m.put("rune", MythFx::rune);
        m.put("magic_circle", MythFx::magicCircle);
        m.put("energy_sphere", MythFx::energySphere);
        m.put("energy_beam", MythFx::energyBeam);
    }

    // 21. Феникс
    private static void wings(Frame f, double c, double sc, double w, double flap, Pen a, Pen b, int per) {
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 9; i++) {
                double fr = i / 8.0;
                double ang = lerp(1.55, -0.35 + 1.5 * fr, w) + flap;
                double len = sc * (0.8 + 0.8 * (1 - abs(fr - 0.45) * 1.4));
                lineL(f, i % 2 == 0 ? a : b, side * 0.25 * sc, c, 0, side * (0.25 * sc + cos(ang) * len),
                        c + sin(ang) * len, -0.1 * sc * fr, per);
            }
        }
    }

    static void phoenix(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5;
        double flap = 0.12 * sin(f.spin(1.2) * 2);
        if (t < 0.62) {
            double open = easeOut(f.ph(0, 0.4)), close = smooth(f.ph(0.5, 0.62));
            double w = open * (1 - close * 0.9);
            wings(f, c, R * 0.5, w, flap, FA, FB, f.n(9));
            lineL(f, FLM, 0, c - R * 0.2, 0, 0, c + R * 0.25, 0, f.n(10));
            sphere(f, CORE, 0, c + R * 0.32, 0, R * 0.06, f.n(6), 0);
            for (int k = 0; k < 4; k++) {
                lineL(f, FB, 0, c - R * 0.2, 0, (k - 1.5) * 0.12 * R, c - R * 0.7, -R * 0.3, f.n(7));
            }
        } else {
            if (f.at(0.62)) {
                burst(f, Particle.FLAME, 0, c, 0, f.n(150), 1.2);
                burst(f, Particle.FIREWORK, 0, c, 0, f.n(80), 1.0);
            }
            double rb = easeOut(f.ph(0.62, 0.78));
            double c2 = c + H * 0.25 * f.ph(0.62, 1);
            double sc = R * (0.2 + 0.55 * easeOut(f.ph(0.62, 0.8)));
            wings(f, c2, sc, rb, flap, GA, GB, f.n(9));
            lineL(f, CORE, 0, c2 - sc * 0.4, 0, 0, c2 + sc * 0.5, 0, f.n(10));
            sphere(f, CORE, 0, c2 + sc * 0.65, 0, sc * 0.12, f.n(6), 0);
            for (int k = 0; k < 4; k++) lineL(f, GA, 0, c2 - sc * 0.4, 0, (k - 1.5) * 0.25 * sc, c2 - sc * 1.4, -sc * 0.6, f.n(8));
            for (int k = 0; k < f.n(20); k++) {
                double ang = k * 0.7 + f.spin(1.0), rr = R * 0.3 * (1 + f.ph(0.62, 1));
                f.p(Particle.FLAME, cos(ang) * rr, c2 + sin(k * 1.3 + f.spin(0.5)) * R * 0.3, sin(ang) * rr);
            }
        }
    }

    // 22. Огненный метеор
    static void fireMeteor(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double u = f.ph(0, 0.55);
        if (t < 0.55) {
            double e = easeIn(u);
            double sy = H * 1.6, sf = -R * 2.2;
            double hy = lerp(sy, 0, e), hf = lerp(sf, 0, e);
            double hx = f.px(0, hf), hz = f.pz(0, hf);
            sphere(f, FLM, hx, hy, hz, R * 0.09 + 0.3, f.n(18), f.spin(2));
            for (int k = 1; k <= 24; k++) {
                double v = max(0, e - k * 0.012);
                double y = lerp(sy, 0, v), fw = lerp(sf, 0, v);
                double ang = k * 0.8 + f.spin(2), rad = 0.1 + k * 0.03;
                double x = f.px(0, fw) + cos(ang) * rad, z = f.pz(0, fw) + sin(ang) * rad;
                if (k % 2 == 0) f.p(Particle.FLAME, x, y, z);
                else f.p(Particle.SMOKE, x, y, z);
            }
        } else {
            if (f.at(0.55)) {
                burst(f, Particle.FLAME, 0, 0.5, 0, f.n(200), 1.2);
                f.p(Particle.EXPLOSION_EMITTER, 0, 0.5, 0);
            }
            double k = easeOut(f.ph(0.55, 0.9)), fade = 1 - easeIn(f.ph(0.9, 1));
            ring(f, Pen.fade(0xFFAA30, 0x303030, 2.0), 0, 0.3, 0, R * 1.8 * k, f.n(90), 0);
            dome(f, Pen.fade(0xFF6A10, 0x602010, 1.8), 0, 0, 0, R * (0.2 + 1.2 * k) * fade, f.n(80), f.spin(0.3));
            for (int i = 0; i < 6; i++) {
                double a = i * TWO_PI / 6 + f.spin(0.2);
                line(f, FLM, cos(a) * R * k * 0.8, 0, sin(a) * R * k * 0.8, cos(a) * R * k * 0.8, R * 0.5 * (1 - k) * fade + 0.2,
                        sin(a) * R * k * 0.8, f.n(6));
            }
        }
    }

    // 23. Ледяная корона
    static void iceCrown(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        int N = 12;
        double out = easeOut(f.ph(0.55, 0.62));
        if (t < 0.85) {
            double br = R * (1 + 0.5 * out);
            ring(f, ICEP, 0, 0.2, 0, br, f.n(48), f.spin(0.1));
            for (int i = 0; i < N; i++) {
                double g = easeOut(f.ph(0.04 + 0.035 * i, 0.22 + 0.035 * i));
                if (g <= 0.01) continue;
                double a = i * TWO_PI / N, tall = H * ((i % 2 == 0) ? 1.0 : 0.6) * g;
                double bx = cos(a) * br, bz = sin(a) * br, lean = out * tall * 0.6;
                double tx = bx + cos(a) * lean, tz = bz + sin(a) * lean, ty = tall * (1 - 0.3 * out);
                for (int k = 0; k < 3; k++) {
                    double o = (k - 1) * R * 0.08;
                    line(f, ICEP, bx - sin(a) * o, 0.2, bz + cos(a) * o, tx, ty, tz, f.n(8));
                }
                f.p(Particle.END_ROD, tx, ty, tz);
                if (i % 3 == 0) f.p(Particle.SNOWFLAKE, tx, ty + 0.3, tz);
            }
            if (f.at(0.55)) burst(f, Particle.SNOWFLAKE, 0, H * 0.3, 0, f.n(100), 0.8);
        } else {
            if (f.at(0.85)) {
                burst(f, Particle.SNOWFLAKE, 0, H * 0.4, 0, f.n(120), 1.0);
                burst(f, Particle.CLOUD, 0, H * 0.3, 0, f.n(40), 0.5);
            }
            double age = f.ph(0.85, 1);
            Pen shard = Pen.block(org.bukkit.Material.ICE);
            for (int k = 0; k < 30; k++) {
                double a = k * TWO_PI / 30, rad = R * 1.5 * (1 + 2 * easeOut(age));
                shard.draw(f, cos(a) * rad, max(0.2, H * 0.5 * (1 - age) + sin(k) * 0.5), sin(a) * rad);
            }
        }
    }

    // 24. Солнечное затмение
    static void eclipse(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5;
        double col = 1 - easeIn(f.ph(0.88, 1)), pop = easeOut(f.ph(0.7, 0.78));
        sphere(f, BLACK, 0, c, 0, R * 0.3 * col, f.n(34), 0);
        double[] rr = {0.38, 0.5, 0.65, 0.85};
        for (int i = 0; i < 4; i++) {
            double rad = R * rr[i] * (1 + 0.8 * pop * (i / 3.0 + 0.3)) * col;
            ringF(f, i % 2 == 0 ? GOLD : PALE, 0, c, 0, rad, f.n(46), f.spin(i % 2 == 0 ? 0.3 : -0.4));
        }
        for (int i = 0; i < 24; i++) {
            double a = i * TWO_PI / 24 + f.spin(0.05), len = R * (0.45 + 0.2 * sin(f.tick * 0.3 + i * 2.1)) * (1 + pop) * col;
            lineL(f, GOLD, cos(a) * R * 0.34 * col, c + sin(a) * R * 0.34 * col, 0, cos(a) * len, c + sin(a) * len, 0, f.n(5));
        }
        double da = f.spin(0.3);
        f.p(Particle.END_ROD, f.px(cos(da) * R * 0.38 * col, 0), c + sin(da) * R * 0.38 * col, f.pz(cos(da) * R * 0.38 * col, 0));
        if (f.at(0.7)) {
            burst(f, Particle.END_ROD, 0, c, 0, f.n(150), 1.2);
            ringF(f, CORE, 0, c, 0, R * 0.4, f.n(70), 0);
        }
    }

    // 25. Лунный портал
    static void moonPortal(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5;
        double open = easeOut(f.ph(0, 0.5)), pulse = 1 + 0.4 * easeOut(f.ph(0.75, 0.8)), col = 1 - easeIn(f.ph(0.9, 1));
        double sc = open * pulse * col;
        int[] sides = {4, 6, 8};
        for (int i = 0; i < 3; i++) {
            starF(f, i % 2 == 0 ? SILV : BLUE, c, R * (0.45 + 0.3 * i) * sc, sides[i], 1,
                    f.spin(0.2) * (i % 2 == 0 ? 1 : -1) + i, f.n(10), 0);
        }
        int nn = f.n(60);
        for (int i = 0; i < nn; i++) {
            double rr = sqrt((i + 0.5) / nn) * R * 0.8 * sc, a = i * 2.39996 + f.spin(0.5) + rr * 0.6;
            f.dt(f.px(cos(a) * rr, 0), c + sin(a) * rr, f.pz(cos(a) * rr, 0), 0xCFE8FF, 0x6080FF, 1.2);
        }
        double mr = R * 0.22 * sc, my = c + R * 1.15 * sc;
        int cn = f.n(40);
        for (int i = 0; i < cn; i++) {
            double a = TWO_PI * i / cn, lx = cos(a) * mr, ly = sin(a) * mr;
            if (hypot(lx - 0.45 * mr, ly) < mr * 0.92) continue;
            f.d(f.px(lx, 0), my + ly, f.pz(lx, 0), 0xF0F4FF, 1.5);
        }
        if (f.at(0.75)) burst(f, Particle.REVERSE_PORTAL, 0, c, 0, f.n(90), 0.9);
    }

    // 26. Магический портал
    static void magicPortal(Frame f) {
        double R = f.r, H = f.h, c = H * 0.45;
        double len = R * 3.2 * easeOut(f.ph(0, 0.4)) * (1 - easeIn(f.ph(0.85, 1)));
        double wide = 1 + 0.8 * smooth(f.ph(0.6, 0.66)), shrink = 1 - 0.95 * easeIn(f.ph(0.85, 1));
        ringF(f, MAG, 0, c, 0, R * wide * shrink, f.n(50), f.spin(0.4));
        int arms = 3, m = f.n(30);
        for (int a = 0; a < arms; a++) {
            for (int j = 1; j <= m; j++) {
                double s = (double) j / m, z = len * s, rad = R * wide * shrink * (1 - 0.65 * s);
                double ang = a * TWO_PI / arms + s * TWO_PI * 1.5 + f.spin(0.9);
                f.d(f.px(cos(ang) * rad, z), c + sin(ang) * rad, f.pz(cos(ang) * rad, z),
                        Colors.mix(0xC040FF, 0x4040FF, s), 1.5);
            }
        }
        for (int k = 0; k < f.n(14); k++) {
            double z = f.rnd.nextDouble() * len, rad = f.rnd.nextDouble() * R * 0.5 * shrink, a = f.rnd.nextDouble() * TWO_PI;
            f.p(Particle.PORTAL, f.px(cos(a) * rad, z), c + sin(a) * rad, f.pz(cos(a) * rad, z));
        }
        if (len > 0.5) sphere(f, CORE, f.px(0, len), c, f.pz(0, len), R * 0.1, f.n(6), f.spin(2));
        if (f.at(0.6)) burst(f, Particle.REVERSE_PORTAL, 0, c, 0, f.n(80), 0.9);
    }

    // 27. Руна
    private static final double[][] GLYPH = {{0, -0.8, 0, 0.8}, {-0.45, 0.5, 0, 0.1}, {0.45, 0.5, 0, 0.1},
            {-0.35, -0.2, 0.35, -0.2}, {-0.5, -0.6, 0, -0.9}, {0.5, -0.6, 0, -0.9}};

    static void rune(Frame f) {
        double R = f.r, t = f.t, y0 = 0.25;
        double expl = easeOut(f.ph(0.7, 1)), sp = 1 + expl * 1.8, rise = expl * R * 0.8;
        double glow = (t >= 0.6 && t < 0.7) ? 1.4 + abs(sin(f.tick * 0.8)) : 1.5;
        Pen pen = t >= 0.7 ? Pen.fade(0x66CCFF, 0xFFFFFF, max(0.4, 1.5 * (1 - expl))) : Pen.dust(0x66CCFF, glow);
        double y = y0 + rise;
        double circ = f.ph(0, 0.25);
        int n = f.n(70);
        for (int i = 0; i < n; i++) {
            double u = (double) i / n;
            if (u > circ) break;
            double a = u * TWO_PI;
            pen.draw(f, cos(a) * R * sp, y, sin(a) * R * sp);
        }
        starP(f, pen, 0, y, 0, R * 0.95 * sp, 3, 1, f.spin(0.05), f.n(14), f.ph(0.2, 0.45));
        starP(f, pen, 0, y, 0, R * 0.95 * sp, 3, 1, f.spin(0.05) + PI, f.n(14), f.ph(0.4, 0.6));
        double gs = R * 0.55 * sp;
        for (int i = 0; i < GLYPH.length; i++) {
            double fr = f.ph(0.5 + 0.02 * i, 0.6 + 0.02 * i);
            if (fr <= 0) continue;
            double[] g = GLYPH[i];
            line(f, pen, g[0] * gs, y, g[1] * gs, (g[0] + (g[2] - g[0]) * fr) * gs, y, (g[1] + (g[3] - g[1]) * fr) * gs,
                    max(2, (int) round(f.n(10) * fr)));
        }
        if (t < 0.7 && f.tick % 2 == 0) {
            double a = f.rnd.nextDouble() * TWO_PI, rr = f.rnd.nextDouble() * R * circ;
            f.v(Particle.END_ROD, cos(a) * rr, y, sin(a) * rr, 0, 1, 0, 0.1);
        }
        if (f.at(0.7)) {
            burst(f, Particle.END_ROD, 0, 0.5, 0, f.n(120), 1.1);
            burst(f, Particle.FIREWORK, 0, 0.5, 0, f.n(60), 0.8);
        }
    }

    // 28. Магический круг
    static void magicCircle(Frame f) {
        double R = f.r, t = f.t, y = 0.15;
        double in = easeOut(f.ph(0, 0.2)), jump = easeOut(f.ph(0.65, 0.72));
        double k = in * (1 + 0.4 * jump) * (1 - 0.9 * easeIn(f.ph(0.9, 1)));
        ring(f, RUNE, 0, y, 0, R * k, f.n(60), f.spin(0.1));
        ring(f, MAG, 0, y, 0, R * 0.86 * k, f.n(52), f.spin(-0.12));
        for (int i = 0; i < 24; i++) {
            double a = i * TWO_PI / 24 + f.spin(-0.1);
            line(f, GOLD, cos(a) * R * 0.88 * k, y, sin(a) * R * 0.88 * k, cos(a) * R * 0.95 * k, y, sin(a) * R * 0.95 * k, 3);
        }
        starP(f, RUNE, 0, y, 0, R * 0.8 * k, 6, 2, f.spin(0.15), f.n(12), 1);
        starP(f, MAG, 0, y, 0, R * 0.5 * k, 5, 2, -f.spin(0.2), f.n(10), 1);
        for (int v = 0; v < 6; v++) {
            double a = f.spin(0.15) + v * TWO_PI / 6;
            ring(f, GOLD, cos(a) * R * 0.8 * k, y, sin(a) * R * 0.8 * k, R * 0.1 * k, f.n(10), f.spin(0.5));
        }
        ring(f, CORE, 0, y, 0, R * 0.12 * k, f.n(10), f.spin(-1));
        if (f.at(0.65)) burst(f, Particle.END_ROD, 0, 0.4, 0, f.n(100), 1.0);
        double pk = easeOut(f.ph(0.65, 0.8));
        if (t >= 0.65 && t < 0.9) {
            double dec = 1 - easeIn(f.ph(0.8, 0.9));
            for (int v = 0; v < 6; v++) {
                double a = f.spin(0.15) + v * TWO_PI / 6;
                line(f, CORE, cos(a) * R * 0.8 * k, y, sin(a) * R * 0.8 * k, cos(a) * R * 0.8 * k, y + R * 0.9 * pk * dec,
                        sin(a) * R * 0.8 * k, f.n(14));
            }
        }
    }

    // 29. Энергетическая сфера
    static void energySphere(Frame f) {
        double R = f.r, H = f.h, c = 1.2 + H * 0.2;
        double conv = easeOut(f.ph(0, 0.5)), bk = easeOut(f.ph(0.65, 0.85)), fade = 1 - easeIn(f.ph(0.92, 1));
        for (int i = 0; i < 3; i++) {
            double rad = R * (0.75 + 0.12 * i) + (1 - conv) * R * 1.4 + bk * R * 3;
            sphere(f, ESH[i], 0, c, 0, rad * fade, f.n(52 * fade + 2), f.spin(i % 2 == 0 ? 0.5 : -0.7));
        }
        sphere(f, CORE, 0, c, 0, R * 0.3 * (0.3 + 0.7 * conv) * (1 + 0.15 * sin(f.tick * 0.7)), f.n(14), f.spin(1.5));
        if (conv < 1) {
            for (int k = 0; k < 10; k++) {
                double[] d = fib(k, 10, f.spin(0.2));
                ray(f, CORE, 0, c, 0, d[0], d[1], d[2], R * 0.9 + (1 - conv) * R * 0.4, R * 0.9 + (1 - conv) * R * 1.4, 3);
            }
        }
        if (bk > 0 && bk < 1) ring(f, CORE, 0, c, 0, R * (0.9 + 3 * bk), f.n(60), 0);
        if (f.at(0.65)) burst(f, Particle.END_ROD, 0, c, 0, f.n(120), 1.3);
    }

    // 30. Энергетический луч
    static void energyBeam(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double conv = easeOut(f.ph(0, 0.5)), col = 1 - easeIn(f.ph(0.88, 1));
        double rad = lerp(R, 0.18, conv);
        int m = f.n(30);
        for (int a = 0; a < 4; a++) {
            for (int j = 0; j < m; j++) {
                double s = (double) j / (m - 1), ang = a * TWO_PI / 4 + s * TWO_PI * 3 + f.spin(2) * (1 + conv);
                double rr = rad * (0.5 + 0.5 * (1 - s * 0.3));
                f.dt(cos(ang) * rr, s * H * col, sin(ang) * rr, 0x66EEFF, 0xAA66FF, 1.4);
            }
        }
        double bh = H * easeOut(f.ph(0.45, 0.6)) * col;
        if (bh > 0.1) {
            line(f, CORE, 0, 0, 0, 0, bh, 0, f.n(30));
            ring(f, Pen.dust(0xFFFFFF, 2.0), 0, bh * 0.5, 0, 0.3 + 0.2 * sin(f.tick), f.n(8), 0);
        }
        ring(f, Pen.fade(0x66EEFF, 0xAA66FF, 1.4), 0, 0.2, 0, R * 0.6 * (1 - conv * 0.5), f.n(36), f.spin(1));
        if (f.at(0.65)) burst(f, Particle.END_ROD, 0, 0.5, 0, f.n(120), 1.4);
        double sw = f.ph(0.65, 0.95);
        if (sw > 0 && sw < 1) ring(f, Pen.fade(0xFFFFFF, 0x66AAFF, 2.2), 0, 0.3, 0, R * 5 * easeOut(sw), f.n(90), 0);
    }
}
