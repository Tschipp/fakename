package tschipp.fakename;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements IProxy {

	@Override
	public void setup(FMLCommonSetupEvent event)
	{
		
	}

	@Override
	public World getWorld()
	{
		return Minecraft.getInstance().world;
	}

}


