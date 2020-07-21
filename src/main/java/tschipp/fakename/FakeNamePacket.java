package tschipp.fakename;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class FakeNamePacket
{

	public String fakename;
	public int entityId;
	public int deleteFakename;

	public FakeNamePacket(PacketBuffer buf)
	{
		this.fakename = buf.readString();
		this.entityId = buf.readInt();
		this.deleteFakename = buf.readInt();
	}

	public FakeNamePacket()
	{

	}

	public FakeNamePacket(String fakename, int entityID, int delete)
	{
		this.fakename = fakename;
		this.entityId = entityID;
		this.deleteFakename = delete;
	}

	public void toBytes(PacketBuffer buf)
	{
		buf.writeString(fakename);
		buf.writeInt(entityId);
		buf.writeInt(deleteFakename);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {

			PlayerEntity toSync = (PlayerEntity) FakeName.proxy.getWorld().getEntityByID(entityId);

			if (toSync != null)
			{
				ctx.get().setPacketHandled(true);
				
				CompoundNBT tag = toSync.getPersistentData();

				if(deleteFakename == 0)
				{
					tag.putString("fakename", fakename);
					Minecraft.getInstance().player.connection.getPlayerInfo(toSync.getGameProfile().getId()).setDisplayName(new StringTextComponent(fakename));
//					toSync.refreshDisplayName();
				}
				else
				{
					tag.remove("fakename");
					Minecraft.getInstance().player.connection.getPlayerInfo(toSync.getGameProfile().getId()).setDisplayName(new StringTextComponent(toSync.getGameProfile().getName()));
//					toSync.refreshDisplayName();
				}
				
			}

		});
	}

}
