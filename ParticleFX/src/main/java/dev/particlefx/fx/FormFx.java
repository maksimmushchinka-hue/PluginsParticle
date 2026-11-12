package dev.particlefx.fx;

import static dev.particlefx.fx.Shapes.*;
import static java.lang.Math.*;

import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Particle;

/** Формы, кристаллы и геометрия (31–40). */
public final class FormFx {
    private FormFx() {}

    private static final Pen CORE = Pen.of(Particle.END_ROD);
    private static final Pen SHIELD = Pen.dust(0x60E0FF, 1.2);
    private static final Pen CR1 = Pen.dust(0xC080FF, 1.5);
    private static final Pen CR2 = Pen.dust(0x80F0FF, 1.5);
    private static final Pen EDGE = Pen.dust(0x80E0FF, 1.4);
    private static final Pen FACE = Pen.dust(0xFFB0FF, 1.4);
    private static final Pen T1 = Pen.dust(0xFF8030, 1.3);
    private static final Pen T2 = Pen.dust(0x40C0FF, 1.3);
    private static final Pen GLASS = Pen.dust(0xBFE8FF, 1.1);
    private static final Pen SAND = Pen.dust(0xF0C860, 1.3);

    public static void register(Map<String, Renderer> m) {
        m.put("pulse_shield", FormFx::shield);
        m.put("crystal", FormFx::crystal);
        m.put("crystal_burst", FormFx::crystalBurst);
        m.put("sandstorm", FormFx::sandstorm);
        m.put("star_rain", FormFx::starRain);
        m.put("spiral_galaxy", FormFx::spiralGalaxy);
        m.put("cube", FormFx::cubeFx);
        m.put("tesseract", FormFx::tesseract);
        m.put("hourglass", FormFx::hourglass);
        m.put("dna_helix", FormFx::dna);
    }

    // 31. Пульсирующий щит
    static void shield(Frame f) {
        double R = f.r, t = f.t;
        double grow = easeOut(f.ph(0, 0.12)), big = easeOut(f.ph(0.7, 0.76)), col = 1 - easeIn(f.ph(0.92, 1));
        double rad = R * (1 + 0.04 * sin(f.tick * 0.4)) * grow * col * (1 + 0.3 * big * (1 - f.ph(0.76, 0.95)));
        for (int j = 1; j <= 5; j++) {
            double lat = j * (PI / 2) / 6;
            ring(f, SHIELD, 0, sin(lat) * rad, 0, cos(lat) * rad, f.n(8 + 12 * cos(lat)), f.spin(0.1 * (j % 2 == 0 ? 1 : -1)));
        }
        for (int k = 0; k < 4; k++) arcV(f, SHIELD, 0, 0, 0, rad, f.n(16), 0, PI, k * PI / 4 + f.spin(0.05));
        for (int p = 0; p < 2; p++) {
            double q = ((t * 3.5) + p * 0.5) % 1.0, lat = q * (PI / 2);
            ring(f, CORE, 0, sin(lat) * rad, 0, cos(lat) * rad, f.n(36), 0);
            ring(f, Pen.fade(0x60E0FF, 0xFFFFFF, 1.4), 0, 0.15, 0, rad * (1 + 2 * q), f.n(48), 0);
        }
        double w = f.ph(0.7, 0.9);
        if (w > 0 && w < 1) ring(f, Pen.fade(0xFFFFFF, 0x60A0FF, 2.0), 0, 0.2, 0, R * (1 + 4 * easeOut(w)), f.n(80), 0);
        if (f.at(0.7)) flatBurst(f, Particle.END_ROD, 0, 0.5, 0, f.n(100), 1.2);
    }

    // 32. Кристалл
    private static void edge(Frame f, Pen p, double c, double[] a, double[] b, double disp, int n) {
        double mx = (a[0] + b[0]) / 2, my = (a[1] + b[1]) / 2 - c, mz = (a[2] + b[2]) / 2;
        double l = sqrt(mx * mx + my * my + mz * mz);
        double dx = 0, dy = 0, dz = 0;
        if (l > 1.0e-6) { dx = mx / l * disp; dy = my / l * disp; dz = mz / l * disp; }
        line(f, p, a[0] + dx, a[1] + dy, a[2] + dz, b[0] + dx, b[1] + dy, b[2] + dz, n);
    }

