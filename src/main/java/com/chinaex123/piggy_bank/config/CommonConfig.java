package com.chinaex123.piggy_bank.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CommonConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue EMERALD_DROP_MAX;
    public static final ModConfigSpec.DoubleValue EMERALD_PER_DAMAGE;
    public static final ModConfigSpec.IntValue PORKCHOP_DROP_MIN;
    public static final ModConfigSpec.IntValue PORKCHOP_DROP_MAX;
    public static final ModConfigSpec.IntValue COOKED_PORKCHOP_DROP_MIN;
    public static final ModConfigSpec.IntValue COOKED_PORKCHOP_DROP_MAX;

    // 战利品配置
    public static final ModConfigSpec.BooleanValue LOOT_ENABLED;
    public static final ModConfigSpec.IntValue LOOT_MIN_COUNT;
    public static final ModConfigSpec.IntValue LOOT_MAX_COUNT;
    public static final ModConfigSpec.IntValue LOOT_STACK_MIN;
    public static final ModConfigSpec.IntValue LOOT_STACK_MAX;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LOOT_WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LOOT_BLACKLIST;
    public static final ModConfigSpec.BooleanValue USE_WHITELIST;

    // 世界生成配置
    public static final ModConfigSpec.BooleanValue SPAWN_ENABLED;
    public static final ModConfigSpec.IntValue SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue SPAWN_MIN_COUNT;
    public static final ModConfigSpec.IntValue SPAWN_MAX_COUNT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("loots");

        EMERALD_DROP_MAX = builder
                .translation("piggy_bank.configuration.emeraldDropMax")
                .comment("Maximum emeralds dropped per entity (0 = unlimited)")
                .defineInRange("emeraldDropMax", 0, 0, Integer.MAX_VALUE);

        EMERALD_PER_DAMAGE = builder
                .translation("piggy_bank.configuration.emeraldPerDamage")
                .comment("Emeralds dropped per damage point (e.g., 2.0 = 1 emerald per 2 damage)")
                .defineInRange("emeraldPerDamage", 2.0, 0.5, 10.0);

        builder.push("porkchop");
        PORKCHOP_DROP_MIN = builder
                .translation("piggy_bank.configuration.porkchopMinDrop")
                .comment("Minimum raw porkchops dropped on death")
                .defineInRange("minDrop", 1, 0, 64);
        PORKCHOP_DROP_MAX = builder
                .translation("piggy_bank.configuration.porkchopMaxDrop")
                .comment("Maximum raw porkchops dropped on death")
                .defineInRange("maxDrop", 3, 1, 64);
        builder.pop();

        builder.push("cooked_porkchop");
        COOKED_PORKCHOP_DROP_MIN = builder
                .translation("piggy_bank.configuration.cookedPorkchopMinDrop")
                .comment("Minimum cooked porkchops dropped when killed by fire")
                .defineInRange("minDrop", 1, 0, 64);
        COOKED_PORKCHOP_DROP_MAX = builder
                .translation("piggy_bank.configuration.cookedPorkchopMaxDrop")
                .comment("Maximum cooked porkchops dropped when killed by fire")
                .defineInRange("maxDrop", 3, 1, 64);
        builder.pop();

        builder.pop();


        builder.push("extra_loot");

        LOOT_ENABLED = builder
                .translation("piggy_bank.configuration.lootEnabled")
                .comment("Enable extra loot drops")
                .define("enabled", true);
        LOOT_MIN_COUNT = builder
                .translation("piggy_bank.configuration.lootMinCount")
                .comment("Minimum number of item types dropped per death")
                .defineInRange("minCount", 1, 1, 1);
        LOOT_MAX_COUNT = builder
                .translation("piggy_bank.configuration.lootMaxCount")
                .comment("Maximum number of item types dropped per death")
                .defineInRange("maxCount", 8, 1, 120);
        LOOT_STACK_MIN = builder
                .translation("piggy_bank.configuration.lootStackMinSize")
                .comment("Minimum stack size for each dropped item")
                .defineInRange("stackMinSize", 1, 1, 64);
        LOOT_STACK_MAX = builder
                .translation("piggy_bank.configuration.lootStackMaxSize")
                .comment("Maximum stack size for each dropped item")
                .defineInRange("stackMaxSize", 4, 1, 64);
        USE_WHITELIST = builder
                .translation("piggy_bank.configuration.useWhitelist")
                .comment("Use whitelist mode (true = whitelist, false = blacklist)")
                .define("useWhitelist", false);
        LOOT_WHITELIST = builder
                .translation("piggy_bank.configuration.lootWhitelist")
                .comment("Whitelist item IDs (supports namespace IDs and regex)")
                .defineList("whitelist",
                        Arrays.asList("", ""),
                        obj -> obj instanceof String);
        LOOT_BLACKLIST = builder
                .translation("piggy_bank.configuration.lootBlacklist")
                .comment("Blacklist item IDs (supports namespace IDs and regex)",
                        "Examples:",
                        "- Single item: minecraft:barrier",
                        "- Entire mod: modid:.* (e.g., create:.*)",
                        "- Regex pattern: .*spawn_egg.* (matches all spawn eggs)")
                .defineList("blacklist",
                        Arrays.asList(
                                "minecraft:barrier",
                                "minecraft:command_block",
                                "minecraft:chain_command_block",
                                "minecraft:repeating_command_block",
                                "minecraft:command_block_minecart",
                                "minecraft:jigsaw",
                                "minecraft:structure_block",
                                "minecraft:structure_void",
                                "minecraft:debug_stick",
                                "minecraft:light",
                                "minecraft:painting",
                                "minecraft:budding_amethyst",
                                "minecraft:bedrock",
                                "minecraft:end_portal_frame",
                                "minecraft:vault",
                                "minecraft:spawner",

                                ".*shulker_box",
                                ".*spawn_egg.*",
                                ".*boat.*",
                                ".*creative.*"
                        ),
                        obj -> obj instanceof String);

        builder.pop();

        builder.push("world_spawn");

        SPAWN_ENABLED = builder
                .translation("piggy_bank.configuration.spawnEnabled")
                .comment("Enable natural spawning")
                .define("enabled", true);
        SPAWN_WEIGHT = builder
                .translation("piggy_bank.configuration.spawnWeight")
                .comment("Spawn weight (lower value = rarer spawns)")
                .defineInRange("weight", 2, 1, 100);
        SPAWN_MIN_COUNT = builder
                .translation("piggy_bank.configuration.spawnMinCount")
                .comment("Minimum spawn count per group")
                .defineInRange("minCount", 1, 1, 10);
        SPAWN_MAX_COUNT = builder
                .translation("piggy_bank.configuration.spawnMaxCount")
                .comment("Maximum spawn count per group")
                .defineInRange("maxCount", 1, 1, 10);

        builder.pop();

        SPEC = builder.build();
    }
}
