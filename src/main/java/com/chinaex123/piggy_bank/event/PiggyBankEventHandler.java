package com.chinaex123.piggy_bank.event;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.config.PBServerConfig;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import com.chinaex123.piggy_bank.entity.ai.AvoidPlayerGoal;
import com.chinaex123.piggy_bank.init.PBSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

/**
 * 宝箱猪事件处理器
 * <p>
 * 使用 NeoForge 事件系统处理宝箱猪实体的运行时行为：
 * 1. 监听实体 Tick 事件，检测周围玩家
 * 2. 已驯服的实体不播放惊吓音效
 * 3. 玩家手持驯服物品并潜行时（吸引状态）不播放惊吓音效
 * 4. 未驯服且检测到附近玩家时，周期性播放惊吓音效
 * <p>
 * 使用 AABB 范围检测（8格）和物品缓存优化性能
 */
@Mod.EventBusSubscriber(modid = PiggyBank.MOD_ID)
public class PiggyBankEventHandler {
    private static final Random RANDOM = new Random();
    private static Item cachedTameItem = null;

    /**
     * 处理世界 Tick 事件
     * 遍历世界中的所有宝箱猪实体，检测周围玩家并根据驯服状态和玩家行为决定是否播放惊吓音效
     *
     * @param event 世界 Tick 事件
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            Level level = event.level;

            // 遍历世界中的所有 PiggyBankEntity
            AABB searchBox = new AABB(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
            );
            for (var entity : level.getEntitiesOfClass(PiggyBankEntity.class, searchBox)) {
                // 检查是否被驯服
                boolean isTamed = false;
                for (var goal : entity.goalSelector.getAvailableGoals()) {
                    if (goal.getGoal() instanceof AvoidPlayerGoal avoidGoal) {
                        if (avoidGoal.getTamedBy() != null) {
                            isTamed = true;
                        }
                        break;
                    }
                }

                // 如果已驯服，不播放惊吓音效
                if (isTamed) {
                    continue;
                }

                // 使用 AABB 检测周围8格内的玩家
                List<Player> nearbyPlayers = level.getEntitiesOfClass(
                        Player.class,
                        entity.getBoundingBox().inflate(8.0D),
                        player -> !player.isCreative() && player.isAlive()
                );

                if (!nearbyPlayers.isEmpty()) {
                    Player nearestPlayer = nearbyPlayers.get(0);

                    // 检查玩家是否手持驯服物品并潜行（吸引状态）
                    if (isPlayerAttracting(nearestPlayer)) {
                        continue;
                    }

                    // 播放惊吓音效（带冷却）
                    if (entity.tickCount % 100 == 0) {
                        var jumpSound = PBSounds.PIGGY_BANK_JUMP1.get();
                        entity.playSound(jumpSound, 0.8F, 1.0F);
                    }
                }
            }
        }
    }

    /**
     * 检查玩家是否正在吸引实体（手持驯服物品并潜行）
     */
    @SuppressWarnings("removal")
    private static boolean isPlayerAttracting(Player player) {
        if (!player.isShiftKeyDown()) {
            return false;
        }

        // 缓存驯服物品
        if (cachedTameItem == null) {
            String itemId = PBServerConfig.TAME_ITEM.get();
            try {
                ResourceLocation location = new ResourceLocation(itemId);
                Item item = BuiltInRegistries.ITEM.get(location);
                if (item != Items.AIR) {
                    cachedTameItem = item;
                } else {
                    // 配置的物品不存在，使用默认值
                    cachedTameItem = Items.AMETHYST_SHARD;
                }
            } catch (Exception e) {
                // 配置解析失败，使用默认值
                cachedTameItem = Items.AMETHYST_SHARD;
            }
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.getItem() == cachedTameItem || offHand.getItem() == cachedTameItem;
    }
}