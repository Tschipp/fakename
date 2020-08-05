package tschipp.fakename;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class FakeNameSuggestionPackets
{

	public static class GetFakeNamePacket
	{

		public String realname;

		public GetFakeNamePacket(PacketBuffer buf)
		{
			this.realname = buf.readString();
		}

		public GetFakeNamePacket()
		{

		}

		public GetFakeNamePacket(String realname)
		{
			this.realname = realname;
		}

		public void toBytes(PacketBuffer buf)
		{
			buf.writeString(realname);
		}

		public void handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity sender = ctx.get().getSender();
				World world = sender.world;

				PlayerEntity target = world.getServer().getPlayerList().getPlayerByUsername(this.realname);
				if (target != null)
				{
					CompoundNBT nbt = target.getPersistentData();
					if (nbt != null && nbt.contains("fakename"))
					{
						String fakename = TextFormatting.getTextWithoutFormattingCodes(nbt.getString("fakename"));
						FakeName.network.send(PacketDistributor.PLAYER.with(() -> sender), new UpdateFakeNamePacket(fakename));
					}
				}
			});
		}
	}
	
	public static class UpdateFakeNamePacket
	{

		public String fakename;

		public UpdateFakeNamePacket(PacketBuffer buf)
		{
			this.fakename = buf.readString();
		}

		public UpdateFakeNamePacket()
		{

		}

		public UpdateFakeNamePacket(String fakename)
		{
			this.fakename = fakename;
		}

		public void toBytes(PacketBuffer buf)
		{
			buf.writeString(fakename);
		}

		public void handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {

				FakeName.proxy.getFakenames().add(this.fakename);
			
			});
		}
	}
}
