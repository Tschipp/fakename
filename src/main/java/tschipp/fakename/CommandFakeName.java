package tschipp.fakename;

import static net.minecraft.command.Commands.literal;

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

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandFakeName
{

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{		
				
		LiteralArgumentBuilder<CommandSource> builder = literal("fakename")

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
								.requires(src -> {boolean b = src.hasPermissionLevel(Config.SERVER.commandPermissionLevelAll.get()); System.out.println(b); System.out.println(Config.SERVER.commandPermissionLevelAll.get()); return b;})
								.executes((cmd) -> {
									return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
								})
						)
						.executes((cmd) -> {
							return handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().asPlayer()));
						})
				)

				.then(
						literal("set")
						.then(
								Commands.argument("target", EntityArgument.players())
								.requires(src -> src.hasPermissionLevel(Config.SERVER.commandPermissionLevelAll.get()))
								.then(
										Commands.argument("fakename", StringArgumentType.string())
										.executes((cmd) -> {
											return handleSetname(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"), StringArgumentType.getString(cmd, "fakename"));
										})
								)
						)
						.then(
								Commands.argument("fakename", StringArgumentType.string())
								.executes((cmd) -> {
									return handleSetname(cmd.getSource(), Collections.singleton(cmd.getSource().asPlayer()), StringArgumentType.getString(cmd, "fakename"));
								})
						)
				);

		dispatcher.register(builder);

	}

	private static int handleSetname(CommandSource source, Collection<ServerPlayerEntity> players, String string)
	{
		string = string.replace("&", "\u00a7") + "§r";

		for (ServerPlayerEntity player : players)
		{
			CompoundNBT tag = player.getPersistentData();
			tag.putString("fakename", string);
			source.sendFeedback(new StringTextComponent(player.getName().getUnformattedComponentText() + "'s name is now " + string), false);
			FakeName.sendPacket(player, string, 0);
		}

		return 1;
	}

	private static int handleClear(CommandSource source, Collection<ServerPlayerEntity> players)
	{
		for (ServerPlayerEntity player : players)
		{
			CompoundNBT tag = player.getPersistentData();
			tag.remove("fakename");
			source.sendFeedback(new StringTextComponent(player.getName().getUnformattedComponentText() + "'s fake name was cleared!"), false);
			FakeName.sendPacket(player, "", 1);
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
				if (TextFormatting.getTextWithoutFormattingCodes(player.getPersistentData().getString("fakename")).equalsIgnoreCase(string))
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

		@SuppressWarnings("unchecked")
		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			if (!(context.getSource() instanceof ISuggestionProvider))
			{
				return Suggestions.empty();
			}
			else if (context.getSource() instanceof ClientSuggestionProvider)
			{
				return ((ClientSuggestionProvider) context.getSource()).getSuggestionsFromServer((CommandContext<ISuggestionProvider>) context, builder);
			}
			else
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
		}

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			String rest = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
			return rest;
		}

		public static class Serializer implements IArgumentSerializer<FakenameArgumentType>
		{

			@Override
			public void write(FakenameArgumentType argument, PacketBuffer buffer)
			{
			}

			@Override
			public FakenameArgumentType read(PacketBuffer buffer)
			{
				return fakename();
			}

			@Override
			public void write(FakenameArgumentType p_212244_1_, JsonObject p_212244_2_)
			{
			}

		}

	}
}
