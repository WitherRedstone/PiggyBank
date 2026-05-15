package com.chinaex123.piggy_bank.entity.client.renderer;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.entity.client.model.PiggyBankModel;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PiggyBankRenderer extends GeoEntityRenderer<PiggyBankEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, "textures/entity/piggy_bank.png");

    public PiggyBankRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, new PiggyBankModel());
    }

    @Override
    public ResourceLocation getTextureLocation(PiggyBankEntity entity) {
        return TEXTURE;
    }
}
