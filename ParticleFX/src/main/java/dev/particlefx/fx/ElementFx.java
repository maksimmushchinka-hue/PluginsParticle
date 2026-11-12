package dev.particlefx.fx;

import static dev.particlefx.fx.Shapes.*;
import static java.lang.Math.*;

import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Particle;

/** Стихии и существа (11–20). */
public final class ElementFx {
    private ElementFx() {}

    private static final Pen BOLT = Pen.of(Particle.ELECTRIC_SPARK);
    private static final Pen GLOW = Pen.of(Particle.END_ROD);
    private static final Pen FLM = Pen.of(Particle.FLAME);
    private static final Pen ICE = Pen.dust(0xAAE0FF, 1.6);
    private static final Pen WATER = Pen.fade(0x70C0FF, 0xFFFFFF, 1.4);
    private static final Pen ROCK = Pen.dust(0x4A3F3A, 1.8);
    private static final Pen LAVA = Pen.dust(0xFF5A10, 2.0);
    private static final Pen WING = Pen.dust(0x6A0E0E, 1.4);

    public static void register(Map<String, Renderer> m) {
        m.put("lightning", ElementFx::lightning);
        m.put("thunder_front", ElementFx::thunderFront);
        m.put("fire_tornado", ElementFx::fireTornado);
        m.put("ice_tornado", ElementFx::iceTornado);
        m.put("waterfall", ElementFx::waterfall);
        m.put("tsunami", ElementFx::tsunami);
        m.put("volcano", ElementFx::volcano);
        m.put("earthquake", ElementFx::earthquake);
        m.put("stone_golem", ElementFx::golem);
        m.put("dragon", ElementFx::dragon);
    }

    // 11. Молния
    static void lightning(Frame f) {
        double R = f.r, H = f.h;
        double[] ts = {0.15, 0.45, 0.75};
        for (int s = 0; s < 3; s++) {
            int start = (int) ceil(ts[s] * f.total - 1.0e-9);
            int age = f.tick - start;
            double bx = (s - 1) * R * 0.35;
            if (age >= 0 && age < 10) {
                bolt(f, BOLT, bx, H, 0, bx, 0.1, 0, 10, R * 0.2, 1000L * s + age / 2, 2, min(1.0, (age + 1) / 3.0));
                if (age < 3) ring(f, GLOW, bx, H, 0, R * 0.5, f.n(24), f.spin(1));
                if (age == 2) {
                    burst(f, Particle.ELECTRIC_SPARK, bx, 0.3, 0, f.n(60), 0.6);
                    sphere(f, GLOW, bx, 0.5, 0, R * 0.15, f.n(12), 0);
                }
                if (age >= 2) ring(f, BOLT, bx, 0.2, 0, R * 0.8 * (age - 2) / 7.0, f.n(36), 0);
            }
            if (f.tick % 3 == 0) {
                f.p(Particle.ELECTRIC_SPARK, bx + (f.rnd.nextDouble() - 0.5) * R * 0.4, f.rnd.nextDouble() * H * 0.6,
                        (f.rnd.nextDouble() - 0.5) * R * 0.4);
            }
        }
    }

    // 12. Грозовой фронт
    static void thunderFront(Frame f) {
        double R = f.r, H = f.h;
        for (int i = 0; i < f.n(34); i++) {
            double l = (f.rnd.nextDouble() * 2 - 1) * R * 1.2, fw = (f.rnd.nextDouble() - 0.3) * R * 0.5 + R * 0.3;
            f.d(f.px(l, fw), H + f.rnd.nextDouble() * 1.2, f.pz(l, fw), i % 3 == 0 ? 0x888899 : 0x44444F, 3.0);
        }
        int n = 6;
        for (int i = 0; i <= n; i++) {
            boolean fin = (i == n);
            double tt = fin ? 0.78 : 0.08 + 0.09 * i;
            int start = (int) ceil(tt * f.total - 1.0e-9), age = f.tick - start;
            int dur = fin ? 14 : 8;
            if (age < 0 || age >= dur) continue;
            int bolts = fin ? n : 1;
            for (int b = 0; b < bolts; b++) {
                int idx = fin ? b : i;
                double lat = (-0.9 + 1.8 * idx / (n - 1.0)) * R;
                double fw = R * 0.3 + ((idx % 2) * 2 - 1) * R * 0.15;
                double x = f.px(lat, fw), z = f.pz(lat, fw);
                bolt(f, BOLT, x, H, z, x, 0.1, z, 10, R * 0.15, 200L * idx + age / 2 + (fin ? 77 : 0), 1,
                        min(1.0, (age + 1) / 3.0));
                if (age == 2) burst(f, Particle.ELECTRIC_SPARK, x, 0.3, z, f.n(fin ? 30 : 22), 0.6);
            }
            if (fin && age == 2) {
                flatBurst(f, Particle.END_ROD, 0, 0.4, f.pz(0, R * 0.3), f.n(80), 1.2);
            }
        }
    }

