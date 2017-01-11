package tschipp.fakename;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	
public void preInit(FMLPreInitializationEvent event) {
		
		super.preInit(event);
		
		
			
		}
		
		public void init(FMLInitializationEvent event) {
			
			super.init(event);
		
		}

		public void postInit(FMLPostInitializationEvent event) {
			
			super.postInit(event);
		
		}
		
		
		@Override
		public World getClientWorld() 
		{
			return Minecraft.getMinecraft().theWorld;
		}

}


