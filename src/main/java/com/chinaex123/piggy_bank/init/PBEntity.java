package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.entity.PiggyBankEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PBEntity {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, PiggyBank.MOD_ID);

    public static final Supplier<EntityType<PiggyBankEntity>> PIGGY_BANK =
            ENTITY_TYPES.register("piggy_bank", () -> EntityType.Builder.of(PiggyBankEntity::new, MobCategory.CREATURE)
                    .sized(1.0f, 2.0f)
                    .build(String.valueOf(ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, "piggy_bank"))));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
