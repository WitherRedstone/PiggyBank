package com.chinaex123.piggy_bank.entity.client.model;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PiggyBankModel extends GeoModel<PiggyBankEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(PiggyBank.MOD_ID, "geckolib/models/entity/piggy_bank.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(PiggyBank.MOD_ID, "textures/entity/piggy_bank.png");
    }

    @Override
    public Identifier getAnimationResource(PiggyBankEntity entity) {
        return Identifier.fromNamespaceAndPath(PiggyBank.MOD_ID, "geckolib/animations/entity/piggy_bank.animation.json");
    }
}
