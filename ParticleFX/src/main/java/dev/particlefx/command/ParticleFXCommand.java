package dev.particlefx.command;

import dev.particlefx.ParticleFXPlugin;
import dev.particlefx.core.Animation;
import dev.particlefx.core.EffectManager;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** Команда /particlefx (алиас /pfx). */
public final class ParticleFXCommand implements CommandExecutor, TabCompleter {
    private final ParticleFXPlugin plugin;

    public ParticleFXCommand(ParticleFXPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub = args.length == 0 ? "menu" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu" -> menu(sender);
            case "list" -> list(sender);
            case "play" -> play(sender, args);
            case "stop" -> stop(sender, args);
            case "reload" -> reload(sender);
            default -> Text.send(sender, "&cНеизвестная подкоманда. Используйте: &f/" + label
                    + " [menu|play <название> [игрок]|stop [игрок]|list|reload]");
        }
        return true;
    }

    private void menu(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            Text.send(sender, "&cМеню доступно только игрокам. Для запуска из консоли: &f/particlefx play <название> <игрок>");
            return;
        }
        if (!p.hasPermission("particlefx.use")) {
            Text.send(p, "&cУ вас нет прав.");
            return;
        }
        plugin.menu().openMain(p, 0);
    }

    private void list(CommandSender sender) {
        if (!sender.hasPermission("particlefx.use")) {
            Text.send(sender, "&cУ вас нет прав.");
            return;
        }
        List<Animation> list = plugin.registry().enabled(plugin.settings());
        Text.send(sender, "&dДоступно анимаций: &f" + list.size());
        StringBuilder sb = new StringBuilder();
        for (Animation a : list) {
            if (sb.length() > 0) sb.append("&8, ");
            sb.append("&7").append(a.index()).append(".&f").append(a.info().id());
            if (sb.length() > 220) {
                sender.sendMessage(Text.c(sb.toString()));
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) sender.sendMessage(Text.c(sb.toString()));
    }

    private void play(CommandSender sender, String[] args) {
        if (!sender.hasPermission("particlefx.use") || !sender.hasPermission("particlefx.play")) {
            Text.send(sender, "&cУ вас нет прав на запуск эффектов.");
            return;
        }
        if (args.length < 2) {
            Text.send(sender, "&cУкажите название: &f/particlefx play <название> [игрок]&c. Список: &f/particlefx list");
            return;
        }
        Animation a = plugin.registry().find(args[1]);
        if (a == null || !plugin.settings().isEnabled(a.info())) {
            Text.send(sender, "&cЭффект «&f" + args[1] + "&c» не найден или отключён. Список: &f/particlefx list");
            return;
        }
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                Text.send(sender, "&cИгрок «&f" + args[2] + "&c» не в сети.");
                return;
            }
            if (!(sender instanceof Player self && self.equals(target)) && !sender.hasPermission("particlefx.admin")) {
                Text.send(sender, "&cЗапускать эффекты для других игроков может только администратор.");
                return;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            Text.send(sender, "&cИз консоли укажите игрока: &f/particlefx play " + a.info().id() + " <игрок>");
            return;
        }
        EffectSettings s = plugin.settings().defaultsFor(a.info());
        boolean bypass = sender.hasPermission("particlefx.admin");
        EffectManager.Result r = plugin.manager().launch(target, a, s, false, bypass);
        switch (r) {
            case OK -> Text.send(sender, "&aЗапущено: &f" + a.info().name() + (target.equals(sender) ? "" : " &7для " + target.getName()));
            case DISABLED -> Text.send(sender, "&cЭта анимация отключена.");
            case PLAYER_LIMIT -> Text.send(sender, "&cДостигнут лимит одновременных эффектов игрока. &f/particlefx stop");
            case SERVER_LIMIT -> Text.send(sender, "&cНа сервере слишком много активных эффектов.");
        }
    }

    private void stop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("particlefx.use")) {
            Text.send(sender, "&cУ вас нет прав.");
            return;
        }
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                Text.send(sender, "&cИгрок «&f" + args[1] + "&c» не в сети.");
                return;
            }
            if (!(sender instanceof Player self && self.equals(target)) && !sender.hasPermission("particlefx.admin")) {
                Text.send(sender, "&cОстанавливать эффекты других игроков может только администратор.");
                return;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            Text.send(sender, "&cИз консоли укажите игрока: &f/particlefx stop <игрок>");
            return;
        }
        int n = plugin.manager().stopAll(target.getUniqueId());
        Text.send(sender, n > 0 ? "&aОстановлено эффектов: &f" + n : "&7Активных эффектов нет.");
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("particlefx.admin")) {
            Text.send(sender, "&cУ вас нет прав.");
            return;
        }
        plugin.reloadAll();
        Text.send(sender, "&aКонфигурация перезагружена.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("menu", "play", "stop", "list"));
            if (sender.hasPermission("particlefx.admin")) subs.add("reload");
            for (String s : subs) if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(s);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
            for (Animation a : plugin.registry().enabled(plugin.settings())) {
                if (a.info().id().startsWith(args[1].toLowerCase(Locale.ROOT))) out.add(a.info().id());
            }
        } else if ((args.length == 3 && args[0].equalsIgnoreCase("play"))
                || (args.length == 2 && args[0].equalsIgnoreCase("stop"))) {
            if (sender.hasPermission("particlefx.admin")) {
                String pre = args[args.length - 1].toLowerCase(Locale.ROOT);
                for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase(Locale.ROOT).startsWith(pre)) out.add(p.getName());
            }
        }
        return out;
    }
}
