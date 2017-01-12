package tschipp.fakename;

import java.util.Iterator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = "fakename", name = "Fake Name", version = "1.2")

public class FakeName {

	@Instance(value = "fakename")
	public static FakeName instance;

	public static SimpleNetworkWrapper network;
	
	public static final String CLIENT_PROXY = "tschipp.fakename.ClientProxy";
	public static final String COMMON_PROXY = "tschipp.fakename.CommonProxy";

	
	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);

		network = NetworkRegistry.INSTANCE.newSimpleChannel("FakeNameChannel");
		network.registerMessage(FakeNamePacketHandler.class, FakeNamePacket.class, 0, Side.CLIENT);

	}



	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		MinecraftForge.EVENT_BUS.register(this);	
		FMLCommonHandler.instance().bus().register(this);
	}



	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);


	}



	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandFakeName());
		event.registerServerCommand(new CommandRealName());

	}



	/*@SubscribeEvent
	public void onChat(ServerChatEvent event) {
		EntityPlayerMP player = event.getPlayer();
		NBTTagCompound tag = player.getEntityData();
		String username = event.getUsername();
		String message = event.getMessage();
		String component = event.getComponent().getFormattedText();
		if(tag.hasKey("fakename") && !tag.hasKey("oldfakename")) {
			event.setComponent(new TextComponentString(event.getComponent().getUnformattedText().replace(username, tag.getString("fakename"))));
		}
		if(tag.hasKey("fakename") && tag.hasKey("oldfakename")) {
			event.setComponent(new TextComponentString(event.getComponent().getUnformattedText().replace(tag.getString("oldfakename"), tag.getString("fakename"))));
		}
		System.out.println(tag);

	} */

	@SubscribeEvent
	public void renderName(PlayerEvent.NameFormat event) {
		NBTTagCompound tag = event.getEntityPlayer().getEntityData();
		if(tag.hasKey("fakename")) {
			event.setDisplayname(tag.getString("fakename"));
		}
		else 
		{
			event.setDisplayname(event.getUsername());
		}


	}  


	@SubscribeEvent
	public void onJoinWorld(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) 
	{
		EntityPlayer player = event.player;
		if(!player.worldObj.isRemote)
		{
			WorldServer serverWorld = (WorldServer) event.player.worldObj;
			Iterator<? extends EntityPlayer> playersTracking = serverWorld.getEntityTracker().getTrackingPlayers(player).iterator();
			if(player.getEntityData() != null && player.getEntityData().hasKey("fakename"))
			{
				while(playersTracking.hasNext())
				{
					FakeName.network.sendTo(new FakeNamePacket(player.getEntityData().getString("fakename") , player.getEntityId(), 0), (EntityPlayerMP) playersTracking.next());
				}
			}
		}
	}  

	@SubscribeEvent
	public void onTracking(PlayerEvent.StartTracking event)
	{
		if(event.getTarget() instanceof EntityPlayer)
		{
			EntityPlayer targetPlayer = (EntityPlayer) event.getTarget();
			System.out.println("The Targeted Player is " + targetPlayer);
			if(targetPlayer.getEntityData() != null && targetPlayer.getEntityData().hasKey("fakename"))
			{
				EntityPlayerMP toRecieve = (EntityPlayerMP) event.getEntityPlayer();
				System.out.println("The Recieving Player is " + toRecieve);

				FakeName.network.sendTo(new FakeNamePacket(targetPlayer.getEntityData().getString("fakename") , targetPlayer.getEntityId(), 0), toRecieve);
			}
		}
	} 



	//Makes Sure that the Data persists on Death
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) 
	{
		EntityPlayer oldPlayer = event.getOriginal();
		EntityPlayer newPlayer = event.getEntityPlayer();

		if(oldPlayer.getEntityData().hasKey("fakename")) 
		{
			String fakename = oldPlayer.getEntityData().getString("fakename");
			newPlayer.getEntityData().setString("fakename", fakename);
		}

	}


}
