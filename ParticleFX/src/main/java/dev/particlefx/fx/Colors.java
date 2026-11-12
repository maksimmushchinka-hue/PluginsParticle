package dev.particlefx.fx;

/** Вспомогательные функции для работы с цветом (0xRRGGBB). */
public final class Colors {
    private Colors() {}

    public static int hue(double h) {
        h = h - Math.floor(h);
        double x = h * 6.0;
        int i = (int) x;
        double fr = x - i;
        double q = 1.0 - fr;
        int r, g, b;
        switch (i % 6) {
            case 0 -> { r = 255; g = (int) (255 * fr); b = 0; }
            case 1 -> { r = (int) (255 * q); g = 255; b = 0; }
            case 2 -> { r = 0; g = 255; b = (int) (255 * fr); }
            case 3 -> { r = 0; g = (int) (255 * q); b = 255; }
            case 4 -> { r = (int) (255 * fr); g = 0; b = 255; }
            default -> { r = 255; g = 0; b = (int) (255 * q); }
        }
        return (r << 16) | (g << 8) | b;
    }

    public static int mix(int a, int b, double x) {
        x = Shapes.clamp01(x);
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int r = (int) (ar + (br - ar) * x);
        int g = (int) (ag + (bg - ag) * x);
        int bl = (int) (ab + (bb - ab) * x);
        return (r << 16) | (g << 8) | bl;
    }
}
