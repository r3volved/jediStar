package fr.jedistar;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import fr.jedistar.utils.TerritoryBattleAssistant;

public class JediStarBot {

	String token;
	DiscordAPI api = Javacord.getApi(token, true);
	JediStarBotCallback botCallback;
	TerritoryBattleAssistant tba;

	public JediStarBot(String inToken) {

		token = inToken;

		api = Javacord.getApi(token, true);
		
		botCallback = new JediStarBotCallback();

		tba = new TerritoryBattleAssistant( "TBAssistant" );
		
	}

	public void connect() {
		
		api.connect(botCallback);

	    tba.start(api);
	    
	}	

}