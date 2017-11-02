package fr.jedistar.classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.StaticVars;

public class TBTerritoryLog {

	final static Logger logger = LoggerFactory.getLogger(TBTerritoryLog.class);

	/* ---- Single response ---- */
	private final static String SQL_INSERT_TERRITORY_LOG = "INSERT INTO tbTerritoryLog (id, territoryID, guildID, phase, cm1, cm2, sm1, platoons) VALUES ( ?,?,?,?,?,?,?,? ) ON DUPLICATE KEY UPDATE phase=?, CM1=?, CM2=?, SM1=?, platoons=?";
	private final static String SQL_FIND_TB_TERRITORY_LOG = "SELECT * FROM tbTerritoryLog WHERE id=? AND guildID=? AND territoryID=?";

	/* ---- Single TBLog ---- */	
	private final static String SQL_FIND_BY_ID = "SELECT * FROM tbTerritoryLog WHERE id=? AND guildID=?";
	private final static String SQL_FIND_BY_ID_AND_PHASE = "SELECT * FROM tbTerritoryLog WHERE id=? AND guildID=? AND phase=?";
	private final static String SQL_FIND_BY_ID_AND_TERRITORY = "SELECT * FROM tbTerritoryLog WHERE id=? AND guildID=? AND territoryID=?";
	
	/* ---- Multiple TBLog ---- */
	private final static String SQL_FIND_TOTAL_TB = "SELECT * FROM tbTerritoryLog WHERE guildID=? ORDER BY id ASC";	
	private final static String SQL_FIND_TOTAL_BY_PHASE = "SELECT * FROM tbTerritoryLog WHERE guildID=? AND phase=? ORDER BY id ASC";
	private final static String SQL_FIND_TOTAL_BY_TERRITORY = "SELECT * FROM tbTerritoryLog WHERE guildID=? AND territoryID=? ORDER BY id ASC";

	
	public Integer logID = 0;
	public String territoryID = null;	
	public Integer guildID = 0;
	public Integer phase = 0;
	public List<String> CM1 = new ArrayList<String>();
	public List<String> CM2 = new ArrayList<String>();
	public List<String> SM1 = new ArrayList<String>();
	public String platoons = "NNNNNN";
	public boolean saved = false;	
	
	
	public TBTerritoryLog() {
		this.saved = false;
	}
			
	public TBTerritoryLog( Integer ID, Integer guildID, String territoryID, Integer phase, String CM1, String CM2, String SM1, String platoons, boolean saved ) {
		this.logID=ID;
		this.guildID=guildID;
		this.territoryID=territoryID;
		this.phase=phase;

		String cm1str = CM1.replace("[","").replace("]","");				  
		if( cm1str != null && cm1str.length() > 0 ) {
			String[] tmpCM1 = cm1str.split(",");
			for( Integer cm1 = 0; cm1 != tmpCM1.length; ++cm1 ) {
				this.CM1.add(tmpCM1[cm1]);
			}
		}
		
		String cm2str = CM2.replace("[","").replace("]","");				  
		if( cm2str != null && cm2str.length() > 0 ) {
			String[] tmpCM2 = cm2str.split(",");
			for( Integer cm2 = 0; cm2 != tmpCM2.length; ++cm2 ) {
				this.CM2.add(tmpCM2[cm2]);
			}
		}
		  
		String sm1str = SM1.replace("[","").replace("]","");				  
		if( sm1str !=  null && sm1str.length() > 0 ) {
			String[] tmpSM1 = sm1str.split(",");
			for( Integer sm1 = 0; sm1 != tmpSM1.length; ++sm1 ) {
				this.SM1.add(tmpSM1[sm1]);
			}
		}
		
		this.platoons=platoons;
		this.saved=saved;
	}

