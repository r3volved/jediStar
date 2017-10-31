package fr.jedistar.classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.StaticVars;

public class TBTerritoryLog {

	final static Logger logger = LoggerFactory.getLogger(TBTerritoryLog.class);

	private final static String SQL_FIND_TB_TERRITORY_LOG = "SELECT * FROM tbTerritoryLog WHERE id=? AND guildID=? AND territoryID=?";
	private final static String SQL_INSERT_TERRITORY_LOG = "INSERT INTO tbTerritoryLog (id, territoryID, guildID, phase, cm1, cm2, sm1, platoons) VALUES ( ?,?,?,?,?,?,?,? ) ON DUPLICATE KEY UPDATE phase=?, CM1=?, CM2=?, SM1=?, platoons=?";
		
	public Integer logID = 0;
	public String territoryID = null;	
	public Integer guildID = 0;
	public Integer phase = 0;
	public List<String> CM1 = new ArrayList<String>();
	public List<String> CM2 = new ArrayList<String>();
	public List<String> SM1 = new ArrayList<String>();
	public String platoons = "NNNNNN";
	public boolean saved = false;	
	
	public TBTerritoryLog( Integer ID, Integer guildID, String territoryID ) {
		
		this.logID=ID;
		this.guildID=guildID;
		this.territoryID=territoryID;
		this.saved = this.populateByPK( ID, guildID, territoryID );

	}
	
	public boolean saveLog() {
		
		this.saved = this.saveNewLog();
		return this.saved;
	}
	

	private boolean saveNewLog() {
		
		  Connection conn = null;
		  PreparedStatement stmt = null;
		  ResultSet rs = null;
		
		  try {
			  conn = StaticVars.getJdbcConnection();
		
			  stmt = conn.prepareStatement(SQL_INSERT_TERRITORY_LOG);
		
			  stmt.setInt(1,this.logID);
			  stmt.setString(2,this.territoryID);
			  stmt.setInt(3,this.guildID);			  
			  stmt.setInt(4,this.phase);
			  stmt.setString(5,this.CM1.toString());
			  stmt.setString(6,this.CM2.toString());
			  stmt.setString(7,this.SM1.toString());
			  stmt.setString(8,this.platoons);

			  stmt.setInt(9,this.phase);
			  stmt.setString(10,this.CM1.toString());
			  stmt.setString(11,this.CM2.toString());
			  stmt.setString(12,this.SM1.toString());
			  stmt.setString(13,this.platoons);

			  logger.debug("Executing query : "+stmt.toString());		
			  stmt.execute();
					
			  return true;
		} catch(SQLException e) {
			  logger.error(e.getMessage());
			  e.printStackTrace();
			  return false;
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
		
	private boolean populateByPK( Integer ID, Integer guildID, String territoryID ) {
		
		  Connection conn = null;
		  PreparedStatement stmt = null;
		  ResultSet rs = null;
		
		  try {
			  conn = StaticVars.getJdbcConnection();
		
			  stmt = conn.prepareStatement(SQL_FIND_TB_TERRITORY_LOG);
		
			  stmt.setInt(1,ID);
			  stmt.setInt(2,guildID);
			  stmt.setString(3,territoryID);
		
			  logger.debug("Executing query : "+stmt.toString());		
			  rs = stmt.executeQuery();
		
			  if(rs.next()) {
				  
				  this.logID=rs.getInt("id");
				  this.territoryID=rs.getString("territoryID");		
				  this.guildID=rs.getInt("guildID");
				  this.phase=rs.getInt("phase");
				  
				  String cm1str = rs.getString("cm1").replace("[","").replace("]","");				  
				  if( cm1str != null && cm1str.length() > 0 ) {
					  String[] tmpCM1 = cm1str.split(",");
					  for( Integer cm1 = 0; cm1 != tmpCM1.length; ++cm1 ) {
						  this.CM1.add(tmpCM1[cm1]);
					  }
				  }

				  String cm2str = rs.getString("cm2").replace("[","").replace("]","");				  
				  if( cm2str != null && cm2str.length() > 0 ) {
					  String[] tmpCM2 = cm2str.split(",");
					  for( Integer cm2 = 0; cm2 != tmpCM2.length; ++cm2 ) {
						  this.CM2.add(tmpCM2[cm2]);
					  }
				  }
				  
				  String sm1str = rs.getString("sm1").replace("[","").replace("]","");				  
				  if( sm1str !=  null && sm1str.length() > 0 ) {
					  String[] tmpSM1 = sm1str.split(",");
					  for( Integer sm1 = 0; sm1 != tmpSM1.length; ++sm1 ) {
						  this.SM1.add(tmpSM1[sm1]);
					  }
				  }
				  
				  this.platoons=rs.getString("platoons");
				  
				  return true;
			  }
			
			  return false;
		} catch(SQLException e) {
			  logger.error(e.getMessage());
			  e.printStackTrace();
			  return false;
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
