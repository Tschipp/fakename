package tschipp.fakename;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;

public class CommandFakeName
{

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("fakename")

				.then(Commands.literal("real").then(Commands.argument("fakename", FakenameArgumentType.fakename())).executes((cmd) -> {
					return handleRealname(cmd.getSource(), cmd.getArgument("fakename", String.class));
				}))

				.then(Commands.literal("clear").requires(src -> src.hasPermissionLevel(Config.SERVER.commandPermissionLevel.get())).executes((cmd) -> {
					return handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().asPlayer()));
				}))

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermissionLevel(Config.SERVER.commandPermissionLevel.get())).executes((cmd) -> {
					return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
				})))

				.then(Commands.literal("set").then(Commands.argument("fakename", StringArgumentType.string())).requires(src -> src.hasPermissionLevel(Config.SERVER.commandPermissionLevel.get())).executes((cmd) -> {
					return handleSetname(cmd.getSource(), Collections.singleton(cmd.getSource().asPlayer()), cmd.getArgument("fakename", String.class));
				}))

				.then(Commands.literal("set").then(Commands.argument("target", EntityArgument.players()).then(Commands.argument("fakename", StringArgumentType.string())).requires(src -> src.hasPermissionLevel(Config.SERVER.commandPermissionLevel.get())).executes((cmd) -> {
					return handleSetname(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"), cmd.getArgument("fakename", String.class));
				})));

		dispatcher.register(builder);

	}

	private static int handleSetname(CommandSource source, Collection<ServerPlayerEntity> players, String string)
	{
		string = string.replace("&", "\u00a7");

		for (ServerPlayerEntity player : players)
		{
			CompoundNBT tag = player.getPersistentData();
			tag.putString("fakename", string);
			source.sendFeedback(new StringTextComponent(player.getName() + "'s name is now " + string), false);
			FakeName.network.send(PacketDistributor.ALL.noArg(), new FakeNamePacket(string, player.getEntityId(), 0));
		}

		return 1;
	}

	private static int handleClear(CommandSource source, Collection<ServerPlayerEntity> players)
	{
		for (ServerPlayerEntity player : players)
		{
			CompoundNBT tag = player.getPersistentData();
			tag.remove("fakename");
			source.sendFeedback(new StringTextComponent(player.getName() + "'s fake name was cleared!"), false);
			FakeName.network.send(PacketDistributor.ALL.noArg(), new FakeNamePacket("", player.getEntityId(), 1));
		}
		
		return 1;
	}

	private static int handleRealname(CommandSource source, String string)
	{
		PlayerList players = source.getServer().getPlayerList();
		for (PlayerEntity player : players.getPlayers())
		{
			if (player.getPersistentData() != null && player.getPersistentData().contains("fakename"))
			{
				if(TextFormatting.getTextWithoutFormattingCodes(player.getPersistentData().getString("fakename")).equalsIgnoreCase(string))
				{
					source.sendFeedback(new StringTextComponent(string + "'s real name is " + player.getGameProfile().getName()), false);
					return 1;
				}
			}
		}
		
		source.sendErrorMessage(new StringTextComponent("No player with that name was found!"));
		return 0;
	}

	public static class FakenameArgumentType implements ArgumentType<String>
	{

		public static FakenameArgumentType fakename()
		{
			return new FakenameArgumentType();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			PlayerList players = ((CommandSource) context.getSource()).getServer().getPlayerList();
			for (PlayerEntity player : players.getPlayers())
			{
				if (player.getPersistentData() != null && player.getPersistentData().contains("fakename"))
				{
					builder.suggest(TextFormatting.getTextWithoutFormattingCodes(player.getPersistentData().getString("fakename")));
				}
			}

			return builder.buildFuture();
		}

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.getRemaining();
		}

	}
}
