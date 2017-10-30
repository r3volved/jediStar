package fr.jedistar.classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.StaticVars;

public class TBTerritoryLog {

	final static Logger logger = LoggerFactory.getLogger(TBTerritoryLog.class);

	private final static String SQL_FIND_TB_TERRITORY_LOG = "SELECT * FROM tbTerritoryLog WHERE id=?";
	
	
	public String logID;
	public String territoryID;	
	public Integer guildID;
	public Integer phase;
	public String CM1;
	public String CM2;
	public String SM1;
	public String Platoons;
	public boolean saved;	
	
	public TBTerritoryLog( String ID ) {
		
		this.saved = populateByID( ID );
		
	}
	
	public TBTerritoryLog( String logID, String territoryID, Integer guildID, Integer phase, String CM1, String CM2, String SM1, String Platoons ) {
		this.logID=logID;
		this.territoryID=territoryID;		
		this.guildID=guildID;
		this.phase=phase;
		this.CM1=CM1;
		this.CM2=CM2;
		this.SM1=SM1;
		this.Platoons=Platoons;
		this.saved=false;
	}
	
	private boolean populateByID( String ID ) {
		
		  Connection conn = null;
		  PreparedStatement stmt = null;
		  ResultSet rs = null;
		
		  try {
			  conn = StaticVars.getJdbcConnection();
		
			  stmt = conn.prepareStatement(SQL_FIND_TB_TERRITORY_LOG);
		
			  stmt.setString(1,ID);
		
			  logger.debug("Executing query : "+stmt.toString());		
			  rs = stmt.executeQuery();
		
			  if(rs.next()) {
				  
				  this.logID=rs.getString("logID");
				  this.territoryID=rs.getString("territoryID");		
				  this.guildID=rs.getInt("guildID");
				  this.phase=rs.getInt("phase");
				  this.CM1=rs.getString("CM1");
				  this.CM2=rs.getString("CM2");
				  this.SM1=rs.getString("SM1");
				  this.Platoons=rs.getString("Platoons");
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
