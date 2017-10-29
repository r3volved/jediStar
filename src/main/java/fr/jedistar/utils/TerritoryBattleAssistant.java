package fr.jedistar.utils;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
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
import de.btobastian.javacord.ImplDiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplUser;
import de.btobastian.javacord.entities.message.embed.Embed;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import fr.jedistar.Main;
import fr.jedistar.StaticVars;
import fr.jedistar.classes.Channel;

public class TerritoryBattleAssistant implements Runnable {

	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final String SELECT_WEBHOOK_REQUEST = "SELECT webhook FROM guild WHERE tbAssistant=1";
	private static final String INSERT_EVENTLOG_REQUEST = "INSERT INTO tbEventLog VALUES(?,?,?,?)";
	private static final String SELECT_LAST_EVENTLOG_REQUEST = "SELECT * FROM tbEventLog ORDER BY startDate DESC LIMIT 1";
	private static final String SELECT_PHASE_TERRITORIES_REQUEST = "SELECT * FROM territoryData WHERE phase=?";
	
	private Thread t;
	private String threadName;
	private Timer reset = new Timer();
	
	public TBEventLog eventLog;	
	public TimeZone eventTimeZone;
	
	public TerritoryBattleAssistant( String name ) {

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		this.eventTimeZone = TimeZone.getTimeZone("UTC");		
		this.eventLog = new TBEventLog(0,Calendar.getInstance(),0,"Hoth - Imperial Invasion");
		this.threadName = name;		
		logger.info("Starting "+this.threadName);

	}
	
	public void run() {
	
		try {

			logger.debug( "Today:"+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( Calendar.getInstance(eventTimeZone).getTime() ) );

			this.eventLog.loadLastEventLog();
			this.eventLog.calculateToday();
			
			//Ensure the timer start date has the time set to 15:00 UTC:+0:00
			Calendar timerInitDateTime = this.eventLog.date;
			timerInitDateTime.setTimeZone(this.eventTimeZone);
			timerInitDateTime.set(Calendar.HOUR_OF_DAY, 17);
			timerInitDateTime.set(Calendar.MINUTE, 0);
			timerInitDateTime.set(Calendar.SECOND, 0);

			reset.scheduleAtFixedRate(new resetPhase(), timerInitDateTime.getTime(), 1000*60*60*24);
			logger.debug( "Timer:"+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( timerInitDateTime.getTime() ) );
			
			logger.info( this.threadName+" started and scheduled successfully");
	        Thread.sleep(500);
	    } catch(InterruptedException e) {
	    	logger.warn("Thread " +  this.threadName + " interrupted.");
	    	e.printStackTrace();
	    }  
	}
	
