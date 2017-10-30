package fr.jedistar.classes;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.Main;
import fr.jedistar.StaticVars;

public class TBEventLog {
	
	final static Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String INSERT_EVENTLOG_REQUEST = "INSERT INTO tbEventLog VALUES(?,?,?,?)";
	private static final String SELECT_LAST_EVENTLOG_REQUEST = "SELECT * FROM tbEventLog ORDER BY date DESC LIMIT 1";

	  public Integer id;
	  public Calendar date = Calendar.getInstance();
	  public Integer phase;
	  public String name;
	  public boolean saved;

	  public TBEventLog() {
		  this.saved = this.loadLastEventLog();
	  }
	  
	  public TBEventLog(Integer id, Calendar date, Integer phase, String name) {
		  this.id = id;
		  this.date = date;
		  this.phase = phase;
		  this.name = name;
		  this.saved=false;
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
				
				this.saved = true;
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
					this.date.setTime(rs.getDate("date"));					
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

	    public boolean calculateToday(TimeZone eventTimeZone) {
	  
	    	Calendar today = Calendar.getInstance(eventTimeZone);
	    	
	    	//TEST
	    	//today.set(Calendar.DATE, 28);
	    	
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
	            	c.set(Calendar.HOUR_OF_DAY, 17);
	            	c.set(Calendar.MINUTE, 0);
	            	c.set(Calendar.SECOND, 0);
          		
	            	//UPDATE OBJECT TO NEW AND SAVE
	            	this.date = c;
	            	this.phase = 0;	            	
	            	this.saveNewLog();

	            }
	    	}    	

	    	return true;
	    }
	}
