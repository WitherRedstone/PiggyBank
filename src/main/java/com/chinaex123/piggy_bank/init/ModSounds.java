package com.chinaex123.piggy_bank.init;

import com.chinaex123.piggy_bank.PiggyBank;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, PiggyBank.MOD_ID);

    public static final Supplier<SoundEvent> PIGGY_BANK_HURT_1 = register("piggy_bank.hurt1");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT_2 = register("piggy_bank.hurt2");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT_3 = register("piggy_bank.hurt3");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
