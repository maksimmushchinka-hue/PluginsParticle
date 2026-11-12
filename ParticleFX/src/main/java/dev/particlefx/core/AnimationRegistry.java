package dev.particlefx.core;

import dev.particlefx.fx.ElementFx;
import dev.particlefx.fx.FinalFx;
import dev.particlefx.fx.FormFx;
import dev.particlefx.fx.MythFx;
import dev.particlefx.fx.Renderer;
import dev.particlefx.fx.SpaceFx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Реестр анимаций: связывает метаданные каталога с реализациями. */
public final class AnimationRegistry {
    private final LinkedHashMap<String, Animation> byId = new LinkedHashMap<>();

    public AnimationRegistry() {
        Map<String, Renderer> impl = new HashMap<>();
        SpaceFx.register(impl);
        ElementFx.register(impl);
        MythFx.register(impl);
        FormFx.register(impl);
        FinalFx.register(impl);
        int i = 1;
        for (AnimationInfo info : AnimationCatalog.ALL) {
            Renderer r = impl.get(info.id());
            if (r == null) throw new IllegalStateException("Нет реализации анимации: " + info.id());
            byId.put(info.id(), new Animation(info, r, i++));
        }
        for (String id : impl.keySet()) {
            if (!byId.containsKey(id)) throw new IllegalStateException("Реализация без каталога: " + id);
        }
    }

    public Collection<Animation> all() {
        return byId.values();
    }

    public List<Animation> enabled(PluginSettings cfg) {
        List<Animation> out = new ArrayList<>();
        for (Animation a : byId.values()) if (cfg.isEnabled(a.info())) out.add(a);
        return out;
    }

    public Animation get(String id) {
        return byId.get(id);
    }

    /** Поиск по идентификатору, номеру (1..50) или названию без учёта регистра. */
    public Animation find(String key) {
        if (key == null) return null;
        String k = key.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        Animation a = byId.get(k);
        if (a != null) return a;
        try {
            int n = Integer.parseInt(k);
            for (Animation x : byId.values()) if (x.index() == n) return x;
        } catch (NumberFormatException ignored) {
            // не число
        }
        for (Animation x : byId.values()) {
            if (x.info().name().toLowerCase(Locale.ROOT).replace(' ', '_').equals(k)) return x;
        }
        return null;
    }
}
