package com.chinaex123.piggy_bank.data;

import com.chinaex123.piggy_bank.PiggyBank;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = PiggyBank.MOD_ID)
public class ModDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModModelsProvider::new);
        event.createProvider(ModEntityTagsProvider::new);
    }
}
