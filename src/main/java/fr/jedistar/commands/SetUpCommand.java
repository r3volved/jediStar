/**
 * 
 */
package fr.jedistar.commands;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.impl.ImplReaction;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.classes.Channel;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.listener.JediStarBotReactionAddListener;

/**
 * @author Jerem
 *
 */
public class SetUpCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(SetUpCommand.class);

	private final String COMMAND;
	private final String COMMAND_GUILD_NUMBER;
	private final String COMMAND_TBASSISTANT;
	private final String COMMAND_WEBHOOK;
	private final String COMMAND_ALERT_ROLE;
	private final String COMMAND_BOOLEAN_TRUE;
	private final String COMMAND_BOOLEAN_FALSE;
	
	private final List<String> COMMANDS = new ArrayList<String>();
	
	private final String CONFIRM_UPDATE_CHANNEL;
	private final String WARN_UPDATE_GUILD;
	private final String WARN_UPDATE_TBASSISTANT;
	private final String WARN_UPDATE_WEBHOOK;
	private final String WARN_UPDATE_ALERT_ROLE;
	private final String SETUP_CHANNEL_OK;	
	private final String CANCEL_MESSAGE;
	
	private final String ERROR_MESSAGE;
	private final String HELP;
	private final String FORBIDDEN;
	private final String PARAMS_NUMBER;
	private final String NUMBER_PROBLEM;
	private final String URL_PROBLEM;
	private final String BOOLEAN_PROBLEM;
	private final String ERROR_NO_CHAN;
	private final String ERROR_NO_GUILD;
	private final String ERROR_NO_WEBHOOK;
	private final String SQL_ERROR;	
	private final String NO_COMMAND_FOUND;
	
	//Nom des champs JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private static final String JSON_SETUP = "setUpCommandParameters";
	
	private static final String JSON_SETUP_HELP = "help";
	
	private static final String JSON_SETUP_COMMANDS = "commands";
	private static final String JSON_SETUP_COMMANDS_BASE = "base";
	private static final String JSON_SETUP_COMMANDS_GUILD_NUMBER = "guildNumber";
	private static final String JSON_SETUP_COMMANDS_TBASSISTANT = "tbAssistant";
	private static final String JSON_SETUP_COMMANDS_WEBHOOK = "webhook";
	private static final String JSON_SETUP_COMMANDS_ALERT_ROLE = "alertRole";
	private static final String JSON_SETUP_COMMANDS_BOOLEAN_TRUE = "toggleON";
	private static final String JSON_SETUP_COMMANDS_BOOLEAN_FALSE = "toggleOFF";
	
	
	private static final String JSON_SETUP_MESSAGES = "messages";
	private static final String JSON_SETUP_MESSAGES_CONFIRM_UPDATE_CHANNEL = "confirmUpdateChannel";
	private static final String JSON_SETUP_MESSAGES_WARN_UPDATE_GUILD = "warnUpdateGuild";
	private static final String JSON_SETUP_MESSAGES_WARN_UPDATE_WEBHOOK = "warnUpdateWebhook";
	private static final String JSON_SETUP_MESSAGES_WARN_UPDATE_TBASSISTANT = "warnUpdateTBAssistant";
	private static final String JSON_SETUP_MESSAGES_WARN_UPDATE_ALERT_ROLE = "warnUpdateAlertRole";
	private static final String JSON_SETUP_MESSAGES_CHANNEL_SETUP_OK = "channelSetupOK";
	private static final String JSON_SETUP_MESSAGES_CANCEL = "cancelAction";
	
	private static final String JSON_SETUP_ERROR_MESSAGES = "errorMessages";
	private static final String JSON_SETUP_ERROR_MESSAGES_FORBIDDEN = "forbidden";
	private static final String JSON_SETUP_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNummber";
	private static final String JSON_SETUP_ERROR_MESSAGES_INCORRECT_NUMBER = "incorrectNumber";
	private static final String JSON_SETUP_ERROR_MESSAGES_INCORRECT_URL = "incorrectURL";
	private static final String JSON_SETUP_ERROR_MESSAGES_INCORRECT_BOOLEAN = "incorrectBoolean";
	private static final String JSON_SETUP_ERROR_NO_CHAN = "noChannel";
	private static final String JSON_SETUP_ERROR_NO_GUILD = "noGuild";
	private static final String JSON_SETUP_ERROR_NO_WEBHOOK = "noWebhook";
	private static final String JSON_SETUP_ERROR_SQL = "sqlError";
	private static final String JSON_SETUP_ERROR_NO_COMMAND = "noCommandFound";


	public SetUpCommand() {
		//Lecture du JSON
		JSONObject params = StaticVars.jsonSettings;

		ERROR_MESSAGE = params.getString(JSON_ERROR_MESSAGE);

		JSONObject setupParams = params.getJSONObject(JSON_SETUP);

		HELP = setupParams.getString(JSON_SETUP_HELP);

		JSONObject commands = setupParams.getJSONObject(JSON_SETUP_COMMANDS);		
		COMMAND = commands.getString(JSON_SETUP_COMMANDS_BASE);
		COMMAND_GUILD_NUMBER = commands.getString(JSON_SETUP_COMMANDS_GUILD_NUMBER);
		COMMAND_TBASSISTANT = commands.getString(JSON_SETUP_COMMANDS_TBASSISTANT);
		COMMAND_WEBHOOK = commands.getString(JSON_SETUP_COMMANDS_WEBHOOK);
		COMMAND_ALERT_ROLE = commands.getString(JSON_SETUP_COMMANDS_ALERT_ROLE);
		COMMAND_BOOLEAN_TRUE = commands.getString(JSON_SETUP_COMMANDS_BOOLEAN_TRUE);
		COMMAND_BOOLEAN_FALSE = commands.getString(JSON_SETUP_COMMANDS_BOOLEAN_FALSE);
		
		COMMANDS.add( COMMAND_GUILD_NUMBER.toLowerCase() );
		COMMANDS.add( COMMAND_TBASSISTANT.toLowerCase() );
		COMMANDS.add( COMMAND_WEBHOOK.toLowerCase() );
		COMMANDS.add( COMMAND_ALERT_ROLE.toLowerCase() );
		COMMANDS.add( HELP.toLowerCase() );
		
		JSONObject messages = setupParams.getJSONObject(JSON_SETUP_MESSAGES);
		CONFIRM_UPDATE_CHANNEL = messages.getString(JSON_SETUP_MESSAGES_CONFIRM_UPDATE_CHANNEL);
		WARN_UPDATE_GUILD = messages.getString(JSON_SETUP_MESSAGES_WARN_UPDATE_GUILD);
		WARN_UPDATE_TBASSISTANT = messages.getString(JSON_SETUP_MESSAGES_WARN_UPDATE_TBASSISTANT);
		WARN_UPDATE_WEBHOOK = messages.getString(JSON_SETUP_MESSAGES_WARN_UPDATE_WEBHOOK);
		WARN_UPDATE_ALERT_ROLE = messages.getString(JSON_SETUP_MESSAGES_WARN_UPDATE_ALERT_ROLE);
		SETUP_CHANNEL_OK = messages.getString(JSON_SETUP_MESSAGES_CHANNEL_SETUP_OK);
		CANCEL_MESSAGE = messages.getString(JSON_SETUP_MESSAGES_CANCEL);
		
		JSONObject errorMessages = setupParams.getJSONObject(JSON_SETUP_ERROR_MESSAGES);
		FORBIDDEN = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_FORBIDDEN);
		PARAMS_NUMBER = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_PARAMS_NUMBER);
		NUMBER_PROBLEM = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_INCORRECT_NUMBER);
		URL_PROBLEM = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_INCORRECT_URL);
		BOOLEAN_PROBLEM = errorMessages.getString(JSON_SETUP_ERROR_MESSAGES_INCORRECT_BOOLEAN);
		ERROR_NO_CHAN = errorMessages.getString(JSON_SETUP_ERROR_NO_CHAN);
		ERROR_NO_GUILD = errorMessages.getString(JSON_SETUP_ERROR_NO_GUILD);
		ERROR_NO_WEBHOOK = errorMessages.getString(JSON_SETUP_ERROR_NO_WEBHOOK);
		SQL_ERROR = errorMessages.getString(JSON_SETUP_ERROR_SQL);
		NO_COMMAND_FOUND = errorMessages.getString(JSON_SETUP_ERROR_NO_COMMAND);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}


	@Override
	public CommandAnswer answer(DiscordAPI api, List<String> params, Message receivedMessage, boolean isAdmin) {

		//Kick out if not admin
		if(!isAdmin) {
			return new CommandAnswer(FORBIDDEN, null);
		}
		
		//Kick out if not enough params
		if(params.size() < 2  || params.size() % 2 > 0) {
			return new CommandAnswer(PARAMS_NUMBER,null);
		}
		
		//alert on help
		if( params.get(0).toLowerCase().equalsIgnoreCase("help") ) {
			return new CommandAnswer(HELP, null);
		}		

		//Kick out if DM
		if(receivedMessage.getChannelReceiver() == null) {
			return new CommandAnswer(ERROR_NO_CHAN, null);
		}

		Channel channel = new Channel(receivedMessage.getChannelReceiver().getId());
		boolean UPDATE_FLAG = false;
		String MESSAGE_FLAG = "";
		
		for( Integer p = 0; p != params.size(); ++p ) {
		
			String cmdParam = params.get(p).toLowerCase();
			String cmdVal = params.get( ++p );
			
			if( !COMMANDS.contains(cmdParam) ) {
				return new CommandAnswer(error(NO_COMMAND_FOUND),null);	
			}
			
			if( COMMAND_GUILD_NUMBER.equalsIgnoreCase(cmdParam) ) {
				
				try {
					Integer guildID = Integer.parseInt(cmdVal);
					if( channel.guildID != guildID ) {

						MESSAGE_FLAG += channel.guildID != null ? String.format(WARN_UPDATE_GUILD,channel.guildID) : "";
						UPDATE_FLAG = channel.guildID != null ? true : UPDATE_FLAG;						

						channel.guildID = guildID;
						
					}
				}
				catch(NumberFormatException e) {
					logger.warn(e.getMessage());
					e.printStackTrace();
					return new CommandAnswer(error(NUMBER_PROBLEM), null);
				}
			}
			
			if( COMMAND_TBASSISTANT.equalsIgnoreCase(cmdParam) ) {
				
				if( !cmdVal.equalsIgnoreCase(COMMAND_BOOLEAN_TRUE) && !cmdVal.equalsIgnoreCase(COMMAND_BOOLEAN_FALSE) ) {
					return new CommandAnswer(String.format(BOOLEAN_PROBLEM, COMMAND_BOOLEAN_TRUE, COMMAND_BOOLEAN_FALSE), null);
				}
				
				 
				boolean newTBA = cmdVal.equalsIgnoreCase(COMMAND_BOOLEAN_TRUE) ? true : false;
				if( channel.tbAssistant != newTBA ) {
					
					MESSAGE_FLAG += WARN_UPDATE_TBASSISTANT;
					UPDATE_FLAG = true;

					channel.tbAssistant = newTBA;

				}
				
			}
	
			if( COMMAND_WEBHOOK.equalsIgnoreCase(cmdParam) ) {
		
				String newWH = cmdVal.equalsIgnoreCase("null") ? null : cmdVal;
				if( channel.webhook != newWH ) {
					
					MESSAGE_FLAG += channel.webhook != null ? String.format(WARN_UPDATE_WEBHOOK) : "";
					UPDATE_FLAG = channel.webhook != null ? true : UPDATE_FLAG;

					channel.webhook = newWH;

				}
			}
			
			if( COMMAND_ALERT_ROLE.equalsIgnoreCase(cmdParam) ) {
				
				String newRole = cmdVal.equalsIgnoreCase("null") || cmdVal.length() == 0 ? null : cmdVal;
				if( channel.alertRole != newRole ) {
					
					MESSAGE_FLAG += channel.alertRole != null ? String.format(WARN_UPDATE_ALERT_ROLE) : "";
					UPDATE_FLAG = channel.alertRole != null ? true : UPDATE_FLAG;

					channel.alertRole = newRole;

				}
			}
		}

		if( channel.saved && UPDATE_FLAG ) {
			
			MESSAGE_FLAG += CONFIRM_UPDATE_CHANNEL;
			
			//ALERT UPDATE CONFIRMATION
			JediStarBotReactionAddListener.addPendingAction(new PendingAction(receivedMessage.getAuthor(),"executeUpdate",this,receivedMessage,1,channel));
			String emojiX = EmojiManager.getForAlias("x").getUnicode();
			String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();
	
			return new CommandAnswer(String.format(MESSAGE_FLAG),null,emojiV,emojiX);						

		}
		
		return new CommandAnswer(executeInsert(channel),null);
		
	}

	/**
	 * Inserts the channel
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public String executeInsert(Channel channel) {

		if( channel.guildID == 0 ) {
			return ERROR_NO_GUILD;
		}
		if( channel.tbAssistant && channel.webhook == null ) {
			return ERROR_NO_WEBHOOK;
		}
		
		return channel.saveChannel() ? SETUP_CHANNEL_OK : SQL_ERROR;
		
	}
	
	
	/**
	 * Updates the channel
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public String executeUpdate(ImplReaction reaction,Channel channel) {

		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

		if(emojiX.equals(reaction.getUnicodeEmoji())) {
			return CANCEL_MESSAGE;
		}

		if(emojiV.equals(reaction.getUnicodeEmoji())) {

			if( channel.guildID == 0 ) {
				return ERROR_NO_GUILD;
			}
			
			if( channel.tbAssistant && channel.webhook == null ) {
				return ERROR_NO_WEBHOOK;
			}
			
			return channel.saveChannel() ? SETUP_CHANNEL_OK : SQL_ERROR;
		
		}
		
		return null;
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
	
	
}
