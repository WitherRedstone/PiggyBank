package com.chinaex123.piggy_bank.event;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.config.PBServerConfig;
import com.chinaex123.piggy_bank.init.PBItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PiggyBank.MOD_ID)
public class PBEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!PBServerConfig.DISABLE_SPAWNER_PLACEMENT.get()) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        if (event.getLevel().getBlockEntity(pos) instanceof SpawnerBlockEntity) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.is(PBItems.PIGGY_BANK_SPAWN_EGG.get())) {
                event.setCanceled(true);
                event.getEntity().displayClientMessage(Component.translatable("piggy_bank.message.cannot_place_in_spawner"), true);
            }
        }
    }
}
