package tschipp.fakename;

import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface IProxy
{
    public void setup(final FMLCommonSetupEvent event);

    public Level getWorld();

    public List<String> getFakenames();

    public void refreshFakenames();
}