    // 13. Огненный торнадо
    static void fireTornado(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double rise = easeOut(f.ph(0, 0.25)), bk = easeOut(f.ph(0.65, 0.72));
        double fade = 1 - 0.7 * easeIn(f.ph(0.9, 1));
        double wid = (1 + 1.5 * bk) * fade, hh = H * rise * fade;
        int arms = 3, m = f.n(40);
        for (int a = 0; a < arms; a++) {
            for (int j = 0; j < m; j++) {
                double s = (double) j / (m - 1);
                double rad = R * (0.12 + 0.88 * s * s) * wid, ang = a * TWO_PI / arms + s * TWO_PI * 1.8 + f.spin(1.3);
                double x = cos(ang) * rad, z = sin(ang) * rad, y = s * hh;
                if (j % 3 == 0) f.d(x, y, z, Colors.mix(0xFF3000, 0xFFB020, s), 1.8);
                else f.p(j % 3 == 1 ? Particle.FLAME : Particle.SMALL_FLAME, x, y, z);
            }
        }
        line(f, FLM, 0, 0, 0, 0, hh, 0, f.n(18));
        if (f.at(0.65)) {
            burst(f, Particle.FLAME, 0, 0.3, 0, f.n(120), 1.0);
            burst(f, Particle.LAVA, 0, 0.5, 0, f.n(30), 0.6);
        }
        if (bk > 0 && bk < 1) ring(f, FLM, 0, 0.3, 0, R * 4 * bk, f.n(60), 0);
        if (f.tick % 2 == 0) f.v(Particle.LARGE_SMOKE, 0, hh, 0, 0, 0.2, 0, 0.5);
    }

    // 14. Ледяной торнадо
    static void iceTornado(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double rise = easeOut(f.ph(0, 0.25)), bk = easeOut(f.ph(0.65, 0.72));
        double fade = 1 - 0.7 * easeIn(f.ph(0.9, 1));
        double wid = (1 + 1.2 * bk) * fade, hh = H * rise * fade;
        int arms = 3, m = f.n(40);
        for (int a = 0; a < arms; a++) {
            for (int j = 0; j < m; j++) {
                double s = (double) j / (m - 1);
                double rad = R * (0.12 + 0.88 * s * s) * wid, ang = a * TWO_PI / arms - s * TWO_PI * 1.8 - f.spin(1.4);
                double x = cos(ang) * rad, z = sin(ang) * rad, y = s * hh;
                if (j % 3 == 0) f.d(x, y, z, Colors.mix(0xFFFFFF, 0x60B8FF, s), 1.7);
                else if (j % 3 == 1) f.p(Particle.SNOWFLAKE, x, y, z);
                else f.d(x, y, z, 0xAAE0FF, 1.3);
            }
        }
        line(f, ICE, 0, 0, 0, 0, hh, 0, f.n(18));
        if (f.at(0.65)) burst(f, Particle.SNOWFLAKE, 0, H * 0.4, 0, f.n(120), 0.9);
        double age = f.ph(0.65, 0.92);
        if (age > 0 && age < 1) {
            Pen shard = Pen.block(Material.PACKED_ICE);
            for (int k = 0; k < 24; k++) {
                double a = k * TWO_PI / 24 + (k % 3) * 0.1;
                double rad = R * (1 + 5 * easeOut(age)) * (0.6 + 0.4 * ((k * 7) % 5) / 4.0);
                double y = H * 0.35 + sin(age * PI) * H * 0.4 * (((k * 5) % 4) / 3.0 + 0.3) - age * H * 0.3;
                shard.draw(f, cos(a) * rad, max(0.2, y), sin(a) * rad);
                if (k % 2 == 0) f.p(Particle.SNOWFLAKE, cos(a) * rad, max(0.2, y) + 0.2, sin(a) * rad);
            }
        }
    }

