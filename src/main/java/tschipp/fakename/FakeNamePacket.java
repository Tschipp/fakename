package tschipp.fakename;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class FakeNamePacket implements IMessage {
	
	public String fakename;
	public int entityId;
	public int deleteFakename;
	
	public FakeNamePacket() {
		
	}
	
	public FakeNamePacket(String fakename, int entityID, int delete) {
		this.fakename = fakename;
		this.entityId = entityID;
		this.deleteFakename = delete;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.fakename = ByteBufUtils.readUTF8String(buf);
		this.entityId = ByteBufUtils.readVarInt(buf, 4);
		this.deleteFakename = ByteBufUtils.readVarInt(buf, 4);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.fakename);
		ByteBufUtils.writeVarInt(buf, this.entityId, 4);
		ByteBufUtils.writeVarInt(buf, this.deleteFakename, 4);
	}

}
