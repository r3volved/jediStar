package fr.jedistar;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.ImplDiscordAPI;
import de.btobastian.javacord.entities.impl.ImplUser;
import fr.jedistar.listener.JediStarBotMessageListener;
import fr.jedistar.listener.JediStarBotMultiReactionAddListener;
import fr.jedistar.listener.JediStarBotReactionAddListener;

public class JediStarBotCallback implements FutureCallback<DiscordAPI> {

	JediStarBotMessageListener messageListener;
	JediStarBotReactionAddListener reactionListener;
	JediStarBotMultiReactionAddListener multiReactionListener;

	public JediStarBotCallback() {
		super();

		messageListener = new JediStarBotMessageListener();
		reactionListener = new JediStarBotReactionAddListener();
		multiReactionListener = new JediStarBotMultiReactionAddListener();
	}
	
	public void onFailure(Throwable t) {
		t.printStackTrace();

	}

	public void onSuccess(DiscordAPI api) {
		
		api.registerListener(messageListener);
		api.registerListener(reactionListener);
		api.registerListener(multiReactionListener);

	}

}
