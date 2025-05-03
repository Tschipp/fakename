package tschipp.fakename;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforgespi.language.IModInfo;
import tschipp.fakename.CommandFakeName.FakenameArgumentType;

@Mod(FakeName.MODID)
public class FakeName
{
	public static final String MODID = "fakename";

    public static final StreamCodec<RegistryFriendlyByteBuf, FakeNamePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FakeNamePacket::fakename,
            ByteBufCodecs.INT, FakeNamePacket::entityId,
            ByteBufCodecs.INT, FakeNamePacket::deleteFakename,
            FakeNamePacket::new
    );

    public static final FakeNamePacket.Type<FakeNamePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FakeName.MODID, "fakenamepacket"));

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MODID);
    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<FakenameArgumentType>> FAKENAME_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("fakename", () -> {
    	return ArgumentTypeInfos.registerByClass(FakenameArgumentType.class, SingletonArgumentInfo.contextFree(FakenameArgumentType::fakename));
    });


    
    public FakeName(ModContainer container)
    {
        COMMAND_ARGUMENT_TYPES.register(container.getEventBus());

        container.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        container.getEventBus().addListener(this::registerPackets);
    }

    public void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        registrar.playToClient(TYPE, CODEC, FakeNamePacket::handle);
    }


    public static void sendPacket(Player player, String fakename, int operation)
    {
        performFakenameOperation(player, fakename, operation);
        PacketDistributor.sendToAllPlayers(new FakeNamePacket(fakename, player.getId(), operation));
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
