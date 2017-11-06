package fr.jedistar.listener;

import java.util.ArrayList;
import java.util.List;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Reaction;
import de.btobastian.javacord.listener.message.ReactionAddListener;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.formats.PendingMultiAction;

public class JediStarBotMultiReactionAddListener implements ReactionAddListener {

	private static List<PendingMultiAction> pendingActions = new ArrayList<PendingMultiAction>();
	
	@Override
	public void onReactionAdd(DiscordAPI api, Reaction reaction, User user) {
		
		for(PendingMultiAction action:pendingActions) {
			
			if( !user.isBot() ) {
				
				if(action.isExpired()) {
					pendingActions.remove(action);
					return;
				}
										
				action.doAction(user, reaction);
				
			}
		}
	}

	public static void addPendingMultiAction(PendingMultiAction action) {
		pendingActions.add(action);
	}
}
