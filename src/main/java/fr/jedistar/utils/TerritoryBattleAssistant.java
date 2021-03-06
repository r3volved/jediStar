package fr.jedistar.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.DiscordAPI;
import fr.jedistar.Main;
import fr.jedistar.StaticVars;
import fr.jedistar.classes.Channel;
import fr.jedistar.classes.TBEventLog;
import fr.jedistar.classes.Territory;

public class TerritoryBattleAssistant implements Runnable {

	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final String SELECT_WEBHOOK_REQUEST = "SELECT * FROM guild WHERE tbAssistant=1";
	private static final String SELECT_PHASE_TERRITORIES_REQUEST = "SELECT * FROM territoryData WHERE phase=?";
	
	private Thread t;
	private String threadName;
	private Timer reset = new Timer();
	private DiscordAPI api = null;
	
	public TBEventLog eventLog;	
	public TimeZone eventTimeZone;
	
	public TerritoryBattleAssistant( String name ) {

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		this.eventTimeZone = TimeZone.getTimeZone("UTC");
		this.eventLog = new TBEventLog(0,Calendar.getInstance(),0,"Hoth - Imperial Invasion");
		this.threadName = name;		

	}
	
	@Override
	public void run() {
	
		try {

			//Load last eventLog and calculate what today should be
			this.eventLog.loadLastEventLog();
			this.eventLog.calculateToday(this.eventTimeZone);
			logger.info("Calculated phase: "+this.eventLog.phase);
	
			//Ensure the timer start date has the time set
			Calendar timerInitDateTime = this.eventLog.date;
			timerInitDateTime.setTimeZone(this.eventTimeZone);
			timerInitDateTime.set(Calendar.HOUR_OF_DAY, 17);
			timerInitDateTime.set(Calendar.MINUTE, 0);
			timerInitDateTime.set(Calendar.SECOND, 0);

			//Schedule the timer phase-start timer to trigger
			reset.scheduleAtFixedRate(new resetPhase(), timerInitDateTime.getTime(), 1000*60*60*24);
			logger.info( "Next start alert: "+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( timerInitDateTime.getTime() ) );
			
			//Add 23 hours
			timerInitDateTime.add(Calendar.DAY_OF_YEAR, 1);
			timerInitDateTime.set(Calendar.HOUR_OF_DAY, 16);
			
			//Set 1-hour remaining timer to trigger one hour before reset
			reset.scheduleAtFixedRate(new alertEnding(), timerInitDateTime.getTime(), 1000*60*60*24);
			logger.info( "Next end alert:   "+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( timerInitDateTime.getTime() ) );
						
	        
	        
			// TESTING
			//Thread.sleep(5000);
			//sendWebhookTrigger( "%tba alert finish" );

	    } catch(Exception e) {
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

            		//Territory battles have ended, so schedule the next one
            		Integer offset = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK) == 0 ? 3 : 4;
            		eventLog.date.add(Calendar.DATE, offset);
            		eventLog.phase = 0;
            		eventLog.saveNewLog();            		

            		//Set cooldown
            		eventLog.phase = offset*-1;
            		
            		logger.debug( "Territory battle cooling down "+eventLog.phase+" days");
            	}
            	
            	if( eventLog.phase > 0 ) {
            		
            		//Territory battles phase has started, send the alert triggers
            		logger.debug( "Phase "+eventLog.phase+" started");
            		eventLog.date.add(Calendar.DATE, 1);            		
            		sendWebhookTrigger( "%tba alert start" );

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

			//Ensure end-warning alert doesn't trigger out-of-phase
			eventLog.calculateToday(eventTimeZone);
			if( eventLog.phase > 0 && eventLog.phase <= 6 ) {
				sendWebhookTrigger( "%tba alert finish" );
			}
		}
	}
	
	
	public void sendWebhookTrigger( String trigger ) {
		List<Channel> activeChannels = getActiveChannels();
		JSONObject alert = new JSONObject();

		alert.put("username", "Territory Battle Assistant");
		alert.put("content", trigger);

		for( Integer i = 0; i != activeChannels.size(); ++i ) {
			try { 
				sendPOST( activeChannels.get(i).webhook, alert );
				Thread.sleep(500);
			} catch(InterruptedException | IOException e) {
				logger.error("channel send: "+e.getMessage());				
			}
		}
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

	public List<Channel> getActiveChannels() {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_WEBHOOK_REQUEST);
						
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			List<Channel> activeChannels = new ArrayList<Channel>();
			
			while(rs.next()) {
				
				activeChannels.add(new Channel(rs.getString("channelID")));
			}
			
			return activeChannels;
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
				return null;
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