    static void crystal(Frame f) {
        double R = f.r, H = f.h, c = H * 0.5;
        double g = easeOut(f.ph(0, 0.5)) * (1 + 0.5 * easeOut(f.ph(0.7, 0.76)));
        double shat = easeOut(f.ph(0.86, 1)) * R * 2.5;
        double rot = f.spin(0.25), hh = H * 0.45 * g, rr = R * 0.55 * g;
        double[] T = {0, c + hh, 0}, B = {0, c - hh, 0};
        double[][] U = new double[6][], L = new double[6][];
        for (int i = 0; i < 6; i++) {
            double a = rot + i * TWO_PI / 6;
            U[i] = new double[]{cos(a) * rr, c + hh * 0.25, sin(a) * rr};
            L[i] = new double[]{cos(a) * rr, c - hh * 0.25, sin(a) * rr};
        }
        int n = f.n(6);
        for (int i = 0; i < 6; i++) {
            int j = (i + 1) % 6;
            edge(f, CR1, c, T, U[i], shat, n);
            edge(f, CR2, c, U[i], U[j], shat, n);
            edge(f, CR1, c, U[i], L[i], shat, n);
            edge(f, CR2, c, L[i], L[j], shat, n);
            edge(f, CR1, c, L[i], B, shat, n);
        }
        if (shat < 0.01) sphere(f, CORE, 0, c, 0, R * 0.15 * g, f.n(8), f.spin(2));
        if (f.at(0.7)) burst(f, Particle.END_ROD, 0, c, 0, f.n(50), 0.7);
        if (f.at(0.86)) {
            burst(f, Particle.END_ROD, 0, c, 0, f.n(100), 1.1);
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(50), 0.9);
        }
    }

    // 33. Кристаллический взрыв
    static void crystalBurst(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.45;
        double extra = 1 + 0.6 * easeOut(f.ph(0.7, 0.78)), col = 1 - easeIn(f.ph(0.88, 1));
        int N = 14;
        for (int i = 0; i < N; i++) {
            double[] d = fib(i, N, f.spin(0.05));
            double st = 0.12 + 0.015 * i, g = easeOut(f.ph(st, st + 0.2));
            if (g <= 0.001) continue;
            double len = R * (0.8 + 0.5 * ((i * 7) % 5) / 4.0) * g * extra * col * (1 + 0.04 * sin(f.tick * 0.8 + i));
            Pen pen = (i % 2 == 0) ? CR2 : CR1;
            ray(f, pen, 0, c, 0, d[0], d[1], d[2], 0.2, len, f.n(9));
            double ux = -d[2], uz = d[0], ul = sqrt(ux * ux + uz * uz);
            if (ul < 1.0e-3) { ux = 1; uz = 0; ul = 1; }
            ux /= ul; uz /= ul;
            double vx = d[1] * uz, vy = d[2] * ux - d[0] * uz, vz = -d[1] * ux;
            double bx = d[0] * len * 0.7, by = d[1] * len * 0.7, bz = d[2] * len * 0.7, w = len * 0.12;
            double[][] cp = {{ux, 0, uz}, {-ux, 0, -uz}, {vx, vy, vz}, {-vx, -vy, -vz}};
            for (double[] q : cp) {
                line(f, pen, d[0] * len, c + d[1] * len, d[2] * len, bx + q[0] * w, c + by + q[1] * w, bz + q[2] * w, f.n(3));
            }
            f.p(Particle.END_ROD, d[0] * len, c + d[1] * len, d[2] * len);
        }
        if (f.at(0.12)) burst(f, Particle.END_ROD, 0, c, 0, f.n(90), 0.9);
        if (f.at(0.88)) {
            burst(f, Particle.CRIT, 0, c, 0, f.n(100), 1.0);
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(60), 1.0);
        }
    }

    // 34. Песчаная буря
    static void sandstorm(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double rise = easeOut(f.ph(0, 0.2)), fade = 1 - easeIn(f.ph(0.9, 1)), hh = H * rise * fade;
        int arms = 4, m = f.n(34);
        for (int a = 0; a < arms; a++) {
            for (int j = 0; j < m; j++) {
                double s = (double) j / (m - 1);
                double rad = R * (0.25 + 0.75 * s) * (1 + 0.15 * sin(s * 9 + f.spin(2)));
                double ang = a * TWO_PI / arms + s * TWO_PI * 1.5 + f.spin(1.8);
                double x = cos(ang) * rad, y = s * hh, z = sin(ang) * rad;
                if (j % 2 == 0) f.blk(Material.SAND, x, y, z);
                else f.d(x, y, z, (a % 2 == 0) ? 0xE0C080 : 0xC8A060, 1.8);
            }
        }
        double blast = f.ph(0.6, 0.85);
        if (f.at(0.6)) flatBurst(f, Particle.CLOUD, 0, 0.3, 0, f.n(80), 0.8);
        if (blast > 0 && blast < 1) {
            double rr = R * (0.8 + 4 * easeOut(blast));
            int nb = f.n(70);
            for (int i = 0; i < nb; i++) {
                double a = TWO_PI * i / nb, y = 0.3 + (i % 4) * 0.5 * (1 - blast);
                if (i % 2 == 0) f.blk(Material.SAND, cos(a) * rr, y, sin(a) * rr);
                else f.d(cos(a) * rr, y, sin(a) * rr, 0xD8B878, 2.0);
            }
        }
    }

    // 35. Звёздный дождь
    private static double[] starPos(double tt, double delay, double apex, double x0, double z0, double dir, double drift) {
        double up = clamp01((tt - delay) / (0.4 - delay));
        double dn = clamp01((tt - 0.46 - delay) / (0.95 - 0.46 - delay));
        if (dn > 0) {
            return new double[]{x0 + cos(dir) * drift * dn, apex * (1 - dn * dn), z0 + sin(dir) * drift * dn, up, dn};
        }
        return new double[]{x0 + sin(tt * 20 + x0) * 0.3, apex * easeOut(up), z0, up, dn};
    }

    static void starRain(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        int N = f.n(26);
        for (int k = 0; k < N; k++) {
            Random rr = new Random(3000 + k);
            double a0 = rr.nextDouble() * TWO_PI, d0 = sqrt(rr.nextDouble()) * R;
            double x0 = cos(a0) * d0, z0 = sin(a0) * d0;
            double apex = H * (0.7 + 0.4 * rr.nextDouble()), dir = rr.nextDouble() * TWO_PI;
            double drift = R * (0.6 + 0.8 * rr.nextDouble()), delay = rr.nextDouble() * 0.12;
            for (int tr = 0; tr < 6; tr++) {
                double tt = t - tr * 0.012;
                if (tt < delay) break;
                double[] p = starPos(tt, delay, apex, x0, z0, dir, drift);
                if (p[4] >= 1.0) break;
                if (tr == 0) {
                    f.p(Particle.END_ROD, p[0], p[1], p[2]);
                    f.d(p[0], p[1], p[2], 0xFFFFFF, 2.0);
                } else {
                    f.dt(p[0], p[1], p[2], 0xFFE070, 0xFF8030, max(0.4, 1.4 - tr * 0.18));
                }
            }
            if (f.at(0.95)) {
                double[] e = starPos(0.95, delay, apex, x0, z0, dir, drift);
                burst(f, Particle.FIREWORK, e[0], 0.2, e[2], 2, 0.2);
            }
        }
        if (f.at(0.4)) burst(f, Particle.END_ROD, 0, H * 0.9, 0, f.n(80), 0.9);
    }

    // 36. Спиральная галактика
    static void spiralGalaxy(Frame f) {
        double R = f.r, H = f.h, c = H * 0.5;
        double reveal = easeOut(f.ph(0, 0.6)), col = 1 - easeIn(f.ph(0.9, 1));
        double tl = 0.55, az = f.spin(0.03);
        int arms = 4, m = f.n(36);
        for (int a = 0; a < arms; a++) {
            for (int j = 1; j <= m; j++) {
                double s = (double) j / m;
                if (s > reveal) break;
                double rad = R * s * col, ang = a * TWO_PI / arms + log(1 + s * 6) * 2.2 + f.spin(0.25) * (1.3 - s);
                double sc = (((a * 131 + j * 17) % 13) / 13.0 - 0.5) * R * 0.08 * (0.3 + s);
                double[] p = tilt(cos(ang) * rad + sc, (((j * 37 + a) % 7) - 3) * 0.03 * R, sin(ang) * rad, tl, az);
                boolean tw = ((f.tick / 3 + j + a) % 5) == 0;
                f.d(p[0], c + p[1], p[2], Colors.mix(0xFFFFFF, 0x5080FF, s), tw ? 2.4 : 1.2);
            }
        }
        for (int i = 0; i < f.n(30); i++) {
            double[] d = fib(i, f.n(30), 0);
            double[] p = tilt(d[0] * R * 0.22, d[1] * R * 0.1, d[2] * R * 0.22, tl, az);
            f.d(p[0], c + p[1], p[2], 0xFFE0A0, 1.6);
        }
        if (f.at(0.9)) burst(f, Particle.END_ROD, 0, c, 0, f.n(90), 0.8);
    }

    // 37. Геометрический куб
    static void cubeFx(Frame f) {
        double R = f.r, t = f.t, c = f.h * 0.5, half = R * 0.5;
        double ay = f.spin(0.2), ax = 0.6 + 0.3 * sin(f.spin(0.1));
        if (t < 0.55) {
            double prog = clamp01(f.ph(0, 0.5));
            cube(f, EDGE, 0, c, 0, half * (0.6 + 0.4 * easeOut(prog)), ay, ax, f.n(14), prog);
        } else {
            double k = easeOut(f.ph(0.55, 0.8)), shrink = 1 - 0.95 * easeIn(f.ph(0.88, 1));
            double[][] nrm = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            double[][] us = {{0, 1, 0}, {0, 1, 0}, {1, 0, 0}, {1, 0, 0}, {1, 0, 0}, {1, 0, 0}};
            double[][] vs = {{0, 0, 1}, {0, 0, 1}, {0, 0, 1}, {0, 0, 1}, {0, 1, 0}, {0, 1, 0}};
            int[][] sg = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
            double fh = half * (1 - 0.35 * k) * shrink, off = half * (1 + 2.2 * k) * shrink;
            for (int i = 0; i < 6; i++) {
                double[][] cr = new double[4][];
                for (int q = 0; q < 4; q++) {
                    double x = nrm[i][0] * off + (us[i][0] * sg[q][0] + vs[i][0] * sg[q][1]) * fh;
                    double y = nrm[i][1] * off + (us[i][1] * sg[q][0] + vs[i][1] * sg[q][1]) * fh;
                    double z = nrm[i][2] * off + (us[i][2] * sg[q][0] + vs[i][2] * sg[q][1]) * fh;
                    cr[q] = rot(x, y, z, ax, ay);
                }
                for (int q = 0; q < 4; q++) {
                    double[] a = cr[q], b = cr[(q + 1) % 4];
                    line(f, FACE, a[0], c + a[1], a[2], b[0], c + b[1], b[2], f.n(10));
                }
                double[] mid = rot(nrm[i][0] * off, nrm[i][1] * off, nrm[i][2] * off, ax, ay);
                f.p(Particle.END_ROD, mid[0], c + mid[1], mid[2]);
            }
        }
        if (f.at(0.55)) burst(f, Particle.END_ROD, 0, c, 0, f.n(60), 0.8);
    }

    // 38. Тессеракт
    private static final double[][] TV = new double[16][];

    static {
        for (int i = 0; i < 16; i++) {
            TV[i] = new double[]{(i & 1) == 0 ? -1 : 1, (i & 2) == 0 ? -1 : 1, (i & 4) == 0 ? -1 : 1, (i & 8) == 0 ? -1 : 1};
        }
    }

    static void tesseract(Frame f) {
        double R = f.r, c = f.h * 0.5;
        double th1 = f.spin(0.12), th2 = f.spin(0.07) + 0.4, th3 = f.spin(0.09);
        double sep = smooth(f.ph(0.65, 0.72)), col = 1 - easeIn(f.ph(0.9, 1));
        double[][] P = new double[16][];
        for (int i = 0; i < 16; i++) {
            double x = TV[i][0], y = TV[i][1], z = TV[i][2], w = TV[i][3];
            double x1 = x * cos(th1) - w * sin(th1), w1 = x * sin(th1) + w * cos(th1);
            double z2 = z * cos(th2) - w1 * sin(th2), w2 = z * sin(th2) + w1 * cos(th2);
            double x3 = x1 * cos(th3) - y * sin(th3), y3 = x1 * sin(th3) + y * cos(th3);
            double s = 1.0 / (2.6 - w2);
            P[i] = new double[]{x3 * s, y3 * s, z2 * s};
        }
        double base = R * 0.9 * col;
        for (int k = 0; k < 2; k++) {
            double sc = base * (k == 0 ? (1 + 0.9 * sep) : 0.55 * (1 - 0.3 * sep));
            Pen pen = k == 0 ? T1 : T2;
            for (int i = 0; i < 16; i++) {
                for (int b = 0; b < 4; b++) {
                    int j = i ^ (1 << b);
                    if (j < i) continue;
                    line(f, pen, P[i][0] * sc, c + P[i][1] * sc, P[i][2] * sc, P[j][0] * sc, c + P[j][1] * sc, P[j][2] * sc, f.n(6));
                }
            }
        }
        if (f.at(0.65)) burst(f, Particle.END_ROD, 0, c, 0, f.n(80), 1.0);
    }

    // 39. Песочные часы
    private static void hp(Frame f, Pen pen, double lat, double y, double fw, double fl, double c) {
        double l2 = lat * cos(fl) - y * sin(fl), y2 = lat * sin(fl) + y * cos(fl);
        pen.draw(f, f.px(l2, fw), c + y2, f.pz(l2, fw));
    }

    static void hourglass(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5, hh = H * 0.5, neck = R * 0.1;
        double flip = PI * smooth(f.ph(0.68, 0.85));
        double g;
        if (t < 0.68) g = 1 - 0.92 * f.ph(0, 0.65);
        else if (t < 0.85) g = 0.08;
        else g = 0.08 + 0.6 * f.ph(0.85, 1);
        double form = easeOut(f.ph(0, 0.1));
        for (int i = -4; i <= 4; i++) {
            double y = hh * i / 4.0 * form, rr = neck + (R - neck) * pow(abs(i) / 4.0, 1.2);
            int n = f.n(14);
            for (int k = 0; k < n; k++) {
                double a = TWO_PI * k / n;
                hp(f, GLASS, cos(a) * rr, y, sin(a) * rr, flip, c);
            }
        }
        Random rr = new Random(77);
        int N = f.n(70);
        for (int i = 0; i < N; i++) {
            double u = rr.nextDouble(), a = rr.nextDouble() * TWO_PI, rd = sqrt(rr.nextDouble()) * 0.85, w = rr.nextDouble();
            if (u <= g) {
                double y = neck + u * (hh - neck), rad = (neck + (R - neck) * (y / hh)) * rd;
                hp(f, SAND, cos(a) * rad, y * form, sin(a) * rad, flip, c);
            }
            double pile = (1 - g) * 0.9;
            if (w <= pile) {
                double y = -hh + w * (hh - neck), rad = (neck + (R - neck) * (abs(y) / hh)) * rd;
                hp(f, SAND, cos(a) * rad, y * form, sin(a) * rad, flip, c);
            }
        }
        if (t < 0.65 || t >= 0.85) {
            double sg = t >= 0.85 ? 1 : -1;
            for (int k = 0; k < 5; k++) {
                double y = sg * ((f.tick * 0.4 + k * 0.2) % 1.0) * hh * 0.85;
                hp(f, SAND, 0, y, 0, flip, c);
            }
        }
        if (f.at(0.85)) {
            burst(f, Particle.END_ROD, 0, c, 0, f.n(60), 0.7);
            ring(f, CORE, 0, c, 0, R, f.n(40), 0);
        }
        if (f.at(0.97)) burst(f, Particle.END_ROD, 0, c, 0, f.n(60), 0.9);
    }

    // 40. ДНК-спираль
    private static final int[] PAIR = {0xFF4040, 0x40FF60, 0xFFE040, 0x4080FF};

    static void dna(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double grow = easeOut(f.ph(0, 0.2)), col = 1 - easeIn(f.ph(0.88, 1));
        double unw = easeIn(f.ph(0.6, 0.85)), extra = TWO_PI * 5 * unw, turns = lerp(3.0, 0.6, unw);
        double sep = 1 + 1.2 * smooth(f.ph(0.78, 0.88)), hh = H * grow * col;
        int m = f.n(54);
        for (int s = 0; s < 2; s++) {
            for (int j = 0; j < m; j++) {
                double u = (double) j / (m - 1), ang = u * turns * TWO_PI + f.spin(0.4) + extra + s * PI, rad = R * sep;
                f.d(cos(ang) * rad, u * hh, sin(ang) * rad, s == 0 ? 0x40A0FF : 0xFF50A0, 1.5);
            }
        }
        int rungs = f.n(18);
        for (int i = 0; i < rungs; i++) {
            double u = (i + 0.5) / rungs, ang = u * turns * TWO_PI + f.spin(0.4) + extra, rad = R * sep;
            line(f, Pen.dust(PAIR[i % 4], 1.2), cos(ang) * rad, u * hh, sin(ang) * rad, cos(ang + PI) * rad, u * hh,
                    sin(ang + PI) * rad, f.n(6));
        }
        if (f.at(0.85)) {
            burst(f, Particle.END_ROD, 0, hh * 0.5, 0, f.n(80), 0.9);
            burst(f, Particle.FIREWORK, 0, hh * 0.5, 0, f.n(40), 0.7);
        }
    }
}
