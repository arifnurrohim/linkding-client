package app.arifnurrohim.linkding;

import android.content.Context;

import java.util.Locale;

public final class Catppuccin {
    public static final String[] FLAVOR_KEYS = {"latte", "frappe", "macchiato", "mocha"};
    public static final String[] FLAVOR_NAMES = {"Latte", "Frappe", "Macchiato", "Mocha"};
    public static final String[] FLAVOR_TAGS = {"Light", "Dark", "Dark", "Dark"};

    public static final String[] ACCENT_KEYS = {
            "rosewater", "flamingo", "pink", "mauve", "red", "maroon", "peach",
            "yellow", "green", "teal", "sky", "sapphire", "blue", "lavender"
    };
    public static final String[] ACCENT_NAMES = {
            "Rosewater", "Flamingo", "Pink", "Mauve", "Red", "Maroon", "Peach",
            "Yellow", "Green", "Teal", "Sky", "Sapphire", "Blue", "Lavender"
    };

    public static final class Palette {
        public final String key;
        public final String name;
        public final boolean dark;
        public final int rosewater;
        public final int flamingo;
        public final int pink;
        public final int mauve;
        public final int red;
        public final int maroon;
        public final int peach;
        public final int yellow;
        public final int green;
        public final int teal;
        public final int sky;
        public final int sapphire;
        public final int blue;
        public final int lavender;
        public final int text;
        public final int subtext1;
        public final int subtext0;
        public final int overlay2;
        public final int overlay1;
        public final int overlay0;
        public final int surface2;
        public final int surface1;
        public final int surface0;
        public final int base;
        public final int mantle;
        public final int crust;

        Palette(String key, String name, boolean dark,
                int rosewater, int flamingo, int pink, int mauve, int red, int maroon,
                int peach, int yellow, int green, int teal, int sky, int sapphire,
                int blue, int lavender, int text, int subtext1, int subtext0,
                int overlay2, int overlay1, int overlay0, int surface2, int surface1,
                int surface0, int base, int mantle, int crust) {
            this.key = key;
            this.name = name;
            this.dark = dark;
            this.rosewater = rosewater;
            this.flamingo = flamingo;
            this.pink = pink;
            this.mauve = mauve;
            this.red = red;
            this.maroon = maroon;
            this.peach = peach;
            this.yellow = yellow;
            this.green = green;
            this.teal = teal;
            this.sky = sky;
            this.sapphire = sapphire;
            this.blue = blue;
            this.lavender = lavender;
            this.text = text;
            this.subtext1 = subtext1;
            this.subtext0 = subtext0;
            this.overlay2 = overlay2;
            this.overlay1 = overlay1;
            this.overlay0 = overlay0;
            this.surface2 = surface2;
            this.surface1 = surface1;
            this.surface0 = surface0;
            this.base = base;
            this.mantle = mantle;
            this.crust = crust;
        }

        public int accent(String accentKey) {
            switch (accentKey) {
                case "rosewater": return rosewater;
                case "flamingo": return flamingo;
                case "pink": return pink;
                case "red": return red;
                case "maroon": return maroon;
                case "peach": return peach;
                case "yellow": return yellow;
                case "green": return green;
                case "teal": return teal;
                case "sky": return sky;
                case "sapphire": return sapphire;
                case "blue": return blue;
                case "lavender": return lavender;
                case "mauve":
                default: return mauve;
            }
        }
    }

    private static final Palette LATTE = new Palette(
            "latte", "Latte", false,
            0xffdc8a78, 0xffdd7878, 0xffea76cb, 0xff8839ef, 0xffd20f39, 0xffe64553,
            0xfffe640b, 0xffdf8e1d, 0xff40a02b, 0xff179299, 0xff04a5e5, 0xff209fb5,
            0xff1e66f5, 0xff7287fd, 0xff4c4f69, 0xff5c5f77, 0xff6c6f85,
            0xff7c7f93, 0xff8c8fa1, 0xff9ca0b0, 0xffacb0be, 0xffbcc0cc,
            0xffccd0da, 0xffeff1f5, 0xffe6e9ef, 0xffdce0e8);

    private static final Palette FRAPPE = new Palette(
            "frappe", "Frappe", true,
            0xfff2d5cf, 0xffeebebe, 0xfff4b8e4, 0xffca9ee6, 0xffe78284, 0xffea999c,
            0xffef9f76, 0xffe5c890, 0xffa6d189, 0xff81c8be, 0xff99d1db, 0xff85c1dc,
            0xff8caaee, 0xffbabbf1, 0xffc6d0f5, 0xffb5bfe2, 0xffa5adce,
            0xff949cbb, 0xff838ba7, 0xff737994, 0xff626880, 0xff51576d,
            0xff414559, 0xff303446, 0xff292c3c, 0xff232634);

    private static final Palette MACCHIATO = new Palette(
            "macchiato", "Macchiato", true,
            0xfff4dbd6, 0xfff0c6c6, 0xfff5bde6, 0xffc6a0f6, 0xffed8796, 0xffee99a0,
            0xfff5a97f, 0xffeed49f, 0xffa6da95, 0xff8bd5ca, 0xff91d7e3, 0xff7dc4e4,
            0xff8aadf4, 0xffb7bdf8, 0xffcad3f5, 0xffb8c0e0, 0xffa5adcb,
            0xff939ab7, 0xff8087a2, 0xff6e738d, 0xff5b6078, 0xff494d64,
            0xff363a4f, 0xff24273a, 0xff1e2030, 0xff181926);

    private static final Palette MOCHA = new Palette(
            "mocha", "Mocha", true,
            0xfff5e0dc, 0xfff2cdcd, 0xfff5c2e7, 0xffcba6f7, 0xfff38ba8, 0xffeba0ac,
            0xfffab387, 0xfff9e2af, 0xffa6e3a1, 0xff94e2d5, 0xff89dceb, 0xff74c7ec,
            0xff89b4fa, 0xffb4befe, 0xffcdd6f4, 0xffbac2de, 0xffa6adc8,
            0xff9399b2, 0xff7f849c, 0xff6c7086, 0xff585b70, 0xff45475a,
            0xff313244, 0xff1e1e2e, 0xff181825, 0xff11111b);

    private Catppuccin() {
    }

    public static Palette palette(String key) {
        if (key == null) return MOCHA;
        switch (key) {
            case "latte": return LATTE;
            case "frappe": return FRAPPE;
            case "macchiato": return MACCHIATO;
            case "mocha":
            default: return MOCHA;
        }
    }

    public static Palette current(Context context) {
        return palette(Prefs.getFlavor(context));
    }

    public static int accentColor(Context context) {
        return current(context).accent(Prefs.getAccent(context));
    }

    public static int onAccent(Context context) {
        return current(context).base;
    }

    public static String accentName(String key) {
        for (int i = 0; i < ACCENT_KEYS.length; i++) {
            if (ACCENT_KEYS[i].equals(key)) return ACCENT_NAMES[i];
        }
        return "Mauve";
    }

    public static String flavorName(String key) {
        for (int i = 0; i < FLAVOR_KEYS.length; i++) {
            if (FLAVOR_KEYS[i].equals(key)) return FLAVOR_NAMES[i];
        }
        return "Mocha";
    }

    public static int withAlpha(int color, float alpha) {
        int a = Math.round(255f * Math.max(0f, Math.min(1f, alpha)));
        return (color & 0x00ffffff) | (a << 24);
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }
}
