package dev.particlefx.core;

import dev.particlefx.fx.Renderer;

/** Зарегистрированная анимация: метаданные + отрисовщик кадров. */
public record Animation(AnimationInfo info, Renderer renderer, int index) {
}