	public TBTerritoryLog( Integer ID, Integer guildID, String territoryID ) {
		
		this.logID=ID;
		this.guildID=guildID;
		this.territoryID=territoryID;
		this.saved = this.populateByPK( ID, guildID, territoryID );

	}
	
	
	public long getMissionTotal( List<String> mission, Territory terr ) {
		
		long total = 0;
		for( Integer i = 1; i <= mission.size(); i+=2 ) {
			
			try {
				
				Integer val = Integer.parseInt(mission.get(i).trim());
				total += terr.missionPoints.get(val);
				
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
				return 0;
			}
			
		}		
		return total;
	}
	
	
	public long getPlatoonTotal( String platoons, Territory terr ) {
		
		long total = 0;
		char[] splitPlatoons = platoons.toUpperCase().toCharArray();
		for( Integer i = 1; i != splitPlatoons.length; ++i ) {
			
			try {
				
				if( splitPlatoons[i] == 'Y' ) {
					total += terr.platoonPoints.get(i);
				}
				
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
				return 0;
			}
			
		}		
		return total;
	}
	
	
	public String history(String type) {
		
		String historyStr = "";
		
		
		
		return historyStr;
		
	}
	
	
	public String report(String type) {
		
		final String REPORT_FULL = "full";
		final String REPORT_PLATOONS = "platoons";
		final String REPORT_SPECIAL_MISSION = "sm";
		final String REPORT_COMBAT_MISSIONS = "cm";
				
		String reportStr = "";
		long starPoints = 0;
		Territory terr = new Territory(this.territoryID);
		
		if( type.equalsIgnoreCase(REPORT_FULL) ) {
			
			if( this.CM1.size() > 1 ) {
				long total = this.getMissionTotal( this.CM1, terr );
				starPoints += total;

				reportStr += "**Combat mission 1**\r\n";
				reportStr += "Participation: *"+String.valueOf(this.CM1.size() / 2)+" member(s)*\r\n";								
				reportStr += "Points earned: *"+NumberFormat.getIntegerInstance().format(total)+"*\r\n";				
			}
			if( terr.combatMissions == 2 && this.CM2.size() > 1 ) {
				long total = this.getMissionTotal( this.CM2, terr );
				starPoints += total;

				reportStr += "\t\r\n";
				reportStr += "**Combat mission 2**\r\n";
				reportStr += "Participation: *"+String.valueOf(this.CM2.size() / 2)+" member(s)*\r\n";
				reportStr += "Points earned: *"+NumberFormat.getIntegerInstance().format(total)+"*\r\n";
			}
			if( terr.specialMission != null && this.SM1.size() > 1 ) {
				long total = this.getMissionTotal( this.SM1, terr );
				starPoints += total;

				reportStr += "\t\r\n";
				reportStr += "**Special mission**\r\n";
				reportStr += "Participation: *"+String.valueOf(this.SM1.size() / 2)+" member(s)*\r\n";
				reportStr += "Points earned: *"+NumberFormat.getIntegerInstance().format(total)+"*\r\n";
				reportStr += "Rewards: *"+terr.specialMission+"*\r\n";
			}
			
			long ptotal = getPlatoonTotal( this.platoons, terr );
			starPoints += ptotal;
			
			reportStr += "\t\r\n";
			reportStr += "__*Estimated points*__: "+NumberFormat.getIntegerInstance().format(ptotal)+"\r\n";
			
			Integer stars = 0;
			stars = starPoints >= (long) terr.starPoints.get(1) ? 1 : stars;
			stars = starPoints >= (long) terr.starPoints.get(2) ? 2 : stars;
			stars = starPoints >= (long) terr.starPoints.get(3) ? 3 : stars;
			
			reportStr += "__*Estimated stars*__:  ";
			for( Integer s = 1; s <= 3; ++s ) {
				reportStr += stars >= s ? ":star:" : ":black_small_square:";
			}
			reportStr += "\r\n";
			
		}		
		
		if( type.equalsIgnoreCase(REPORT_PLATOONS) ) {
			
			char[] p = this.platoons.toCharArray();
			reportStr += "**"+terr.territoryName+"**\r\n";
			
			long ptotal = getPlatoonTotal( this.platoons, terr );
			starPoints += ptotal;
			
			reportStr += "```\r\n";
			reportStr += "Platoons: | 1 | 2 | 3 | 4 | 5 |\r\n";
			reportStr += "          | ";
			reportStr += "N".equalsIgnoreCase(String.valueOf(p[0])) ? "- | " : "F | ";
			reportStr += "N".equalsIgnoreCase(String.valueOf(p[1])) ? "- | " : "F | ";
			reportStr += "N".equalsIgnoreCase(String.valueOf(p[2])) ? "- | " : "F | ";
			reportStr += "N".equalsIgnoreCase(String.valueOf(p[3])) ? "- | " : "F | ";
			reportStr += "N".equalsIgnoreCase(String.valueOf(p[4])) ? "- | " : "F | ";
			reportStr += "\r\n";
			reportStr += "-------------------------------\r\n";
			reportStr += "Estimated points: "+NumberFormat.getIntegerInstance().format(ptotal)+"\r\n";			
			reportStr += "```";
		}
		
		if( type.equalsIgnoreCase(REPORT_COMBAT_MISSIONS) ) {
			
		}
		
		if( type.equalsIgnoreCase(REPORT_SPECIAL_MISSION) ) {
			
		}

		return reportStr;
		
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
	
	public List<TBTerritoryLog> getLogs( Integer id, Integer guildID, String territoryID, Integer phase ) {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			if( id != 0 ) {
				
				if( guildID != 0 ) {
					
					if( phase != 0 ) {
						
						stmt = conn.prepareStatement(SQL_FIND_BY_ID_AND_PHASE);
						stmt.setInt(1,id);
						stmt.setInt(2,guildID);
						stmt.setInt(3,phase);
					} else if( territoryID != null ) {

						stmt = conn.prepareStatement(SQL_FIND_BY_ID_AND_TERRITORY);
						stmt.setInt(1,id);
						stmt.setInt(2,guildID);
						stmt.setString(3,territoryID);
					} else {
						
						stmt = conn.prepareStatement(SQL_FIND_BY_ID);
						stmt.setInt(1,id);
						stmt.setInt(2,guildID);						
					}					
				}
				
			} else {
				
				if( phase != 0 ) {
					
					stmt = conn.prepareStatement(SQL_FIND_TOTAL_BY_PHASE);
					stmt.setInt(1,guildID);
					stmt.setInt(2,phase);
				} else if( territoryID != null ) {

					stmt = conn.prepareStatement(SQL_FIND_TOTAL_BY_TERRITORY);
					stmt.setInt(1,guildID);
					stmt.setString(2,territoryID);
				} else {

					stmt = conn.prepareStatement(SQL_FIND_TOTAL_TB);
					stmt.setInt(1,guildID);
				}
			}
		
			logger.debug("Executing query : "+stmt.toString());		
			rs = stmt.executeQuery();
		
			List<TBTerritoryLog> logList = new ArrayList<TBTerritoryLog>();
			
			while(rs.next()) {
				
				logList.add( new TBTerritoryLog(rs.getInt("id"), rs.getInt("guildID"), rs.getString("territoryID"), rs.getInt("phase"), rs.getString("cm1"), rs.getString("cm2"), rs.getString("sm1"), rs.getString("platoons"), true ) );				
				
			}
			
			return logList;

		} catch(SQLException e) {
			  logger.error("C : "+e.getMessage());
			  e.printStackTrace();
			  return null;
		}
		finally {
			try {
				if(rs != null) { rs.close(); }
				if(stmt != null) { stmt.close(); }
			} catch (SQLException e) {
				logger.error("F : "+e.getMessage());
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
