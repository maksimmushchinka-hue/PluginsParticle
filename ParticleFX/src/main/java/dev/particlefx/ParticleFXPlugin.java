package dev.particlefx;

import dev.particlefx.command.ParticleFXCommand;
import dev.particlefx.core.AnimationRegistry;
import dev.particlefx.core.EffectManager;
import dev.particlefx.core.PluginSettings;
import dev.particlefx.gui.MenuManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParticleFXPlugin extends JavaPlugin implements Listener {
    private PluginSettings settings;
    private AnimationRegistry registry;
    private EffectManager manager;
    private MenuManager menu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            registry = new AnimationRegistry();
        } catch (IllegalStateException ex) {
            getLogger().severe("Ошибка реестра анимаций: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        settings = new PluginSettings(this);
        settings.load();
        manager = new EffectManager(this);
        menu = new MenuManager(this);
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(this, this);
        PluginCommand cmd = getCommand("particlefx");
        if (cmd == null) {
            getLogger().severe("Команда particlefx не объявлена в plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ParticleFXCommand handler = new ParticleFXCommand(this);
        cmd.setExecutor(handler);
        cmd.setTabCompleter(handler);
        manager.start();
        getLogger().info("Загружено анимаций: " + registry.all().size());
    }

    @Override
    public void onDisable() {
        if (menu != null) menu.closeAll();
        if (manager != null) manager.shutdown();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (manager != null) manager.stopAll(e.getPlayer().getUniqueId());
    }

    public void reloadAll() {
        reloadConfig();
        settings.load();
        menu.closeAll();
        manager.purgeDisabled();
    }

    public PluginSettings settings() { return settings; }
    public AnimationRegistry registry() { return registry; }
    public EffectManager manager() { return manager; }
    public MenuManager menu() { return menu; }
}
