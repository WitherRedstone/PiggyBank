package com.chinaex123.piggy_bank.entity;

import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.entity.ai.AvoidPlayerGoal;
import com.chinaex123.piggy_bank.init.ModSounds;
import com.chinaex123.piggy_bank.util.LootManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * 宝箱猪实体类
 * 使用 GeckoLib 动画系统，具有自定义行为和声音
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PiggyBankEntity extends AgeableMob implements GeoEntity {
    // GeckoLib 动画缓存实例
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 定义三种动画：待机、行走、受伤
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.piggy_bank.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.piggy_bank.walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.piggy_bank.hurt");

    // 待机时的随机声音列表
    private static final List<Supplier<SoundEvent>> IDLE_SOUNDS = Arrays.asList(
            ModSounds.PIGGY_BANK_IDLE1,
            ModSounds.PIGGY_BANK_IDLE2,
            ModSounds.PIGGY_BANK_IDLE3
    );
    // 行走时的随机声音列表
    private static final List<Supplier<SoundEvent>> STEP_SOUNDS = Arrays.asList(
            ModSounds.PIGGY_BANK_STEP1,
            ModSounds.PIGGY_BANK_STEP2,
            ModSounds.PIGGY_BANK_STEP3,
            ModSounds.PIGGY_BANK_STEP4,
            ModSounds.PIGGY_BANK_STEP5
    );
    // 收到惊吓的随机声音列表
    private static final List<Supplier<SoundEvent>> JUMP_SOUNDS = Arrays.asList(
            ModSounds.PIGGY_BANK_JUMP1,
            ModSounds.PIGGY_BANK_JUMP2
    );
    // 受伤时的随机声音列表
    private static final List<Supplier<SoundEvent>> HURT_SOUNDS = Arrays.asList(
            ModSounds.PIGGY_BANK_HURT1,
            ModSounds.PIGGY_BANK_HURT2,
            ModSounds.PIGGY_BANK_HURT3,
            ModSounds.PIGGY_BANK_HURT4,
            ModSounds.PIGGY_BANK_HURT5
    );
    private static final Random RANDOM = new Random();

    // 计时器
    private int idleSoundTimer = 0; // 待机声音计时器
    private int walkSoundTimer = 0; // 行走声音计时器
    private int panicTimer = 0; // 恐慌逃跑计时器
    private int totalEmeraldDropped = 0; // 累计掉落的绿宝石数量

    /**
     * 构造方法
     * @param entityType 实体类型
     * @param level 游戏世界
     */
    public PiggyBankEntity(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 创建实体属性配置
     * @return 属性构建器
     */
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D) // 最大生命值
                .add(Attributes.MOVEMENT_SPEED, 0.25D) // 基础移动速度
                .add(Attributes.FOLLOW_RANGE, 16.0D); // 跟随范围
    }

    /**
     * 注册实体的 AI 行为目标
     * 优先级越小，优先级越高
     */
    @Override
    protected void registerGoals() {
        /* 优先级0：漂浮在水面 */
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 从配置加载驯服物品
        String itemId = CommonConfig.TAME_ITEM.get();
        Item tameItem;
        try {
            ResourceLocation location = ResourceLocation.parse(itemId);
            tameItem = BuiltInRegistries.ITEM.get(location);
        } catch (Exception e) {
            tameItem = Items.AMETHYST_SHARD;
        }

        final Item finalTameItem = tameItem;
        /* 优先级1：被配置的驯服物品吸引，速度1.2 */
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.2D, stack -> stack.is(finalTameItem), false));
        /* 优先级2：避开玩家，距离8格，逃跑速度1.35D */
        this.goalSelector.addGoal(2, new AvoidPlayerGoal(this, 8.0D, 1.35D));
        /* 优先级3：惊慌失措（受伤时触发） */
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.35D));
        /* 优先级4：随机走动，避开水，速度0.8 */
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        /* 优先级5：看向附近的生物，距离8格 */
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        /* 优先级6：随机环顾四周 **/
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /**
     * 处理玩家与实体的交互逻辑
     * 主要用于驯服功能：检测玩家是否手持配置的驯服物品并右键点击实体
     *
     * @param player 进行交互的玩家
     * @param hand   交互使用的手（主手或副手）
     * @return 交互结果，驯服成功返回 SUCCESS，否则返回父类的处理结果
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 获取 AvoidPlayerGoal 实例
        for (var goal : this.goalSelector.getAvailableGoals()) {
            if (goal.getGoal() instanceof AvoidPlayerGoal avoidGoal) {
                if (avoidGoal.tryTame(player)) {
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
                break;
            }
        }

        return super.mobInteract(player, hand);
    }


    /**
     * 每 tick 执行的 AI 逻辑
     * 处理受伤加速、声音播放等
     */
    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            // 受伤时设置恐慌计时器
            if (this.hurtTime > 0) {
                panicTimer = 400;
            }

            // 根据恐慌计时器调整移动速度
            if (panicTimer > 0) {
                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.35D);
                panicTimer--;
            } else {
                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25D);
            }

            // 播放移动或待机声音
            if (this.isMoving()) {
                walkSoundTimer++;
                if (walkSoundTimer >= 20) {
                    var stepSound = STEP_SOUNDS.get(RANDOM.nextInt(STEP_SOUNDS.size())).get();
                    this.playSound(stepSound, 0.5F, 1.0F);
                    walkSoundTimer = 0;
                }
            } else if (panicTimer <= 0) {
                idleSoundTimer++;
                if (idleSoundTimer >= 20 * 5) {
                    var ambientSound = this.getAmbientSound();
                    if (ambientSound != null) {
                        this.playSound(ambientSound, 0.5F, 1.0F);
                    }
                    idleSoundTimer = 0;
                }
            }
        }
    }

    /**
     * 实体受伤时的处理
     * @param source 伤害来源
     * @param amount 伤害值
     * @return 是否成功受伤
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (amount > 0) {
            SoundEvent randomHurt = HURT_SOUNDS.get(RANDOM.nextInt(HURT_SOUNDS.size())).get();
            this.playSound(randomHurt, 0.8F, 1.0F);

            boolean hasLimit = CommonConfig.EMERALD_DROP_MAX.get() > 0;
            if (!hasLimit || totalEmeraldDropped < CommonConfig.EMERALD_DROP_MAX.get()) {
                // 使用实际生命值来计算，避免秒杀时掉落过多
                float healthBefore = this.getHealth();
                boolean result = super.hurt(source, amount);
                float healthAfter = this.getHealth();
                float actualDamage = healthBefore - healthAfter;

                int emeraldCount = Math.max(1, (int) Math.floor(actualDamage / CommonConfig.EMERALD_PER_DAMAGE.get()));

                if (hasLimit) {
                    int remaining = CommonConfig.EMERALD_DROP_MAX.get() - totalEmeraldDropped;
                    emeraldCount = Math.min(emeraldCount, remaining);
                }

                this.spawnAtLocation(new ItemStack(Items.EMERALD, emeraldCount));
                totalEmeraldDropped += emeraldCount;

                return result;
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * 实体死亡时的处理
     * @param damageSource 伤害来源
     */
    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            // 播放原版猪的死亡声音
            this.playSound(SoundEvents.PIG_DEATH, 0.8F, 1.0F);

            // 生成固定掉落物
            ItemStack dropItem;
            if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                int minDrop = CommonConfig.COOKED_PORKCHOP_DROP_MIN.get();
                int maxDrop = CommonConfig.COOKED_PORKCHOP_DROP_MAX.get();
                int dropCount = RANDOM.nextInt(maxDrop - minDrop + 1) + minDrop;
                dropItem = new ItemStack(Items.COOKED_PORKCHOP, dropCount);
            } else {
                int minDrop = CommonConfig.PORKCHOP_DROP_MIN.get();
                int maxDrop = CommonConfig.PORKCHOP_DROP_MAX.get();
                int dropCount = RANDOM.nextInt(maxDrop - minDrop + 1) + minDrop;
                dropItem = new ItemStack(Items.PORKCHOP, dropCount);
            }
            this.spawnAtLocation(dropItem);

            // 生成额外战利品
            List<ItemStack> extraLoot = LootManager.getRandomLoot(this.level());
            for (ItemStack loot : extraLoot) {
                this.spawnAtLocation(loot);
            }
        }
        super.die(damageSource);
    }

    /**
     * 读取 NBT 数据
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.totalEmeraldDropped = compound.getInt("TotalEmeraldDropped");

        // 读取驯服状态
        long most = compound.contains("TamedByMost") ? compound.getLong("TamedByMost") : 0L;
        long least = compound.contains("TamedByLeast") ? compound.getLong("TamedByLeast") : 0L;

        if (most != 0 || least != 0) {
            UUID tamedBy = new UUID(most, least);

            // 应用到 Goal
            for (var goal : this.goalSelector.getAvailableGoals()) {
                if (goal.getGoal() instanceof AvoidPlayerGoal avoidGoal) {
                    try {
                        Field field = AvoidPlayerGoal.class.getDeclaredField("tamedBy");
                        field.setAccessible(true);
                        field.set(avoidGoal, tamedBy);
                    } catch (Exception e) {
                        // 忽略异常
                    }
                    break;
                }
            }
        }
    }

    /**
     * 写入 NBT 数据
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TotalEmeraldDropped", this.totalEmeraldDropped);

        // 写入驯服状态
        for (var goal : this.goalSelector.getAvailableGoals()) {
            if (goal.getGoal() instanceof AvoidPlayerGoal avoidGoal) {
                UUID tamedBy = avoidGoal.getTamedBy();
                if (tamedBy != null) {
                    compound.putLong("TamedByMost", tamedBy.getMostSignificantBits());
                    compound.putLong("TamedByLeast", tamedBy.getLeastSignificantBits());
                }
                break;
            }
        }
    }

    /**
     * 判断实体是否在移动
     * @return true 如果正在移动
     */
    private boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D;
    }

    /**
     * 注册 GeckoLib 动画控制器
     * 根据实体状态自动切换动画
     * @param controllers 控制器注册器
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            // 优先播放受伤动画
            if (this.hurtTime > 0) {
                event.setAndContinue(HURT);
                return PlayState.CONTINUE;
            }
            // 根据移动状态播放行走或待机动画
            if (event.isMoving()) {
                event.setAndContinue(WALK);
            } else {
                event.setAndContinue(IDLE);
            }
            return PlayState.CONTINUE;
        }));
    }

    /**
     * 获取繁殖后代
     * @param serverLevel 服务器世界
     * @param ageableMob 另一个可繁殖实体
     * @return null（不支持繁殖）
     */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    /**
     * 获取 GeckoLib 动画缓存实例
     * @return 动画缓存
     */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
