package com.ecofocus.shop;

import java.util.*;

/**
 * Mağazadaki tüm karakterlerin tanımlandığı tek yer.
 *
 * Yeni karakter eklemek için sadece bu sınıfa bir satır ekle:
 *   POINTS  → puan ile satın alınır
 *   CHALLENGE → tamamlanan challenge sayısıyla açılır (ücretsiz)
 *
 * Örnek:
 *   register("unicorn",  "🦄", "Unicorn",      UnlockType.POINTS,    500,  0);
 */
public class ShopItemRegistry {

    /**
     * TEAM_HOURS      : ekip challenge dakikaları harcanarak satın alınır (price = dakika)
     * CHALLENGE_HOURS : bireysel odak seansı dakikaları harcanarak satın alınır (price = dakika)
     */
    public enum UnlockType { POINTS, CHALLENGE, TEAM_CHALLENGE, TEAM_HOURS, CHALLENGE_HOURS }

    public record ShopItemDef(
        String type,
        String emoji,
        String name,
        UnlockType unlockType,
        int price,            // POINTS tipinde: kaç puan; CHALLENGE tipinde: 0
        int challengeThreshold // CHALLENGE tipinde: kaç challenge; POINTS tipinde: 0
    ) {}

    private static final List<ShopItemDef> ITEMS = new ArrayList<>();

