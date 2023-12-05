package me.rodrigo.lobby.lib;

public class MinecraftColorCode {
    public static String Black = "§0";
    public static String DarkBlue = "§1";
    public static String DarkGreen = "§2";
    public static String DarkAqua = "§3";
    public static String DarkRed = "§4";
    public static String DarkPurple = "§5";
    public static String Gold = "§6";
    public static String Gray = "§7";
    public static String DarkGray = "§8";
    public static String Grey = "§7";
    public static String DarkGrey = "§8";
    public static String Blue = "§9";
    public static String Green = "§a";
    public static String Aqua = "§b";
    public static String Red = "§c";
    public static String LightPurple = "§d";
    public static String Yellow = "§e";
    public static String White = "§f";
    public static String Obfuscated = "§k";
    public static String Magic = "§k";
    public static String Bold = "§b";
    public static String Strikethrough = "§m";
    public static String Underline = "§n";
    public static String Italic = "§o";
    public static String Reset = "§r";
    public static String BLACK = "§0";
    public static String DARK_BLUE = "§1";
    public static String DARK_GREEN = "§2";
    public static String DARK_AQUA = "§3";
    public static String DARK_RED = "§4";
    public static String DARK_PURPLE = "§5";
    public static String GOLD = "§6";
    public static String GRAY = "§7";
    public static String DARK_GRAY = "§8";
    public static String GREY = "§7";
    public static String DARK_GREY = "§8";
    public static String BLUE = "§9";
    public static String GREEN = "§a";
    public static String AQUA = "§b";
    public static String RED = "§c";
    public static String LIGHT_PURPLE = "§d";
    public static String YELLOW = "§e";
    public static String WHITE = "§f";
    public static String OBFUSCATED = "§k";
    public static String MAGIC = "§k";
    public static String BOLD = "§b";
    public static String STRIKETHROUGH = "§m";
    public static String UNDERLINE = "§n";
    public static String ITALIC = "§o";
    public static String RESET = "§r";

    public static String ReplaceAllColorCodes(final String Identifier, final String RawStr) {
        return RawStr.replaceAll(Identifier, "§");
    }

    public static String ReplaceAllAmpersands(final String RawStr) {
        return RawStr.replaceAll("&", "§");
    }
}