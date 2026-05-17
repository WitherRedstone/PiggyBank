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

    /* 待机音效 */
    public static final Supplier<SoundEvent> PIGGY_BANK_IDLE1 = register("piggy_bank.idle1");
    public static final Supplier<SoundEvent> PIGGY_BANK_IDLE2 = register("piggy_bank.idle2");
    public static final Supplier<SoundEvent> PIGGY_BANK_IDLE3 = register("piggy_bank.idle3");

    /* 行走音效 */
    public static final Supplier<SoundEvent> PIGGY_BANK_STEP1 = register("piggy_bank.step1");
    public static final Supplier<SoundEvent> PIGGY_BANK_STEP2 = register("piggy_bank.step2");
    public static final Supplier<SoundEvent> PIGGY_BANK_STEP3 = register("piggy_bank.step3");
    public static final Supplier<SoundEvent> PIGGY_BANK_STEP4 = register("piggy_bank.step4");
    public static final Supplier<SoundEvent> PIGGY_BANK_STEP5 = register("piggy_bank.step5");

    /* 惊吓音效 */
    public static final Supplier<SoundEvent> PIGGY_BANK_JUMP1 = register("piggy_bank.jump1");
    public static final Supplier<SoundEvent> PIGGY_BANK_JUMP2 = register("piggy_bank.jump2");

    /* 受伤音效 */
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT1 = register("piggy_bank.hurt1");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT2 = register("piggy_bank.hurt2");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT3 = register("piggy_bank.hurt3");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT4 = register("piggy_bank.hurt4");
    public static final Supplier<SoundEvent> PIGGY_BANK_HURT5 = register("piggy_bank.hurt5");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(PiggyBank.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
