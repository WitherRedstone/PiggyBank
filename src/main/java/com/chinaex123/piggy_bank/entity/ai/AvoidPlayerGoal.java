package com.chinaex123.piggy_bank.entity.ai;

import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * 宝箱猪的逃跑与驯服 AI 目标
 * <p>
 * 实现以下功能：
 * 1. 检测周围玩家并执行逃跑行为（类似猫的逃跑机制）
 * 2. 手持配置的驯服物品并潜行时不会触发逃跑，反而会被吸引
 * 3. 使用驯服物品右键点击实体可进行驯服（有概率失败）
 * 4. 驯服成功后永久不再逃跑
 * 5. 驯服状态通过 NBT 持久化保存
 * <p>
 * 驯服成功时生成爱心粒子效果，失败时生成烟雾粒子效果
 */
public class AvoidPlayerGoal extends Goal {
    private final PiggyBankEntity piggyBank;
    private final double speedModifier;
    private final double detectionRange;
    private Player nearestPlayer;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private UUID tamedBy;
    private Item tameItem;

    public AvoidPlayerGoal(PiggyBankEntity piggyBank, double detectionRange, double speedModifier) {
        this.piggyBank = piggyBank;
        this.detectionRange = detectionRange;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.loadTameItem();
    }

    /**
     * 从配置加载驯服物品
     */
    private void loadTameItem() {
        String itemId = CommonConfig.TAME_ITEM.get();
        try {
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(location);
            if (item != Items.AIR) {
                this.tameItem = item;
            } else {
                // 配置的物品不存在，使用默认值
                this.tameItem = Items.AMETHYST_SHARD;
            }
        } catch (Exception e) {
            // 配置解析失败，使用默认值
            this.tameItem = Items.AMETHYST_SHARD;
        }
    }

    @Override
    public boolean canUse() {
        // 如果已经被驯服，不执行逃跑
        if (tamedBy != null) {
            return false;
        }

        Level level = piggyBank.level();
        List<Player> nearbyPlayers = level.getEntitiesOfClass(
                Player.class,
                piggyBank.getBoundingBox().inflate(detectionRange),
                player -> !player.isCreative() && player.isAlive()
        );

        if (nearbyPlayers.isEmpty()) {
            return false;
        }

        nearestPlayer = nearbyPlayers.getFirst();

        // 检查玩家是否手持驯服物品并潜行
        if (isPlayerHoldingTameItem(nearestPlayer)) {
            return false;
        }

        // 计算逃跑目标位置
        double dx = piggyBank.getX() - nearestPlayer.getX();
        double dz = piggyBank.getZ() - nearestPlayer.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance <= 0) {
            return false;
        }

        // 在远离玩家的方向上设置目标点
        wantedX = piggyBank.getX() + (dx / distance) * 8.0D;
        wantedY = piggyBank.getY();
        wantedZ = piggyBank.getZ() + (dz / distance) * 8.0D;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (tamedBy != null) {
            return false;
        }

        if (nearestPlayer == null || !nearestPlayer.isAlive()) {
            return false;
        }

        // 如果玩家开始手持驯服物品并潜行，停止逃跑
        if (isPlayerHoldingTameItem(nearestPlayer)) {
            return false;
        }

        double dx = piggyBank.getX() - nearestPlayer.getX();
        double dz = piggyBank.getZ() - nearestPlayer.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        return distance < detectionRange;
    }

    @Override
    public void start() {
        piggyBank.getNavigation().moveTo(wantedX, wantedY, wantedZ, speedModifier);
    }

    @Override
    public void tick() {
        if (nearestPlayer != null) {
            // 持续更新逃跑方向
            double dx = piggyBank.getX() - nearestPlayer.getX();
            double dz = piggyBank.getZ() - nearestPlayer.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                Vec3 escapeVector = new Vec3(dx / distance * speedModifier, piggyBank.getDeltaMovement().y, dz / distance * speedModifier);
                piggyBank.setDeltaMovement(escapeVector);

                // 面向逃跑方向
                float yaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
                piggyBank.setYRot(yaw);
                piggyBank.yHeadRot = yaw;
            }
        }
    }

    @Override
    public void stop() {
        nearestPlayer = null;
    }

    /**
     * 检查玩家是否手持驯服物品并潜行
     */
    private boolean isPlayerHoldingTameItem(Player player) {
        if (!player.isShiftKeyDown()) {
            return false;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.is(tameItem) || offHand.is(tameItem);
    }

    /**
     * 尝试驯服实体
     */
    public boolean tryTame(Player player) {
        // 检查是否已经驯服
        if (tamedBy != null) {
            return false;
        }

        // 只在服务器端执行
        if (piggyBank.level().isClientSide()) {
            return false;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!mainHand.is(tameItem) && !offHand.is(tameItem)) {
            return false;
        }

        // 消耗物品（无论成功失败都消耗）
        if (mainHand.is(tameItem)) {
            mainHand.shrink(1);
        } else if (offHand.is(tameItem)) {
            offHand.shrink(1);
        }

        // 根据配置概率判断是否驯服成功
        double successRate = CommonConfig.TAME_SUCCESS_RATE.get();
        boolean success = piggyBank.getRandom().nextDouble() < successRate;

        if (success) {
            // 驯服成功
            tamedBy = player.getUUID();

            // 生成爱心粒子效果
            if (piggyBank.level() instanceof ServerLevel serverLevel) {
                Vec3 pos = piggyBank.position();
                for (int i = 0; i < 20; i++) {
                    double offsetX = (piggyBank.getRandom().nextDouble() - 0.5) * 2.0;
                    double offsetY = piggyBank.getRandom().nextDouble() * 2.0;
                    double offsetZ = (piggyBank.getRandom().nextDouble() - 0.5) * 2.0;
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            pos.x + offsetX,
                            pos.y + offsetY,
                            pos.z + offsetZ,
                            1,
                            0.0, 0.0, 0.0,
                            0.1
                    );
                }
            }
        } else {
            // 驯服失败，生成黑色烟雾粒子
            if (piggyBank.level() instanceof ServerLevel serverLevel) {
                Vec3 pos = piggyBank.position();
                for (int i = 0; i < 10; i++) {
                    double offsetX = (piggyBank.getRandom().nextDouble() - 0.5) * 1.5;
                    double offsetY = piggyBank.getRandom().nextDouble() * 1.5;
                    double offsetZ = (piggyBank.getRandom().nextDouble() - 0.5) * 1.5;
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            pos.x + offsetX,
                            pos.y + offsetY,
                            pos.z + offsetZ,
                            1,
                            0.0, 0.0, 0.0,
                            0.1
                    );
                }
            }
        }

        return success;
    }

    /**
     * 获取驯服者 UUID
     */
    public UUID getTamedBy() {
        return tamedBy;
    }
}
