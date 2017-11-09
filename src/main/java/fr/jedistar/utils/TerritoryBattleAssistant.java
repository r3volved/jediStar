package fr.jedistar.utils;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.Main;
import fr.jedistar.StaticVars;
import fr.jedistar.classes.Channel;
import fr.jedistar.classes.TBEventLog;
import fr.jedistar.classes.Territory;
import fr.jedistar.commands.TBAssistantCommand;
import fr.jedistar.formats.CommandAnswer;

public class TerritoryBattleAssistant implements Runnable {

	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final String SELECT_WEBHOOK_REQUEST = "SELECT * FROM guild WHERE tbAssistant=1";
	private static final String SELECT_PHASE_TERRITORIES_REQUEST = "SELECT * FROM territoryData WHERE phase=?";
	
	private Thread t;
	private String threadName;
	private Timer reset = new Timer();
	DiscordAPI api;
	
	public TBEventLog eventLog;	
	public TimeZone eventTimeZone;
	
	public TerritoryBattleAssistant( String name ) {

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		this.eventTimeZone = TimeZone.getTimeZone("UTC");
		this.eventLog = new TBEventLog(0,Calendar.getInstance(),0,"Hoth - Imperial Invasion");
		this.threadName = name;		
		logger.info("Creating "+this.threadName);

	}
	
	public void run() {
	
		try {

			logger.debug( "Today:"+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( Calendar.getInstance(eventTimeZone).getTime() ) );

			this.eventLog.loadLastEventLog();
			this.eventLog.calculateToday(this.eventTimeZone);
			logger.info("Calculated phase: "+this.eventLog.phase);
	
			//Ensure the timer start date has the time set to 15:00 UTC:+0:00
			Calendar timerInitDateTime = this.eventLog.date;
			timerInitDateTime.setTimeZone(this.eventTimeZone);
			timerInitDateTime.set(Calendar.HOUR_OF_DAY, 17);
			timerInitDateTime.set(Calendar.MINUTE, 0);
			timerInitDateTime.set(Calendar.SECOND, 0);

			reset.scheduleAtFixedRate(new resetPhase(), timerInitDateTime.getTime(), 1000*60*60*24);
			logger.info( "Next start alert: "+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( timerInitDateTime.getTime() ) );
			
			timerInitDateTime.add(Calendar.DAY_OF_YEAR, 1);
			timerInitDateTime.set(Calendar.HOUR_OF_DAY, 16);
			reset.scheduleAtFixedRate(new alertEnding(), timerInitDateTime.getTime(), 1000*60*60*24);
			logger.info( "Next end alert:   "+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( timerInitDateTime.getTime() ) );
						
	        Thread.sleep(500);
	    } catch(InterruptedException e) {
	    	logger.warn("Thread " +  this.threadName + " interrupted.");
	    	e.printStackTrace();
	    }  
	}
	
	public void start(DiscordAPI api) {
	  if (t == null) {
		 this.api = api;
         t = new Thread (this, threadName);
         
         logger.info("Launching "+this.threadName);
         t.start ();         
      }
	}
	
	private class resetPhase extends TimerTask {
		@Override
		public void run() {
			
            try {
            	logger.debug("Ending phase "+eventLog.phase);            	
            	if( ++eventLog.phase > 6 ) {

            		//TB HAS ENDED, GET THE COOLDOWN
            		Integer offset = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK) == 0 ? 3 : 4;
            		eventLog.date.add(Calendar.DATE, offset);
            		eventLog.phase = 0;
            		
            		//Schedule the next one
            		eventLog.saveNewLog();            		
            		eventLog.phase = offset*-1;
            		
            		logger.debug( "Territory battle cooling down "+eventLog.phase+" days");
            	}
            	
            	if( eventLog.phase > 0 ) {
            		
            		//TB PHASE HAS STARTED, SEND START TRIGGERS
            		logger.debug( "Phase "+eventLog.phase+" started");
            		eventLog.date.add(Calendar.DATE, 1);            		
            		sendStartTriggers( eventLog.phase );
            		
            	}
            	
                Thread.sleep(5);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
		}
	}

	
	private class alertEnding extends TimerTask {
		@Override
		public void run() {

			sendEndingTriggers( eventLog.phase );
		}
	}
	
	
/*	public boolean sendStartAlerts( Integer phase ) {

		
		CommandAnswer answer = TBAssistantCommand.answer(api,messageParts,receivedMessage,isAdmin);
		
		if(answer == null) {
			return;
		}
		
		String message ="";		
		if(!"".equals(answer.getMessage())) {
			message = String.format(MESSAGE, receivedMessage.getAuthor().getMentionTag(),answer.getMessage());
		}
		
		EmbedBuilder embed = answer.getEmbed();
		
		if(embed != null) {
			embed.addField("-","Bot designed by [JediStar](https://jedistar.jimdo.com)", false);
		}
		
		Future<Message> future = receivedMessage.reply(message, embed);
		
		if(answer.getReactions() != null && !answer.getReactions().isEmpty()) {
			Message sentMessage = null;
			try {
				sentMessage = future.get(1, TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				return;
			}
							
			for(String reaction : answer.getReactions()) {
				try {
					sentMessage.addUnicodeReaction(reaction).get(1, TimeUnit.MINUTES);
					Thread.sleep(250);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
					return;
				}
			}
			
		}
		
	}
	
*/

