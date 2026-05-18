package com.chinaex123.piggy_bank.util;

import com.chinaex123.piggy_bank.config.CommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.IHolderExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.chinaex123.piggy_bank.PiggyBank.LOGGER;

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
            "creative",
            "music"
    };

    /**
     * 获取随机战利品列表
     * 根据配置生成指定数量和范围的随机物品
     * @return 战利品物品堆列表
     */
    public static List<ItemStack> getRandomLoot(Level level) {
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
                int stackSize = RANDOM.nextInt(stackMax - stackMin + 1) + stackMin;

                if (randomItem == Items.ENCHANTED_BOOK) {
                    // 附魔书特殊处理：每个堆独立附魔
                    for (int j = 0; j < stackSize; j++) {
                        ItemStack bookStack = new ItemStack(Items.ENCHANTED_BOOK, 1);
                        addRandomEnchantment(bookStack, level, true);
                        loot.add(bookStack);
                    }
                } else if (isEnchantableEquipment(new ItemStack(randomItem))) {
                    // 可附魔装备：每个都独立附魔
                    for (int j = 0; j < stackSize; j++) {
                        ItemStack itemStack = new ItemStack(randomItem, 1);

                        // 50% 概率附魔
                        if (RANDOM.nextBoolean()) {
                            addRandomEnchantment(itemStack, level, false);
                        }

                        loot.add(itemStack);
                    }
                } else {
                    // 普通物品：直接堆叠
                    ItemStack itemStack = new ItemStack(randomItem, stackSize);
                    loot.add(itemStack);
                }
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
     * 检查物品是否为可附魔的装备
     * @param itemStack 物品堆
     * @return 如果是可附魔装备返回 true
     */
    private static boolean isEnchantableEquipment(ItemStack itemStack) {
        // 排除附魔书
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        // 检查是否有附魔能力
        return itemStack.is(Tags.Items.ENCHANTABLES);
    }

    /**
     * 为物品添加随机附魔
     * @param itemStack 物品堆
     * @param level 游戏世界
     * @param isBook 是否为附魔书
     */
    private static void addRandomEnchantment(ItemStack itemStack, Level level, boolean isBook) {
        try {
            // 获取附魔注册表
            var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            // 获取可用于随机战利品的附魔
            var optional = enchantmentRegistry.get(EnchantmentTags.ON_RANDOM_LOOT);

            List<Holder<Enchantment>> availableEnchantments;
            availableEnchantments = optional.map(holders -> holders.stream().toList()).orElseGet(() ->
                    enchantmentRegistry.listElements().map(IHolderExtension::getDelegate).toList());

            if (availableEnchantments.isEmpty()) {
                return;
            }

            // 如果不是附魔书，需要过滤出该物品支持的附魔
            List<Holder<Enchantment>> finalEnchantments = availableEnchantments;
            if (!isBook) {
                List<Holder<Enchantment>> compatibleEnchantments = new ArrayList<>();
                for (Holder<Enchantment> holder : availableEnchantments) {
                    Enchantment enchantment = holder.value();
                    if (enchantment.canEnchant(itemStack)) {
                        compatibleEnchantments.add(holder);
                    }
                }

                if (compatibleEnchantments.isEmpty()) {
                    return;
                }

                finalEnchantments = compatibleEnchantments;
            }

            // 随机选择附魔数量
            int maxEnchants = isBook ? CommonConfig.ENCHANTED_BOOK_MAX_ENCHANTS.get() : CommonConfig.EQUIPMENT_MAX_ENCHANTS.get();
            int enchantCount = RANDOM.nextInt(maxEnchants) + 1;

            // 使用 Mutable 构建附魔
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            // 为了避免重复附魔，复制一份可用附魔列表
            List<Holder<Enchantment>> remainingEnchantments = new ArrayList<>(finalEnchantments);

            for (int i = 0; i < enchantCount && !remainingEnchantments.isEmpty(); i++) {
                // 随机选择一个附魔
                int index = RANDOM.nextInt(remainingEnchantments.size());
                var holder = remainingEnchantments.get(index);
                var enchantment = holder.value();
                int enchantLevel = RANDOM.nextInt(enchantment.getMaxLevel()) + 1;

                // 设置附魔
                mutable.set(holder, enchantLevel);

                // 移除已选的附魔，避免重复
                remainingEnchantments.remove(index);
            }

            // 应用到物品
            if (isBook) {
                itemStack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            } else {
                itemStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
            }
        } catch (Exception e) {
            LOGGER.error("添加随机附魔失败", e);
        }
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
