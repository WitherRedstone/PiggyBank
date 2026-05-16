package com.chinaex123.piggy_bank.util;

import com.chinaex123.piggy_bank.config.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 战利品管理器
 * 负责处理生物死亡时的额外战利品掉落逻辑
 * 支持黑白名单过滤和正则表达式匹配
 */
public class LootManager {
    // 随机数生成器
    private static final Random RANDOM = new Random();

    // 缓存所有已注册的物品，避免每次掉落都遍历注册表
    private static List<Item> cachedItems = null;

    // 编译后的白名单正则模式列表
    private static List<Pattern> whitelistPatterns = null;

    // 编译后的黑名单正则模式列表
    private static List<Pattern> blacklistPatterns = null;

    // 黑名单物品ID（完整匹配）
    private static final String[] HARDCODED_BLACKLIST_ITEMS = {
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
            "minecraft:spawner"
    };

    // 黑名单物品ID前缀（包含匹配）
    private static final String[] HARDCODED_BLACKLIST_PREFIXES = {
            "spawn_egg",
            "creative"
    };

    /**
     * 获取随机战利品列表
     * 根据配置生成指定数量和范围的随机物品
     * @return 战利品物品堆列表
     */
    public static List<ItemStack> getRandomLoot() {
        // 检查是否启用战利品系统
        if (!CommonConfig.LOOT_ENABLED.get()) {
            return new ArrayList<>();
        }

        // 初始化缓存（首次调用时）
        if (cachedItems == null) {
            cacheAllItems();
        }
        if (whitelistPatterns == null || blacklistPatterns == null) {
            compilePatterns();
        }

        List<ItemStack> loot = new ArrayList<>();

        // 获取掉落的物品种类数量范围
        int minCount = CommonConfig.LOOT_MIN_COUNT.get();
        int maxCount = CommonConfig.LOOT_MAX_COUNT.get();
        int dropTypes = RANDOM.nextInt(maxCount - minCount + 1) + minCount;

        // 获取每种物品的堆叠数量范围
        int stackMin = CommonConfig.LOOT_STACK_MIN.get();
        int stackMax = CommonConfig.LOOT_STACK_MAX.get();

        // 获取当前使用的过滤模式
        boolean useWhitelist = CommonConfig.USE_WHITELIST.get();

        // 生成指定数量的随机物品
        for (int i = 0; i < dropTypes; i++) {
            Item randomItem = selectRandomItem(useWhitelist);
            if (randomItem != null) {
                // 在配置范围内随机生成物品数量
                int stackSize = RANDOM.nextInt(stackMax - stackMin + 1) + stackMin;
                loot.add(new ItemStack(randomItem, stackSize));
            }
        }

        return loot;
    }

    /**
     * 缓存所有注册的物品
     * 遍历游戏注册表中的所有物品并存储到列表中
     */
    private static void cacheAllItems() {
        cachedItems = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            cachedItems.add(item);
        }
    }

    /**
     * 编译正则表达式模式
     * 将配置文件中的字符串模式编译为高效的 Pattern 对象
     */
    private static void compilePatterns() {
        // 编译白名单模式
        whitelistPatterns = new ArrayList<>();
        for (String pattern : CommonConfig.LOOT_WHITELIST.get()) {
            whitelistPatterns.add(Pattern.compile(pattern));
        }

        // 编译黑名单模式
        blacklistPatterns = new ArrayList<>();
        for (String pattern : CommonConfig.LOOT_BLACKLIST.get()) {
            blacklistPatterns.add(Pattern.compile(pattern));
        }
    }

    /**
     * 根据黑白名单选择随机物品
     * @param useWhitelist true=使用白名单模式，false=使用黑名单模式
     * @return 随机选择的物品，如果没有符合条件的物品则返回 null
     */
    private static Item selectRandomItem(boolean useWhitelist) {
        if (cachedItems.isEmpty()) {
            return null;
        }

        List<Item> filteredItems = new ArrayList<>();

        // 遍历所有缓存的物品进行过滤
        for (Item item : cachedItems) {
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

            // 检查硬编码黑名单
            if (isHardcodedBlacklisted(itemId)) {
                continue;
            }

            if (useWhitelist) {
                // 白名单模式：只保留匹配白名单的物品
                if (matchesAnyPattern(itemId, whitelistPatterns)) {
                    // 同时不能匹配黑名单
                    if (!matchesAnyPattern(itemId, blacklistPatterns)) {
                        filteredItems.add(item);
                    }
                }
            } else {
                // 黑名单模式：排除匹配黑名单的物品
                if (!matchesAnyPattern(itemId, blacklistPatterns)) {
                    filteredItems.add(item);
                }
            }
        }

        if (filteredItems.isEmpty()) {
            return null;
        }

        // 从符合条件的物品中随机选择一个
        return filteredItems.get(RANDOM.nextInt(filteredItems.size()));
    }

    /**
     * 检查物品是否在硬编码黑名单中
     * @param itemId 物品的命名空间ID
     * @return 如果在黑名单中返回 true
     */
    private static boolean isHardcodedBlacklisted(String itemId) {
        // 检查完整匹配
        for (String item : HARDCODED_BLACKLIST_ITEMS) {
            if (itemId.equals(item)) {
                return true;
            }
        }

        // 检查前缀匹配
        for (String prefix : HARDCODED_BLACKLIST_PREFIXES) {
            if (itemId.contains(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查物品ID是否匹配任意一个正则模式
     * @param itemId 物品的命名空间ID（如 "minecraft:diamond"）
     * @param patterns 正则表达式模式列表
     * @return 如果匹配任意一个模式则返回 true
     */
    private static boolean matchesAnyPattern(String itemId, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(itemId).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除缓存
     * 当配置文件重载时调用，强制下次掉落时重新加载物品列表和正则模式
     */
    public static void clearCache() {
        cachedItems = null;
        whitelistPatterns = null;
        blacklistPatterns = null;
    }
}
