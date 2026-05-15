package com.chinaex123.piggy_bank.entity;

import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.init.ModSounds;
import com.chinaex123.piggy_bank.util.LootManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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

    // 受伤时的随机声音列表（3个不同的受伤音效）
    private static final List<Supplier<SoundEvent>> HURT_SOUNDS = Arrays.asList(
            ModSounds.PIGGY_BANK_HURT_1,
            ModSounds.PIGGY_BANK_HURT_2,
            ModSounds.PIGGY_BANK_HURT_3
    );
    private static final Random RANDOM = new Random();

    // 声音播放计时器
    private int idleSoundTimer = 0;      // 待机声音计时器
    private int walkSoundTimer = 0;      // 行走声音计时器
    private int panicTimer = 0;          // 恐慌逃跑计时器（控制受伤后的加速持续时间）
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
                .add(Attributes.MAX_HEALTH, 40.0D)        // 最大生命值
                .add(Attributes.MOVEMENT_SPEED, 0.35D)    // 基础移动速度
                .add(Attributes.FOLLOW_RANGE, 16.0D);     // 跟随范围：16格
    }

    /**
     * 注册实体的 AI 行为目标
     * 优先级越小，优先级越高
     */
    @Override
    protected void registerGoals() {
        /* 优先级0：漂浮在水面 */
        this.goalSelector.addGoal(0, new FloatGoal(this));
        /* 优先级1：被金锭吸引，速度1.2 */
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.2D, stack -> stack.is(Items.GOLD_INGOT), false));
        /* 优先级2：避开玩家，距离8格，快速逃离 */
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.5D, 1.5D));
        /* 优先级3：惊慌失措，速度1.0 */
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.0D));
        /* 优先级4：随机走动，避开水，速度0.8 */
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        /* 优先级5：看向附近的生物，距离8格 */
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        /* 优先级6：随机环顾四周 **/
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /**
     * 每 tick 执行的 AI 逻辑
     * 处理受伤加速、声音播放等
     */
    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            // 受伤时设置恐慌计时器为 200 tick（10秒）
            if (this.hurtTime > 0) {
                panicTimer = 200;
            }

            // 根据恐慌计时器调整移动速度
            if (panicTimer > 0) {
                // 恐慌逃跑时的速度：0.5（持续10秒）
                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25D);
                panicTimer--;
            } else {
                // 正常行走速度：0.35
                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25D);
            }

            // 播放移动或待机声音
            if (this.isMoving()) {
                /* 每 20 tick（1秒）播放一次脚步声 */
                walkSoundTimer++;
                if (walkSoundTimer >= 20) {
                    this.playSound(SoundEvents.PIG_STEP, 0.5F, 1.0F);
                    walkSoundTimer = 0;
                }
            } else {
                /* 每 80 tick（4秒）播放一次待机声音 */
                idleSoundTimer++;
                if (idleSoundTimer >= 80) {
                    this.playSound(SoundEvents.PIG_AMBIENT, 0.5F, 1.0F);
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
        if (!this.level().isClientSide && amount > 0) {
            // 随机播放一个受伤音效
            SoundEvent randomHurt = HURT_SOUNDS.get(RANDOM.nextInt(HURT_SOUNDS.size())).get();
            this.playSound(randomHurt, 0.8F, 1.0F);

            // 检查是否达到上限（0表示无上限）
            boolean hasLimit = CommonConfig.EMERALD_DROP_MAX.get() > 0;
            if (!hasLimit || totalEmeraldDropped < CommonConfig.EMERALD_DROP_MAX.get()) {
                int emeraldCount = Math.max(1, (int) Math.floor(amount / CommonConfig.EMERALD_PER_DAMAGE.get()));

                // 如果有上限，确保不超过
                if (hasLimit) {
                    int remaining = CommonConfig.EMERALD_DROP_MAX.get() - totalEmeraldDropped;
                    emeraldCount = Math.min(emeraldCount, remaining);
                }

                this.spawnAtLocation(new ItemStack(Items.EMERALD, emeraldCount));
                totalEmeraldDropped += emeraldCount;
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
            List<ItemStack> extraLoot = LootManager.getRandomLoot();
            for (ItemStack loot : extraLoot) {
                this.spawnAtLocation(loot);
            }
        }
        super.die(damageSource);
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
