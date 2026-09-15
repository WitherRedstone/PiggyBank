package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface PBItems {
    DeferredRegister.Items ITEMS_REGISTER = DeferredRegister.createItems(PiggyBank.MOD_ID);

    Supplier<DeferredSpawnEggItem> PIGGY_BANK_SPAWN_EGG = ITEMS_REGISTER.register("piggy_bank_spawn_egg",
            () -> new DeferredSpawnEggItem(PBEntitys.PIGGY_BANK, 0xFFC0CB, 0xFF69B4, new Item.Properties()));

    static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
