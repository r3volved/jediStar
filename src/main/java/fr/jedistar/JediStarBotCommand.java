package fr.jedistar;

import java.util.List;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.formats.CommandAnswer;

public interface JediStarBotCommand {
	
	public String getCommand();

	public CommandAnswer answer(DiscordAPI api, List<String> params, Message receivedMessage, boolean isAdmin);
	
}
