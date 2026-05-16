package com.chinaex123.piggy_bank.world;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.init.ModEntitys;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

/**
 * 宝箱猪生物群系修改器
 * 负责在指定的主世界群系中注册宝箱猪的自然生成规则
 */
public class PiggyBankBiomeModifiers {

    // 生物群系修改器注册表
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, PiggyBank.MOD_ID);

    // 序列化器注册表（用于数据包加载）
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PiggyBank.MOD_ID);

    // 注册序列化器
    public static final Supplier<MapCodec<? extends BiomeModifier>> PIGGY_BANK_SPAWN_CODEC =
            SERIALIZERS.register("piggy_bank_spawn", () -> PiggyBankSpawnModifier.CODEC);

    // 注册生物群系修改器实例
    public static final Supplier<BiomeModifier> PIGGY_BANK_SPAWN =
            BIOME_MODIFIERS.register("piggy_bank_spawn", PiggyBankSpawnModifier::new);

    // 允许生成的群系列表（平原、森林、沼泽）
    private static final List<TagKey<Biome>> SPAWN_BIOMES = List.of(
            Tags.Biomes.IS_PLAINS, // 平原类群系
            Tags.Biomes.IS_FOREST, // 森林类群系
            Tags.Biomes.IS_SWAMP // 沼泽类群系
    );

    /**
     * 宝箱猪生成修改器实现
     * 根据配置和群系标签控制宝箱猪的自然生成
     */
    public record PiggyBankSpawnModifier() implements BiomeModifier {
        /**
         * 修改生物群系的生成设置
         * @param biome 当前生物群系
         * @param phase 修改阶段
         * @param builder 生物群系信息构建器
         */
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            // 只在 ADD 阶段执行
            if (phase == Phase.ADD) {
                // 检查配置中是否启用了自然生成
                if (!CommonConfig.SPAWN_ENABLED.get()) {
                    return;
                }

                // 限制只在主世界生成，排除深暗之域
                if (biome.is(BiomeTags.IS_OVERWORLD) && !biome.is(Biomes.DEEP_DARK)) {
                    // 检查当前群系是否在允许的列表中
                    boolean shouldSpawn = SPAWN_BIOMES.stream().anyMatch(biome::is);

                    if (shouldSpawn) {
                        // 从配置文件读取生成参数
                        int weight = CommonConfig.SPAWN_WEIGHT.get(); // 生成权重
                        int minCount = CommonConfig.SPAWN_MIN_COUNT.get(); // 最小生成数量
                        int maxCount = CommonConfig.SPAWN_MAX_COUNT.get(); // 最大生成数量

                        // 添加宝箱猪的生成规则到生物群系
                        builder.getMobSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(ModEntitys.PIGGY_BANK.get(), weight, minCount, maxCount)
                        );
                    }
                }
            }
        }

        /**
         * 返回序列化编码器
         * @return MapCodec 实例
         */
        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }

        // 静态编码器实例（无参数，使用 unit 模式）
        public static final MapCodec<PiggyBankSpawnModifier> CODEC = MapCodec.unit(PiggyBankSpawnModifier::new);
    }
}
