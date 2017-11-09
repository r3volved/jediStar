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
import java.util.Calendar;
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
import de.btobastian.javacord.entities.message.Reaction;
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
	private final String COMMAND_HELP;
	private final String COMMAND_ALERT;
	private final String COMMAND_START;
	private final String COMMAND_FINISH;
	private final String COMMAND_PHASE;
	private final String COMMAND_LOG;
	private final String COMMAND_REPORT;
	private final String COMMAND_PLATOONS;
	private final String COMMAND_COMBAT_MISSION1;
	private final String COMMAND_COMBAT_MISSION2;
	private final String COMMAND_COMBAT_MISSION;
	private final String COMMAND_SPECIAL_MISSION;
	
	
	private final List<String> COMMANDS = new ArrayList<String>();
	
	private final String HELP;
	
	private final String UPDATE_LOG_OK;	
	private final String CANCEL_MESSAGE;
	private final String ALERT_UPDATE_TERRITORY_LOG;
	private final String ALERT_LOG_ACTIVITY_TITLE;
	private final String ALERT_LOG_ACTIVITY_DESCRIPTION;
	private final String ALERT_PHASE_START_TITLE;
	private final String ALERT_PHASE_START_DESCRIPTION;
	private final String ALERT_TERRITORY_START_DESCRIPTION;
	private final String ALERT_PHASE_END_TITLE;
	private final String ALERT_PHASE_END_DESCRIPTION;
	private final String MESSAGE_PLATOON_LOG;
	private final String MESSAGE_COMBAT_MISSION_LOG;
	private final String MESSAGE_SPECIAL_MISSION_LOG;
	private final String MESSAGE_CONFIRMED_PLATOON_LOGGED;
	private final String MESSAGE_CONFIRMED_MISSION_LOGGED;
	private final String MESSAGE_REPORT_TITLE;
	private final String MESSAGE_REPORT_PHASE;

	private final String ERROR_MESSAGE;
	private final String ERROR_MESSAGE_FORBIDDEN;
	private final String ERROR_MESSAGE_SQL;
	private final String ERROR_MESSAGE_NO_CHANNEL;
	private final String ERROR_MESSAGE_NO_TERRITORY;
	private final String ERROR_MESSAGE_NO_TERRITORY_IN_PHASE;
	private final String ERROR_MESSAGE_NO_GUILD_NUMBER;
	private final String ERROR_MESSAGE_NO_MISSION;
	private final String ERROR_MESSAGE_NO_LOG;
	private final String ERROR_MESSAGE_BAD_PHASE;
	private final String ERROR_MESSAGE_PARAMS_NUMBER;
	private final String ERROR_COMMAND;
	private final String ERROR_INCORRECT_NUMBER;
	private final String ERROR_DB_UPDATE;
	private final String ERROR_NO_CURRENT_TB;
	private final String TOO_MUCH_RESULTS;
	private final String SQL_ERROR;	
	private final String ERROR_ALREADY_LOGGED;
	private final String ERROR_ALREADY_LOGGED_DETAILS;
	private final String ERROR_INCORRECT_MISSION_TIERS;
	private final String ERROR_MESSAGE_NO_MISSION_IN_TERRITORY;
	
	//Nom des champs JSON
	private final static String JSON_ERROR_MESSAGE = "errorMessage";

	private static final String JSON_TBA = "tbaCommandParameters";
	
	private static final String JSON_TBA_HELP = "help";
	
	private static final String JSON_TBA_COMMANDS = "commands";
	private static final String JSON_TBA_COMMANDS_BASE = "base";
	private static final String JSON_TBA_COMMANDS_HELP = "help";
	private static final String JSON_TBA_COMMANDS_ALERT = "alert";
	private static final String JSON_TBA_COMMANDS_START = "start";
	private static final String JSON_TBA_COMMANDS_FINISH = "finish";
	private static final String JSON_TBA_COMMANDS_PHASE = "phase";
	private final static String JSON_TBA_COMMANDS_LOG = "log";
	private final static String JSON_TBA_COMMANDS_REPORT = "report";
	private final static String JSON_TBA_COMMANDS_PLATOONS = "platoons";
	private static final String JSON_TBA_COMMANDS_COMBAT_MISSION1 = "cm1";
	private static final String JSON_TBA_COMMANDS_COMBAT_MISSION2 = "cm2";
	private static final String JSON_TBA_COMMANDS_COMBAT_MISSION = "cm";	
	private static final String JSON_TBA_COMMANDS_SPECIAL_MISSION = "sm";	
	
	private static final String JSON_TBA_MESSAGES = "messages";
	private static final String JSON_TBA_MESSAGES_UPDATE_LOG_OK = "updateLogOK";
	private static final String JSON_TBA_MESSAGES_CANCEL = "cancelAction";
	private static final String JSON_TBA_MESSAGES_ALERT_UPDATE_TERRITORY_LOG = "alertUpdateTerrLog";
	private static final String JSON_TBA_MESSAGES_ALERT_LOG_ACTIVITY_TITLE = "alertLogActivityTitle";
	private static final String JSON_TBA_MESSAGES_ALERT_LOG_ACTIVITY_DESCRIPTION = "alertLogActivityDescription";
	private static final String JSON_TBA_MESSAGES_ALERT_PHASE_START_TITLE = "alertPhaseStartingTitle";
	private static final String JSON_TBA_MESSAGES_ALERT_PHASE_START_DESCRIPTION = "alertPhaseStartingDescription";
	private static final String JSON_TBA_MESSAGES_ALERT_TERRITORY_START_DESCRIPTION = "alertTerritoryStartingDescription";
	private static final String JSON_TBA_MESSAGES_ALERT_PHASE_END_TITLE = "alertPhaseFinishTitle";
	private static final String JSON_TBA_MESSAGES_ALERT_PHASE_END_DESCRIPTION = "alertPhaseFinishDescription";
	private static final String JSON_TBA_MESSAGES_PLATOON_LOG = "alertPlatoonLog";
	private static final String JSON_TBA_MESSAGES_COMBAT_MISSION_LOG = "alertCombatMissionLog";
	private static final String JSON_TBA_MESSAGES_SPECIAL_MISSION_LOG = "alertSpecialMissionLog";
	private static final String JSON_TBA_MESSAGES_CONFIRMED_PLATOON_LOGGED = "confirmedPlatoonLogged";
	private static final String JSON_TBA_MESSAGES_CONFIRMED_MISSION_LOGGED = "confirmedMissionLogged";
	private static final String JSON_TBA_MESSAGES_REPORT_TITLE = "reportTitle";
	private static final String JSON_TBA_MESSAGES_REPORT_PHASE = "reportPhase";
	
	private final static String JSON_TB_ERROR_MESSAGES = "errorMessages";
	private static final String JSON_TB_ERROR_MESSAGES_FORBIDDEN = "forbidden";
	private final static String JSON_TB_ERROR_MESSAGES_SQL = "sqlError";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CHANNEL = "noChannel";
	private final static String JSON_TB_ERROR_MESSAGES_NO_TERRITORY = "noTerritory";
	private final static String JSON_TB_ERROR_MESSAGES_NO_TERRITORY_IN_PHASE = "noTerritoryInPhase";
	private final static String JSON_TB_ERROR_MESSAGES_NO_GUILD = "noGuildNumber";
	private final static String JSON_TB_ERROR_MESSAGES_NO_MISSION = "badMission";
	private final static String JSON_TB_ERROR_MESSAGES_NO_LOG = "noLog";
	private final static String JSON_TB_ERROR_MESSAGES_BAD_PHASE = "badPhase";
	private final static String JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER = "paramsNumber";
	private final static String JSON_TB_ERROR_MESSAGES_COMMAND = "commandError";
	private final static String JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER = "incorrectNumber";
	private final static String JSON_TB_ERROR_MESSAGES_DB_UPDATE = "dbUpdateError";
	private final static String JSON_TB_ERROR_MESSAGES_NO_CURRENT_TB = "dbNoCurrentTB";
	private final static String JSON_TB_TOO_MUCH_RESULTS = "tooMuchResults";
	private static final String JSON_SETUP_ERROR_SQL = "sqlError";
	private static final String JSON_TB_ERROR_ALREADY_LOGGED = "alreadyLogged";
	private static final String JSON_TB_ERROR_ALREADY_LOGGED_DETAILS = "alreadyLoggedDetails";
	private static final String JSON_TB_ERROR_INCORRECT_MISSION_TIERS = "incorrectMissionTiers";
	private static final String JSON_TB_ERROR_NO_MISSION_IN_TERRITORY = "noMission";
	

	public TBAssistantCommand() {
		//Lecture du JSON
		JSONObject params = StaticVars.jsonSettings;

		ERROR_MESSAGE = params.getString(JSON_ERROR_MESSAGE);

		JSONObject tbaParams = params.getJSONObject(JSON_TBA);

		HELP = tbaParams.getString(JSON_TBA_HELP);

		JSONObject commands = tbaParams.getJSONObject(JSON_TBA_COMMANDS);		
		COMMAND = commands.getString(JSON_TBA_COMMANDS_BASE);
		COMMAND_HELP = commands.getString(JSON_TBA_COMMANDS_HELP);
		COMMAND_ALERT = commands.getString(JSON_TBA_COMMANDS_ALERT);
		COMMAND_START = commands.getString(JSON_TBA_COMMANDS_START);
		COMMAND_FINISH = commands.getString(JSON_TBA_COMMANDS_FINISH);
		COMMAND_PHASE = commands.getString(JSON_TBA_COMMANDS_PHASE);
		COMMAND_LOG = commands.getString(JSON_TBA_COMMANDS_LOG);
		COMMAND_REPORT = commands.getString(JSON_TBA_COMMANDS_REPORT);
		COMMAND_PLATOONS = commands.getString(JSON_TBA_COMMANDS_PLATOONS);
		COMMAND_COMBAT_MISSION1 = commands.getString(JSON_TBA_COMMANDS_COMBAT_MISSION1);
		COMMAND_COMBAT_MISSION2 = commands.getString(JSON_TBA_COMMANDS_COMBAT_MISSION2);
		COMMAND_COMBAT_MISSION = commands.getString(JSON_TBA_COMMANDS_COMBAT_MISSION);
		COMMAND_SPECIAL_MISSION = commands.getString(JSON_TBA_COMMANDS_SPECIAL_MISSION);
		
		COMMANDS.add( COMMAND_START.toLowerCase() );
		COMMANDS.add( COMMAND_FINISH.toLowerCase() );
		COMMANDS.add( COMMAND_PHASE.toLowerCase() );
		COMMANDS.add( HELP.toLowerCase() );
		
		JSONObject messages = tbaParams.getJSONObject(JSON_TBA_MESSAGES);
		UPDATE_LOG_OK = messages.getString(JSON_TBA_MESSAGES_UPDATE_LOG_OK);
		CANCEL_MESSAGE = messages.getString(JSON_TBA_MESSAGES_CANCEL);
		ALERT_UPDATE_TERRITORY_LOG = messages.getString(JSON_TBA_MESSAGES_ALERT_UPDATE_TERRITORY_LOG);
		ALERT_LOG_ACTIVITY_TITLE = messages.getString(JSON_TBA_MESSAGES_ALERT_LOG_ACTIVITY_TITLE);
		ALERT_LOG_ACTIVITY_DESCRIPTION = messages.getString(JSON_TBA_MESSAGES_ALERT_LOG_ACTIVITY_DESCRIPTION);
		ALERT_PHASE_START_TITLE = messages.getString(JSON_TBA_MESSAGES_ALERT_PHASE_START_TITLE);
		ALERT_PHASE_START_DESCRIPTION = messages.getString(JSON_TBA_MESSAGES_ALERT_PHASE_START_DESCRIPTION);
		ALERT_TERRITORY_START_DESCRIPTION = messages.getString(JSON_TBA_MESSAGES_ALERT_TERRITORY_START_DESCRIPTION);
		ALERT_PHASE_END_TITLE = messages.getString(JSON_TBA_MESSAGES_ALERT_PHASE_END_TITLE);
		ALERT_PHASE_END_DESCRIPTION = messages.getString(JSON_TBA_MESSAGES_ALERT_PHASE_END_DESCRIPTION);
		MESSAGE_PLATOON_LOG = messages.getString(JSON_TBA_MESSAGES_PLATOON_LOG);
		MESSAGE_COMBAT_MISSION_LOG = messages.getString(JSON_TBA_MESSAGES_COMBAT_MISSION_LOG);
		MESSAGE_SPECIAL_MISSION_LOG = messages.getString(JSON_TBA_MESSAGES_SPECIAL_MISSION_LOG);
		MESSAGE_CONFIRMED_PLATOON_LOGGED = messages.getString(JSON_TBA_MESSAGES_CONFIRMED_PLATOON_LOGGED);
		MESSAGE_CONFIRMED_MISSION_LOGGED = messages.getString(JSON_TBA_MESSAGES_CONFIRMED_MISSION_LOGGED);
		MESSAGE_REPORT_TITLE = messages.getString(JSON_TBA_MESSAGES_REPORT_TITLE);
		MESSAGE_REPORT_PHASE = messages.getString(JSON_TBA_MESSAGES_REPORT_PHASE);	
		
		JSONObject errorMessages = tbaParams.getJSONObject(JSON_TB_ERROR_MESSAGES);
		ERROR_MESSAGE_SQL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_SQL);
		ERROR_MESSAGE_FORBIDDEN = errorMessages.getString(JSON_TB_ERROR_MESSAGES_FORBIDDEN);
		ERROR_MESSAGE_NO_CHANNEL = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CHANNEL);
		ERROR_MESSAGE_NO_TERRITORY = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_TERRITORY);
		ERROR_MESSAGE_NO_TERRITORY_IN_PHASE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_TERRITORY_IN_PHASE);
		ERROR_MESSAGE_NO_GUILD_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_GUILD);
		ERROR_MESSAGE_NO_MISSION = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_MISSION);
		ERROR_MESSAGE_NO_LOG = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_LOG);
		ERROR_MESSAGE_BAD_PHASE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_BAD_PHASE);
		ERROR_MESSAGE_PARAMS_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_PARAMS_NUMBER);
		ERROR_COMMAND = errorMessages.getString(JSON_TB_ERROR_MESSAGES_COMMAND);
		ERROR_INCORRECT_NUMBER = errorMessages.getString(JSON_TB_ERROR_MESSAGES_INCORRECT_NUMBER);
		ERROR_DB_UPDATE = errorMessages.getString(JSON_TB_ERROR_MESSAGES_DB_UPDATE);
		ERROR_NO_CURRENT_TB = errorMessages.getString(JSON_TB_ERROR_MESSAGES_NO_CURRENT_TB);
		TOO_MUCH_RESULTS = errorMessages.getString(JSON_TB_TOO_MUCH_RESULTS);
		SQL_ERROR = errorMessages.getString(JSON_SETUP_ERROR_SQL);
		ERROR_ALREADY_LOGGED = errorMessages.getString(JSON_TB_ERROR_ALREADY_LOGGED);
		ERROR_ALREADY_LOGGED_DETAILS = errorMessages.getString(JSON_TB_ERROR_ALREADY_LOGGED_DETAILS);
		ERROR_INCORRECT_MISSION_TIERS = errorMessages.getString(JSON_TB_ERROR_INCORRECT_MISSION_TIERS);
		ERROR_MESSAGE_NO_MISSION_IN_TERRITORY = errorMessages.getString(JSON_TB_ERROR_NO_MISSION_IN_TERRITORY);
	}

	@Override
	public String getCommand() {
		return COMMAND;
	}
	
	
	@Override
	public CommandAnswer answer(DiscordAPI api, List<String> params, Message receivedMessage, boolean isAdmin) {

		//Kick out if not enough params
		if(params.size() == 0) {
			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
		}

		//Kick out if DM
		if(receivedMessage.getChannelReceiver() == null) {
			return new CommandAnswer(ERROR_MESSAGE_NO_CHANNEL, null);
		}
		
		//alert on help
		if( COMMAND_HELP.equalsIgnoreCase(params.get(0)) ) {
			return new CommandAnswer(HELP, null);
		}
		
		Channel channel = new Channel(receivedMessage.getChannelReceiver().getId());
		TBEventLog thisEvent = new TBEventLog();
		thisEvent.loadLastEventLog();
		thisEvent.calculateToday(TimeZone.getDefault());
		
		if( thisEvent.phase < 1 || thisEvent.phase > 6 ) {
			return new CommandAnswer(String.format(ERROR_NO_CURRENT_TB,( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( thisEvent.date.getTime() )),null);
		}
		
		List<Territory> territories = getAllTerritoryMatches(thisEvent.phase);
		
		if( COMMAND_ALERT.equalsIgnoreCase(params.get(0)) ) {
				
			EmbedBuilder embed = new EmbedBuilder();
			Color eColor = Color.RED;
			
			if( params.size() > 1 ) {
				
				if( COMMAND_START.equalsIgnoreCase(params.get(1)) ) {
					
					/** Handle: %tba alert start */
					
					embed.setTitle(String.format(ALERT_PHASE_START_TITLE, thisEvent.phase));
										
					String alertDescription = String.format(ALERT_PHASE_START_DESCRIPTION, thisEvent.phase);
					if( channel.alertRole != null ) {
						//Add a role-mention if alertRole exists for this channel
						alertDescription = "<@&"+channel.alertRole+">\r\n\r\n"+alertDescription;
					}
					
					embed.setDescription(alertDescription);					
					embed.setColor(eColor);

					//Print title
					api.getChannelById(channel.channelID).sendMessage(null,embed);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					//ADD TERRITORY BATTLE ALERTS
					for( Integer t = 0; t < territories.size(); t++ ) {

						//SET COLORED TITLE - CYAN for Airspace, WHITE for ground regular, ORANGE for special mission
						embed.setTitle(territories.get(t).territoryID+" - "+territories.get(t).territoryName);
						embed.setDescription(String.format(ALERT_TERRITORY_START_DESCRIPTION, territories.get(t).territoryID));
						eColor = territories.get(t).specialMission != null ? Color.ORANGE : Color.WHITE;
						eColor = territories.get(t).combatType == 2 ? Color.CYAN : eColor;
						embed.setColor(eColor);
						
						//Print title
						api.getChannelById(channel.channelID).sendMessage(null,embed);
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//ALERT THE PLATOON LOGS
						alertPlatoons( api, receivedMessage, territories.get(t), thisEvent, channel );
						//ALERT THE MISSION LOGS
						alertMissions( api, receivedMessage, territories.get(t), thisEvent, channel );				

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
					
					//ALERT THE HOW-TO
					embed.setTitle(ALERT_LOG_ACTIVITY_TITLE);
					embed.setDescription(ALERT_LOG_ACTIVITY_DESCRIPTION);
					eColor = Color.RED;
					embed.setColor(eColor);

					return new CommandAnswer(null,embed);
					
				}
				
				if( COMMAND_FINISH.equalsIgnoreCase(params.get(1)) ) {
										
					/** Handle: %tba alert finish */
					
					embed.setTitle(String.format(ALERT_PHASE_END_TITLE,thisEvent.phase));
					
					String alertDescription = String.format(ALERT_PHASE_END_DESCRIPTION, thisEvent.phase);
					if( channel.alertRole != null ) {
						//Add a role-mention if alertRole exists for this channel
						alertDescription = "<@&"+channel.alertRole+"> - "+alertDescription;
					}
					
					embed.setDescription(alertDescription);		
					eColor = Color.RED;
					embed.setColor(eColor);
					
					return new CommandAnswer(null,embed);
					
				}
				
			}	
			
			/** Handle: %tba alert */
			
			embed.setAuthor(String.format(MESSAGE_REPORT_TITLE,thisEvent.id,channel.guildID),"","");
			embed.setTitle(String.format(MESSAGE_REPORT_PHASE,thisEvent.name, thisEvent.phase));
			eColor = Color.RED;
			embed.setColor(eColor);
			
			return new CommandAnswer(null,embed);
			
		}
		
		/**
		 * REPORT progress
		 * 
		 * %tba report phase
		 * 
		 */
		if(COMMAND_REPORT.equalsIgnoreCase(params.get(0))) {
			
			/** Handle: %tb report ... */

			EmbedBuilder embed = new EmbedBuilder();					
			embed.setAuthor(String.format(MESSAGE_REPORT_TITLE,thisEvent.id,channel.guildID),"","");
			embed.setTitle(String.format(MESSAGE_REPORT_PHASE,thisEvent.name, thisEvent.phase));
			embed.setDescription("**-**");
			embed.setColor(Color.BLUE);

			if( params.size() == 1 || COMMAND_PHASE.equalsIgnoreCase(params.get(1)) ) {
				
				/** Handle: %tba report */
				/** Handle: %tba report phase <num> */
					
				Integer phase = thisEvent.phase;
								
				if( params.size() > 2 ) {
					try {
						phase = Integer.parseInt(params.get(2));
					} catch( NumberFormatException e ) {
						logger.error(e.getMessage());
						return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
					}
				}

				if( phase < 1 || phase > 6  || phase > thisEvent.phase ) {
					return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
				}
					
				//GET PHASE # REPORT
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
									
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report("full");
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);
								
			}
			

			/** 
			 * Report current platoons
			 * Handle: %tba report platoons
			 *         %tba report platoons phase <num>
			 */
			
			if( COMMAND_PLATOONS.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tba report platoons phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 || phase > thisEvent.phase ) {
							return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
						}

					}

					/** Handle: %tba report platoons HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
					}
											
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report(COMMAND_PLATOONS);
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tba report platoons */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
				}
				
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report(COMMAND_PLATOONS);
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);				
				
			}

			
			 /** Handle: %tba report sm */

			
			if( COMMAND_SPECIAL_MISSION.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tba report sm phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 || phase > thisEvent.phase ) {
							return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
						}
					}

					/** Handle: %tba report sm HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
					}
					
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report(COMMAND_SPECIAL_MISSION);
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tba report sm */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
				}
				
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					if( myLog.get(l).SM1.size() > 0 ) {
						String reportStr = myLog.get(l).report(COMMAND_SPECIAL_MISSION);
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
				}
					
				return new CommandAnswer(null,embed);				
				
			}
			
			 /** Handle: %tba report cm */

			
			if( COMMAND_COMBAT_MISSION.equalsIgnoreCase(params.get(1)) ) {
								
				Integer phase = thisEvent.phase;

				if( params.size() > 2 ) {
					
					if( COMMAND_PHASE.equalsIgnoreCase(params.get(2)) ) {
				
						/** Handle: %tba report cm phase <num> */
							
						if( params.size() == 4 ) {
							try {
								phase = Integer.parseInt(params.get(3));
							} catch( NumberFormatException e ) {
								logger.error(e.getMessage());
								return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
							}
						}
						
						if( phase < 1 || phase > 6 || phase > thisEvent.phase ) {
							return new CommandAnswer(ERROR_MESSAGE_BAD_PHASE,null);
						}
						
					}

					/** Handle: %tba report cm HO3A */
					
					String terrName = params.get(2);
					for( Integer i = 3; i != params.size(); ++i ) {
						terrName += " "+params.get(i);
					}
					
					List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, terrName, 0);
					
					if( myLog.size() == 0 ) {
						return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
					}
					
					for( Integer l = 0; l != myLog.size(); ++l ) {		
						String reportStr = myLog.get(l).report(COMMAND_COMBAT_MISSION);
						reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
						embed.addField(myLog.get(l).territoryID, reportStr, false);
					}
						
					return new CommandAnswer(null,embed);					
				
				}

				/** Handle: %tba report cm */
				
				List<TBTerritoryLog> myLog = new TBTerritoryLog().getLogs(thisEvent.id, channel.guildID, null, phase);
				
				if( myLog.size() == 0 ) {
					return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
				}
				
				for( Integer l = 0; l != myLog.size(); ++l ) {		
					String reportStr = myLog.get(l).report(COMMAND_COMBAT_MISSION);
					reportStr += l < myLog.size()-1 ? "**-**\r\n" : ""; 
					embed.addField(myLog.get(l).territoryID, reportStr, false);
				}
					
				return new CommandAnswer(null,embed);				
				
			}

			
			/** Handle: %tb report HO2A
			 *          %tb report Overlook
			 */
			
			if( params.size() > 1 ) {
				
				String terrString = params.get(1);
				for( Integer p = 2; p != params.size(); ++p ) {
					terrString += " "+params.get(p);
				}
				
				Territory territory = new Territory(terrString);				
				TBTerritoryLog myLog = new TBTerritoryLog(thisEvent.id, channel.guildID, territory.territoryID);
				
				if( !terrString.equalsIgnoreCase(territory.territoryID) ) {
					
					//Ambiguous entry
					if( territory.phase > thisEvent.phase ) {
						return new CommandAnswer(ERROR_MESSAGE_NO_TERRITORY_IN_PHASE,null);
					}
													
					if( !myLog.saved ) { 					
						return new CommandAnswer(ERROR_MESSAGE_NO_LOG,null);
					}
					
				}
	
				String reportStr = myLog.report("full");
				embed.setTitle(territory.territoryName);
				embed.addField(myLog.territoryID, reportStr, false);
					
				return new CommandAnswer(null,embed);
				
			}

			return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
			
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
						return new CommandAnswer(ERROR_MESSAGE_FORBIDDEN, null);
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
						return new CommandAnswer(String.format(MESSAGE_CONFIRMED_PLATOON_LOGGED, terrLog.territoryID, platoon),null);

					}
										
					return new CommandAnswer(ERROR_INCORRECT_NUMBER,null);
					
				}

				
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
			     
				if( COMMAND_SPECIAL_MISSION.equalsIgnoreCase(mType) && mNum == 1 ) {
					
					if( terr.specialMission == null ) { 
						return new CommandAnswer(ERROR_MESSAGE_NO_MISSION_IN_TERRITORY,null);
					}
					
					if( newVal < 0 || newVal > 3) {
						return new CommandAnswer(String.format(ERROR_INCORRECT_MISSION_TIERS, terrLog.territoryID, mission, 3),null);
					}
						
					
					//SM
					String requestUser = receivedMessage.getAuthor().getName();
					if( terrLog.SM1.indexOf(requestUser) >= 0 ) {						
						
						if( terrLog.SM1.get(terrLog.SM1.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
							return new CommandAnswer(String.format(ERROR_ALREADY_LOGGED_DETAILS, terrLog.territoryID, mission),null);
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
					return new CommandAnswer(String.format(MESSAGE_CONFIRMED_MISSION_LOGGED, terrLog.territoryID, receivedMessage.getAuthor().getName(), mission, newVal),null);
					
				} 
										
				if( COMMAND_COMBAT_MISSION.equalsIgnoreCase(mType) ) {
					
					if( mNum > terr.combatMissions || mNum < 1 ) {
						return new CommandAnswer(ERROR_MESSAGE_NO_MISSION_IN_TERRITORY,null);
					}
					
					if( newVal < 0 || newVal > 6) {
						return new CommandAnswer(String.format(ERROR_INCORRECT_MISSION_TIERS, terrLog.territoryID, mission, 6),null);
					}
					
					//CM
					//String requestUser = receivedMessage.getAuthor().getId();
					String requestUser = receivedMessage.getAuthor().getName();

					if( mNum == 1 && terrLog.CM1.contains(requestUser) || mNum == 2 && terrLog.CM2.contains(requestUser) ) {						
						
						String ALERT_UPDATE = "";
						if( mNum == 1 ) {
							
							if( terrLog.CM1.get(terrLog.CM1.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
								return new CommandAnswer(String.format(ERROR_ALREADY_LOGGED_DETAILS, terrLog.territoryID, mission),null);
							}
							
							ALERT_UPDATE = String.format(ALERT_UPDATE_TERRITORY_LOG, terrLog.CM1.get(terrLog.CM1.indexOf(requestUser)+1) , newVal.toString());
							terrLog.CM1.set(terrLog.CM1.indexOf(requestUser)+1, newVal.toString());							
						} else {
							
							if( terrLog.CM2.get(terrLog.CM2.indexOf(requestUser)+1).trim().equals(newVal.toString().trim()) ) {
								return new CommandAnswer(String.format(ERROR_ALREADY_LOGGED_DETAILS, terrLog.territoryID, mission),null);
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
					return new CommandAnswer(String.format(MESSAGE_CONFIRMED_MISSION_LOGGED, terrLog.territoryID, receivedMessage.getAuthor().getName(), mission, newVal),null);
					
				}
					
				return new CommandAnswer(ERROR_MESSAGE_NO_MISSION,null);			
				
			}	
			
			return new CommandAnswer(String.format(ERROR_NO_CURRENT_TB, ( new SimpleDateFormat( "yyyy-MM-dd" ) ).format( lastTBLog.date.getTime() )) ,null);			
			
		}
		
		return new CommandAnswer( ERROR_COMMAND, null );
	}
	
	
	public void alert(DiscordAPI api, List<String> params, Channel channel, boolean isAdmin) {
		
		//Kick out if not enough params
		if(params.size() == 0) {
			api.getChannelById(channel.channelID).sendMessage(ERROR_MESSAGE_PARAMS_NUMBER);
			//return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);
			return;
		}
		
		//Kick out if no channel
		if(channel.channelID == null || channel.channelID.length() == 0) {
			api.getChannelById(channel.channelID).sendMessage(ERROR_MESSAGE_NO_CHANNEL);
			return;
			//return new CommandAnswer(ERROR_MESSAGE_NO_CHANNEL, null);
		}
		
		TBEventLog thisEvent = new TBEventLog();
		thisEvent.loadLastEventLog();
		thisEvent.calculateToday(TimeZone.getDefault());
		
		if( thisEvent.phase < 1 || thisEvent.phase > 6 ) {
			api.getChannelById(channel.channelID).sendMessage(String.format(ERROR_NO_CURRENT_TB,( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( thisEvent.date.getTime() )));
			return;
			//return new CommandAnswer(String.format(ERROR_NO_CURRENT_TB,( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( thisEvent.date.getTime() )),null);
		}
		
		List<Territory> territories = getAllTerritoryMatches(thisEvent.phase);
		
		
		if( COMMAND_ALERT.equalsIgnoreCase(params.get(0)) ) {
		
			EmbedBuilder embed = new EmbedBuilder();
			Color eColor = Color.RED;
			
			if( params.size() > 1 ) {
				
				if( COMMAND_START.equalsIgnoreCase(params.get(1)) ) {
					
					/** Handle: %tba alert start */
					
					embed.setTitle(String.format(ALERT_PHASE_START_TITLE, thisEvent.phase));
					embed.setDescription(String.format(ALERT_PHASE_START_DESCRIPTION, thisEvent.phase));					
					embed.setColor(eColor);

					//Print title
					try {
						api.getChannelById(channel.channelID).sendMessage("-",embed);
						Thread.sleep(1000);
					} catch (InterruptedException | NullPointerException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

					
					//ADD TERRITORY BATTLE ALERTS
					for( Integer t = 0; t < territories.size(); t++ ) {

						//SET COLORED TITLE - CYAN for Airspace, WHITE for ground regular, ORANGE for special mission
						embed.setTitle(territories.get(t).territoryID+" - "+territories.get(t).territoryName);
						embed.setDescription(String.format(ALERT_TERRITORY_START_DESCRIPTION, territories.get(t).territoryID));
						eColor = territories.get(t).specialMission != null ? Color.ORANGE : Color.WHITE;
						eColor = territories.get(t).combatType == 2 ? Color.CYAN : eColor;
						embed.setColor(eColor);
						
						//Print title
						api.getChannelById(channel.channelID).sendMessage(null,embed);
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//ALERT THE PLATOON LOGS
						alertPlatoons( api, null, territories.get(t), thisEvent, channel );
						//ALERT THE MISSION LOGS
						alertMissions( api, null, territories.get(t), thisEvent, channel );				

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
					
					//ALERT THE HOW-TO
					embed.setTitle(ALERT_LOG_ACTIVITY_TITLE);
					embed.setDescription(ALERT_LOG_ACTIVITY_DESCRIPTION);
					eColor = Color.RED;
					embed.setColor(eColor);

					api.getChannelById(channel.channelID).sendMessage(null,embed);
					return;

					//return new CommandAnswer(null,embed);
					
				}
				
				if( COMMAND_FINISH.equalsIgnoreCase(params.get(1)) ) {
										
					/** Handle: %tba alert finish */
					
					embed.setTitle(String.format(ALERT_PHASE_END_TITLE,thisEvent.phase));
					embed.setDescription(ALERT_PHASE_END_DESCRIPTION);
					eColor = Color.RED;
					embed.setColor(eColor);
					
					api.getChannelById(channel.channelID).sendMessage(null,embed);
					return;
					
					//return new CommandAnswer(null,embed);
					
				}
				
			}	
			
			/** Handle: %tba alert */
			
			embed.setAuthor(String.format(MESSAGE_REPORT_TITLE,thisEvent.id,channel.guildID),"","");
			embed.setTitle(String.format(MESSAGE_REPORT_PHASE,thisEvent.name, thisEvent.phase));
			eColor = Color.RED;
			embed.setColor(eColor);
			
			api.getChannelById(channel.channelID).sendMessage(null,embed);
			return;
			//return new CommandAnswer(null,embed);
			
		}
	
		api.getChannelById(channel.channelID).sendMessage(ERROR_MESSAGE_PARAMS_NUMBER);
		return;
		//return new CommandAnswer(ERROR_MESSAGE_PARAMS_NUMBER,null);

	}
	

	/**
	 * Updates the appropriate log via emoji
	 * @param user
	 * @param reaction
	 * @param logID
	 * @param messageID
	 * @param missionType
	 * @return
	 */
	public void executeLogUpdate(ImplUser user, ImplReaction reaction, Integer logID, String messageID, String missionType ) {
		
		if( messageID == null || reaction.getMessage().getId() == null ) {
			return;
		}
		
		if( messageID.equals(reaction.getMessage().getId()) ) {
			if( missionType == "p" ) {
				executePlatoonEmojiUpdate(user, reaction, logID, messageID);				
			} else {
				executeMissionEmojiUpdate(user, reaction, logID, messageID, missionType);
			}
		}
	}	
	
	/**
	 * Updates the mission via emoji logging
	 * @param user
	 * @param reaction
	 * @param logID
	 * @param messageID
	 * @param missionType
	 * @return
	 */
	public void executeMissionEmojiUpdate(ImplUser user, ImplReaction reaction, Integer logID, String messageID, String missionType) {

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
		String terrID = "```".equals(reaction.getMessage().getContent().substring(0,3)) ? reaction.getMessage().getContent().substring(3, 7) : reaction.getMessage().getContent().substring(0, 4);
		TBTerritoryLog log = new TBTerritoryLog(logID, channel.guildID, terrID);
				
		List<String> mission = missionType.equalsIgnoreCase("sm") ? log.SM1 : log.CM1;
		mission = missionType.equalsIgnoreCase("cm2") ? log.CM2 : mission;
		
		String player = user.getName();
		Integer tier = emojis.indexOf(reaction.getUnicodeEmoji())+1;
		
		logger.info( player+" : "+log.logID.toString()+"."+log.guildID.toString()+"."+log.territoryID+"."+missionType+"."+reaction.getUnicodeEmoji() );

		try { 
			
			Thread.sleep(5000);
			
			if( mission.contains(player) ) {
				
				Thread.sleep(500);
				user.sendMessage(String.format(ERROR_ALREADY_LOGGED_DETAILS,log.territoryID,missionType));
				return;
			}
				
			mission.add(player);
			mission.add(tier.toString().trim());
	
			if( missionType.equalsIgnoreCase("sm") ) {
				if( tier > 3 ) { 
				
					Thread.sleep(500);
					user.sendMessage(String.format(ERROR_INCORRECT_MISSION_TIERS,log.territoryID,missionType));
					return; 
				}
				log.SM1 = mission;
			} else if( missionType.equalsIgnoreCase("cm1") ) {
				if( log.territoryID.toLowerCase().charAt(log.territoryID.length()-1) == 'a' && log.phase > 2 && tier > 3 ) { 
					
					Thread.sleep(500);
					user.sendMessage(String.format(ERROR_INCORRECT_MISSION_TIERS,log.territoryID,missionType));
					return; 
				}
				log.CM1 = mission;
			} else if( missionType.equalsIgnoreCase("cm2") ) {
				if( log.territoryID.toLowerCase().charAt(log.territoryID.length()-1) == 'a' && log.phase > 2 && tier > 3 ) { 
					
					Thread.sleep(500);
					user.sendMessage(String.format(ERROR_INCORRECT_MISSION_TIERS,log.territoryID,missionType));
					return; 
				}
				log.CM2 = mission;
			}
	
			log.saveLog();
			Thread.sleep(500);
			
			if( !log.saved ) {
				user.sendMessage(ERROR_DB_UPDATE);
				return;
			} else {				
				reaction.getMessage().getChannelReceiver().sendMessage(String.format(MESSAGE_CONFIRMED_MISSION_LOGGED, log.territoryID, player, missionType, tier));
			}
		} catch( InterruptedException e ) {
			logger.error(e.getMessage());
			return;
		}
	}
	
	/**
	 * Updates the platoon via emoji logging
	 * @param user
	 * @param reaction
	 * @param logID
	 * @param messageID
	 * @return
	 */
	public void executePlatoonEmojiUpdate(User user, ImplReaction reaction, Integer logID, String messageID) {

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
		String terrID = "```".equals(reaction.getMessage().getContent().substring(0,3)) ? reaction.getMessage().getContent().substring(3, 7) : reaction.getMessage().getContent().substring(0, 4);
		
		TBTerritoryLog log = new TBTerritoryLog(logID, channel.guildID, terrID);
				
		//Platoon # is the indexOf +1
		Integer platoon = emojis.indexOf(reaction.getUnicodeEmoji())+1;

		char[] platoons = log.platoons.toCharArray();
		char flag = 'Y';
		
		//IF it already equals 'Y'
		if( platoons[platoon-1] == flag ) {
			user.sendMessage(String.format(ERROR_ALREADY_LOGGED,log.territoryID,platoon));
			return;
		}
		
		String player = user.getName();
		logger.info( player+" : "+log.logID.toString()+"."+log.guildID.toString()+"."+log.territoryID+".platoon."+reaction.getUnicodeEmoji() );

		platoons[platoon-1] = flag;
		log.platoons = String.copyValueOf(platoons);
		
		try { 		
			if( !log.saveLog() ) {
				user.sendMessage(ERROR_DB_UPDATE);
				return;
			} else {
				Thread.sleep(250);
				reaction.getMessage().getChannelReceiver().sendMessage(String.format(MESSAGE_CONFIRMED_PLATOON_LOGGED, log.territoryID, platoon));
			}
		} catch( InterruptedException e ) {
			logger.error(e.getMessage());
			return;
		}
		
	}
		
	/**
	 * Updates a log by command
	 * @param reaction
	 * @param log
	 * @return
	 */
	public String executeTerritoryLogUpdate(ImplReaction reaction,TBTerritoryLog log) {

		String emojiX = EmojiManager.getForAlias("x").getUnicode();
		String emojiV = EmojiManager.getForAlias("white_check_mark").getUnicode();

		if(emojiX.equals(reaction.getUnicodeEmoji())) {
			return CANCEL_MESSAGE;
		}

		if(emojiV.equals(reaction.getUnicodeEmoji())) {			
			return log.saveLog() ? UPDATE_LOG_OK : SQL_ERROR;		
		}
		
		return null;
	}

	private void alertPlatoons( DiscordAPI api, Message receivedMessage, Territory territory, TBEventLog thisEvent, Channel channel ) {
		
		String missionType = "p";
		
		Future<Message> future = api.getChannelById(channel.channelID).sendMessage(String.format(MESSAGE_PLATOON_LOG, territory.territoryID));

		try {
			Message sentMessage = null;
			sentMessage = future.get(1, TimeUnit.MINUTES);
			Message reactionMessage = receivedMessage != null ? receivedMessage : sentMessage; 
			
			JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,reactionMessage,24,thisEvent.id,sentMessage.getId(),missionType));				

			Thread.sleep(800);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	private void alertMissions( DiscordAPI api, Message receivedMessage, Territory territory, TBEventLog thisEvent, Channel channel ) {
		
		for( Integer cm = 1; cm <= territory.combatMissions; ++cm ) {
			
			String missionType = "cm"+cm;
		
			Future<Message> future = api.getChannelById(channel.channelID).sendMessage(String.format(MESSAGE_COMBAT_MISSION_LOG, territory.territoryID, cm));

			try {
				Message sentMessage = null;
				sentMessage = future.get(1, TimeUnit.MINUTES);		
				Message reactionMessage = receivedMessage != null ? receivedMessage : sentMessage; 

				JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,reactionMessage,24,thisEvent.id,sentMessage.getId(),missionType));				

				Thread.sleep(800);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}

		
		if( territory.specialMission != null ) {

			String missionType = "sm";
			
			Future<Message> future = api.getChannelById(channel.channelID).sendMessage(String.format(MESSAGE_SPECIAL_MISSION_LOG, territory.territoryID));

			try {
				Message sentMessage = null;
				sentMessage = future.get(1, TimeUnit.MINUTES);					
				JediStarBotMultiReactionAddListener.addPendingMultiAction(new PendingMultiAction(api.getYourself(),"executeLogUpdate",this,receivedMessage,24,thisEvent.id,sentMessage.getId(),missionType));				

				Thread.sleep(800);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
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
				  
				  matches.add( new Territory(rs.getString("territoryID"),rs.getString("territoryName"),rs.getString("tbName"),rs.getInt("phase"),rs.getInt("combatType"),rs.getInt("starPoints1"),rs.getInt("starPoints2"),rs.getInt("starPoints3"),rs.getString("ability"),rs.getString("affectedTerritories"),rs.getString("requiredUnits"),rs.getString("specialMission"),rs.getInt("specialPoints"),rs.getInt("combatMissions"),rs.getInt("missionPoints1"),rs.getInt("missionPoints2"),rs.getInt("missionPoints3"),rs.getInt("missionPoints4"),rs.getInt("missionPoints5"),rs.getInt("missionPoints6"),rs.getInt("platoonPoints1"),rs.getInt("platoonPoints2"),rs.getInt("platoonPoints3"),rs.getInt("platoonPoints4"),rs.getInt("platoonPoints5"),rs.getInt("platoonPoints6"),rs.getInt("minDeployStar1"),rs.getInt("minDeployStar2"),rs.getInt("minDeployStar3"),rs.getInt("minGPStar3"),rs.getString("notes") ) );
				  
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

}
