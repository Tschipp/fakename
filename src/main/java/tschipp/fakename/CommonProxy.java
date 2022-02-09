package tschipp.fakename;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy implements IProxy {

    @Override
    public void setup(FMLCommonSetupEvent event)
    {

    }

    @Override
    public Level getWorld()
    {
        return null;
    }

    @Override
    public List<String> getFakenames()
    {
        return Collections.emptyList();
    }

    @Override
    public void refreshFakenames()
    {
    }

}
