package com.chinaex123.piggy_bank.data;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.init.ModEntityTags;
import com.chinaex123.piggy_bank.init.ModEntitys;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagsProvider extends EntityTypeTagsProvider {

    public ModEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PiggyBank.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModEntityTags.PIG)
                .add(EntityType.PIG)
                .add(ModEntitys.PIGGY_BANK.get());
    }
}
