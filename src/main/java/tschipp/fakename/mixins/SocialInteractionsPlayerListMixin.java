package tschipp.fakename.mixins;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

@Mixin(SocialInteractionsPlayerList.class)
public class SocialInteractionsPlayerListMixin {

	@Shadow
	private List<PlayerEntry> players;
	
	@Shadow
	private SocialInteractionsScreen socialInteractionsScreen;

	
	@Inject(method = "updateFiltersAndScroll(Ljava/util/Collection;D)V", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screens/social/SocialInteractionsPlayerList.updateFilteredPlayers()V"))
	private void onUpdatePlayerList(Collection<UUID> uuids, double d, CallbackInfo callback) 
	{
		for(int i = 0; i < players.size(); i++)
		{
			PlayerEntry en = players.get(i);
	        @SuppressWarnings("resource")
			PlayerInfo playerinfo = Minecraft.getInstance().player.connection.getPlayerInfo(en.getPlayerId());
			if(playerinfo != null)
			{
				Component dispName = playerinfo.getTabListDisplayName();
				if(dispName != null && !en.getPlayerName().equals(dispName.getString()))
				{
					players.set(i, new PlayerEntry(Minecraft.getInstance(), this.socialInteractionsScreen, playerinfo.getProfile().getId(), dispName.getString(), playerinfo::getSkinLocation, true));
				}
			}
		}

	}

}
