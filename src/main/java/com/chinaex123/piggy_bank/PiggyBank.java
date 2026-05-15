package com.chinaex123.piggy_bank;

import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import com.chinaex123.piggy_bank.entity.client.renderer.PiggyBankRenderer;
import com.chinaex123.piggy_bank.init.ModEntitys;
import com.chinaex123.piggy_bank.init.ModItems;
import com.chinaex123.piggy_bank.init.ModSounds;
import com.chinaex123.piggy_bank.util.LootManager;
import com.chinaex123.piggy_bank.world.PiggyBankBiomeModifiers;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

@Mod(PiggyBank.MOD_ID)
public class PiggyBank {
    public static final String MOD_ID = "piggy_bank";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PiggyBank(IEventBus modEventBus, ModContainer modContainer) {
        ModEntitys.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        PiggyBankBiomeModifiers.BIOME_MODIFIERS.register(modEventBus);
        PiggyBankBiomeModifiers.SERIALIZERS.register(modEventBus);
        modEventBus.addListener(PiggyBank::registerAttributes);
        modEventBus.addListener(PiggyBank::registerRenderers);
        modEventBus.addListener(PiggyBank::addCreative);
        modEventBus.addListener(PiggyBank::onConfigReload);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntitys.PIGGY_BANK.get(), PiggyBankEntity.createAttributes().build());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntitys.PIGGY_BANK.get(), PiggyBankRenderer::new);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.PIGGY_BANK_SPAWN_EGG.get());
        }
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            LootManager.clearCache();
        }
    }

}
