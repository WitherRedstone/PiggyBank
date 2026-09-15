package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public interface PBItems {
    DeferredRegister<Item> ITEMS_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, PiggyBank.MOD_ID);

    Supplier<ForgeSpawnEggItem> PIGGY_BANK_SPAWN_EGG = ITEMS_REGISTER.register("piggy_bank_spawn_egg",
            () -> new ForgeSpawnEggItem(PBEntity.PIGGY_BANK, 0xFFC0CB, 0xFF69B4, new Item.Properties()));

    static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