	public boolean sendStartTriggers( Integer phase ) {
		List<Channel> activeChannels = getWebhooks();
		JSONObject alert = new JSONObject();

		String msg = "%tba alert start";
		alert.put("username", "Territory Battle Assistant");
		alert.put("content", msg);

		for( Integer i = 0; i != activeChannels.size(); ++i ) {
			try { 
				sendPOST( activeChannels.get(i).webhook, alert );
				Thread.sleep(500);
			} catch(InterruptedException | IOException e) {
				logger.error("channel send: "+e.getMessage());
			}

		}
		
		return true;
	}
	
	public boolean sendEndingTriggers( Integer phase ) {
		List<Channel> activeChannels = getWebhooks();
		JSONObject alert = new JSONObject();

		String msg = "%tba alert finish";
		alert.put("username", "Territory Battle Assistant");
		alert.put("content", msg);

		for( Integer i = 0; i != activeChannels.size(); ++i ) {
			try { 
				sendPOST( activeChannels.get(i).webhook, alert );
				Thread.sleep(500);
			} catch(InterruptedException | IOException e) {
				logger.error("channel send: "+e.getMessage());
			}

		}
		
		return true;
	}
	
	
	private static void sendPOST( String postUrl, JSONObject jsonToPost ) throws IOException {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
			
		    HttpPost request = new HttpPost(postUrl);
		    StringEntity params = new StringEntity(jsonToPost.toString());
		    params.setContentType("application/json");
		    request.setEntity(params);
		    request.addHeader("Content-Type", "application/json; charset=utf-8");
		    
		    HttpResponse response = httpClient.execute(request);
		    
		    logger.debug("Webhook request:  "+jsonToPost.toString());
		    logger.debug("Webhook response: "+response.toString());

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
		    httpClient.close();
		}
	}

	public List<Channel> getWebhooks() {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_WEBHOOK_REQUEST);
						
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			List<Channel> activeWebhooks = new ArrayList<Channel>();
			
			while(rs.next()) {
				
				activeWebhooks.add(new Channel(rs.getString("channelID")));
			}
			
			return activeWebhooks;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
		finally {

			try {
				if(rs != null) { rs.close(); }
				if(stmt != null) { stmt.close(); }				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}		
	}
	
    public List<Territory> getTerritories( Integer phase ) {
    	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_PHASE_TERRITORIES_REQUEST);
			stmt.setInt(1, phase);
			
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			List<Territory> territories = new ArrayList<Territory>();
			while(rs.next()) {				
				territories.add( new Territory(rs.getString("territoryID"),rs.getString("territoryName"),rs.getString("tbName"),rs.getInt("phase"),rs.getInt("combatType"),rs.getInt("starPoints1"),rs.getInt("starPoints2"),rs.getInt("starPoints3"),rs.getString("ability"),rs.getString("affectedTerritories"),rs.getString("requiredUnits"),rs.getString("specialMission"),rs.getInt("specialPoints"),rs.getInt("combatMissions"),rs.getInt("missionPoints1"),rs.getInt("missionPoints2"),rs.getInt("missionPoints3"),rs.getInt("missionPoints4"),rs.getInt("missionPoints5"),rs.getInt("missionPoints6"),rs.getInt("platoonPoints1"),rs.getInt("platoonPoints2"),rs.getInt("platoonPoints3"),rs.getInt("platoonPoints4"),rs.getInt("platoonPoints5"),rs.getInt("platoonPoints6"),rs.getInt("minDeployStar1"),rs.getInt("minDeployStar2"),rs.getInt("minDeployStar3"),rs.getInt("minGPStar3"),rs.getString("notes")) );
			}		
			return territories;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			return null;
		}
		finally {

			try {
				if(rs != null) { rs.close(); }			
				if(stmt != null) { stmt.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}		
	}

}