    // 15. Водяной водопад
    static void waterfall(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double fall = R * 0.9, grow = easeOut(f.ph(0, 0.15)), big = easeOut(f.ph(0.7, 0.95));
        double fade = 1 - 0.8 * easeIn(f.ph(0.92, 1));
        int m = f.n(28);
        for (int w = -2; w <= 2; w++) {
            double lat = w * R * 0.07;
            for (int j = 0; j < m; j++) {
                double s = ((double) j / m + t * 3.0 + w * 0.13) % 1.0;
                if (s > grow) continue;
                double fw = fall * s, y = H * (1 - s * s);
                double x = f.px(lat, fw), z = f.pz(lat, fw);
                if (j % 2 == 0) f.p(Particle.FALLING_WATER, x, y, z);
                else f.d(x, y, z, Colors.mix(0x60B0FF, 0xFFFFFF, s * 0.5), 1.5);
            }
        }
        double lx = f.px(0, fall), lz = f.pz(0, fall);
        for (int i = 0; i < 3; i++) {
            double rad = ((t * 2.5 + i / 3.0) % 1.0) * R * 0.8 * (1 + 1.4 * big) * fade;
            ring(f, WATER, lx, 0.15, lz, rad, f.n(48), 0);
            if (i == 0) ring(f, Pen.of(Particle.SPLASH), lx, 0.2, lz, rad * 0.9, f.n(16), 0);
        }
        if (f.at(0.7)) fan(f, Particle.SPLASH, lx, 0.3, lz, f.n(100), 0.9, 1.0);
    }

    // 16. Цунами
    static void tsunami(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double rise = easeOut(f.ph(0, 0.35)), roll = f.ph(0.6, 0.95);
        double d = -R * 0.6 + R * 3.0 * easeOut(roll);
        double hw0 = H * rise * (1 - 0.7 * f.ph(0.8, 1));
        int m = f.n(34), q = 6;
        for (int i = 0; i < m; i++) {
            double l = ((double) i / (m - 1) * 2 - 1) * R;
            double env = 1 - 0.45 * pow(abs(l) / R, 2);
            double hw = hw0 * env;
            for (int j = 0; j < q; j++) {
                double u = (double) j / (q - 1);
                double y = hw * u, fw = d + 0.55 * hw * u * u + sin(l * 0.8 + f.spin(1.5)) * 0.3;
                double x = f.px(l, fw), z = f.pz(l, fw);
                if (j == q - 1) f.p(Particle.SPLASH, x, y, z);
                else f.d(x, y, z, Colors.mix(0x1050C0, 0x90E0FF, u), 1.6);
            }
        }
        if (f.at(0.6)) {
            flatBurst(f, Particle.SPLASH, f.px(0, d), 0.3, f.pz(0, d), f.n(80), 1.0);
            fan(f, Particle.SPLASH, f.px(0, d), 0.5, f.pz(0, d), f.n(60), 0.8, 1.0);
        }
    }

