package tschipp.fakename;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FakenameEvents
{

	@SubscribeEvent
	public static void serverLoad(FMLServerStartingEvent event)
	{
		CommandFakeName.register(event.getCommandDispatcher());
	}

	@SubscribeEvent
	public static void renderName(PlayerEvent.NameFormat event)
	{
		CompoundNBT tag = event.getPlayer().getPersistentData();
		if (tag.contains("fakename"))
		{
			event.setDisplayname(tag.getString("fakename"));
		}
		else
		{
			event.setDisplayname(event.getUsername());
		}

	}

	@SubscribeEvent
	public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
	{
		PlayerEntity player = event.getPlayer();
		if (!player.world.isRemote)
		{
			if (player.getPersistentData().contains("fakename"))
				FakeName.sendPacket(player, player.getPersistentData().getString("fakename"), 0);
			
			for(PlayerEntity other : player.getServer().getPlayerList().getPlayers())
			{
				if (other.getPersistentData().contains("fakename"))
					FakeName.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new FakeNamePacket(other.getPersistentData().getString("fakename"), other.getEntityId(), 0));
			}
		}
	}

	@SubscribeEvent
	public static void onTracking(PlayerEvent.StartTracking event)
	{
		if (event.getTarget() instanceof PlayerEntity)
		{
			PlayerEntity targetPlayer = (PlayerEntity) event.getTarget();
			if (targetPlayer.getPersistentData() != null && targetPlayer.getPersistentData().contains("fakename"))
			{
				ServerPlayerEntity toRecieve = (ServerPlayerEntity) event.getPlayer();
				FakeName.network.send(PacketDistributor.PLAYER.with(() -> toRecieve), new FakeNamePacket(targetPlayer.getPersistentData().getString("fakename"), targetPlayer.getEntityId(), 0));
			}
		}
	}

	// Makes Sure that the Data persists on Death
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		PlayerEntity oldPlayer = event.getOriginal();
		PlayerEntity newPlayer = event.getPlayer();

		if (oldPlayer.getPersistentData().contains("fakename"))
		{
			String fakename = oldPlayer.getPersistentData().getString("fakename");
			newPlayer.getPersistentData().putString("fakename", fakename);
		}

	}

}
