package com.chinaex123.piggy_bank.world;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.config.PBServerConfig;
import com.chinaex123.piggy_bank.init.PBBiomeTags;
import com.chinaex123.piggy_bank.init.PBEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

/**
 * 宝箱猪生物群系修改器
 * 负责在指定的主世界群系中注册宝箱猪的自然生成规则
 */
public class PiggyBankBiomeModifiers {

    // 序列化器注册表（用于数据包加载）
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PiggyBank.MOD_ID);

    // 注册序列化器
    public static final Supplier<Codec<? extends BiomeModifier>> PIGGY_BANK_SPAWN_CODEC =
            SERIALIZERS.register("piggy_bank_spawn", () -> PiggyBankSpawnModifier.CODEC);

    // 允许生成的群系列表（平原、森林、沼泽）
    private static final List<TagKey<Biome>> SPAWN_BIOMES = List.of(
            Tags.Biomes.IS_PLAINS, // 平原类群系
            PBBiomeTags.IS_FOREST, // 森林类群系
            Tags.Biomes.IS_SWAMP // 沼泽类群系
    );

    /**
     * 宝箱猪生成修改器实现
     * 根据配置和群系标签控制宝箱猪的自然生成
     */
    public static final class PiggyBankSpawnModifier implements BiomeModifier {
        public static final Codec<PiggyBankSpawnModifier> CODEC = Codec.unit(PiggyBankSpawnModifier::new);

        public PiggyBankSpawnModifier() {
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                if (!PBServerConfig.SPAWN_ENABLED.get()) {
                    return;
                }

                if (biome.is(BiomeTags.IS_OVERWORLD) && !biome.is(Biomes.DEEP_DARK)) {
                    boolean shouldSpawn = SPAWN_BIOMES.stream().anyMatch(biome::is);

                    if (shouldSpawn) {
                        int weight = PBServerConfig.SPAWN_WEIGHT.get();
                        int minCount = PBServerConfig.SPAWN_MIN_COUNT.get();
                        int maxCount = PBServerConfig.SPAWN_MAX_COUNT.get();

                        builder.getMobSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(PBEntity.PIGGY_BANK.get(), weight, minCount, maxCount)
                        );
                    }
                }
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return PIGGY_BANK_SPAWN_CODEC.get();
        }
    }
}