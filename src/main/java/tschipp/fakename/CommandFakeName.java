package tschipp.fakename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CommandFakeName extends CommandBase implements ICommand {

	private final List names;

	public CommandFakeName()
	{
		names = new ArrayList();
		names.add("fakename");
		names.add("fn");
	}

	@Override
	public int compareTo(ICommand o)
	{
        return this.getCommandName().compareTo(o.getCommandName());
	}

	@Override
	public String getCommandName()
	{
		return "fakename";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{

		return "/fakename <mode> <args...>";
	}

	public String getCommandUsageReal()
	{
		return "/fakename real <fakename> ";
	}

	public String getCommandUsageClear()
	{
		return "/fakename clear <player>";
	}

	public String getCommandUsageSet()
	{
		return "/fakename set <player> <fakename> OR /fakename set <fakename>";
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
		if (args[0].toLowerCase().equals("set"))
		{
			// Handling set <Playername> <Fakename>
			if (args.length == 3)
			{
				String playername = args[1];
				String fakename = args[2];
				EntityPlayerMP player = CommandBase.getPlayer(server, sender, playername);
				NBTTagCompound tag = player.getEntityData();
				fakename = fakename.replace("&", "§");
				tag.setString("fakename", fakename);
				if (sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityPlayerMP && !playername.equals(((EntityPlayer) sender).getGameProfile().getName()))
				{
					sender.addChatMessage(new TextComponentString(playername + "'s name is now " + fakename));
				}
				player.addChatMessage(new TextComponentString("Your name is now " + fakename));
				FakeName.network.sendToAll(new FakeNamePacket(fakename , player.getEntityId()));
				player.refreshDisplayName();
			}
			// Handling set <Fakename>
			else if (args.length == 2)
			{
				String fakename = args[1];
				EntityPlayerMP player = CommandBase.getPlayer(server, sender, sender.getName());
				NBTTagCompound tag = player.getEntityData();
				fakename = fakename.replace("&", "§");
				tag.setString("fakename", fakename);
				player.addChatMessage(new TextComponentString("Your name is now " + fakename));
				FakeName.network.sendToAll(new FakeNamePacket(fakename , player.getEntityId()));
				player.refreshDisplayName();
			}

			else
			{
				throw new WrongUsageException(this.getCommandUsageSet());
			}
			
			// Handling real <Fakename>
		} else if (args[0].toLowerCase().equals("real"))
		{
			if (args.length == 2)
			{
				String fakename = args[1];
				List<EntityPlayerMP> players = server.getPlayerList().getPlayerList();
				for (int i = 0; i < players.size(); i++)
				{
					if (players.get(i).getEntityData() != null && players.get(i).getEntityData().hasKey("fakename"))
					{
						String fakeNamePlayer = players.get(i).getEntityData().getString("fakename");
						String toRemove;
						while (fakeNamePlayer.contains("§"))
						{
							toRemove = fakeNamePlayer.substring((fakeNamePlayer.indexOf("§")), fakeNamePlayer.indexOf("§") + 2);
							fakeNamePlayer = fakeNamePlayer.replace(toRemove, "");
						}
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
				throw new WrongUsageException(this.getCommandUsageReal());
			}

		}
		//Handling clear <playername>
		else if (args[0].toLowerCase().equals("clear"))
		{
			if (args.length == 2)
			{
				String playername = args[1];
				EntityPlayerMP player = CommandBase.getPlayer(server, sender, playername);
				if(player.getEntityData() != null && player.getEntityData().hasKey("fakename"))
				{
					player.getEntityData().removeTag("fakename");
					sender.addChatMessage(new TextComponentString(playername+"'s Fake Name was removed"));
					player.refreshDisplayName();
				}
				else
				{
					sender.addChatMessage(new TextComponentString(TextFormatting.RED + "The provided Player does not have a Fake Name"));
				}
			}
			else
			{
				throw new WrongUsageException(this.getCommandUsageClear());
			}
			
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
				return CommandBase.getListOfStringsMatchingLastWord(args, "set", "real", "clear");
			}

			if (args.length == 2 && (args[0].equals("set") || args[0].equals("clear")))
			{
				return CommandBase.getListOfStringsMatchingLastWord(args, server.getAllUsernames());
			}

			else
			{
				return Collections.<String>emptyList();
			}

		}

		return Collections.<String>emptyList();

	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{

		return false;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

}