    // 17. Вулкан
    static void volcano(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double cone = H * 0.4, craterR = R * 0.18;
        for (int i = 0; i < 5; i++) {
            double y = cone * i / 4.0, rad = lerp(R, craterR, i / 4.0);
            ring(f, i == 4 ? LAVA : ROCK, 0, y, 0, rad, f.n(26 - 3 * i), f.spin(0.02));
        }
        if (t < 0.5) {
            double k = easeOut(f.ph(0, 0.5)), hh = H * k;
            int m = f.n(30);
            for (int j = 0; j < m; j++) {
                double s = (double) j / (m - 1);
                double rad = craterR * (0.4 + 0.8 * s) * (1 + 0.2 * sin(s * 9 + f.spin(1))), ang = s * TWO_PI * 3 + f.spin(1.5);
                f.p(s < 0.5 ? Particle.SMOKE : Particle.LARGE_SMOKE, cos(ang) * rad, cone + hh * s, sin(ang) * rad);
                if (j % 3 == 0) f.d(cos(ang) * rad * 0.5, cone + hh * s * 0.7, sin(ang) * rad * 0.5, 0xFF6A10, 1.6);
            }
        } else {
            if (f.at(0.5)) {
                fan(f, Particle.LAVA, 0, cone, 0, f.n(100), 0.9, 1.6);
                fan(f, Particle.FLAME, 0, cone, 0, f.n(100), 1.1, 1.4);
                f.p(Particle.EXPLOSION_EMITTER, 0, cone, 0);
            }
            double g = 14.0, vy0 = sqrt(2 * g * max(1.0, H * 0.9)), T = 2 * vy0 / g;
            double tau = f.sec(0.5), tauEnd = (0.9 - 0.5) * f.total / 20.0;
            for (int i = 0; i < 14; i++) {
                Random rr = new Random(40 + i);
                double az = rr.nextDouble() * TWO_PI, reach = R * (0.4 + 1.0 * rr.nextDouble());
                double vyi = vy0 * (0.6 + 0.4 * rr.nextDouble()), Ti = 2 * vyi / g, vh = reach / T;
                double raw = tau - i * 0.17;
                if (raw < 0) continue;
                double cycle = Ti + 0.3, cyc = floor(raw / cycle);
                if (cyc * cycle + i * 0.17 > tauEnd) continue;
                double ti = raw - cyc * cycle;
                if (ti > Ti) continue;
                for (int k = 0; k < 4; k++) {
                    double tk = ti - k * 0.06;
                    if (tk < 0) break;
                    double y = cone + vyi * tk - 0.5 * g * tk * tk;
                    if (y < 0) break;
                    double x = cos(az) * vh * tk, z = sin(az) * vh * tk;
                    if (k == 0) {
                        f.p(Particle.FLAME, x, y, z);
                        f.d(x, y, z, 0xFF6A10, 2.2);
                    } else {
                        f.p(Particle.SMOKE, x, y, z);
                    }
                }
            }
            if (t < 0.9) {
                double ch = H * 1.2 * easeOut(f.ph(0.5, 0.62));
                line(f, LAVA, 0, cone, 0, 0, cone + ch, 0, f.n(20));
            }
            double pk = f.ph(0.5, 0.78);
            if (pk > 0 && pk < 1) ring(f, Pen.fade(0x9A8070, 0x303030, 2.0), 0, 0.3, 0, R * 1.6 * easeOut(pk), f.n(70), 0);
        }
    }

    // 18. Землетрясение
    static void earthquake(Frame f) {
        double R = f.r, t = f.t, y = 0.15;
        if (t < 0.97) {
            for (int i = 0; i < 5; i++) {
                double u = (t * 2.4 + i * 0.2) % 1.0;
                ring(f, Pen.fade(0xB09070, 0x403020, 1.6), 0, y, 0, R * u * (t > 0.7 ? 1.0 : 0.85), f.n(30 + 40 * u), 0);
            }
        }
        double grow = easeOut(f.ph(0.05, 0.55));
        for (int k = 0; k < 8; k++) {
            double a = k * TWO_PI / 8 + 0.2, len = R * grow * (0.8 + 0.2 * sin(k * 3.1));
            int m = f.n(22);
            for (int j = 1; j <= m; j++) {
                double dd = len * j / m, jit = sin(dd * 2.7 + k * 1.9) * 0.5;
                double x = cos(a) * dd - sin(a) * jit, z = sin(a) * dd + cos(a) * jit;
                f.d(x, y, z, j % 2 == 0 ? 0xFF5A10 : 0x201510, 1.2);
            }
        }
        for (int i = 0; i < f.n(8); i++) {
            double a = f.rnd.nextDouble() * TWO_PI, rr = f.rnd.nextDouble() * R * grow;
            f.blk(Material.COARSE_DIRT, cos(a) * rr, y + f.rnd.nextDouble() * 0.8, sin(a) * rr);
        }
        if (f.at(0.7)) {
            fan(f, Particle.CLOUD, 0, 0.3, 0, f.n(100), 0.7, 0.4);
            for (int i = 0; i < f.n(40); i++) {
                double a = f.rnd.nextDouble() * TWO_PI, rr = f.rnd.nextDouble() * R * 0.9;
                f.blk(Material.STONE, cos(a) * rr, 0.4 + f.rnd.nextDouble() * 2.5, sin(a) * rr);
            }
        }
        double sw = f.ph(0.7, 0.92);
        if (sw > 0 && sw < 1) ring(f, Pen.fade(0xFFE0A0, 0x504030, 2.0), 0, 0.25, 0, R * 1.6 * easeOut(sw), f.n(80), 0);
    }

