package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface PBItems {
    DeferredRegister.Items ITEMS_REGISTER = DeferredRegister.createItems(PiggyBank.MOD_ID);

    DeferredItem<Item> PIGGY_BANK_SPAWN_EGG = ITEMS_REGISTER.registerItem("piggy_bank_spawn_egg",
            (prop) -> new SpawnEggItem(prop.spawnEgg(PBEntitys.PIGGY_BANK.get())));

    static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
