package com.misterd.agritech.command;

import com.misterd.agritech.config.Config;
import com.misterd.agritech.config.PlantablesConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ATCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("agritech")
                        .then(Commands.literal("reload")
                                .executes(context -> reloadAll(context.getSource()))
                                .then(Commands.literal("plantables")
                                        .executes(context -> {
                                            try {
                                                PlantablesConfig.loadConfig();
                                                context.getSource().sendSuccess(() -> Component.literal("Agritech plantables config reloaded successfully!"), true);
                                                return 1;
                                            } catch (Exception e) {
                                                context.getSource().sendFailure(Component.literal("Failed to reload Agritech plantables config: " + e.getMessage()));
                                                return 0;
                                            }
                                        }))
                                .then(Commands.literal("config")
                                        .executes(context -> {
                                            try {
                                                Config.load();
                                                context.getSource().sendSuccess(() -> Component.literal("Agritech main config reloaded successfully!"), true);
                                                return 1;
                                            } catch (Exception e) {
                                                context.getSource().sendFailure(Component.literal("Failed to reload Agritech main config: " + e.getMessage()));
                                                return 0;
                                            }
                                        })))
        );
    }

    private static int reloadAll(CommandSourceStack source) {
        try {
            Config.load();
            PlantablesConfig.loadConfig();
            source.sendSuccess(() -> Component.literal("All Agritech configs reloaded successfully!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload Agritech configs: " + e.getMessage()));
            return 0;
        }
    }
}