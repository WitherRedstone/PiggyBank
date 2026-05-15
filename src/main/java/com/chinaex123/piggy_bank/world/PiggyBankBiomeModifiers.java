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

public class PiggyBankBiomeModifiers {

    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, PiggyBank.MOD_ID);

    public static final Supplier<BiomeModifier> PIGGY_BANK_SPAWN =
            BIOME_MODIFIERS.register("piggy_bank_spawn", PiggyBankSpawnModifier::new);

    // 可生成的群系列表
    private static final List<TagKey<Biome>> SPAWN_BIOMES = List.of(
            Tags.Biomes.IS_PLAINS, // 平原
            Tags.Biomes.IS_FOREST, // 森林
            Tags.Biomes.IS_SWAMP // 沼泽
    );

    /**
     * 自定义生物群系修改器
     * 在配置的群系中添加宝箱猪的生成
     */
    public record PiggyBankSpawnModifier() implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                // 检查是否启用生成
                if (!CommonConfig.SPAWN_ENABLED.get()) {
                    return;
                }

                // 只在主世界生成，排除深暗之域
                if (biome.is(BiomeTags.IS_OVERWORLD) && !biome.is(Biomes.DEEP_DARK)) {
                    // 检查当前群系是否在允许的列表中
                    boolean shouldSpawn = SPAWN_BIOMES.stream().anyMatch(biome::is);

                    if (shouldSpawn) {
                        // 从配置读取生成参数
                        int weight = CommonConfig.SPAWN_WEIGHT.get();
                        int minCount = CommonConfig.SPAWN_MIN_COUNT.get();
                        int maxCount = CommonConfig.SPAWN_MAX_COUNT.get();

                        // 添加生成配置
                        builder.getMobSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(ModEntitys.PIGGY_BANK.get(), weight, minCount, maxCount)
                        );
                    }
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }

        public static final MapCodec<PiggyBankSpawnModifier> CODEC = MapCodec.unit(PiggyBankSpawnModifier::new);
    }
}
