package com.chinaex123.piggy_bank;

import com.chinaex123.piggy_bank.config.PBServerConfig;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import com.chinaex123.piggy_bank.entity.client.renderer.PiggyBankRenderer;
import com.chinaex123.piggy_bank.init.PBEntity;
import com.chinaex123.piggy_bank.init.PBItems;
import com.chinaex123.piggy_bank.init.PBSounds;
import com.chinaex123.piggy_bank.util.LootManager;
import com.chinaex123.piggy_bank.world.PiggyBankBiomeModifiers;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PiggyBank.MOD_ID)
@SuppressWarnings("removal")
public class PiggyBank {
    public static final String MOD_ID = "piggy_bank";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PiggyBank() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PBEntity.register(modEventBus);
        PBItems.register(modEventBus);
        PBSounds.register(modEventBus);
        PiggyBankBiomeModifiers.SERIALIZERS.register(modEventBus);
        modEventBus.addListener(PiggyBank::registerAttributes);
        modEventBus.addListener(PiggyBank::registerRenderers);
        modEventBus.addListener(PiggyBank::addCreative);
        modEventBus.addListener(PiggyBank::onConfigReload);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PBServerConfig.SPEC);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PBEntity.PIGGY_BANK.get(), PiggyBankEntity.createAttributes().build());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PBEntity.PIGGY_BANK.get(), PiggyBankRenderer::new);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(PBItems.PIGGY_BANK_SPAWN_EGG.get());
        }
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == PBServerConfig.SPEC) {
            LootManager.clearCache();
        }
    }

}