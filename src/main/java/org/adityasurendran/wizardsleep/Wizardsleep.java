package org.adityasurendran.wizardsleep;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.UUID;

public class Wizardsleep implements ModInitializer {


    private static final HashSet<UUID> waitingPlayers = new HashSet<>();

    @Override
    public void onInitialize() {


        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (!world.isClient()) {

                BlockPos clicked = hitResult.getBlockPos();
                BlockPos below = player.getBlockPos().down();


                if (clicked.equals(below)) {
                    waitingPlayers.add(player.getUuid());

                    player.sendMessage(Text.literal("Type /sleep confirm to sleep"), false);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    CommandManager.literal("sleep")
                            .then(CommandManager.literal("confirm")
                                    .executes(context -> sleepPlayer(context.getSource()))
                            )
            );

        });
    }


    private int sleepPlayer(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayer();
            ServerWorld world = source.getWorld();


            if (!waitingPlayers.contains(player.getUuid())) {
                player.sendMessage(Text.literal("Click the block under you first."), false);
                return 0;
            }

            waitingPlayers.remove(player.getUuid());

            if (world.isDay()) {
                player.sendMessage(Text.literal("Not nighttime mate."), false);
                return 0;
            }

            BlockPos below = player.getBlockPos().down();

            if (!world.getBlockState(below).isSolidBlock(world, below)) {
                player.sendMessage(Text.literal("You must stand on solid ground."), false);
                return 0;
            }

            world.setTimeOfDay(1000);

            player.setHealth(player.getMaxHealth());

            player.sendMessage(Text.literal("Ya slept on the ground son."), false);

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}