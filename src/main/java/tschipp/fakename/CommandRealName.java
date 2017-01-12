package tschipp.fakename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandRealName extends CommandBase implements ICommand {

	private final List names;

	public CommandRealName()
	{
		names = new ArrayList();
		names.add("realname");
		names.add("rn");
		names.add("rname");

	}

	@Override
	public int compareTo(ICommand o)
	{
		return this.getCommandName().compareTo(o.getCommandName());
	}

	@Override
	public String getCommandName()
	{
		return "realname";
	}


	@Override
	public String getCommandUsage(ICommandSender sender)
	{

		return "/realname <fakename>";
	}

	@Override
	public List<String> getCommandAliases()
	{
		return this.names;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if (args.length == 1)
			{
				//The Fake name that the player Entered
				String fakename = args[0];
				List<EntityPlayerMP> players = server.getPlayerList().getPlayerList();
				for (int i = 0; i < players.size(); i++)
				{
					if (players.get(i).getEntityData() != null && players.get(i).getEntityData().hasKey("fakename"))
					{
						//The player's actual fake name
						String fakeNamePlayer = players.get(i).getEntityData().getString("fakename");
						String toRemove;
						//Removes any Formatting Codes
						while (fakeNamePlayer.contains("\u00a7"))
						{
							toRemove = fakeNamePlayer.substring((fakeNamePlayer.indexOf("\u00a7")), fakeNamePlayer.indexOf("\u00a7") + 2);
							fakeNamePlayer = fakeNamePlayer.replace(toRemove, "");
						}
						//Removes any whitespaces;
						fakeNamePlayer = fakeNamePlayer.replace(" ", "");
						//Compares the two names
						if(fakeNamePlayer.toLowerCase().equals(fakename.toLowerCase()))
						{
							sender.addChatMessage(new TextComponentString(fakeNamePlayer + "'s real name is " + players.get(i).getGameProfile().getName()));
							return;
						}
					}

					if (i == players.size() - 1)
					{
						sender.addChatMessage(new TextComponentString(TextFormatting.RED + "There is no Player with the Fake Name '" + fakename + "'"));
					}
				}

			} else
			{
				throw new WrongUsageException(this.getCommandUsage(sender));
			}


		}
		else
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
	}
	
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
        return true;
	}
	
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{

		if (args.length > 0)
		{
			if (args.length == 1)
			{
				List<String> fakenames = new ArrayList<String>();
				List<EntityPlayerMP> players = server.getPlayerList().getPlayerList();
				for (int i = 0; i < players.size(); i++)
				{
					if (players.get(i).getEntityData() != null && players.get(i).getEntityData().hasKey("fakename"))
					{
						String fakename = TextFormatting.getTextWithoutFormattingCodes(players.get(i).getEntityData().getString("fakename"));
						fakename = fakename.replace(" ", "");
						fakenames.add(fakename);
					}

				}

				return fakenames;
			}

			else
			{
				return Collections.<String>emptyList();
			}

		}

		return Collections.<String>emptyList();

	}


}
