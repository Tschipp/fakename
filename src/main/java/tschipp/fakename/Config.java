package tschipp.fakename;


import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;


public class Config
{
	public static final ServerConfig SERVER;
	public static final ModConfigSpec SERVER_SPEC;

	static
	{
		final Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static class ServerConfig
	{	
		public final ModConfigSpec.IntValue commandPermissionLevelSelf;
		
		public final ModConfigSpec.IntValue commandPermissionLevelAll;
		
		public ServerConfig(ModConfigSpec.Builder builder)
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
