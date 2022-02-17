package tschipp.fakename;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import tschipp.fakename.CommandFakeName.FakenameArgumentType;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(FakeName.MODID)
public class FakeName
{
	public static final String MODID = "fakename";

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
        event.enqueueWork(() -> {
        	 FakeName.network = NetworkRegistry.newSimpleChannel(new ResourceLocation(FakeName.MODID, "fakenamechannel"), () -> FakeName.info.getVersion().toString(), s -> true, s -> true);
             FakeName.network.registerMessage(0, FakeNamePacket.class, FakeNamePacket::toBytes, FakeNamePacket::new, FakeNamePacket::handle);
        
             ArgumentTypes.register(MODID + ":" + "fakename", FakenameArgumentType.class, new FakenameArgumentType.Serializer());
        });
       
    }

    public static void sendPacket(Player player, String fakename, int operation)
    {
        performFakenameOperation(player, fakename, operation);
        FakeName.network.send(PacketDistributor.ALL.noArg(), new FakeNamePacket(fakename, player.getId(), operation));
    }

    public static void performFakenameOperation(Player player, String fakename, int operation)
    {
        CompoundTag tag = player.getPersistentData();

        if (operation == 0)
        {
            tag.putString("fakename", fakename);
            player.refreshDisplayName();
        }
         else
         {
            tag.remove("fakename");
            player.refreshDisplayName();
        }
    }



}