    static {
        // ── Puan ile satın alınan karakterler ────────────────────────────────
        register("sunflower", "🌻", "Ayçiçeği",        UnlockType.POINTS,    45,   0);
        register("havuc",     "🥕", "Havuç",           UnlockType.POINTS,    25,   0);
        register("sogan",     "🧅", "Soğan",           UnlockType.POINTS,    25,   0);
        register("tree",      "🌳", "Ağaç",            UnlockType.POINTS,    60,   0);
        register("pinetree",  "🌲", "Çam Ağacı",       UnlockType.POINTS,    90,   0);
        register("palmtree",  "🌴", "Palmiye",         UnlockType.POINTS,   120,   0);
        register("butterfly", "🦋", "Kelebek",         UnlockType.POINTS,    45,   0);
        register("sheep",     "🐑", "Koyun",           UnlockType.POINTS,   240,   0);
        register("ram",       "🐏", "Koç",             UnlockType.POINTS,   240,   0);
        register("goat",      "🐐", "Keçi",            UnlockType.POINTS,   240,   0);
        register("deer",      "🦌", "Geyik",           UnlockType.POINTS,   325,   0);
        register("rabbit",    "🐇", "Tavşan",          UnlockType.POINTS,   240,   0);
        register("cow",       "🐄", "İnek",            UnlockType.POINTS,   300,   0);
        register("cat",       "🐈", "Kedi",            UnlockType.POINTS,   240,   0);
        register("cactus",    "🌵", "Kaktüs",          UnlockType.POINTS,    60,   0);
        register("mushroom",  "🍄", "Mantar",          UnlockType.POINTS,    90,   0);
        register("tulip",     "🌷", "Lale",            UnlockType.POINTS,    25,   0);
        register("goose",     "🪿", "Kaz",             UnlockType.POINTS,   240,   0);
        register("rooster",   "🐓", "Tavuk",           UnlockType.POINTS,   120,   0);
        register("chick",     "🐥", "Civciv",          UnlockType.POINTS,   180,   0);
        register("eagle",     "🦅", "Kartal",          UnlockType.POINTS,   360,   0);

        // ── Çiftlik & Yapılar  ─────────────
        register("kuyu",       "🪣",  "Kuyu",          UnlockType.TEAM_HOURS,  360,  0); //  6sa
        register("ahir",       "🏚️", "Ahır",          UnlockType.TEAM_HOURS,  720,  0); // 12sa
        register("degirmen",   "⚙️",  "Değirmen",      UnlockType.TEAM_HOURS, 1080,  0); // 18sa
        register("manav",      "🏪",  "Manav",         UnlockType.TEAM_HOURS, 1440,  0); // 24sa

        // ── Su canlıları ──────────────────────────────────────────────────────
        register("octopus",     "🐙", "Ahtapot",       UnlockType.POINTS,   240,   0);
        register("lobster",     "🦞", "Istakoz",        UnlockType.POINTS,   205,   0);
        register("jellyfish",   "🪼", "Denizanası",     UnlockType.POINTS,   205,   0);
        register("whale",       "🐳", "Balina",         UnlockType.POINTS,   600,   0);
        register("coral",       "🪸", "Mercan",         UnlockType.POINTS,   165,   0);
        register("tropicalfish","🐠", "Tropik Balık",   UnlockType.POINTS,   120,   0);
        register("blowfish",    "🐡", "Balon Balığı",   UnlockType.POINTS,   180,   0);
        register("dolphin",     "🐬", "Yunus",          UnlockType.POINTS,   600,   0);
        register("shark",       "🦈", "Köpek Balığı",   UnlockType.POINTS,   600,   0);
        register("seal",        "🦭", "Fok",            UnlockType.POINTS,   285,   0);
        register("otter",       "🦦", "Su Samuru",      UnlockType.POINTS,   285,   0);

        // ── Yeni hayvanlar  ───────────────────────────────────────────
        register("blackbird",  "🐦‍⬛", "Karga",           UnlockType.POINTS,  240,  0);
        register("duck",       "🦆",  "Ördek",           UnlockType.POINTS,  240,  0);
        register("dove",       "🕊️",  "Güvercin",        UnlockType.POINTS,  240,  0);
        register("servicedog", "🐕‍🦺", "Siyah Köpek",    UnlockType.POINTS,  240,  0);
        register("guidedog",   "🦮",  "Köpek",   UnlockType.POINTS,  240,  0);
        register("ladybug",    "🐞",  "Uğur Böceği",     UnlockType.POINTS,   70,  0);
        register("spider",     "🕷️",  "Örümcek",         UnlockType.POINTS,   70,  0);
        register("hedgehog",   "🦔",  "Kirpi",           UnlockType.POINTS,  120,  0);
        register("snail",      "🐌",  "Salyangoz",       UnlockType.POINTS,  120,  0);
        register("bat",        "🦇",  "Yarasa",          UnlockType.POINTS,  240,  0);
        register("turkey",     "🦃",  "Hindi",           UnlockType.POINTS,  240,  0);
        register("peacock",    "🦚",  "Tavuskuşu",       UnlockType.POINTS,  600,  0);
        register("dodo",       "🦤",  "Dodo",            UnlockType.POINTS,  600,  0);
        register("crocodile",  "🐊",  "Timsah",          UnlockType.POINTS,  285,  0);
        register("squirrel",   "🐿️",  "Sincap",          UnlockType.POINTS,  360,  0);
        register("ant",        "🐜",  "Karınca",         UnlockType.POINTS,   60,  0);
        register("scorpion",   "🦂",  "Akrep",           UnlockType.POINTS,  180,  0);
        register("snake",      "🐍",  "Yılan",           UnlockType.POINTS,  180,  0);
        register("trex",       "🦖",  "T-Rex",           UnlockType.CHALLENGE_HOURS, 2160, 0);
        register("sauropod",   "🦕",  "Dinozor",         UnlockType.CHALLENGE_HOURS, 2160, 0);

        // ── Süs ──────────────────────────────────────
        register("fountain",   "⛲",  "Çeşme",           UnlockType.POINTS,  900,  0);
        register("island",     "🏝️",  "Ada",             UnlockType.TEAM_HOURS, 2160, 0);

        register("turtle",     "🐢",  "Kaplumbağa",      UnlockType.POINTS,  240,  0);
        register("canoe",      "🛶",  "Kano",            UnlockType.POINTS,  300,  0);
        register("sailboat",   "⛵",  "Yelkenli",        UnlockType.POINTS,  400,  0);
        register("rowwoman",   "🚣🏻‍♀️", "Kadın Kürekçi",  UnlockType.TEAM_HOURS, 1440, 0);
        register("rowman",     "🚣‍♂️", "Erkek Kürekçi",  UnlockType.TEAM_HOURS, 1440, 0);
        register("mermaid",    "🧜‍♀️", "Denizkızı",       UnlockType.CHALLENGE_HOURS, 4320, 0);
        register("merman",     "🧜‍♂️", "Denizadamı",      UnlockType.CHALLENGE_HOURS, 4320, 0);

        // ── İnsanlar ────────────────────────────────────────────────────
        register("person",       "🧍",    "Erkek 1", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("person_light", "🧍🏻",   "Erkek 2", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("person_medium","🧍🏽",   "Erkek 3", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("person_mdark", "🧍🏾",   "Erkek 4", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("person_dark",  "🧍🏿",   "Erkek 5", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("woman_light",  "🧍🏻‍♀️", "Kadın 1", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("woman_mlight", "🧍🏼‍♀️", "Kadın 2", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("woman_medium", "🧍🏽‍♀️", "Kadın 3", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("woman_mdark",  "🧍🏾‍♀️", "Kadın 4", UnlockType.CHALLENGE_HOURS, 1080, 0);
        register("woman_dark",   "🧍🏿‍♀️", "Kadın 5", UnlockType.CHALLENGE_HOURS, 1080, 0);

        // ── Süs (25 puan) ─────────────────────────────────────────────────────
        register("log",        "🪵", "Kütük",             UnlockType.POINTS, 45, 0);
        register("xmastree",   "🎄", "Noel Ağacı",        UnlockType.POINTS,  900, 0);
        register("phoenix",    "🐦‍🔥", "Anka Kuşu",       UnlockType.CHALLENGE_HOURS, 4320, 0);
        register("pavement_beige", "⬜", "Yol",              UnlockType.POINTS, 25, 0);
        register("fence_e",   "🪵", "Düz Çit",          UnlockType.POINTS, 25, 0);
        register("fence_n",   "🪵", "Yan Çit",          UnlockType.POINTS, 25, 0);
        register("haybales",  "🌾", "Saman Balyası",   UnlockType.POINTS, 25, 0);

        // ── Challenge ile açılan özel karakterler ─────────────────────────────

        // ── Ekip challenge ile açılan özel karakterler ────────────────────────
        register("bee",       "🐝", "Arı",            UnlockType.CHALLENGE_HOURS, 720, 0); // 12sa bireysel odak
    }

    private static void register(String type, String emoji, String name,
                                  UnlockType unlockType, int price, int challengeThreshold) {
        ITEMS.add(new ShopItemDef(type, emoji, name, unlockType, price, challengeThreshold));
    }

    public static List<ShopItemDef> all() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static Optional<ShopItemDef> find(String type) {
        return ITEMS.stream().filter(i -> i.type().equals(type)).findFirst();
    }
}
