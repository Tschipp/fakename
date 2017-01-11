package tschipp.fakename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
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
		names.add("fname");
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
		return "/fakename clear [player]";
	}

	public String getCommandUsageSet()
	{
		return "/fakename set [player] <fakename>";
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
					List<Entity> player = CommandBase.getEntityList(server, sender, playername);
					for(int i = 0; i < player.size(); i++)
					{
						if(player.get(i) instanceof EntityPlayerMP)
						{
							NBTTagCompound tag = player.get(i).getEntityData();
							fakename = fakename.replace("&", "§");
							fakename = fakename.replace("/-", " ");
							tag.setString("fakename", fakename);
							if (sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityPlayerMP && !playername.equals(((EntityPlayer) sender).getGameProfile().getName()))
							{
								sender.addChatMessage(new TextComponentString(playername + "'s name is now " + fakename));
							}
							player.get(i).addChatMessage(new TextComponentString("Your name is now " + fakename));
							FakeName.network.sendToAll(new FakeNamePacket(fakename , player.get(i).getEntityId(), 0));
							((EntityPlayer) player.get(i)).refreshDisplayName();
						}
						else
						{
							sender.addChatMessage(new TextComponentString(TextFormatting.RED + "You can not select non-player entities"));
						}
					}
				}
				// Handling set <Fakename>
				else if (args.length == 2)
				{
					String fakename = args[1];
					EntityPlayerMP player = CommandBase.getPlayer(server, sender, sender.getName());
					NBTTagCompound tag = player.getEntityData();
					fakename = fakename.replace("&", "§");
					fakename = fakename.replace("/-", " ");
					tag.setString("fakename", fakename);
					player.addChatMessage(new TextComponentString("Your name is now " + fakename));
					FakeName.network.sendToAll(new FakeNamePacket(fakename , player.getEntityId(), 0));
					player.refreshDisplayName();
				}

				else
				{
					throw new WrongUsageException(this.getCommandUsageSet());
				}

				// Handling real <Fakename>
			} 
			//Handling clear <playername>
			else if (args[0].toLowerCase().equals("clear"))
			{
				if (args.length == 2)
				{
					String playername = args[1];
					List<Entity> player = CommandBase.getEntityList(server, sender, playername);
					for(int i = 0; i < player.size(); i++)
					{
						if(player.get(i) instanceof EntityPlayerMP)
						{
							if(player.get(i).getEntityData() != null && player.get(i).getEntityData().hasKey("fakename"))
							{
								player.get(i).getEntityData().removeTag("fakename");
								if (sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityPlayerMP && !playername.equals(((EntityPlayer) sender).getGameProfile().getName()))
								{
									sender.addChatMessage(new TextComponentString(playername+"'s Fake Name was removed"));
								}
								player.get(i).addChatMessage(new TextComponentString("Your Fake Name was removed"));
								FakeName.network.sendToAll(new FakeNamePacket("something" , player.get(i).getEntityId(), 1));
								((EntityPlayer) player.get(i)).refreshDisplayName();
							}
							else
							{
								sender.addChatMessage(new TextComponentString(TextFormatting.RED + "The provided Player does not have a Fake Name"));
							}
						}
						else
						{
							sender.addChatMessage(new TextComponentString(TextFormatting.RED + "You can not select Entities"));
						}
					}
				}
				else if (args.length == 1)
				{
					EntityPlayerMP player = (EntityPlayerMP)sender;
					if(player.getEntityData() != null && player.getEntityData().hasKey("fakename"))
					{
						player.getEntityData().removeTag("fakename");
						sender.addChatMessage(new TextComponentString("Your Fake Name was removed"));
						FakeName.network.sendToAll(new FakeNamePacket("something" , player.getEntityId(), 1));
						player.refreshDisplayName();
					}
					else
					{
						sender.addChatMessage(new TextComponentString(TextFormatting.RED + "You do not have a Fake Name"));
					}

				}
				else
				{
					throw new WrongUsageException(this.getCommandUsageClear());
				}

			}
			else
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
		return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{

		if (args.length > 0)
		{
			if (args.length == 1)
			{
				return CommandBase.getListOfStringsMatchingLastWord(args, "set", "clear");
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
		return 2;
	}

}
