package tschipp.fakename;


import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config
{
	public static final ServerConfig SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;

	static
	{
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static class ServerConfig
	{	
		public final IntValue commandPermissionLevel;
		
		public ServerConfig(ForgeConfigSpec.Builder builder)
		{
			commandPermissionLevel = builder
					.comment("Permission Level of the command")
					.defineInRange("commandPermissionLevel", 2, 0, 10);
		}
	}
}
