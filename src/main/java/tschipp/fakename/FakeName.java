package tschipp.fakename;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import tschipp.fakename.FakeNameSuggestionPackets.GetFakeNamePacket;
import tschipp.fakename.FakeNameSuggestionPackets.UpdateFakeNamePacket;

@Mod(FakeName.MODID)
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
        FakeName.network.registerMessage(1, GetFakeNamePacket.class, GetFakeNamePacket::toBytes, GetFakeNamePacket::new, GetFakeNamePacket::handle);
        FakeName.network.registerMessage(2, UpdateFakeNamePacket.class, UpdateFakeNamePacket::toBytes, UpdateFakeNamePacket::new, UpdateFakeNamePacket::handle);

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
