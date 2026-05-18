package com.chinaex123.piggy_bank.entity.client.renderer;

import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import com.chinaex123.piggy_bank.init.ModEntitys;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PiggyBankRenderer extends GeoEntityRenderer<PiggyBankEntity, EntityRenderState> {

    public PiggyBankRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntitys.PIGGY_BANK.get());
    }
}
