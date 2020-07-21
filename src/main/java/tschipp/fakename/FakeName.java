package tschipp.fakename;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.IModInfo;

@Mod(FakeName.MODID)
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FakeName
{
	public static final String MODID = "fakename";

	public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	public static SimpleChannel network;

	public static IModInfo info;
	
	public FakeName()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
		
		info = ModLoadingContext.get().getActiveContainer().getModInfo();
	}
	
	private void setup(final FMLCommonSetupEvent event)
	{
		proxy.setup(event);
		
		FakeName.network = NetworkRegistry.newSimpleChannel(new ResourceLocation(FakeName.MODID, "fakenamechannel"), () -> FakeName.info.getVersion().toString(), s -> true, s -> true);
		FakeName.network.registerMessage(0, FakeNamePacket.class, FakeNamePacket::toBytes, FakeNamePacket::new, FakeNamePacket::handle);

	}
	
	
}
