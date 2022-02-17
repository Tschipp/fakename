package tschipp.fakename;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        CompoundTag tag = event.getPlayer().getPersistentData();
        if (tag.contains("fakename"))
        {
            event.setDisplayname(new TextComponent(tag.getString("fakename")));
        }
         else
         {
            event.setDisplayname(event.getUsername());
        }

    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getPlayer();
        if (!player.level.isClientSide)
        {
            if (player.getPersistentData().contains("fakename"))
                FakeName.sendPacket(player, player.getPersistentData().getString("fakename"), 0);

            for(Player other : player.getServer().getPlayerList().getPlayers())
            {
                if (other.getPersistentData().contains("fakename"))
                    FakeName.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new FakeNamePacket(other.getPersistentData().getString("fakename"), other.getId(), 0));
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
                ServerPlayer toRecieve = (ServerPlayer) event.getPlayer();
                FakeName.network.send(PacketDistributor.PLAYER.with(() -> toRecieve), new FakeNamePacket(targetPlayer.getPersistentData().getString("fakename"), targetPlayer.getId(), 0));
            }
        }
    }

    // Makes Sure that the Data persists on Death
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event)
    {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getPlayer();

        if (oldPlayer.getPersistentData().contains("fakename"))
        {
            String fakename = oldPlayer.getPersistentData().getString("fakename");
            newPlayer.getPersistentData().putString("fakename", fakename);
        }

    }

}
