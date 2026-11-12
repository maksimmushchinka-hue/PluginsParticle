package dev.particlefx.gui;

import dev.particlefx.core.Animation;
import dev.particlefx.core.EffectSettings;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Уникальный владелец инвентаря плагина — по нему отличаем наши меню от обычных сундуков. */
public final class MenuHolder implements InventoryHolder {
    public enum Type { MAIN, SETTINGS }

    final Type type;
    final int page;
    final Animation anim;
    final EffectSettings settings;
    Inventory inventory;

    MenuHolder(Type type, int page, Animation anim, EffectSettings settings) {
        this.type = type;
        this.page = page;
        this.anim = anim;
        this.settings = settings;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
