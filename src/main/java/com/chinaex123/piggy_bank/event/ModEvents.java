package com.chinaex123.piggy_bank.event;

import com.chinaex123.piggy_bank.PiggyBank;
import com.chinaex123.piggy_bank.config.CommonConfig;
import com.chinaex123.piggy_bank.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PiggyBank.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!CommonConfig.DISABLE_SPAWNER_PLACEMENT.get()) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        if (event.getLevel().getBlockEntity(pos) instanceof SpawnerBlockEntity) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.is(ModItems.PIGGY_BANK_SPAWN_EGG.get())) {
                event.setCanceled(true);
                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.translatable("piggy_bank.message.cannot_place_in_spawner"));
                }
            }
        }
    }
}
