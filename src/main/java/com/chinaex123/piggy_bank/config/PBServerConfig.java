package com.chinaex123.piggy_bank.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class PBServerConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue EMERALD_DROP_MAX;
    public static final ForgeConfigSpec.DoubleValue EMERALD_PER_DAMAGE;
    public static final ForgeConfigSpec.IntValue PORKCHOP_DROP_MIN;
    public static final ForgeConfigSpec.IntValue PORKCHOP_DROP_MAX;
    public static final ForgeConfigSpec.IntValue COOKED_PORKCHOP_DROP_MIN;
    public static final ForgeConfigSpec.IntValue COOKED_PORKCHOP_DROP_MAX;

    // 战利品配置
    public static final ForgeConfigSpec.BooleanValue LOOT_ENABLED;
    public static final ForgeConfigSpec.IntValue LOOT_MIN_COUNT;
    public static final ForgeConfigSpec.IntValue LOOT_MAX_COUNT;
    public static final ForgeConfigSpec.IntValue LOOT_STACK_MIN;
    public static final ForgeConfigSpec.IntValue LOOT_STACK_MAX;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_NAMESPACE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_NAMESPACE_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue USE_WHITELIST;
    public static final ForgeConfigSpec.IntValue ENCHANTED_BOOK_MAX_ENCHANTS;
    public static final ForgeConfigSpec.IntValue EQUIPMENT_MAX_ENCHANTS;

    // 世界生成配置
    public static final ForgeConfigSpec.BooleanValue SPAWN_ENABLED;
    public static final ForgeConfigSpec.IntValue SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue SPAWN_MIN_COUNT;
    public static final ForgeConfigSpec.IntValue SPAWN_MAX_COUNT;

    // 刷怪笼配置
    public static final ForgeConfigSpec.BooleanValue DISABLE_SPAWNER_PLACEMENT;

    // 驯服配置
    public static final ForgeConfigSpec.ConfigValue<String> TAME_ITEM;
    public static final ForgeConfigSpec.DoubleValue TAME_SUCCESS_RATE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("实体战利品").push("Entity Loot");
        EMERALD_DROP_MAX = builder
                .comment("每只生物最多掉落的绿宝石数量（0表示无上限）")
                .comment("Maximum emeralds dropped per entity (0 = unlimited)")
                .defineInRange("emeraldDropMax", 64, 0, Integer.MAX_VALUE);
        EMERALD_PER_DAMAGE = builder
                .comment("造成多少伤害才能掉落一次绿宝石")
                .comment("How much damage is required per Emerald drop?")
                .defineInRange("emeraldPerDamage", 2.0, 0.5, Integer.MAX_VALUE);
        PORKCHOP_DROP_MIN = builder
                .comment("死亡时最少掉落的生猪肉数量")
                .comment("Minimum raw porkchops dropped on death")
                .defineInRange("porkchopMinDrop", 1, 0, Integer.MAX_VALUE);
        PORKCHOP_DROP_MAX = builder
                .comment("死亡时最多掉落的生猪肉数量")
                .comment("Maximum raw porkchops dropped on death")
                .defineInRange("porkchopMaxDrop", 3, 1, Integer.MAX_VALUE);
        COOKED_PORKCHOP_DROP_MIN = builder
                .comment("被火焰击杀时最少掉落的熟猪排数量")
                .comment("Minimum cooked porkchops dropped when killed by fire")
                .defineInRange("cookedPorkchopMinDrop", 1, 0, Integer.MAX_VALUE);
        COOKED_PORKCHOP_DROP_MAX = builder
                .comment("被火焰击杀时最多掉落的熟猪排数量")
                .comment("Maximum cooked porkchops dropped when killed by fire")
                .defineInRange("cookedPorkchopMaxDrop", 3, 1, Integer.MAX_VALUE);
        builder.pop();


        builder.comment("额外战利品").push("Extra Loot");
        LOOT_ENABLED = builder
                .comment("是否启用额外战利品掉落")
                .comment("Enable extra loot drops")
                .define("lootEnabled", true);
        LOOT_MIN_COUNT = builder
                .comment("每次死亡最少掉落的物品种类数")
                .comment("Minimum number of item types dropped per death")
                .defineInRange("lootMinCount", 1, 1, Integer.MAX_VALUE);
        LOOT_MAX_COUNT = builder
                .comment("每次死亡最多掉落的物品种类数")
                .comment("Maximum number of item types dropped per death")
                .defineInRange("lootMaxCount", 8, 1, Integer.MAX_VALUE);
        LOOT_STACK_MIN = builder
                .comment("每种物品最少掉落的数量")
                .comment("Minimum stack size for each dropped item")
                .defineInRange("lootStackMinSize", 1, 1, Integer.MAX_VALUE);
        LOOT_STACK_MAX = builder
                .comment("每种物品最多掉落的数量")
                .comment("Maximum stack size for each dropped item")
                .defineInRange("lootStackMaxSize", 4, 1, Integer.MAX_VALUE);
        builder.comment("黑名单 & 白名单").push("Whitelist & Blacklist");
        USE_WHITELIST = builder
                .comment("是否使用白名单模式（true = 白名单，false = 黑名单）")
                .comment("Use whitelist mode (true = whitelist, false = blacklist)")
                .define("useWhitelist", false);
        LOOT_WHITELIST = builder
                .comment("白名单物品ID列表")
                .comment("Whitelist item IDs")
                .defineList("lootWhitelist", List.of(), obj -> obj instanceof String);
        LOOT_BLACKLIST = builder
                .comment("黑名单物品ID列表")
                .comment("Blacklist item IDs")
                .defineList("lootBlacklist",
                        Arrays.asList(
                                ".*shulker_box",
                                ".*boat.*",
                                ".*harness",
                                ".*bundle",
                                ".*bed",
                                ".*nautilus_armor",
                                ".*horse_armor"
                        ),
                        obj -> obj instanceof String);
        MOD_NAMESPACE_WHITELIST = builder
                .comment("模组命名空间白名单")
                .comment("Mod namespace whitelist")
                .defineList("modNamespaceWhitelist", List.of(), obj -> obj instanceof String);
        MOD_NAMESPACE_BLACKLIST = builder
                .comment("模组命名空间黑名单")
                .comment("Mod namespace blacklist")
                .defineList("modNamespaceBlacklist", List.of(
                        "geckolib"
                ), obj -> obj instanceof String);
        ENCHANTED_BOOK_MAX_ENCHANTS = builder
                .comment("附魔书最大附魔数量")
                .comment("Maximum number of enchantments on enchanted books")
                .defineInRange("enchantedBookMaxEnchants", 5, 1, Integer.MAX_VALUE);
        EQUIPMENT_MAX_ENCHANTS = builder
                .comment("装备最大附魔数量")
                .comment("Maximum number of enchantments on equipment")
                .defineInRange("equipmentMaxEnchants", 3, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();


        builder.comment("世界生成").push("World Spawn");
        SPAWN_ENABLED = builder
                .comment("是否启用自然生成")
                .comment("Enable natural spawning")
                .define("spawnEnabled", true);
        SPAWN_WEIGHT = builder
                .comment("生成权重")
                .comment("Spawn weight")
                .defineInRange("spawnWeight", 2, 1, 100);
        SPAWN_MIN_COUNT = builder
                .comment("每次生成最少数量")
                .comment("Minimum spawn count per group")
                .defineInRange("spawnMinCount", 1, 1, Integer.MAX_VALUE);
        SPAWN_MAX_COUNT = builder
                .comment("每次生成最多数量")
                .comment("Maximum spawn count per group")
                .defineInRange("spawnMaxCount", 1, 1, Integer.MAX_VALUE);
        builder.pop();


        builder.comment("刷怪笼").push("Spawner");
        DISABLE_SPAWNER_PLACEMENT = builder
                .comment("禁止将刷怪蛋放入刷怪笼")
                .comment("Disable placing Piggy Bank spawn eggs in spawners")
                .define("disableSpawnerPlacement", true);
        builder.pop();


        builder.comment("驯服").push("Taming");
        TAME_ITEM = builder
                .comment("用于驯服的物品")
                .comment("Item used to tame the Piggy Bank")
                .define("tameItem", "minecraft:amethyst_shard");
        TAME_SUCCESS_RATE = builder
                .comment("驯服成功率")
                .comment("Success rate for taming")
                .defineInRange("tameSuccessRate", 0.1, 0.0, 1.0);
        builder.pop();

        SPEC = builder.build();
    }
}