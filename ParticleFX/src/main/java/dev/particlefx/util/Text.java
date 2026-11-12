package dev.particlefx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/** Работа с текстом: цветовые коды через '&', сообщения и форматирование чисел. */
public final class Text {
    public static final String PREFIX = "&8[&dParticleFX&8] &r";
    private static final LegacyComponentSerializer LEG = LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    public static Component c(String s) {
        return LEG.deserialize(s).decoration(TextDecoration.ITALIC, false);
    }

    public static void send(CommandSender to, String msg) {
        to.sendMessage(LEG.deserialize(PREFIX + msg));
    }

    public static String num(double v) {
        String s = String.format(Locale.US, "%.2f", v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s;
    }

    public static List<String> wrap(String s, int width, String prefix) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String w : s.split(" ")) {
            if (line.length() + w.length() + 1 > width && line.length() > 0) {
                out.add(prefix + line);
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(w);
        }
        if (line.length() > 0) out.add(prefix + line);
        return out;
    }
}
