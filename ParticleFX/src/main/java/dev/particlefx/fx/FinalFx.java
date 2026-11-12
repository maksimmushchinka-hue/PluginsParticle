package dev.particlefx.fx;

import static dev.particlefx.fx.Shapes.*;
import static java.lang.Math.*;

import java.util.Map;
import java.util.Random;
import org.bukkit.Particle;

/** Символы, природа и финальные эффекты (41–50). */
public final class FinalFx {
    private FinalFx() {}

    private static final Pen CORE = Pen.of(Particle.END_ROD);
    private static final Pen HEART = Pen.dust(0xFF3060, 1.6);
    private static final Pen HEART2 = Pen.dust(0xFF90B0, 1.3);
    private static final Pen[] PETAL = {Pen.dust(0xFF7AB8, 1.4), Pen.dust(0xE040A0, 1.4), Pen.dust(0xFFD060, 1.3)};
    private static final Pen STAR = Pen.dust(0xFFE060, 1.7);
    private static final Pen RAY = Pen.fade(0xFFFFFF, 0xFFA020, 1.5);
    private static final Pen COL1 = Pen.dust(0x66EEFF, 1.5);
    private static final Pen COL2 = Pen.dust(0xAA66FF, 1.5);
    private static final Pen BEAM = Pen.dust(0xFF3050, 1.8);

    public static void register(Map<String, Renderer> m) {
        m.put("heart", FinalFx::heart);
        m.put("flower_burst", FinalFx::flower);
        m.put("tree_of_life", FinalFx::tree);
        m.put("whirlpool", FinalFx::whirlpool);
        m.put("star_burst", FinalFx::starBurst);
        m.put("double_helix", FinalFx::doubleHelix);
        m.put("light_arch", FinalFx::lightArch);
        m.put("energy_column", FinalFx::energyColumn);
        m.put("shockwave", FinalFx::shockwave);
        m.put("apocalypse", FinalFx::apocalypse);
    }

    // 41. Сердце
    private static double[] heartPt(double th) {
        double x = 16 * pow(sin(th), 3);
        double y = 13 * cos(th) - 5 * cos(2 * th) - 2 * cos(3 * th) - cos(4 * th);
        return new double[]{x / 16.0, y / 16.0};
    }

