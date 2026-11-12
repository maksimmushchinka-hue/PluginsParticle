package dev.particlefx.core;

/** Допустимый диапазон числового параметра. */
public record Range(double min, double max) {
    public Range {
        if (min > max) {
            double t = min;
            min = max;
            max = t;
        }
    }

    /** Возвращает значение в границах; NaN и бесконечности заменяются на def (тоже ограниченное). */
    public double fix(double v, double def) {
        if (Double.isNaN(v) || Double.isInfinite(v)) v = def;
        if (Double.isNaN(v) || Double.isInfinite(v)) v = min;
        return Math.max(min, Math.min(max, v));
    }
}
