package dev.particlefx.fx;

/** Рисует один кадр (один игровой тик) анимации. */
@FunctionalInterface
public interface Renderer {
    void render(Frame f);
}
