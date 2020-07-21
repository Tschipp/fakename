package tschipp.fakename;

import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy implements IProxy {

	@Override
	public void setup(FMLCommonSetupEvent event)
	{		
		
	}

	@Override
	public World getWorld()
	{
		return null;
	}

}
