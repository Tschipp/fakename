package tschipp.fakename;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class FakenameEvents
{

    @SubscribeEvent
    public static void serverLoad(RegisterCommandsEvent event)
    {
        CommandFakeName.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void renderName(PlayerEvent.NameFormat event)
    {
        CompoundTag tag = event.getEntity().getPersistentData();
        if (tag.contains("fakename"))
        {
            event.setDisplayname(Component.literal(tag.getString("fakename")));
        }
         else
         {
            event.setDisplayname(event.getUsername());
        }

    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getEntity();
        if (!player.getCommandSenderWorld().isClientSide)
        {
            if (player.getPersistentData().contains("fakename"))
                FakeName.sendPacket(player, player.getPersistentData().getString("fakename"), 0);

            for(Player other : player.getServer().getPlayerList().getPlayers())
            {
                if (other.getPersistentData().contains("fakename"))
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new FakeNamePacket(other.getPersistentData().getString("fakename"), other.getId(), 0));
            }
        }
    }

    @SubscribeEvent
    public static void onTracking(PlayerEvent.StartTracking event)
    {
        if (event.getTarget() instanceof Player)
        {
            Player targetPlayer = (Player) event.getTarget();
            if (targetPlayer.getPersistentData() != null && targetPlayer.getPersistentData().contains("fakename"))
            {
                ServerPlayer toRecieve = (ServerPlayer) event.getEntity();
                PacketDistributor.sendToPlayer((ServerPlayer) toRecieve, new FakeNamePacket(targetPlayer.getPersistentData().getString("fakename"), targetPlayer.getId(), 0));
            }
        }
    }

    // Makes Sure that the Data persists on Death
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event)
    {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        if (oldPlayer.getPersistentData().contains("fakename"))
        {
            String fakename = oldPlayer.getPersistentData().getString("fakename");
            newPlayer.getPersistentData().putString("fakename", fakename);
        }

    }

}