    // 19. Каменный голем
    private static final double[][] GOLEM = buildGolem();

    private static double[][] buildGolem() {
        double[][] boxes = {
                {-0.55, 1.1, 0, 0.4, 1.1, 0.4}, {0.55, 1.1, 0, 0.4, 1.1, 0.4}, {0, 3.5, 0, 1.1, 1.3, 0.7},
                {-1.75, 3.4, 0, 0.45, 1.4, 0.45}, {1.75, 3.4, 0, 0.45, 1.4, 0.45}, {0, 5.45, 0, 0.6, 0.65, 0.6}};
        int[] counts = {26, 26, 70, 28, 28, 30};
        int total = 0;
        for (int c : counts) total += c;
        double[][] out = new double[total][];
        Random r = new Random(7);
        int idx = 0;
        for (int b = 0; b < boxes.length; b++) {
            for (int k = 0; k < counts[b]; k++) {
                double[] v = {r.nextDouble() * 2 - 1, r.nextDouble() * 2 - 1, r.nextDouble() * 2 - 1};
                if (r.nextDouble() < 0.75) v[r.nextInt(3)] = r.nextBoolean() ? 1 : -1;
                double[] bx = boxes[b];
                out[idx++] = new double[]{bx[0] + v[0] * bx[3], bx[1] + v[1] * bx[4], bx[2] + v[2] * bx[5],
                        r.nextDouble(), r.nextDouble() * TWO_PI};
            }
        }
        return out;
    }

    static void golem(Frame f) {
        double R = f.r, H = f.h, t = f.t, u = H / 6.2;
        double build = H * easeOut(f.ph(0, 0.55));
        double shake = (t >= 0.55 && t < 0.8) ? 0.04 * sin(f.tick * 1.7) : 0;
        double tau = f.sec(0.8);
        for (int i = 0; i < GOLEM.length; i++) {
            double[] p = GOLEM[i];
            double lat = p[0] * u, y = p[1] * u, fw = p[2] * u;
            if (t < 0.8) {
                if (y > build) continue;
            } else {
                double ti = max(0, tau - p[3] * 0.6);
                if (ti > 1.6) continue;
                y = max(0.1, y - 0.5 * 9 * ti * ti);
                lat = lat * (1 + 0.8 * ti) + cos(p[4]) * ti * R * 0.5;
                fw = fw * (1 + 0.8 * ti) + sin(p[4]) * ti * R * 0.5;
            }
            double x = f.px(lat, fw) + shake, z = f.pz(lat, fw);
            int k = i % 3;
            if (k == 0) f.blk(Material.STONE, x, y, z);
            else f.d(x, y, z, k == 1 ? 0x808080 : 0x5A5A5A, 1.6);
        }
        if (build > H * 0.9 && t < 0.8) {
            f.p(Particle.END_ROD, f.px(-0.25 * u, 0.6 * u), 5.6 * u, f.pz(-0.25 * u, 0.6 * u));
            f.p(Particle.END_ROD, f.px(0.25 * u, 0.6 * u), 5.6 * u, f.pz(0.25 * u, 0.6 * u));
        }
        if (f.at(0.8)) fan(f, Particle.CLOUD, 0, H * 0.3, 0, f.n(70), 0.35, 0.2);
    }

