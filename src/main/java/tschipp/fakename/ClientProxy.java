package tschipp.fakename;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements IProxy {

	private List<String> fakenames = new ArrayList<String>();
	
	@Override
	public void setup(FMLCommonSetupEvent event)
	{
		
	}

	@Override
	public World getWorld()
	{
		return Minecraft.getInstance().world;
	}

	@Override
	public List<String> getFakenames()
	{
		return fakenames;
	}

	@Override
	public void refreshFakenames()
	{
		
	}

}


