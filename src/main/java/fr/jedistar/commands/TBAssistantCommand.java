/**
 * 
 */
package fr.jedistar.commands;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplUser;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.entities.message.impl.ImplReaction;

import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.classes.Channel;
import fr.jedistar.classes.TBEventLog;
import fr.jedistar.classes.TBTerritoryLog;
import fr.jedistar.classes.Territory;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.formats.PendingMultiAction;
import fr.jedistar.listener.JediStarBotMultiReactionAddListener;
import fr.jedistar.listener.JediStarBotReactionAddListener;

/**
 * @author Nathan
 *
 */
public class TBAssistantCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(TBAssistantCommand.class);

	private final static String SQL_GUILD_ID = "SELECT guildID FROM guild WHERE channelID=?;";
	private final static String SQL_FIND_ALL_TERRITORIES_BY_PHASE = "SELECT * FROM territoryData WHERE phase=?";

	private final String COMMAND;
	private final String COMMAND_ALERT;
	private final String COMMAND_START;
	private final String COMMAND_FINISH;
	private final String COMMAND_PHASE;
	private final String COMMAND_LOG;
	private final String COMMAND_REPORT;
	private final String COMMAND_BOOLEAN_TRUE;
	private final String COMMAND_BOOLEAN_FALSE;
	
	private final List<String> COMMANDS = new ArrayList<String>();
	
	private final String HELP;
	
	private final String CONFIRM_UPDATE_CHANNEL;
	private final String WARN_UPDATE_GUILD;
	private final String WARN_UPDATE_TBASSISTANT;
	private final String WARN_UPDATE_WEBHOOK;
	private final String SETUP_CHANNEL_OK;	
	private final String CANCEL_MESSAGE;
	private final String ALERT_UPDATE_TERRITORY_LOG;

	private final String ERROR_MESSAGE;
	private final String ERROR_MESSAGE_SQL;
	private final String ERROR_MESSAGE_NO_CHANNEL;
	private final String ERROR_MESSAGE_NO_TERRITORY;
	private final String ERROR_MESSAGE_NO_TERRITORY_IN_PHASE;
	private final String ERROR_MESSAGE_NO_GUILD_NUMBER;
	private final String ERROR_MESSAGE_NO_MISSION;
	private final String ERROR_MESSAGE_BAD_PHASE;
	private final String ERROR_MESSAGE_PARAMS_NUMBER;
	private final String ERROR_COMMAND;
	private final String ERROR_INCORRECT_NUMBER;
	private final String ERROR_DB_UPDATE;
	private final String ERROR_NO_CURRENT_TB;
	private final String TOO_MUCH_RESULTS;
	private final String SQL_ERROR;	
	
	//Nom des champs JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private static final String JSON_TBA = "tbaCommandParameters";
	
	private static final String JSON_TBA_HELP = "help";
	
	private static final String JSON_TBA_COMMANDS = "commands";
	private static final String JSON_TBA_COMMANDS_BASE = "base";
	private static final String JSON_TBA_COMMANDS_ALERT = "alert";
	private static final String JSON_TBA_COMMANDS_START = "start";
	private static final String JSON_TBA_COMMANDS_FINISH = "finish";
	private static final String JSON_TBA_COMMANDS_PHASE = "phase";
	private final static String JSON_TBA_COMMANDS_LOG = "log";
	private final static String JSON_TBA_COMMANDS_REPORT = "report";
	private static final String JSON_TBA_COMMANDS_BOOLEAN_TRUE = "toggleON";
	private static final String JSON_TBA_COMMANDS_BOOLEAN_FALSE = "toggleOFF";
		
	private static final String JSON_TBA_MESSAGES = "messages";
	private static final String JSON_TBA_MESSAGES_CONFIRM_UPDATE_CHANNEL = "confirmUpdateChannel";
	private static final String JSON_TBA_MESSAGES_WARN_UPDATE_GUILD = "warnUpdateGuild";
	private static final String JSON_TBA_MESSAGES_WARN_UPDATE_WEBHOOK = "warnUpdateWebhook";
	private static final String JSON_TBA_MESSAGES_WARN_UPDATE_TBASSISTANT = "warnUpdateTBAssistant";
	private static final String JSON_TBA_MESSAGES_CHANNEL_SETUP_OK = "channelSetupOK";
	private static final String JSON_TBA_MESSAGES_CANCEL = "cancelAction";
	private static final String JSON_TBA_MESSAGES_ALERT_UPDATE_TERRITORY_LOG = "alertUpdateTerrLog";
	
	private final static String JSON_TB_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_TB_ERROR_MESSAGES_SQL = "sqlError";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CHANNEL = "noChannel";
	private final static String JSON_TB_ERROR_MESSAGES_NO_TERRITORY = "noTerritory";
	private final static String JSON_TB_ERROR_MESSAGES_NO_TERRITORY_IN_PHASE = "noTerritoryInPhase";
	private final static String JSON_TB_ERROR_MESSAGES_NO_GUILD = "noGuildNumber";
	private final static String JSON_TB_ERROR_MESSAGES_BAD_PHASE = "badPhase";
	private final static String JSON_TB_ERROR_MESSAGES_NO_MISSION = "badMission";
	private final static String JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNumber";
	private final static String JSON_TB_ERROR_MESSAGES_COMMAND = "commandError";
	private final static String JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER = "incorrectNumber";
	private final static String JSON_TB_ERROR_MESSAGES_DB_UPDATE = "dbUpdateError";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CURRENT_TB = "dbNoCurrentTB";
	private final static String JSON_TB_TOO_MUCH_RESULTS = "tooMuchResults";
	private static final String JSON_SETUP_ERROR_SQL = "sqlError";


	public TBAssistantCommand() {
		//Lecture du JSON
		JSONObject params = StaticVars.jsonSettings;

		ERROR_MESSAGE = params.getString(JSON_ERROR_MESSAGE);

		JSONObject tbaParams = params.getJSONObject(JSON_TBA);

		HELP = tbaParams.getString(JSON_TBA_HELP);

		JSONObject commands = tbaParams.getJSONObject(JSON_TBA_COMMANDS);		
		COMMAND = commands.getString(JSON_TBA_COMMANDS_BASE);
		COMMAND_ALERT = commands.getString(JSON_TBA_COMMANDS_ALERT);
		COMMAND_START = commands.getString(JSON_TBA_COMMANDS_START);
		COMMAND_FINISH = commands.getString(JSON_TBA_COMMANDS_FINISH);
		COMMAND_PHASE = commands.getString(JSON_TBA_COMMANDS_PHASE);
		COMMAND_LOG = commands.getString(JSON_TBA_COMMANDS_LOG);
		COMMAND_REPORT = commands.getString(JSON_TBA_COMMANDS_REPORT);		
		COMMAND_BOOLEAN_TRUE = commands.getString(JSON_TBA_COMMANDS_BOOLEAN_TRUE);
		COMMAND_BOOLEAN_FALSE = commands.getString(JSON_TBA_COMMANDS_BOOLEAN_FALSE);
		
		COMMANDS.add( COMMAND_START.toLowerCase() );
		COMMANDS.add( COMMAND_FINISH.toLowerCase() );
		COMMANDS.add( COMMAND_PHASE.toLowerCase() );
		COMMANDS.add( HELP.toLowerCase() );
		
		JSONObject messages = tbaParams.getJSONObject(JSON_TBA_MESSAGES);
		CONFIRM_UPDATE_CHANNEL = messages.getString(JSON_TBA_MESSAGES_CONFIRM_UPDATE_CHANNEL);
		WARN_UPDATE_GUILD = messages.getString(JSON_TBA_MESSAGES_WARN_UPDATE_GUILD);
		WARN_UPDATE_TBASSISTANT = messages.getString(JSON_TBA_MESSAGES_WARN_UPDATE_TBASSISTANT);
		WARN_UPDATE_WEBHOOK = messages.getString(JSON_TBA_MESSAGES_WARN_UPDATE_WEBHOOK);
		SETUP_CHANNEL_OK = messages.getString(JSON_TBA_MESSAGES_CHANNEL_SETUP_OK);
		CANCEL_MESSAGE = messages.getString(JSON_TBA_MESSAGES_CANCEL);
		ALERT_UPDATE_TERRITORY_LOG = messages.getString(JSON_TBA_MESSAGES_ALERT_UPDATE_TERRITORY_LOG);
		
		JSONObject errorMessages = tbaParams.getJSONObject(JSON_TB_ERROR_MESSAGES);
		ERROR_MESSAGE_SQL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_SQL);
		ERROR_MESSAGE_NO_CHANNEL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CHANNEL);
		ERROR_MESSAGE_NO_TERRITORY = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_TERRITORY);
		ERROR_MESSAGE_NO_TERRITORY_IN_PHASE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_TERRITORY_IN_PHASE);
		ERROR_MESSAGE_NO_GUILD_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_GUILD);
		ERROR_MESSAGE_NO_MISSION = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_MISSION);
		ERROR_MESSAGE_BAD_PHASE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_BAD_PHASE);
		ERROR_MESSAGE_PARAMS_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER);
		ERROR_COMMAND = errorMessages.getString(JSON_TB_ERROR_MESSAGES_COMMAND);
		ERROR_INCORRECT_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER);
		ERROR_DB_UPDATE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_DB_UPDATE);
		ERROR_NO_CURRENT_TB = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CURRENT_TB);
		TOO_MUCH_RESULTS = errorMessages.getString(JSON_TB_TOO_MUCH_RESULTS);
		SQL_ERROR = errorMessages.getString(JSON_SETUP_ERROR_SQL);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}


	@Override
	public CommandAnswer answer(DiscordAPI api, List<String> params, Message receivedMessage, boolean isAdmin) {

		//alert on help
		if( params.get(0).toLowerCase().equalsIgnoreCase("help") ) {
			return new CommandAnswer(HELP, null);
		}
		
		//Kick out if not enough params
		if(params.size() == 0) {
			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
		}

		//Kick out if DM
		if(receivedMessage.getChannelReceiver() == null) {
			return new CommandAnswer(ERROR_MESSAGE_NO_CHANNEL, null);
		}
		
		Channel channel = new Channel(receivedMessage.getChannelReceiver().getId());

		TBEventLog thisEvent = new TBEventLog();
		thisEvent.loadLastEventLog();
		thisEvent.calculateToday(TimeZone.getDefault());
		
		if( thisEvent.id == null ) {
			return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
		}
		
		List<Territory> territories = getAllTerritoryMatches(thisEvent.phase);
		
		if( COMMAND_ALERT.equalsIgnoreCase(params.get(0)) ) {
			
			/** Handle: %tba alert [platoons|missions] */

			
			String emoji1 = EmojiManager.getForAlias("one").getUnicode();
			String emoji2 = EmojiManager.getForAlias("two").getUnicode();
			String emoji3 = EmojiManager.getForAlias("three").getUnicode();
			String emoji4 = EmojiManager.getForAlias("four").getUnicode();
			String emoji5 = EmojiManager.getForAlias("five").getUnicode();
			String emoji6 = EmojiManager.getForAlias("six").getUnicode();

			EmbedBuilder embed = new EmbedBuilder();
			Color eColor = Color.RED;
			
			//ADD TERRITORY BATTLE ALERTS
			for( Integer t = 0; t < territories.size(); t++ ) {

				embed.setTitle(territories.get(t).territoryID+" - "+territories.get(t).territoryName);
				eColor = territories.get(t).specialMission != null ? Color.ORANGE : Color.WHITE;
				eColor = territories.get(t).combatType == 2 ? Color.CYAN : eColor;
				embed.setColor(eColor);
				
				api.getChannelById(channel.channelID).sendMessage(null,embed);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				String pStr = alertPlatoons( api, receivedMessage, territories.get(t), thisEvent, channel );
				String mStr = alertMissions( api, receivedMessage, territories.get(t), thisEvent, channel );				

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			
			embed.setTitle("Log your activity with the following reactions");
			embed.setDescription(":one::two::three::four::five::six:");
			eColor = Color.RED;
			embed.setColor(eColor);

			return new CommandAnswer(null,embed);
			
		}
		
		/**
		 * REPORT progress
		 * 
		 * %tb report phase
		 * 
		 */
		if(COMMAND_REPORT.equalsIgnoreCase(params.get(0))) {
			
			/** Handle: %tb report ... */
			
			
			if( COMMAND_PHASE.equalsIgnoreCase(params.get(1)) ) {
				
				/** Handle: %tb report phase <num> */
					
				Integer phase = 0;
								
				if( params.size() == 2 ) {			
					phase = thisEvent.phase;
				} else {
					try {
						phase = Integer.parseInt(params.get(2));
					} catch( NumberFormatException e ) {
						logger.error(e.getMessage());
						return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
					}
				}

				if( phase < 1 || phase > 6 ) {
					return new CommandAnswer("ERROR_MESSAGE_BAD_PHASE"+phase.toString(),null);
				}
				
				if( phase < 1 || phase > thisEvent.phase ) {
					return new CommandAnswer("ERROR_MESSAGE_OUT_OF_PHASE",null);
				}
					
				//GET PHASE # REPORT
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				EmbedBuilder embed = new EmbedBuilder();					
				embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
				embed.setTitle("Phase "+phase);
				embed.setDescription("**-**");
				embed.setColor(Color.BLUE);
					
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report("full");
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);
								
			}
			

			/** 
			 * Report current platoons
			 * Handle: %tb report platoons
			 *         %tb report platoons phase <num>
			 */
			
			final String COMMAND_PLATOONS = "platoons";
			if( COMMAND_PLATOONS.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tb report platoons phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 ) {
							return new CommandAnswer("ERROR_MESSAGE_BAD_PHASE",null);
						}
						
						if( phase < 1 || phase > thisEvent.phase ) {
							return new CommandAnswer("ERROR_MESSAGE_OUT_OF_PHASE",null);
						}
					}

					/** Handle: %tb report platoons HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
					}
					
					EmbedBuilder embed = new EmbedBuilder();					
					embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
					embed.setTitle("Phase "+phase);
					embed.setDescription("**-**");
					embed.setColor(Color.BLUE);
						
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report("platoons");
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tb report platoons */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
				}
				
				EmbedBuilder embed = new EmbedBuilder();					
				embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
				embed.setTitle("Phase "+phase);
				embed.setDescription("**-**");
				embed.setColor(Color.BLUE);
					
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report("platoons");
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);				
				
			}

			
			 /** Handle: %tb report sm */

			
			final String COMMAND_SM = "sm";
			if( COMMAND_SM.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tb report sm phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 ) {
							return new CommandAnswer("ERROR_MESSAGE_BAD_PHASE",null);
						}
						
						if( phase < 1 || phase > thisEvent.phase ) {
							return new CommandAnswer("ERROR_MESSAGE_OUT_OF_PHASE",null);
						}
					}

					/** Handle: %tb report sm HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
					}
					
					EmbedBuilder embed = new EmbedBuilder();					
					embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
					embed.setTitle("Phase "+phase);
					embed.setDescription("**-**");
					embed.setColor(Color.BLUE);
						
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report("sm");
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tb report sm */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
				}
				
				EmbedBuilder embed = new EmbedBuilder();					
				embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
				embed.setTitle("Phase "+phase);
				embed.setDescription("**-**");
				embed.setColor(Color.BLUE);
					
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report("sm");
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);				
				
			}
			
			 /** Handle: %tb report cm */

			
			final String COMMAND_CM = "cm";
			if( COMMAND_CM.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tb report cm phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 ) {
							return new CommandAnswer("ERROR_MESSAGE_BAD_PHASE",null);
						}
						
						if( phase < 1 || phase > thisEvent.phase ) {
							return new CommandAnswer("ERROR_MESSAGE_OUT_OF_PHASE",null);
						}
					}

					/** Handle: %tb report cm HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
					}
					
					EmbedBuilder embed = new EmbedBuilder();					
					embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
					embed.setTitle("Phase "+phase);
					embed.setDescription("**-**");
					embed.setColor(Color.BLUE);
						
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report("cm");
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tb report cm */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
				}
				
				EmbedBuilder embed = new EmbedBuilder();					
				embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
				embed.setTitle("Phase "+phase);
				embed.setDescription("**-**");
				embed.setColor(Color.BLUE);
					
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report("cm");
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);				
				
			}

			
			/** Handle: %tb report HO2A
			 *          %tb report Overlook
			 */
			String terrString = params.get(1);
			for( Integer p = 2; p != params.size(); ++p ) {
				terrString += " "+params.get(p);
			}
			
			Territory territory = new Territory(terrString);
			
			EmbedBuilder embed = new EmbedBuilder();					
			embed.setAuthor("Territory Battle report ("+thisEvent.id+" - "+channel.guildID+")","","");
			TBTerritoryLog myLog = new TBTerritoryLog(thisEvent.id, channel.guildID, territory.territoryID);
			
			if( !terrString.equalsIgnoreCase(territory.territoryID) ) {
				
				//Ambiguous entry
				if( territory.phase > thisEvent.phase ) {
					return new CommandAnswer("ERROR_MESSAGE_AMBIGUOUS_OUT_OF_PHASE",null);
				}
												
				if( !myLog.saved ) { 					
					return new CommandAnswer("ERROR_MESSAGE_NO_LOG",null);
				}
				
			}

			embed.setTitle(territory.territoryName);
			embed.setDescription("**-**");
			embed.setColor(Color.BLUE);
			
			String reportStr = myLog.report("full");
			embed.addField(myLog.territoryID, reportStr, false);
				
			return new CommandAnswer(null,embed);
			
		}
			
			
		if(params.size() < 4) {
			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
		}

		/**
		 * LOG COMMAND (Append with + and remove with -)
		 * %tb log <territoryID> [platoon|<missionID>] [+|-]<number>
		 * 
		 * Example, log squadron 5 complete in HO3A
		 * %tb log HO3B platoon 5
		 * 
		 * Example, screwed up and squadron 5 was not complete - remove
		 * %tb log HO3B platoon -5
		 * 
		 * Example, log combat mission 1 complete 4/6 in HO5C
		 * %tb log HO5C CM1 4 
		 */
		if(COMMAND_LOG.equalsIgnoreCase(params.get(0))) {
						
			/** Handle: %tb log ... */
			
			TBEventLog lastTBLog = new TBEventLog();
			lastTBLog.calculateToday(TimeZone.getDefault());
			
			if( lastTBLog.phase > 0 && lastTBLog.phase <= 6 ) {
				// Current TB is in-phase
				
				/** Handle: %tb log <terrID> ... */
				
				String terrID = params.get(1);								
				if( !terrID.substring(2,3).trim().equalsIgnoreCase(lastTBLog.phase.toString().trim()) ) {
					
					//The phase number in the territory ID doesn't match the current TB phase
					return new CommandAnswer(ERROR_MESSAGE_NO_TERRITORY_IN_PHASE,null);					
				}
				
				Territory terr = new Territory(terrID);
				if( terr.territoryID == null || !terr.territoryID.equalsIgnoreCase(terrID) ) {
					
					//The ID of the new territory isn't the string I passed - not found
					return new CommandAnswer(ERROR_MESSAGE_NO_TERRITORY,null);					
				}
				
				//Territory log will find itself if exists or start new
				TBTerritoryLog terrLog = new TBTerritoryLog(lastTBLog.id, channel.guildID, terr.territoryID);				
				terrLog.phase = terrLog.phase == 0 ? lastTBLog.phase : terrLog.phase;
								
				String type = params.get(2);
				if( type.equalsIgnoreCase("platoon") ) {
					
					//Kick out if not admin
					if(!isAdmin) {
						return new CommandAnswer("ERROR_MESSAGE_FORBIDDEN", null);
					}
					
					/** Handle: %tb log <terrID> [platoon] ... */
					
					Integer platoon = 0;					
					try {						
						platoon = Integer.parseInt(params.get(3));					
					} catch( NumberFormatException e ) {						
						logger.error(e.getMessage());
						return new CommandAnswer(ERROR_INCORRECT_NUMBER,null);
					}	

					char flag = 'Y';
					
					/** Handle: %tb log <terrID> [platoon] <num> */
					
					if( platoon >= -6  && platoon < 0 ) {
						
						platoon = platoon*-1;
						flag = 'N';
						
					}
					
					if( platoon > 0  && platoon <= 6 ) {
							
						char[] platoons = terrLog.platoons.toCharArray();
						platoons[platoon-1] = flag;
						terrLog.platoons = String.copyValueOf(platoons);
						terrLog.saveLog();
						return new CommandAnswer("Logged",null);

					}
										
					return new CommandAnswer(ERROR_INCORRECT_NUMBER,null);
					
				}
				
				/** Handle: %tb log <terrID> <deploy> ... */				
				
				// TO DO...
				
				/** Handle: %tb log <terrID> <mission> ... */

				String mission = params.get(2);
				
				String mType = mission.substring(0, 2);								
				Integer mNum = 0;
				Integer newVal = 0;
				try {
					mNum = Integer.parseInt(mission.substring(mission.length()-1));
					newVal = Integer.parseInt(params.get(3).trim());										
				} catch( NumberFormatException e ) {
					logger.error( e.getMessage() );
					return new CommandAnswer(ERROR_INCORRECT_NUMBER,null);
				}
			     
				if( mType.equalsIgnoreCase("sm") && mNum == 1 ) {
					
					if( terr.specialMission == null ) { 
						return new CommandAnswer("This territory doesn't have a special mission",null);
					}
					
					if( newVal < 0 || newVal > 3) {
						return new CommandAnswer("This mission only has 0-3 tiers",null);
					}
						
					
					//SM
					String requestUser = receivedMessage.getAuthor().getName();
					if( terrLog.SM1.indexOf(requestUser) >= 0 ) {						
						
						if( terrLog.SM1.get(terrLog.SM1.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
							return new CommandAnswer(String.format("Already logged as %s",newVal.toString()),null);
						}
						
						String ALERT_UPDATE = String.format(ALERT_UPDATE_TERRITORY_LOG, terrLog.SM1.get(terrLog.SM1.indexOf(requestUser)+1) , newVal.toString());
						terrLog.SM1.set(terrLog.SM1.indexOf(requestUser)+1, newVal.toString());
						
						//ALERT UPDATE CONFIRMATION
						JediStarBotReactionAddListener.addPendingAction(new PendingAction(receivedMessage.getAuthor(),"executeTerritoryLogUpdate",this,receivedMessage,1,terrLog));
						String emojiX = EmojiManager.getForAlias("x").getUnicode();
						String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();
				
						return new CommandAnswer(ALERT_UPDATE,null,emojiV,emojiX);						

					}
					
					//LOG
					terrLog.SM1.add(requestUser);
					terrLog.SM1.add(newVal.toString());
					terrLog.saveLog();
					return new CommandAnswer("Logged SM",null);
					
				} 
										
				if( mType.equalsIgnoreCase("cm") ) {
					
					if( mNum > terr.combatMissions || mNum < 1 ) {
						return new CommandAnswer("This territory doesn't have that many combat missions",null);
					}
					
					if( newVal < 0 || newVal > 6) {
						return new CommandAnswer("This mission only has 0-6 tiers",null);
					}
					
					//CM
					//String requestUser = receivedMessage.getAuthor().getId();
					String requestUser = receivedMessage.getAuthor().getName();

					if( mNum == 1 && terrLog.CM1.contains(requestUser) || mNum == 2 && terrLog.CM2.contains(requestUser) ) {						
						
						String ALERT_UPDATE = "";
						if( mNum == 1 ) {
							
							if( terrLog.CM1.get(terrLog.CM1.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
								return new CommandAnswer(String.format("Already logged as %s",newVal.toString()),null);
							}
							
							ALERT_UPDATE = String.format(ALERT_UPDATE_TERRITORY_LOG, terrLog.CM1.get(terrLog.CM1.indexOf(requestUser)+1) , newVal.toString());
							terrLog.CM1.set(terrLog.CM1.indexOf(requestUser)+1, newVal.toString());							
						} else {
							
							if( terrLog.CM2.get(terrLog.CM2.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
								return new CommandAnswer(String.format("Already logged as %s",newVal.toString()),null);
							}
							
							ALERT_UPDATE = String.format(ALERT_UPDATE_TERRITORY_LOG, terrLog.CM2.get(terrLog.CM2.indexOf(requestUser)+1) , newVal.toString());
							terrLog.CM2.set(terrLog.CM2.indexOf(requestUser)+1, newVal.toString());
						}
						
						//ALERT UPDATE CONFIRMATION
						JediStarBotReactionAddListener.addPendingAction(new PendingAction(receivedMessage.getAuthor(),"executeTerritoryLogUpdate",this,receivedMessage,1,terrLog));
						String emojiX = EmojiManager.getForAlias("x").getUnicode();
						String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();
				
						return new CommandAnswer(ALERT_UPDATE,null,emojiV,emojiX);						
						
					}
					
					//LOG
					if( mNum == 1 ) { 
						terrLog.CM1.add(requestUser);
						terrLog.CM1.add(newVal.toString());
					} else { 
						terrLog.CM2.add(requestUser);
						terrLog.CM2.add(newVal.toString());
					}
					
					terrLog.saveLog();
					return new CommandAnswer("Logged CM"+mNum,null);
					
				}
					
				return new CommandAnswer(ERROR_MESSAGE_NO_MISSION,null);			
				
			}	
			
			return new CommandAnswer(String.format(ERROR_NO_CURRENT_TB, ( new SimpleDateFormat( "yyyy-MM-dd" ) ).format( lastTBLog.date.getTime() )) ,null);			
			
		}

		
		
		return new CommandAnswer("NOTHING_HAPPENED",null);
		
	}
	

	/**
	 * Updates the platoon
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public void executeLogUpdate(ImplUser user, ImplReaction reaction, Integer logID, String missionType, String messageID) {
		
		if( messageID == null || reaction.getMessage().getId() == null ) {
			return;
		}
		
		if( messageID.equals(reaction.getMessage().getId()) ) {
			if( missionType == "p" ) {
				executePlatoonUpdate(user, reaction, logID, messageID);				
			} else {
				executeMissionUpdate(user, reaction, logID, messageID, missionType);
			}
		}
	}	
	
	/**
	 * Updates the platoon
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public void executeMissionUpdate(ImplUser user, ImplReaction reaction, Integer logID, String messageID, String missionType) {

		List<String> emojis = new ArrayList<String>();
		emojis.add(EmojiManager.getForAlias("one").getUnicode());
		emojis.add(EmojiManager.getForAlias("two").getUnicode());
		emojis.add(EmojiManager.getForAlias("three").getUnicode());
		emojis.add(EmojiManager.getForAlias("four").getUnicode());
		emojis.add(EmojiManager.getForAlias("five").getUnicode());
		emojis.add(EmojiManager.getForAlias("six").getUnicode());

		if(!emojis.contains(reaction.getUnicodeEmoji())) {
			return;
		}

		Channel channel = new Channel(reaction.getMessage().getChannelReceiver().getId());
		String terrID = reaction.getMessage().getContent().substring(0, 4);
		TBTerritoryLog log = new TBTerritoryLog(logID, channel.guildID, terrID);
		
				
		/** Handle: %tb log <terrID> [cm1|cm2|sm1] <num> */
		
		List<String> mission = missionType.equalsIgnoreCase("sm") ? log.SM1 : log.CM1;
		mission = missionType.equalsIgnoreCase("cm2") ? log.CM2 : mission;
		
		String player = user.getName();
		Integer tier = emojis.indexOf(reaction.getUnicodeEmoji())+1;
		
		logger.info( player+" : "+log.logID.toString()+"."+log.guildID.toString()+"."+log.territoryID+"."+missionType+"."+reaction.getUnicodeEmoji() );

		Message remsg;
		try { 
			
			Thread.sleep(5000);
			
			if( mission.contains(player) ) {
				
				Thread.sleep(500);
				
				//remsg = reaction.getMessage().getChannelReceiver().sendMessage("<@!"+user.getId()+"> already logged").get(1, TimeUnit.MINUTES);
				return;
			}
				
			mission.add(player);
			mission.add(tier.toString().trim());
	
			if( missionType.equalsIgnoreCase("sm") ) {
				if( tier > 3 ) { 
				
					Thread.sleep(500);
					
					//remsg = reaction.getMessage().getChannelReceiver().sendMessage("1-3 only").get(1, TimeUnit.MINUTES);
					return; 
				}
				log.SM1 = mission;
			} else if( missionType.equalsIgnoreCase("cm1") ) {
				if( log.territoryID.toLowerCase().charAt(log.territoryID.length()-1) == 'a' && log.phase > 2 && tier > 3 ) { 
					
					Thread.sleep(500);
					
					//remsg = reaction.getMessage().getChannelReceiver().sendMessage("1-3 only").get(1, TimeUnit.MINUTES);
					return; 
				}
				log.CM1 = mission;
			} else if( missionType.equalsIgnoreCase("cm2") ) {
				if( log.territoryID.toLowerCase().charAt(log.territoryID.length()-1) == 'a' && log.phase > 2 && tier > 3 ) { 
					
					Thread.sleep(500);
					
					//remsg = reaction.getMessage().getChannelReceiver().sendMessage("1-3 only").get(1, TimeUnit.MINUTES);
					return; 
				}
				log.CM2 = mission;
			}
	
			if( !log.saveLog() ) {
				
				Thread.sleep(500);
				
				//remsg = reaction.getMessage().getChannelReceiver().sendMessage("could not log - "+player+" - "+tier.toString()).get(1, TimeUnit.MINUTES);
				return;
			} 
			
			Thread.sleep(500);
			
			remsg = reaction.getMessage().getChannelReceiver().sendMessage("logged - "+player+" - "+tier.toString()).get(1, TimeUnit.MINUTES);
			
		} catch( InterruptedException | ExecutionException | TimeoutException e ) {
			logger.error(e.getMessage());
			return;
		}
	}

	
	/**
	 * Updates the platoon
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public void executePlatoonUpdate(User user, ImplReaction reaction, Integer logID, String messageID) {

		List<String> emojis = new ArrayList<String>();
		emojis.add(EmojiManager.getForAlias("one").getUnicode());
		emojis.add(EmojiManager.getForAlias("two").getUnicode());
		emojis.add(EmojiManager.getForAlias("three").getUnicode());
		emojis.add(EmojiManager.getForAlias("four").getUnicode());
		emojis.add(EmojiManager.getForAlias("five").getUnicode());
		emojis.add(EmojiManager.getForAlias("six").getUnicode());

		if(!emojis.contains(reaction.getUnicodeEmoji())) {
			return;
		}

		Channel channel = new Channel(reaction.getMessage().getChannelReceiver().getId());
		String terrID = reaction.getMessage().getContent().substring(0, 4);
		
		TBTerritoryLog log = new TBTerritoryLog(logID, channel.guildID, terrID);
				
		char flag = 'Y';
		Integer platoon = emojis.indexOf(reaction.getUnicodeEmoji())+1;
		
		/** Handle: %tb log <terrID> [platoon] <num> */

		char[] platoons = log.platoons.toCharArray();
		
		if( platoons[platoon-1] == flag ) {
			return;
		}
		
		String player = user.getName();
		logger.info( player+" : "+log.logID.toString()+"."+log.guildID.toString()+"."+log.territoryID+".platoon."+reaction.getUnicodeEmoji() );

		platoons[platoon-1] = flag;
		log.platoons = String.copyValueOf(platoons);
		log.saveLog();
		
		reaction.getMessage().getChannelReceiver().sendMessage(log.territoryID+" : Platoon "+String.valueOf(platoon)+" has been logged as full");
	
	}

	
		
	/**
	 * Updates the channel
	 * @param serverID
	 * @param channel
	 * @return
	 */
	public String executeTerritoryLogUpdate(ImplReaction reaction,TBTerritoryLog log) {

		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

		if(emojiX.equals(reaction.getUnicodeEmoji())) {
			return CANCEL_MESSAGE;
		}

		if(emojiV.equals(reaction.getUnicodeEmoji())) {

			
			return log.saveLog() ? "Territory battle log updated" : SQL_ERROR;
		
		}
		
		return null;
	}

	
	/**
	 * Gets the guild ID associated with this Discord server from the DB
	 * @param message
	 * @return
	 */
	private Integer getGuildIDFromDB(Message message) {

		String channelID = message.getChannelReceiver().getId();

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_GUILD_ID);

			stmt.setString(1,channelID);

			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			if(rs.next()) {
				return rs.getInt("guildID");
			}
			return -1;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public List<Territory> getAllTerritoryMatches( Integer phase ) {
		
		List<Territory> matches = new ArrayList<Territory>();
		
		  Connection conn = null;
		  PreparedStatement stmt = null;
		  ResultSet rs = null;
		
		  try {
			  
			  conn = StaticVars.getJdbcConnection();
		
			  stmt = conn.prepareStatement(SQL_FIND_ALL_TERRITORIES_BY_PHASE);
		
			  stmt.setInt(1,phase);
		
			  logger.debug("Executing query : "+stmt.toString());		
			  rs = stmt.executeQuery();
		
			  while(rs.next()) {
				  
				  matches.add( new Territory(rs.getString("territoryID"),rs.getString("territoryName"),rs.getString("tbName"),rs.getInt("phase"),rs.getInt("combatType"),rs.getInt("starPoints1"),rs.getInt("starPoints2"),rs.getInt("starPoints3"),rs.getString("ability"),rs.getString("affectedTerritories"),rs.getString("requiredUnits"),rs.getString("specialMission"),rs.getInt("combatMissions"),rs.getInt("missionPoints1"),rs.getInt("missionPoints2"),rs.getInt("missionPoints3"),rs.getInt("missionPoints4"),rs.getInt("missionPoints5"),rs.getInt("missionPoints6"),rs.getInt("platoonPoints1"),rs.getInt("platoonPoints2"),rs.getInt("platoonPoints3"),rs.getInt("platoonPoints4"),rs.getInt("platoonPoints5"),rs.getInt("platoonPoints6"),rs.getInt("minDeployStar1"),rs.getInt("minDeployStar2"),rs.getInt("minDeployStar3"),rs.getInt("minGPStar3"),rs.getString("notes") ) );
				  
			  }
			
			  return matches;
			  
		} catch(SQLException e) {
			  logger.error(e.getMessage());
			  e.printStackTrace();
			  return null;
		}
		finally {
			try {
				if(rs != null) { rs.close(); }
				if(stmt != null) { stmt.close(); }
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		
	}

	
	private String alertPlatoons( DiscordAPI api, Message receivedMessage, Territory territory, TBEventLog thisEvent, Channel channel ) {
		
		String missionType = "p";
		
		Future<Message> future = api.getChannelById(channel.channelID).sendMessage(territory.territoryID+": __Platoon log__ : *Add reactions to this message*");

		try {
			Message sentMessage = null;
			sentMessage = future.get(1, TimeUnit.MINUTES);					
			JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,receivedMessage,24,thisEvent.id,missionType,sentMessage.getId()));				

			Thread.sleep(800);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			return "ALERT_ERROR";
		}
		
		return null;
	}
	
	private String alertMissions( DiscordAPI api, Message receivedMessage, Territory territory, TBEventLog thisEvent, Channel channel ) {
		
		for( Integer cm = 1; cm <= territory.combatMissions; ++cm ) {
			
			String missionType = "cm"+cm;
		
			Future<Message> future = api.getChannelById(channel.channelID).sendMessage(territory.territoryID+": __Combat Mission "+cm+" log__ : *Add reactions to this message*");

			try {
				Message sentMessage = null;
				sentMessage = future.get(1, TimeUnit.MINUTES);					
				JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,receivedMessage,24,thisEvent.id,missionType,sentMessage.getId()));				

				Thread.sleep(800);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				return "ALERT_ERROR";
			}
		}

		
		if( territory.specialMission != null ) {

			String missionType = "sm";
			
			Future<Message> future = api.getChannelById(channel.channelID).sendMessage(territory.territoryID+": __Special Mission log__ : *Add reactions to this message*");

			try {
				Message sentMessage = null;
				sentMessage = future.get(1, TimeUnit.MINUTES);					
				JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,receivedMessage,24,thisEvent.id,missionType,sentMessage.getId()));				

				Thread.sleep(800);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				return "ALERT_ERROR";
			}

		}
		return null;
	}
	
	private String error(String message) {
		return ERROR_MESSAGE +"**"+ message + "**\r\n\r\n"+ HELP;
	}
	
	
}
