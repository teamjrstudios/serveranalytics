package com.gungens.serverAnalytics.libs;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
