package tschipp.fakename;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FakeNamePacketHandler implements IMessageHandler<FakeNamePacket, IMessage>{

	@Override
	public IMessage onMessage(final FakeNamePacket message, final MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();


		mainThread.addScheduledTask(new Runnable() {

			@Override
			public void run() {

				EntityPlayer toSync = (EntityPlayer)FakeName.proxy.getClientWorld().getEntityByID(message.entityId);

				if(toSync != null)
				{
					if(message.deleteFakename == 0)
					{
						NBTTagCompound tag = toSync.getEntityData();
						tag.setString("fakename", message.fakename);
						toSync.refreshDisplayName();
					}
					else
					{
						NBTTagCompound tag = toSync.getEntityData();
						tag.removeTag("fakename");
						toSync.refreshDisplayName();

					}

				}

			}});

		return null;
	}



}


