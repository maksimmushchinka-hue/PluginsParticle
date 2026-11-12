package dev.particlefx.gui;

import dev.particlefx.ParticleFXPlugin;
import dev.particlefx.core.Animation;
import dev.particlefx.core.AnimationInfo;
import dev.particlefx.core.EffectManager;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.PluginSettings;
import dev.particlefx.core.Range;
import dev.particlefx.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Меню на базе стандартного инвентаря: список анимаций и настройки эффекта. */
public final class MenuManager implements Listener {
    private static final int PER_PAGE = 45;
    private static final int SLOT_PREV = 45, SLOT_PAGE = 48, SLOT_CLOSE = 49, SLOT_NEXT = 53;
    private static final int SLOT_BACK = 45, SLOT_PREVIEW = 47, SLOT_INFO = 49, SLOT_RESET = 50, SLOT_PLAY = 51,
            SLOT_MODE = 40;
    private static final String[] PNAME = {"Масштаб", "Длительность (с)", "Скорость", "Плотность частиц",
            "Радиус (блоков)", "Высота (блоков)"};
    private static final Material[] PICON = {Material.AMETHYST_SHARD, Material.CLOCK, Material.FEATHER,
            Material.GLOWSTONE_DUST, Material.COMPASS, Material.LADDER};

    private final ParticleFXPlugin plugin;

