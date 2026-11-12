package dev.particlefx.core;

import org.bukkit.Material;

/** Описание анимации: название, иконка, значения по умолчанию и применимые настройки. */
public record AnimationInfo(String id, String name, String description, Material icon, String particles,
                            String shape, String stages, String peak, double duration, double radius,
                            double height, boolean hasRadius, boolean hasHeight) {
}
