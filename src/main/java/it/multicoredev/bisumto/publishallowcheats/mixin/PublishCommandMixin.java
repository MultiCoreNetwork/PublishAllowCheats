package it.multicoredev.bisumto.publishallowcheats.mixin;

import com.google.common.escape.ArrayBasedEscaperMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PublishCommand.class)
public class PublishCommandMixin {
    @Shadow
    private static SimpleCommandExceptionType FAILED_EXCEPTION;
    @Shadow
    private static DynamicCommandExceptionType ALREADY_PUBLISHED_EXCEPTION;

    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            (LiteralArgumentBuilder)(
                (LiteralArgumentBuilder)(
                    (LiteralArgumentBuilder)(
                        (LiteralArgumentBuilder) CommandManager.literal("publish").requires(
                            (serverCommandSource) -> {
                                return serverCommandSource.hasPermissionLevel(4);
                            }
                        )
                    ).executes(
                        (commandContext) -> {
                            return execute((ServerCommandSource)commandContext.getSource(), NetworkUtils.findLocalPort(), false);
                        }
                    )
                ).then(
                    CommandManager.argument("port", IntegerArgumentType.integer(0, 65535)).executes(
                        (commandContext) -> {
                            return execute((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true);
                        }
                    )
                )
            ).then(
                ((LiteralArgumentBuilder)CommandManager.literal("allowCheats").executes(
                    (commandContext) -> {
                        return execute((ServerCommandSource)commandContext.getSource(), NetworkUtils.findLocalPort(), true);
                    }
                ).then(
                    CommandManager.argument("port", IntegerArgumentType.integer(0, 65535)).executes(
                        (commandContext) -> {
                            return execute((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true);
                        }
                    )
                ))
            )
        );
    }

    private static int execute(ServerCommandSource source, int port, boolean allowCheats) throws CommandSyntaxException {
        if (source.getMinecraftServer().isRemote()) {
            throw ALREADY_PUBLISHED_EXCEPTION.create(source.getMinecraftServer().getServerPort());
        } else if (!source.getMinecraftServer().openToLan(source.getMinecraftServer().getDefaultGameMode(), allowCheats, port)) {
            throw FAILED_EXCEPTION.create();
        } else {
            source.sendFeedback(new TranslatableText("commands.publish.success", new Object[]{port}), true);
            return port;
        }
    }
}



 