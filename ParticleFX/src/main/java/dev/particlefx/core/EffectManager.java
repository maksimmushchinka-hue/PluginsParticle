package dev.particlefx.core;

import dev.particlefx.ParticleFXPlugin;
import dev.particlefx.fx.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** Единственный планировщик эффектов: одна повторяющаяся задача в основном потоке. */
public final class EffectManager {
    public enum Result { OK, DISABLED, PLAYER_LIMIT, SERVER_LIMIT }

    private static final int HARD_CAP = 100;

    private static final class Active {
        final UUID owner;
        final Animation anim;
        final EffectSettings s;
        final World world;
        final double ox, oy, oz, fx, fz, r, h;
        final int total;
        final boolean preview;
        final Random rnd = new Random();
        int tick;

        Active(UUID owner, Animation anim, EffectSettings s, World world, double ox, double oy, double oz,
               double fx, double fz, boolean preview) {
            this.owner = owner;
            this.anim = anim;
            this.s = s;
            this.world = world;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.fx = fx;
            this.fz = fz;
            this.r = s.radius * s.scale;
            this.h = s.height * s.scale;
            this.total = Math.max(20, (int) Math.round(s.duration * 20.0));
            this.preview = preview;
        }
    }

    private final ParticleFXPlugin plugin;
    private final List<Active> active = new ArrayList<>();
    private BukkitTask task;

    public EffectManager(ParticleFXPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, this::tickAll, 1L, 1L);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        active.clear();
    }

    public int activeCount() {
        return active.size();
    }

    public Result launch(Player owner, Animation anim, EffectSettings raw, boolean preview, boolean bypass) {
        PluginSettings cfg = plugin.settings();
        if (!cfg.isEnabled(anim.info())) return Result.DISABLED;
        EffectSettings s = raw.copy();
        s.sanitize(cfg, anim.info());
        if (preview) {
            s.duration = Math.min(s.duration, cfg.previewMaxSeconds);
            s.scale = Math.max(0.1, s.scale * cfg.previewScale);
            s.density = Math.max(0.1, s.density * cfg.previewDensity);
            active.removeIf(a -> a.preview && a.owner.equals(owner.getUniqueId()));
        }
        if (active.size() >= HARD_CAP) return Result.SERVER_LIMIT;
        if (!bypass) {
            int mine = 0;
            for (Active a : active) if (a.owner.equals(owner.getUniqueId())) mine++;
            if (mine >= cfg.maxActivePlayer) return Result.PLAYER_LIMIT;
            if (active.size() >= cfg.maxActiveServer) return Result.SERVER_LIMIT;
        }
        Location loc = owner.getLocation();
        double dx = loc.getDirection().getX(), dz = loc.getDirection().getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0e-4) {
            double yaw = Math.toRadians(loc.getYaw());
            dx = -Math.sin(yaw);
            dz = Math.cos(yaw);
            len = 1.0;
        }
        dx /= len;
        dz /= len;
        LaunchMode mode = preview ? LaunchMode.FRONT : s.mode;
        double dist = mode == LaunchMode.FRONT ? Math.min(60.0, cfg.frontDistance + 0.5 * s.radius * s.scale) : 0.0;
        double ox = loc.getX() + dx * dist, oz = loc.getZ() + dz * dist, oy = loc.getY();
        if (!Double.isFinite(ox) || !Double.isFinite(oy) || !Double.isFinite(oz)) return Result.DISABLED;
        active.add(new Active(owner.getUniqueId(), anim, s, loc.getWorld(), ox, oy, oz, dx, dz, preview));
        return Result.OK;
    }

    public int stopAll(UUID owner) {
        int before = active.size();
        active.removeIf(a -> a.owner.equals(owner));
        return before - active.size();
    }

    public void purgeDisabled() {
        PluginSettings cfg = plugin.settings();
        active.removeIf(a -> !cfg.isEnabled(a.anim.info()));
    }

    private void tickAll() {
        if (active.isEmpty()) return;
        PluginSettings cfg = plugin.settings();
        int global = 0;
        Iterator<Active> it = active.iterator();
        while (it.hasNext()) {
            Active a = it.next();
            Player owner = Bukkit.getPlayer(a.owner);
            if (owner == null || !owner.isOnline()) {
                it.remove();
                continue;
            }
            if (Bukkit.getWorld(a.world.getUID()) == null || (a.preview && !owner.getWorld().equals(a.world))) {
                it.remove();
                continue;
            }
            if (a.tick > a.total) {
                it.remove();
                continue;
            }
            int budget = Math.min(cfg.maxParticlesEffect, cfg.maxParticlesGlobal - global);
            if (budget <= 0) {
                a.tick++;
                continue;
            }
            Player viewer = (a.preview && cfg.previewPrivate) ? owner : null;
            Frame fr = new Frame(a.world, a.ox, a.oy, a.oz, a.fx, a.fz, a.tick, a.total, a.r, a.h, a.s.speed,
                    a.s.density, a.rnd, viewer, cfg.longRange, budget);
            try {
                a.anim.renderer().render(fr);
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Ошибка анимации " + a.anim.info().id() + ", эффект остановлен", ex);
                it.remove();
                continue;
            }
            global += fr.used();
            a.tick++;
        }
    }
}
