package tschipp.fakename;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;

public class FakeNameSuggestionPackets
{

    public static class GetFakeNamePacket
    {

        public String realname;

        public GetFakeNamePacket(FriendlyByteBuf buf)
        {
            this.realname = buf.readUtf();
        }

        public GetFakeNamePacket()
        {

        }

        public GetFakeNamePacket(String realname)
        {
            this.realname = realname;
        }

        public void toBytes(FriendlyByteBuf buf)
        {
            buf.writeUtf(realname);
        }

        public void handle(Supplier<Context> ctx)
        {
            ctx.get().enqueueWork(() -> {

                ServerPlayer sender = ctx.get().getSender();
                Level world = sender.level;

                Player target = world.getServer().getPlayerList().getPlayerByName(this.realname);
                if (target != null)
                {
                    CompoundTag nbt = target.getPersistentData();
                    if (nbt != null && nbt.contains("fakename"))
                    {
                        String fakename = nbt.getString("fakename");
                        FakeName.network.send(PacketDistributor.PLAYER.with(() -> sender), new UpdateFakeNamePacket(fakename));
                    }
                }
            });
        }
    }

    public static class UpdateFakeNamePacket
    {

        public String fakename;

        public UpdateFakeNamePacket(FriendlyByteBuf buf)
        {
            this.fakename = buf.readUtf();
        }

        public UpdateFakeNamePacket()
        {

        }

        public UpdateFakeNamePacket(String fakename)
        {
            this.fakename = fakename;
        }

        public void toBytes(FriendlyByteBuf buf)
        {
            buf.writeUtf(fakename);
        }

        public void handle(Supplier<Context> ctx)
        {
            ctx.get().enqueueWork(() -> {

                FakeName.proxy.getFakenames().add(this.fakename);

            });
        }
    }
}
