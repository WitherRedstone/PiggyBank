package com.chinaex123.piggy_bank.entity.client.model;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PiggyBankModel extends GeoModel<PiggyBankEntity> {

    @Override
    public ResourceLocation getModelResource(PiggyBankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, "geo/piggy_bank.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PiggyBankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, "textures/entity/piggy_bank.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PiggyBankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, "animations/piggy_bank.animation.json");
    }
}
