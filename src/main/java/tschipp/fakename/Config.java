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
		public final IntValue commandPermissionLevelSelf;
		
		public final IntValue commandPermissionLevelAll;
		
		public ServerConfig(ForgeConfigSpec.Builder builder)
		{
			builder.push("settings");
			commandPermissionLevelAll = builder
					.comment("Permission Level of the command. This is the level needed to be able to change other people's fakename")
					.defineInRange("commandPermissionLevelAll", 2, 0, 10);
			
			commandPermissionLevelSelf = builder
					.comment("Permission Level of the command. This is the level needed to be able to change your own fakename")
					.defineInRange("commandPermissionLevelSelf", 0, 0, 10);
			builder.pop();
		}
	}
}
