package com.lilyy2565.hypixelautotip;

public enum ChatColor {
    BLACK("§0", "Black"),
    DARK_BLUE("§1", "Dark Blue"),
    DARK_GREEN("§2", "Dark Green"),
    DARK_AQUA("§3", "Dark Aqua"),
    DARK_RED("§4", "Dark Red"),
    DARK_PURPLE("§5", "Dark Purple"),
    GOLD("§6", "Gold"),
    GRAY("§7", "Gray"),
    DARK_GRAY("§8", "Dark Gray"),
    BLUE("§9", "Blue"),
    GREEN("§a", "Green"),
    AQUA("§b", "Aqua"),
    RED("§c", "Red"),
    LIGHT_PURPLE("§d", "Light Purple"),
    YELLOW("§e", "Yellow"),
    WHITE("§f", "White");

    private final String code;
    private final String name;

    ChatColor(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ChatColor fromName(String name) {
        for (ChatColor color : values()) {
            if (color.toString().equals(name)) {
                return color;
            }
        }

        return LIGHT_PURPLE;
    }
}