    public MenuManager(ParticleFXPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------- предметы ----------
    private static ItemStack item(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.c(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> l = new ArrayList<>();
                for (String s : lore) l.add(Text.c(s));
                meta.lore(l);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    private static ItemStack item(Material m, String name, String... lore) {
        return item(m, name, List.of(lore));
    }

    // ---------- главное меню ----------
    public void openMain(Player p, int page) {
        List<Animation> list = plugin.registry().enabled(plugin.settings());
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        int pg = Math.max(0, Math.min(pages - 1, page));
        MenuHolder h = new MenuHolder(MenuHolder.Type.MAIN, pg, null, null);
        h.inventory = Bukkit.createInventory(h, 54, Text.c("&5ParticleFX &8— анимации &7(" + (pg + 1) + "/" + pages + ")"));
        fillMain(h, list, pages);
        p.openInventory(h.inventory);
    }

    private void fillMain(MenuHolder h, List<Animation> list, int pages) {
        Inventory inv = h.inventory;
        inv.clear();
        for (int s = 0; s < PER_PAGE; s++) {
            int idx = h.page * PER_PAGE + s;
            if (idx >= list.size()) break;
            Animation a = list.get(idx);
            AnimationInfo i = a.info();
            List<String> lore = new ArrayList<>(Text.wrap(i.description(), 38, "&7"));
            lore.add("");
            lore.add("&8Номер: &f" + a.index() + " &8| ID: &f" + i.id());
            lore.add("&8Длительность по умолч.: &f" + Text.num(i.duration()) + " с");
            lore.add("");
            lore.add("&eНажмите, чтобы настроить и запустить");
            inv.setItem(s, item(i.icon(), "&b" + i.name(), lore));
        }
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int s = 45; s < 54; s++) inv.setItem(s, filler);
        if (h.page > 0) inv.setItem(SLOT_PREV, item(Material.ARROW, "&a« Назад", "&7Страница " + h.page + "/" + pages));
        if (h.page < pages - 1) {
            inv.setItem(SLOT_NEXT, item(Material.ARROW, "&aВперёд »", "&7Страница " + (h.page + 2) + "/" + pages));
        }
        inv.setItem(SLOT_PAGE, item(Material.PAPER, "&fСтраница " + (h.page + 1) + "/" + pages,
                "&7Анимаций: &f" + list.size()));
        inv.setItem(SLOT_CLOSE, item(Material.BARRIER, "&cЗакрыть"));
    }

    // ---------- настройки ----------
    public void openSettings(Player p, Animation a, int returnPage) {
        EffectSettings s = plugin.settings().defaultsFor(a.info());
        MenuHolder h = new MenuHolder(MenuHolder.Type.SETTINGS, returnPage, a, s);
        h.inventory = Bukkit.createInventory(h, 54, Text.c("&5Настройка: &f" + a.info().name()));
        fillSettings(h);
        p.openInventory(h.inventory);
    }

    private static int base(int param) {
        return (1 + param / 2) * 9 + (param % 2 == 0 ? 1 : 5);
    }

    private static boolean applicable(AnimationInfo i, int param) {
        if (param == 4) return i.hasRadius();
        if (param == 5) return i.hasHeight();
        return true;
    }

    private static double value(EffectSettings s, int param) {
        return switch (param) {
            case 0 -> s.scale;
            case 1 -> s.duration;
            case 2 -> s.speed;
            case 3 -> s.density;
            case 4 -> s.radius;
            default -> s.height;
        };
    }

    private static void set(EffectSettings s, int param, double v) {
        switch (param) {
            case 0 -> s.scale = v;
            case 1 -> s.duration = v;
            case 2 -> s.speed = v;
            case 3 -> s.density = v;
            case 4 -> s.radius = v;
            default -> s.height = v;
        }
    }

    private Range range(int param) {
        PluginSettings c = plugin.settings();
        return switch (param) {
            case 0 -> c.scale;
            case 1 -> c.duration;
            case 2 -> c.speed;
            case 3 -> c.density;
            case 4 -> c.radius;
            default -> c.height;
        };
    }

    private double step(int param) {
        PluginSettings c = plugin.settings();
        return switch (param) {
            case 0 -> c.stepScale;
            case 1 -> c.stepDuration;
            case 2 -> c.stepSpeed;
            case 3 -> c.stepDensity;
            case 4 -> c.stepRadius;
            default -> c.stepHeight;
        };
    }

    private void fillSettings(MenuHolder h) {
        Inventory inv = h.inventory;
        inv.clear();
        AnimationInfo info = h.anim.info();
        EffectSettings s = h.settings;
        ItemStack filler = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);
        EffectSettings defs = plugin.settings().defaultsFor(info);
        for (int i = 0; i < 6; i++) {
            int b = base(i);
            if (!applicable(info, i)) {
                ItemStack na = item(Material.GRAY_STAINED_GLASS_PANE, "&8" + PNAME[i] + ": не применимо");
                inv.setItem(b, na);
                inv.setItem(b + 1, na);
                inv.setItem(b + 2, na);
                continue;
            }
            Range r = range(i);
            double st = step(i);
            inv.setItem(b, item(Material.RED_STAINED_GLASS_PANE, "&c− " + Text.num(st),
                    "&7Shift: &f− " + Text.num(st * 5)));
            inv.setItem(b + 1, item(PICON[i], "&e" + PNAME[i] + ": &f" + Text.num(value(s, i)),
                    "&7Минимум: &f" + Text.num(r.min()), "&7Максимум: &f" + Text.num(r.max()),
                    "&7По умолчанию: &f" + Text.num(value(defs, i))));
            inv.setItem(b + 2, item(Material.LIME_STAINED_GLASS_PANE, "&a+ " + Text.num(st),
                    "&7Shift: &f+ " + Text.num(st * 5)));
        }
        inv.setItem(SLOT_MODE, item(Material.ENDER_PEARL, "&eРежим запуска: &f" + s.mode.label(),
                "&7Нажмите, чтобы переключить:", "&7• На месте игрока", "&7• Перед игроком"));
        inv.setItem(SLOT_BACK, item(Material.ARROW, "&7« К списку анимаций"));
        inv.setItem(SLOT_PREVIEW, item(Material.YELLOW_CONCRETE, "&eПредпросмотр",
                "&7Короткая уменьшенная версия,", "&7видимая только вам"));
        List<String> infoLore = new ArrayList<>(Text.wrap(info.description(), 38, "&7"));
        infoLore.add("");
        infoLore.add("&8Частицы: &7" + info.particles());
        infoLore.add("&8Этапы: &7" + info.stages());
        infoLore.add("&8Пик: &7" + info.peak());
        inv.setItem(SLOT_INFO, item(info.icon(), "&b" + info.name(), trim(infoLore)));
        inv.setItem(SLOT_RESET, item(Material.HOPPER, "&6Сбросить значения"));
        inv.setItem(SLOT_PLAY, item(Material.LIME_CONCRETE, "&a▶ Запустить"));
        inv.setItem(53, item(Material.BARRIER, "&cЗакрыть"));
    }

    private static List<String> trim(List<String> in) {
        List<String> out = new ArrayList<>();
        for (String s : in) {
            if (s.length() > 120) s = s.substring(0, 117) + "...";
            out.addAll(s.length() > 44 ? Text.wrap(s.replaceFirst("^(&.)+", ""), 44, "&7") : List.of(s));
        }
        return out;
    }

    // ---------- события ----------
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof MenuHolder h)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        Inventory clicked = e.getClickedInventory();
        if (clicked == null || clicked != top) return;
        int slot = e.getSlot();
        if (slot < 0 || slot >= top.getSize()) return;
        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType().isAir()) return;
        if (h.type == MenuHolder.Type.MAIN) clickMain(p, h, slot);
        else clickSettings(p, h, slot, e.isShiftClick());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof MenuHolder) e.setCancelled(true);
    }

    private void clickMain(Player p, MenuHolder h, int slot) {
        List<Animation> list = plugin.registry().enabled(plugin.settings());
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        if (slot < PER_PAGE) {
            int idx = h.page * PER_PAGE + slot;
            if (idx < list.size()) openSettings(p, list.get(idx), h.page);
        } else if (slot == SLOT_PREV && h.page > 0) {
            openMain(p, h.page - 1);
        } else if (slot == SLOT_NEXT && h.page < pages - 1) {
            openMain(p, h.page + 1);
        } else if (slot == SLOT_CLOSE) {
            closeLater(p);
        }
    }

    private void clickSettings(Player p, MenuHolder h, int slot, boolean shift) {
        AnimationInfo info = h.anim.info();
        for (int i = 0; i < 6; i++) {
            if (!applicable(info, i)) continue;
            int b = base(i);
            if (slot == b || slot == b + 2) {
                double mult = shift ? 5.0 : 1.0;
                double v = value(h.settings, i) + (slot == b ? -1 : 1) * step(i) * mult;
                v = Math.round(v * 100.0) / 100.0;
                set(h.settings, i, range(i).fix(v, value(h.settings, i)));
                fillSettings(h);
                return;
            }
        }
        switch (slot) {
            case SLOT_MODE -> {
                h.settings.mode = h.settings.mode.next();
                fillSettings(h);
            }
            case SLOT_RESET -> {
                EffectSettings d = plugin.settings().defaultsFor(info);
                h.settings.scale = d.scale;
                h.settings.duration = d.duration;
                h.settings.speed = d.speed;
                h.settings.density = d.density;
                h.settings.radius = d.radius;
                h.settings.height = d.height;
                h.settings.mode = d.mode;
                fillSettings(h);
            }
            case SLOT_BACK -> openMain(p, h.page);
            case SLOT_PLAY -> launch(p, h, false);
            case SLOT_PREVIEW -> launch(p, h, true);
            case 53 -> closeLater(p);
            default -> {
                // пустые и информационные ячейки игнорируются
            }
        }
    }

    private void launch(Player p, MenuHolder h, boolean preview) {
        if (!p.hasPermission(preview ? "particlefx.use" : "particlefx.play")) {
            Text.send(p, "&cУ вас нет прав для этого действия.");
            return;
        }
        boolean bypass = p.hasPermission("particlefx.admin");
        EffectManager.Result r = plugin.manager().launch(p, h.anim, h.settings, preview, bypass);
        switch (r) {
            case OK -> {
                Text.send(p, (preview ? "&eПредпросмотр: &f" : "&aЗапущено: &f") + h.anim.info().name());
                closeLater(p);
            }
            case DISABLED -> Text.send(p, "&cЭта анимация отключена.");
            case PLAYER_LIMIT -> Text.send(p, "&cДостигнут лимит ваших эффектов. Используйте &f/particlefx stop&c.");
            case SERVER_LIMIT -> Text.send(p, "&cНа сервере слишком много активных эффектов. Попробуйте позже.");
        }
    }

    private void closeLater(Player p) {
        Bukkit.getScheduler().runTask(plugin, p::closeInventory);
    }

    /** Закрывает все открытые меню плагина (при перезагрузке и выключении). */
    public void closeAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) p.closeInventory();
        }
    }
}
