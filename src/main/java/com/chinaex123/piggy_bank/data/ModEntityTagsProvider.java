package com.chinaex123.piggy_bank.data;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.init.PBEntityTags;
import com.chinaex123.piggy_bank.init.PBEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagsProvider extends EntityTypeTagsProvider {

    public ModEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PiggyBank.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PBEntityTags.PIG)
                .add(EntityType.PIG)
                .add(PBEntity.PIGGY_BANK.get());
    }
}