package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public interface PBBiomeTags {

    TagKey<Biome> IS_FOREST = basicBiomeTag("is_forest");

    private static TagKey<Biome> basicBiomeTag(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, name));
    }
}
