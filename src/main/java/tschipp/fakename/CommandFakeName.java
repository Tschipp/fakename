package tschipp.fakename;

import static net.minecraft.commands.Commands.literal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class CommandFakeName {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {

        LiteralArgumentBuilder<CommandSourceStack> builder = literal("fakename")

                .then(
                        literal("real")
                                .then(
                                        Commands.argument("fakename", FakenameArgumentType.fakename())
                                                .executes((cmd) -> {
                                                    return handleRealname(cmd.getSource(), cmd.getArgument("fakename", String.class));
                                                })
                                )
                        )

                .then(
                        literal("clear")
                                .then(
                                        Commands.argument("target", EntityArgument.players())
                                                .requires(src -> { boolean b = src.hasPermission(Config.SERVER.commandPermissionLevelAll.get()); return b;})
                                                .executes((cmd) -> {
                                                    return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
                                                })
                                )
                                .executes((cmd) -> {
                                    return handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()));
                                })
                )

                .then(
                        literal("set")
                        .then(
                                Commands.argument("fakename", StringArgumentType.string())
                                        .executes((cmd) -> {
                                            return handleSetname(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()), StringArgumentType.getString(cmd, "fakename"));
                                        })
                        )
                        .then(
                                Commands.argument("target", EntityArgument.players())
                                .requires(src -> src.hasPermission(Config.SERVER.commandPermissionLevelAll.get()))
                                .then(
                                        Commands.argument("fakename", StringArgumentType.string())
                                        .executes((cmd) -> {
                                            return handleSetname(cmd.getSource(),EntityArgument.getPlayers(cmd, "target"),StringArgumentType.getString(cmd,"fakename"));
                                                })
                                )
                        )
                        
                );

        dispatcher.register(builder);
    }

    private static int handleSetname(CommandSourceStack source, Collection<ServerPlayer> players, String string) {
        string = string.replace("&", "\u00a7") + "\u00a7r";

        for (ServerPlayer player : players)
        {
            CompoundTag tag = player.getPersistentData();
            tag.putString("fakename", string);
            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s name is now " + string));
            FakeName.sendPacket(player, string, 0);
        }

        return 1;
    }

    private static int handleClear(CommandSourceStack source, Collection<ServerPlayer> players)
    {
        for (ServerPlayer player : players)
        {
            CompoundTag tag = player.getPersistentData();
            tag.remove("fakename");
            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s fake name was cleared!"));
            FakeName.sendPacket(player, "", 1);
        }

        return 1;
    }

    private static int handleRealname(CommandSourceStack source, String string)
    {
    	String copy = string;
        string = string.replace("&", "\u00a7") + "\u00a7r";
    	string = ChatFormatting.stripFormatting(string);
        PlayerList players = source.getServer().getPlayerList();
        boolean succ = false;
        for (Player player : players.getPlayers())
        {
            if (player.getPersistentData() != null && player.getPersistentData().contains("fakename"))
            {
            	String fn = ChatFormatting.stripFormatting(player.getPersistentData().getString("fakename"));
                if (fn.equalsIgnoreCase(string))
                {
                    source.sendSystemMessage(Component.literal(copy + "'s real name is " + player.getGameProfile().getName()));
                    succ = true;
                }
            }
        }

        if(succ)
        	return 1;
        
        source.sendFailure(Component.literal("No player with that name was found!"));
        return 0;
    }

    public static class FakenameArgumentType implements ArgumentType<String>
    {

        public static FakenameArgumentType fakename()
        {
            return new FakenameArgumentType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
        {
            
            if(context.getSource() instanceof CommandSourceStack cs)
            {
                PlayerList players = cs.getServer().getPlayerList();
                for (Player player : players.getPlayers())
                {
                    if (player.getPersistentData() != null && player.getPersistentData().contains("fakename"))
                    {
                    	String name = ChatFormatting.stripFormatting(player.getPersistentData().getString("fakename"));
                    	name = name.contains(" ") ? ('"' + name + '"') : name;
                    	
                        builder.suggest(name);
                    }
                }

                return builder.buildFuture();
            }
            else if (context.getSource() instanceof SharedSuggestionProvider)
            {
                return ((SharedSuggestionProvider) context.getSource()).customSuggestion((CommandContext<SharedSuggestionProvider>) context);
            }
            else
            {
                return Suggestions.empty();
            } 
        }

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException
        {
        	return reader.readString();
        }

//        public static class Serializer implements ArgumentTypeInfo<FakenameArgumentType>
//        {
//
//            @Override
//            public void serializeToNetwork(FakenameArgumentType p_121579_, FriendlyByteBuf p_121580_)
//            {
//            }
//
//            @Override
//            public FakenameArgumentType deserializeFromNetwork(FriendlyByteBuf p_121581_)
//            {
//                return fakename();
//            }
//
//            @Override
//            public void serializeToJson(FakenameArgumentType p_121577_, JsonObject p_121578_)
//            {
//            }
//
//        }

    }
}