    static void heart(Frame f) {
        double R = f.r, t = f.t, c = f.h * 0.5 + R * 0.6;
        double form = easeOut(f.ph(0, 0.4)), expand = easeOut(f.ph(0.75, 1));
        double beat = (t >= 0.4 && t < 0.75) ? 0.18 * pow(sin(f.ph(0.4, 0.75) * PI * 4), 2) : 0;
        double sc = R * (1 + beat) * (t >= 0.75 ? 1 + 2.5 * expand : 1);
        int N = f.n(80);
        Random rr = new Random(11);
        for (int i = 0; i < N; i++) {
            double[] p = heartPt(TWO_PI * i / N);
            double sx = rr.nextGaussian() * R * 1.2, sy = rr.nextGaussian() * R * 1.2, sz = rr.nextGaussian() * R * 1.2;
            double tx = p[0] * sc, ty = p[1] * sc;
            double lat = lerp(sx, tx, form), y = lerp(sy, ty, form), fw = lerp(sz, 0, form);
            double size = t >= 0.75 ? max(0.3, 1.6 * (1 - expand)) : 1.6;
            f.d(f.px(lat, fw), c + y, f.pz(lat, fw), 0xFF3060, size);
        }
        if (t < 0.75) {
            int n2 = f.n(40);
            for (int i = 0; i < n2; i++) {
                double[] p = heartPt(TWO_PI * i / n2);
                f.d(f.px(p[0] * sc * 0.6 * form, 0), c + p[1] * sc * 0.6 * form, f.pz(p[0] * sc * 0.6 * form, 0), 0xFF90B0, 1.3);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                double age = f.ph(0.75 + 0.05 * i, 1);
                if (age <= 0 || age >= 1) continue;
                ringF(f, i % 2 == 0 ? HEART : HEART2, 0, c, 0, R * (1 + 3 * easeOut(age)), f.n(60), 0);
            }
        }
        if (f.at(0.75)) {
            burst(f, Particle.HEART, 0, c, 0, f.n(20), 0.3);
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(70), 0.8);
        }
    }

    // 42. Цветочный взрыв
    static void flower(Frame f) {
        double R = f.r, base = 0.4;
        int[] cnt = {8, 8, 6};
        double[] len = {1.0, 0.8, 0.55};
        double bst = easeOut(f.ph(0.8, 1));
        for (int l = 0; l < 3; l++) {
            double open = easeOut(f.ph(0.06 + 0.14 * l, 0.45 + 0.1 * l));
            double tl = lerp(0.1, 1.35 - 0.3 * l, open);
            double L = R * len[l] * (1 + 0.6 * bst);
            for (int p = 0; p < cnt[l]; p++) {
                double az = p * TWO_PI / cnt[l] + l * 0.4 + f.spin(0.03);
                double ax = cos(az) * sin(tl), ay = cos(tl), az2 = sin(az) * sin(tl);
                double px = -sin(az), pz = cos(az);
                int m = f.n(8);
                for (int j = 1; j <= m; j++) {
                    double s = (double) j / m, w = R * 0.32 * pow(sin(PI * s), 0.8) * (1 - 0.25 * l);
                    double bx = ax * L * s, by = ay * L * s, bz = az2 * L * s;
                    for (int sd = -1; sd <= 1; sd += 2) {
                        PETAL[l].draw(f, bx + px * w * sd, base + by, bz + pz * w * sd);
                    }
                }
            }
        }
        sphere(f, CORE, 0, base + R * 0.15, 0, R * 0.08, f.n(6), f.spin(1));
        if (f.at(0.8)) {
            burst(f, Particle.CHERRY_LEAVES, 0, base + R * 0.4, 0, f.n(60), 0.5);
            burst(f, Particle.FIREWORK, 0, base + R * 0.4, 0, f.n(40), 0.6);
        }
    }

    // 43. Дерево жизни
    static void tree(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double trunk = easeOut(f.ph(0, 0.28));
        int m = f.n(26);
        for (int s = 0; s < 3; s++) {
            for (int j = 0; j < m; j++) {
                double u = (double) j / (m - 1);
                if (u > trunk) break;
                double rad = R * 0.1 * (1 - 0.5 * u) + 0.05, ang = s * TWO_PI / 3 + u * TWO_PI * 1.5;
                f.d(cos(ang) * rad, u * H * 0.55, sin(ang) * rad, 0x8B5A2B, 1.5);
            }
        }
        for (int r = 0; r < 5; r++) {
            double az = r * TWO_PI / 5 + 0.3;
            line(f, Pen.dust(0x6B4220, 1.3), 0, 0.2, 0, cos(az) * R * 0.6 * trunk, 0.1, sin(az) * R * 0.6 * trunk, f.n(8));
        }
        double wave = f.ph(0.72, 0.92) * H * 1.2;
        boolean flashing = t >= 0.72 && t <= 0.92;
        for (int b = 0; b < 7; b++) {
            double g = easeOut(f.ph(0.22 + 0.05 * b, 0.5 + 0.05 * b));
            if (g <= 0.001) continue;
            double az = b * 2.4, reach = R * (0.55 + 0.07 * (b % 4)), y0 = H * (0.32 + 0.045 * b), y1 = y0 + H * 0.28;
            double[] P1 = {cos(az) * reach * 0.5, y0 + H * 0.12, sin(az) * reach * 0.5};
            double[] P2 = {cos(az) * reach, y1, sin(az) * reach};
            int bm = f.n(10);
            for (int j = 1; j <= bm; j++) {
                double u = (double) j / bm * g, ob = 1 - u;
                f.d(2 * ob * u * P1[0] + u * u * P2[0], ob * ob * y0 + 2 * ob * u * P1[1] + u * u * P2[1],
                        2 * ob * u * P1[2] + u * u * P2[2], 0x7A4A22, 1.4);
            }
            if (g > 0.95) {
                for (int tw = 0; tw < 2; tw++) {
                    double ta = az + (tw == 0 ? 0.7 : -0.7);
                    double tx = P2[0] + cos(ta) * R * 0.3, ty = P2[1] + H * 0.1, tz = P2[2] + sin(ta) * R * 0.3;
                    line(f, Pen.dust(0x7A4A22, 1.2), P2[0], P2[1], P2[2], tx, ty, tz, f.n(5));
                    Random lr = new Random(b * 10 + tw);
                    for (int q = 0; q < f.n(8); q++) {
                        double lx = tx + lr.nextGaussian() * R * 0.18, ly = ty + lr.nextGaussian() * R * 0.12,
                                lz = tz + lr.nextGaussian() * R * 0.18;
                        f.d(lx, ly, lz, (q % 2 == 0) ? 0x3DDC4A : 0x9BEF5A, 1.8);
                        if (flashing && abs(ly - wave) < H * 0.1) f.p(Particle.END_ROD, lx, ly, lz);
                    }
                }
            }
        }
        if (f.at(0.72)) burst(f, Particle.END_ROD, 0, H * 0.7, 0, f.n(80), 0.8);
        if (t > 0.9 && f.tick % 3 == 0) f.p(Particle.CHERRY_LEAVES, (f.rnd.nextDouble() - 0.5) * R, H * 0.8, (f.rnd.nextDouble() - 0.5) * R);
    }

    // 44. Водоворот
    static void whirlpool(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double suck = easeIn(f.ph(0.6, 0.75));
        for (int l = 0; l < 4; l++) {
            int m = f.n(30);
            for (int j = 0; j < m; j++) {
                double s = (double) j / m;
                double rad = R * (0.12 + 0.88 * s) * (1 - 0.93 * suck) * (1 - 0.1 * l);
                double ang = l * PI / 2 + s * TWO_PI * 2 + f.spin(0.5) * (1.6 - s);
                double y = H * 0.9 * pow(rad / R, 1.3) * (1 - 0.5 * suck) + 0.1 * l;
                double x = cos(ang) * rad, z = sin(ang) * rad;
                if (j % 5 == 0) f.p(Particle.BUBBLE_POP, x, y, z);
                else f.d(x, y, z, l % 2 == 0 ? 0x2E7DDC : 0xD8F0FF, 1.5);
            }
        }
        double col = H * 2 * easeOut(f.ph(0.75, 0.88)) * (1 - easeIn(f.ph(0.88, 0.9)));
        if (t >= 0.75 && t < 0.9) {
            line(f, Pen.dust(0x60B0FF, 2.0), 0, 0, 0, 0, col, 0, f.n(30));
            for (int i = 0; i < 4; i++) {
                double y = col * (i + 1) / 4.0;
                ring(f, Pen.fade(0x60B0FF, 0xFFFFFF, 1.6), 0, y, 0, R * 0.2 * (1 + i * 0.4), f.n(24), f.spin(2));
            }
        }
        if (f.at(0.75)) fan(f, Particle.SPLASH, 0, 0.5, 0, f.n(120), 1.0, 1.8);
        double w = f.ph(0.9, 1);
        if (w > 0) ring(f, Pen.fade(0x60B0FF, 0xFFFFFF, 1.8), 0, 0.3, 0, R * 1.8 * easeOut(w), f.n(80), 0);
    }

    // 45. Звёздный взрыв
    static void starBurst(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5;
        double draw = f.ph(0, 0.35), col = 1 - easeIn(f.ph(0.9, 1));
        double pulse = t >= 0.35 ? 1 + 0.12 * sin(f.ph(0.35, 0.55) * TWO_PI * 3) : 1;
        double rot = f.spin(0.15), sc = R * pulse * col;
        double[][] v = new double[10][];
        for (int i = 0; i < 10; i++) {
            double ang = rot + PI / 2 + i * PI / 5, rad = (i % 2 == 0) ? sc : sc * 0.42;
            v[i] = new double[]{cos(ang) * rad, sin(ang) * rad};
        }
        double lim = draw * 10;
        for (int i = 0; i < 10; i++) {
            double fr = clamp01(lim - i);
            if (fr <= 0) break;
            int j = (i + 1) % 10;
            lineL(f, STAR, v[i][0], c + v[i][1], 0, v[i][0] + (v[j][0] - v[i][0]) * fr,
                    c + v[i][1] + (v[j][1] - v[i][1]) * fr, 0, max(2, (int) round(f.n(12) * fr)));
        }
        sphere(f, CORE, 0, c, 0, sc * 0.12, f.n(8), 0);
        double ray = easeOut(f.ph(0.55, 0.8));
        if (f.at(0.55)) burst(f, Particle.FIREWORK, 0, c, 0, f.n(120), 1.0);
        if (ray > 0) {
            double k1 = 1 + 3.5 * ray;
            for (int i = 0; i < 10; i += 2) {
                double x = v[i][0], y = v[i][1];
                lineL(f, RAY, x, c + y, 0, x * k1, c + y * k1, 0, f.n(10));
                lineL(f, RAY, x, c + y, 0, x * 1.3, c + y * 1.3, sc * 3 * ray, f.n(8));
                lineL(f, RAY, x, c + y, 0, x * 1.3, c + y * 1.3, -sc * 3 * ray, f.n(8));
            }
        }
    }

    // 46. Двойная спираль
    static void doubleHelix(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.5;
        double grow = easeOut(f.ph(0, 0.2)), conv = easeIn(f.ph(0.5, 0.75));
        int m = f.n(56);
        if (t < 0.75) {
            for (int s = 0; s < 2; s++) {
                double dir = s == 0 ? 1 : -1;
                for (int j = 0; j < m; j++) {
                    double u = (double) j / (m - 1), ang = dir * (u * TWO_PI * 3 + f.spin(0.5)), rad = R * (1 - 0.9 * conv);
                    f.d(cos(ang) * rad, lerp(u * H * grow, c, conv), sin(ang) * rad, s == 0 ? 0xFF8030 : 0x30C0FF, 1.6);
                }
            }
            double p0 = f.spin(0.5) / PI;
            for (int k = (int) ceil(p0); k < p0 + 6; k++) {
                double u = (k - p0) / 6.0;
                if (u < 0 || u > 1) continue;
                double ang = u * TWO_PI * 3 + f.spin(0.5), rad = R * (1 - 0.9 * conv);
                f.p(Particle.END_ROD, cos(ang) * rad, lerp(u * H * grow, c, conv), sin(ang) * rad);
            }
            if (conv > 0) sphere(f, CORE, 0, c, 0, R * 0.15 * (0.3 + conv * 1.4), f.n(12), f.spin(3));
        } else {
            double k = easeOut(f.ph(0.75, 1));
            sphere(f, Pen.fade(0xFFFFFF, 0xFF6020, 1.8), 0, c, 0, R * 3 * k, f.n(70), f.spin(0.3));
            ring(f, Pen.fade(0x66EEFF, 0xFF8030, 1.8), 0, c, 0, R * 4 * k, f.n(70), 0);
        }
        if (f.at(0.75)) {
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(140), 1.2);
            burst(f, Particle.END_ROD, 0, c, 0, f.n(60), 1.0);
        }
    }

    // 47. Арка света
    static void lightArch(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double g = easeOut(f.ph(0, 0.4));
        if (t < 0.65) {
            for (int b = 0; b < 6; b++) {
                double rad = R * (1 - 0.07 * b), hh = H * (1 - 0.07 * b);
                int n = f.n(26);
                for (int i = 0; i < n; i++) {
                    double a = i / (n - 1.0) * g * (PI / 2);
                    for (int sd = 0; sd < 2; sd++) {
                        double aa = sd == 0 ? a : PI - a;
                        double lat = cos(aa) * rad;
                        f.d(f.px(lat, 0), sin(aa) * hh, f.pz(lat, 0), Colors.hue(b / 7.0 * 0.8), 1.5);
                    }
                }
            }
        } else {
            for (int w = 0; w < 4; w++) {
                double age = f.ph(0.65 + 0.07 * w, 1);
                if (age <= 0 || age >= 1) continue;
                double mm = 1 + 2.4 * easeOut(age);
                int n = f.n(34);
                for (int i = 0; i < n; i++) {
                    if ((i + w * 3 + f.tick / 2) % 3 == 0) continue;
                    double a = PI * i / (n - 1.0), lat = cos(a) * R * mm;
                    f.d(f.px(lat, 0), sin(a) * H * (1 + 0.4 * (mm - 1)) * (1 - 0.5 * age), f.pz(lat, 0),
                            Colors.hue(w / 4.0 * 0.8), 1.5 * (1 - age) + 0.4);
                }
            }
        }
        if (f.at(0.65)) burst(f, Particle.END_ROD, 0, H * 0.5, 0, f.n(100), 0.8);
    }

    // 48. Энергетическая колонна
    static void energyColumn(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double col = 1 - easeIn(f.ph(0.9, 1)), surge = easeOut(f.ph(0.7, 0.8));
        for (int i = 0; i < 6; i++) {
            double u = (t * 1.6 + i / 6.0) % 1.0;
            double y = H * u * (1 + 0.6 * surge) * col;
            double rad = R * (0.35 + 0.65 * abs(sin(u * PI * 2 + i))) * (1 + 2 * surge * (1 - f.ph(0.8, 1))) * col;
            ring(f, (i % 2 == 0) ? COL1 : COL2, 0, y, 0, rad + 0.05, f.n(34), f.spin(i % 2 == 0 ? 1 : -1));
        }
        line(f, CORE, 0, 0, 0, 0, H * col * easeOut(f.ph(0, 0.15)), 0, f.n(26));
        ring(f, COL1, 0, 0.2, 0, R * (0.8 + 0.2 * sin(f.tick * 0.4)), f.n(30), 0);
        if (f.at(0.7)) burst(f, Particle.END_ROD, 0, H * 0.5, 0, f.n(120), 1.3);
        double sw = f.ph(0.7, 0.95);
        if (sw > 0 && sw < 1) ring(f, Pen.fade(0xFFFFFF, 0x40A0FF, 2.0), 0, 0.3, 0, R * 5 * easeOut(sw), f.n(80), 0);
    }

    // 49. Шоковая волна
    static void shockwave(Frame f) {
        double R = f.r, c = f.h * 0.3;
        double[] ts = {0.0, 0.22, 0.44, 0.7};
        for (int w = 0; w < 4; w++) {
            double age = (f.t - ts[w]) / (w == 3 ? 0.28 : 0.32);
            if (f.at(ts[w])) burst(f, Particle.END_ROD, 0, c, 0, f.n(w == 3 ? 140 : 50), w == 3 ? 1.4 : 0.8);
            if (age < 0 || age > 1) continue;
            double rad = R * easeOut(age) * (w == 3 ? 1.7 : 1.0);
            Pen pn = Pen.fade(w % 2 == 0 ? 0xFFFFFF : 0x80C0FF, w % 2 == 0 ? 0x4060FF : 0xFF8040, 2.0 * (1 - age) + 0.6);
            ring(f, pn, 0, c, 0, rad, f.n(56), 0);
            for (int k = 0; k < 3; k++) ringV(f, pn, 0, c, 0, rad, f.n(56), 0, k * PI / 3);
            ring(f, pn, 0, 0.2, 0, rad * 1.1, f.n(40), 0);
        }
    }

    // 50. Финальный апокалипсис
    static void apocalypse(Frame f) {
        double R = f.r, H = f.h, t = f.t, c = H * 0.4;
        if (t < 0.34) {
            double k = f.ph(0, 0.3), cl = 1 - 0.7 * easeIn(f.ph(0.28, 0.34));
            int m = f.n(36);
            for (int a = 0; a < 3; a++) {
                for (int j = 0; j < m; j++) {
                    double s = (double) j / (m - 1), rad = R * (0.2 + 0.9 * s) * (1 - 0.4 * k) * cl;
                    double ang = a * TWO_PI / 3 + s * TWO_PI * 2 + f.spin(1.5);
                    f.d(cos(ang) * rad, s * H * easeOut(k * 3), sin(ang) * rad, Colors.mix(0x8A1030, 0xB040FF, s), 1.8);
                }
            }
            ring(f, Pen.dust(0xFF2030, 1.6), 0, 0.2, 0, R * (1 + 0.1 * sin(f.tick * 0.6)), f.n(50), f.spin(1));
        }
        if (f.at(0.3)) {
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(150), 1.5);
            burst(f, Particle.END_ROD, 0, c, 0, f.n(100), 1.0);
            f.p(Particle.EXPLOSION_EMITTER, 0, c, 0);
        }
        if (t >= 0.3 && t < 0.42) {
            double k = easeOut(f.ph(0.3, 0.42));
            sphere(f, Pen.dust(0xFFFFFF, 2.0), 0, c, 0, R * (0.3 + 1.2 * k), f.n(90), f.spin(0.5));
        }
        if (t >= 0.4 && t < 0.7) {
            for (int i = 0; i < 4; i++) {
                double age = f.ph(0.4 + 0.05 * i, 0.7);
                if (age <= 0 || age >= 1) continue;
                double rad = R * (0.4 + 2.4 * easeOut(age));
                Pen pn = Pen.fade(i % 2 == 0 ? 0xFFFFFF : 0xFF6040, 0x6020A0, 1.8);
                if (i % 2 == 0) ring(f, pn, 0, c, 0, rad, f.n(60), 0);
                else ringV(f, pn, 0, c, 0, rad, f.n(60), 0, i * 0.7);
            }
        }
        if (t >= 0.55 && t < 0.85) {
            double g = easeOut(f.ph(0.55, 0.68)), dec = 1 - easeIn(f.ph(0.78, 0.85));
            for (int i = 0; i < 8; i++) {
                double a = i * TWO_PI / 8 + f.spin(0.2), bx = cos(a) * R * 0.9, bz = sin(a) * R * 0.9;
                line(f, BEAM, bx, 0, bz, bx, H * 2 * g * dec, bz, f.n(30));
            }
            line(f, CORE, 0, 0, 0, 0, H * 3 * g * dec, 0, f.n(36));
            if (f.at(0.55)) burst(f, Particle.FLAME, 0, 0.5, 0, f.n(80), 1.0);
        }
        if (t >= 0.82) {
            double k = easeIn(f.ph(0.82, 1)), inv = 1 - k;
            sphere(f, Pen.dust(0x201030, 2.6), 0, c, 0, R * 1.5 * inv + 0.1, f.n(70), f.spin(1));
            ring(f, Pen.fade(0xFF4060, 0xFFFFFF, 1.6), 0, c, 0, R * 2 * inv, f.n(60), f.spin(-1));
            ringV(f, Pen.fade(0xFF4060, 0xFFFFFF, 1.6), 0, c, 0, R * 2 * inv, f.n(60), 0, f.spin(0.5));
            sphere(f, CORE, 0, c, 0, R * 0.2 * k, f.n(12), 0);
        }
        if (f.at(0.98)) {
            burst(f, Particle.END_ROD, 0, c, 0, f.n(180), 1.6);
            burst(f, Particle.FIREWORK, 0, c, 0, f.n(100), 1.4);
            f.p(Particle.EXPLOSION_EMITTER, 0, c, 0);
        }
    }
}