    // 20. Дракон
    private static double[] dragonPos(double R, double H, double u, double ph) {
        double lat = lerp(-R * 1.6, R * 1.6, u);
        double fw = R * 0.35 * sin(PI * u) + 0.7 * sin(u * 18 - ph);
        double y = H * 0.45 + H * 0.4 * sin(PI * u) + 0.6 * sin(u * 14 - ph);
        return new double[]{lat, y, fw};
    }

    static void dragon(Frame f) {
        double R = f.r, H = f.h, t = f.t;
        double u = -0.1 + 1.2 * t, ph = f.spin(1.2);
        for (int k = 0; k < 26; k++) {
            double uk = u - k * 0.011;
            if (uk < 0 || uk > 1) continue;
            double[] p = dragonPos(R, H, uk, ph);
            double w = max(0.12, R * 0.045 * (1.3 - abs(k - 6) / 18.0));
            double[][] off = {{w, 0}, {-w, 0}, {0, w}, {0, -w}};
            for (double[] o : off) {
                double fw = p[2] + o[0], y = p[1] + o[1];
                f.d(f.px(p[0], fw), y, f.pz(p[0], fw), (k % 4 == 0) ? 0xFF5A1A : 0x8A1010, 1.5);
            }
            if (k == 0) {
                f.p(Particle.FLAME, f.px(p[0], p[2]), p[1], f.pz(p[0], p[2]));
                f.p(Particle.END_ROD, f.px(p[0], p[2] + w), p[1] + w * 1.6, f.pz(p[0], p[2] + w));
                f.p(Particle.END_ROD, f.px(p[0], p[2] - w), p[1] + w * 1.6, f.pz(p[0], p[2] - w));
            }
            if (k == 6) {
                double flap = 0.55 * sin(f.tick * 0.35), span = R * 0.5;
                double[] angs = {0.35 + flap, flap * 0.8, -0.35 + flap * 0.6};
                double[] lens = {1.0, 0.85, 0.6};
                for (int side = -1; side <= 1; side += 2) {
                    double[] tl = new double[3], ty = new double[3], tf = new double[3];
                    for (int q = 0; q < 3; q++) {
                        tl[q] = p[0] - span * 0.3 * q;
                        ty[q] = p[1] + sin(angs[q]) * span * lens[q];
                        tf[q] = p[2] + side * cos(angs[q]) * span * lens[q];
                        lineL(f, WING, p[0], p[1], p[2], tl[q], ty[q], tf[q], f.n(8));
                    }
                    for (int q = 0; q < 2; q++) lineL(f, WING, tl[q], ty[q], tf[q], tl[q + 1], ty[q + 1], tf[q + 1], f.n(5));
                }
            }
        }
        double age = f.ph(0.62, 0.97);
        double[] hp = dragonPos(R, H, -0.1 + 1.2 * 0.62, 0);
        if (f.at(0.62)) burst(f, Particle.FLAME, f.px(hp[0], hp[2]), hp[1], f.pz(hp[0], hp[2]), f.n(60), 0.5);
        if (age > 0 && age < 1) {
            double rho = R * 2.0 * easeOut(age);
            for (int w = 0; w < 3; w++) {
                double rw = rho * (1 - 0.25 * w);
                int n = f.n(26);
                for (int i = 0; i < n; i++) {
                    double th = -1.0 + 2.0 * i / (n - 1.0);
                    double lat = hp[0] + sin(th) * rw, fw = hp[2] + cos(th) * rw;
                    double y = max(0.3, hp[1] * (1 - age) + 0.4 * w);
                    if (w == 0) f.p(Particle.FLAME, f.px(lat, fw), y, f.pz(lat, fw));
                    else f.d(f.px(lat, fw), y, f.pz(lat, fw), w == 1 ? 0xFF7A20 : 0xFFB040, 1.7);
                }
            }
        }
    }
}