	public void start() {
	  if (t == null) {
         t = new Thread (this, threadName);
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
            		Integer offset = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 0 ? 3 : 4;
            		eventLog.date.add(Calendar.DATE, offset);
            		eventLog.phase = 0;
            		
            		//Schedule the next one
            		eventLog.saveNewLog();
            		
            		eventLog.phase = offset*-1;
            		
            		logger.debug( "Territory battle cooling down "+eventLog.phase+" days");
            	}
            	
            	if( eventLog.phase > 0 ) {
            		logger.debug( "Phase "+eventLog.phase+" started");
            		eventLog.date.add(Calendar.DATE, 1);            		
            		sendAlerts( eventLog.phase );
            		
            	}
                Thread.sleep(5);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
		}
	}
	
	public boolean sendAlerts( Integer phase ) {
		
		List<String> activeWebhooks = getWebhooks();
		List<Territory> territories = getTerritories(phase);
		
		EmbedBuilder embed = new EmbedBuilder();
		JSONArray embeds = new JSONArray();
		JSONObject alert = new JSONObject();
				
		//ADD TERRITORY BATTLE ALERTS
		
		/*
		 * NEED TO REFACTOR THIS FOR LANGUAGES
		 */
		
		for( Integer t = 0; t < territories.size(); t++ ) {
			
			if( territories.get(t).specialMission != null ) {
				embed.setTitle(territories.get(t).territoryID+" : "+territories.get(t).territoryName);
				embed.setDescription("SM1 : Special Mission\r\n*"+territories.get(t).specialMission+"*");
				embed.setColor(Color.ORANGE);
				embeds.put(embed.toJSONObject());
			}
			
			for( Integer cm = 0; cm < territories.get(t).combatMissions; ++cm ) {
				embed.setTitle(territories.get(t).territoryID+" : "+territories.get(t).territoryName);
				embed.setDescription("CM"+(cm+1)+" : Combat Mission");
				if( territories.get(t).combatType == 1 ) {
					embed.setColor(Color.WHITE);
				} else {
					embed.setColor(Color.CYAN);
				}
				embeds.put(embed.toJSONObject());
			}
		}
		
		String msg = "```css\r\n"+territories.get(0).tbName+"\r\nPhase "+phase+" has started```";
		
		alert.put("username", "Territory Battle Assistant");
		alert.put("content", msg);
		alert.put("embeds", embeds );

		for( Integer i = 0; i != activeWebhooks.size(); ++i ) {
			try { 
				sendPOST( activeWebhooks.get(i), alert );
			} catch(IOException e) {
				logger.error(e.getMessage());
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
		    request.addHeader("content-type", "application/json");
		    request.addHeader("Accept","application/json");
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		    //logger.debug("Webhook request:  "+jsonToPost.toString());
		    //logger.debug("Webhook response: "+response.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
		    httpClient.close();
		}
	}

	public List<String> getWebhooks() {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_WEBHOOK_REQUEST);
						
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			List<String> activeWebhooks = new ArrayList<String>();
			
			while(rs.next()) {
				
				activeWebhooks.add(rs.getString("webhook"));
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
				territories.add( new Territory(rs.getString("territoryID"),rs.getString("territoryName"),rs.getString("tbName"),rs.getInt("phase"),rs.getInt("combatType"),rs.getInt("starPoints1"),rs.getInt("starPoints2"),rs.getInt("starPoints3"),rs.getString("ability"),rs.getString("affectedTerritories"),rs.getString("requiredUnits"),rs.getString("specialMission"),rs.getInt("combatMissions"),rs.getInt("missionPoints1"),rs.getInt("missionPoints2"),rs.getInt("missionPoints3"),rs.getInt("missionPoints4"),rs.getInt("missionPoints5"),rs.getInt("missionPoints6"),rs.getInt("platoonPoints1"),rs.getInt("platoonPoints2"),rs.getInt("platoonPoints3"),rs.getInt("platoonPoints4"),rs.getInt("platoonPoints5"),rs.getInt("platoonPoints6"),rs.getInt("minDeployStar1"),rs.getInt("minDeployStar2"),rs.getInt("minDeployStar3"),rs.getInt("minGPStar3"),rs.getString("notes")) );
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

	public class TBEventLog {
	  public Integer id;
	  public Calendar date = Calendar.getInstance();
	  public Integer phase;
	  public String name;

	  public TBEventLog(Integer id, Calendar date, Integer phase, String name) {
		  this.id = id;
		  this.date = date;
		  this.phase = phase;
		  this.name = name;
	  }
	  
	  public boolean saveNewLog() {
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try {
				conn = StaticVars.getJdbcConnection();

				stmt = conn.prepareStatement(INSERT_EVENTLOG_REQUEST);
				
				stmt.setInt(1, ++this.id);
				stmt.setDate(2, new Date(this.date.getTimeInMillis()));
				stmt.setInt(3, this.phase);
				stmt.setString(4, this.name);
				
				logger.debug("Executing query : "+stmt.toString());
				stmt.executeUpdate();
				
				return true;
			}
			catch(SQLException e) {
				logger.error(e.getMessage());
				return false;
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
	  
	  public boolean loadLastEventLog() {
			
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try {
				conn = StaticVars.getJdbcConnection();

				stmt = conn.prepareStatement(SELECT_LAST_EVENTLOG_REQUEST);
							
				logger.debug("Executing query : "+stmt.toString());
				rs = stmt.executeQuery();
				
				if(rs.next()) {
					
					this.id = rs.getInt("id");
					this.date = Calendar.getInstance();
					this.date.setTime(rs.getDate("startDate"));					
					this.phase = rs.getInt("phase");
					this.name = rs.getString("tbName");
					logger.debug( "Last: "+( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" ) ).format( this.date.getTime() ) );			
					
					return true;
				}
				else {
					return false;
				}
			}
			catch(SQLException e) {
				logger.error(e.getMessage());
				return false;
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

	    public boolean calculateToday() {
	    	
	    	Calendar today = Calendar.getInstance(eventTimeZone);
	    	
	    	Integer phase = 0;
	    	
	    	if( today.after( this.date ) ) {

	    		Long diff = today.getTimeInMillis() - this.date.getTimeInMillis();
	    		phase = Integer.parseInt( diff.toString() );
	    		phase = phase / 1000 / 60 / 60 / 24; 

	    		if( phase <= 6 ) {

	            	//IN-PHASE
	            	this.date = today;
	            	this.phase = phase;
	            	
	            } else {
	            	
	            	//TB IS OVER BUT NO NEXT SCHEDULED SO SCHEDULE
	            	logger.warn("Scheduling problem - rescheduling::"+this.phase);
	            	Calendar c = this.date;
	            	c.add(Calendar.DATE, (6-this.phase));
            		Integer offset = c.get(Calendar.DAY_OF_WEEK) == 0 ? 3 : 4;
            		c.add(Calendar.DATE, offset);
            		
            		//UPDATE OBJECT TO NEW AND SAVE
	            	this.date = c;
	            	this.phase = 0;
	            	this.saveNewLog();

	            }
	    	}    	

	    	return true;
	    }
	}

	public class Territory {
		  public String territoryID;
		  public String territoryName;
		  public String tbName;
		  public Integer phase;
		  public Integer combatType;
		  public Integer[] starPoints = { 0, 0, 0, 0};
		  public String ability;
		  public String affectedTerritories;
		  public String requiredUnits;
		  public String specialMission;
		  public Integer combatMissions;
		  public Integer[] missionPoints = { 0, 0, 0, 0, 0, 0, 0 };
		  public Integer[] platoonPoints = { 0, 0, 0, 0, 0, 0, 0 };
		  public Integer[] minDeployStar = { 0, 0, 0, 0 };
		  public Integer minGPStar3;
		  public String notes;
		  
		  public Territory(String territoryID,String territoryName,String tbName,Integer phase,Integer combatType,Integer starPoints1,Integer starPoints2,Integer starPoints3,String ability,String affectedTerritories,String requiredUnits,String specialMission,Integer combatMissions,Integer missionPoints1,Integer missionPoints2,Integer missionPoints3,Integer missionPoints4,Integer missionPoints5,Integer missionPoints6,Integer platoonPoints1,Integer platoonPoints2,Integer platoonPoints3,Integer platoonPoints4,Integer platoonPoints5,Integer platoonPoints6,Integer minDeployStar1,Integer minDeployStar2,Integer minDeployStar3,Integer minGPStar3,String notes) {
			  this.territoryID=territoryID;
			  this.territoryName=territoryName;
			  this.tbName=tbName;
			  this.phase=phase;
			  this.combatType=combatType;
			  this.starPoints[1]=starPoints1;
			  this.starPoints[2]=starPoints2;
			  this.starPoints[3]=starPoints3;
			  this.ability=ability;
			  this.affectedTerritories=affectedTerritories;
			  this.requiredUnits=requiredUnits;
			  this.specialMission=specialMission;
			  this.combatMissions=combatMissions;
			  this.missionPoints[1]=missionPoints1;
			  this.missionPoints[2]=missionPoints2;
			  this.missionPoints[3]=missionPoints3;
			  this.missionPoints[4]=missionPoints4;
			  this.missionPoints[5]=missionPoints5;
			  this.missionPoints[6]=missionPoints6;
			  this.platoonPoints[1]=platoonPoints1;
			  this.platoonPoints[2]=platoonPoints2;
			  this.platoonPoints[3]=platoonPoints3;
			  this.platoonPoints[4]=platoonPoints4;
			  this.platoonPoints[5]=platoonPoints5;
			  this.platoonPoints[6]=platoonPoints6;
			  this.minDeployStar[1]=minDeployStar1;
			  this.minDeployStar[2]=minDeployStar2;
			  this.minDeployStar[3]=minDeployStar3;
			  this.minGPStar3=minGPStar3;
			  this.notes=notes;
		  }
	}
}