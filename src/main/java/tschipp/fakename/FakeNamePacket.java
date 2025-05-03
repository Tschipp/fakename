package tschipp.fakename;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FakeNamePacket(String fakename, int entityId, int deleteFakename) implements CustomPacketPayload {


    public void handle(IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();

            Player toSync = (Player) mc.level.getEntity(entityId);

            if (toSync != null) {
                FakeName.performFakenameOperation(toSync, fakename, deleteFakename);

                if (deleteFakename == 0)
                    mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId()).setTabListDisplayName(Component.literal(fakename));
                else
                    mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId()).setTabListDisplayName(Component.literal(toSync.getGameProfile().getName()));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return FakeName.TYPE;
    }
}
