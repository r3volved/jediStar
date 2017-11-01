package fr.jedistar.classes;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.StaticVars;

public class Territory {
		
	final static Logger logger = LoggerFactory.getLogger(Territory.class);

	private final static String SQL_FIND_TERRITORY = "SELECT * FROM territoryData WHERE territoryID=? OR territoryName LIKE ?";
	
	  public String territoryID;
	  public String territoryName;
	  public String tbName;
	  public Integer phase;
	  public Integer combatType;
	  public List<Integer> starPoints = new ArrayList<Integer>();
	  public String ability;
	  public List<String> affectedTerritories = new ArrayList<String>();
	  public List<String> requiredUnits = new ArrayList<String>();
	  public String specialMission;
	  public Integer combatMissions;
	  public List<Integer> missionPoints = new ArrayList<Integer>();
	  public List<Integer> platoonPoints = new ArrayList<Integer>();
	  public List<Integer> minDeployStar = new ArrayList<Integer>();
	  public Integer minGPStar3;
	  public String notes;
	  
	  public Territory(String name) {
		  
		 this.getClosest( name ); 
		  
	  }
	  
	  public Territory(String territoryID,String territoryName,String tbName,Integer phase,Integer combatType,Integer starPoints1,Integer starPoints2,Integer starPoints3,String ability,String affectedTerritories,String requiredUnits,String specialMission,Integer combatMissions,Integer missionPoints1,Integer missionPoints2,Integer missionPoints3,Integer missionPoints4,Integer missionPoints5,Integer missionPoints6,Integer platoonPoints1,Integer platoonPoints2,Integer platoonPoints3,Integer platoonPoints4,Integer platoonPoints5,Integer platoonPoints6,Integer minDeployStar1,Integer minDeployStar2,Integer minDeployStar3,Integer minGPStar3,String notes) {

		  this.territoryID=territoryID;
		  this.territoryName=territoryName;
		  this.tbName=tbName;
		  this.phase=phase;
		  this.combatType=combatType;
		  this.starPoints.add(0);
		  this.starPoints.add(starPoints1);
		  this.starPoints.add(starPoints2);
		  this.starPoints.add(starPoints3);
		  this.ability=ability;
		  
		  if( affectedTerritories != null ) {
			  String[] splitAT = affectedTerritories.split(":");
			  for( Integer at = 0; at != splitAT.length; ++at ) {
				  this.affectedTerritories.add( splitAT[at] );
			  }
		  }
		  
		  if( requiredUnits != null ) {
			  String[] splitRU = requiredUnits.split(":");
			  for( Integer ru = 0; ru != splitRU.length; ++ru ) {
				  this.requiredUnits.add( splitRU[ru] );
			  }
		  }
		  
		  this.specialMission=specialMission;
		  this.combatMissions=combatMissions;
		  this.missionPoints.add(0);
		  this.missionPoints.add(missionPoints1);
		  this.missionPoints.add(missionPoints2);
		  this.missionPoints.add(missionPoints3);
		  this.missionPoints.add(missionPoints4);
		  this.missionPoints.add(missionPoints5);
		  this.missionPoints.add(missionPoints6);
		  this.platoonPoints.add(0);
		  this.platoonPoints.add(platoonPoints1);
		  this.platoonPoints.add(platoonPoints2);
		  this.platoonPoints.add(platoonPoints3);
		  this.platoonPoints.add(platoonPoints4);
		  this.platoonPoints.add(platoonPoints5);
		  this.platoonPoints.add(platoonPoints6);
		  this.minDeployStar.add(0);
		  this.minDeployStar.add(minDeployStar1);
		  this.minDeployStar.add(minDeployStar2);
		  this.minDeployStar.add(minDeployStar3);
		  this.minGPStar3=minGPStar3;
		  this.notes=notes;
	  }
	  
	  public String territoryDescription() {
		  String description = "";
		  
		  //Star points
		  description += ":star::black_small_square::black_small_square: "+this.starPoints.get(1)+"\r\n";
		  description += ":star::star::black_small_square: "+this.starPoints.get(2)+"\r\n";
		  description += ":star::star::star: "+this.starPoints.get(3)+"\r\n";
		  description += "----------\r\n";
		  
		  //Platoon stuff
		  description += "**Platoon points** : ```1-4 : "+this.platoonPoints.get(1)+"\r\n5/6 : "+this.platoonPoints.get(6)+"```";
		  
		  if( this.affectedTerritories != null ) {
			  description += "**Affected territories** : ```";
			  for( Integer at = 0; at != this.affectedTerritories.size(); ++at ) {
				  description += this.affectedTerritories.get(at)+"\r\n";
			  }
			  description += "```";
		  }
		  
		  description += "**Special Ability** : ```"+this.ability+"```";
		  		  
		  for( Integer i = 0; i != this.combatMissions; ++i ) {
				Integer points = this.combatType == 2 ? 3 : 6;
				description += "**CM"+(i+1)+" : Combat Mission**";
				description += "```Points "+points+"/"+points+" : ` "+this.missionPoints.get(points)+"```";
		  }
		  
		  if( this.specialMission != null ) {
				description += "**SM1 : Special Mission**";
				description += "```";
				description += "Rewards    : "+this.specialMission+"\r\n";
				description += "Required   : "+this.requiredUnits+"\r\n";
				description += "Points 3/3 : "+this.missionPoints.get(3)+"```";
		  }
		  
		  return description;
	  }
	  
	  public EmbedBuilder getTerritoryInfoEmbed() {
		  if( this.territoryID == null ) { return null; }
		  
		  EmbedBuilder tEmbed = new EmbedBuilder();		  
		  
		  tEmbed.setAuthor(this.tbName,"","");
		  tEmbed.setTitle(this.territoryID+" : "+this.territoryName);
		  tEmbed.setDescription("");
		  if( this.combatType == 2 ) { 
			  tEmbed.setColor(Color.CYAN);
		  } else { 
			  tEmbed.setColor(Color.WHITE);
		  }
		  
		  //Star points
		  String stars = ":star::black_small_square::black_small_square: *"+NumberFormat.getIntegerInstance().format(this.starPoints.get(1))+"*\r\n:star::star::black_small_square: *"+NumberFormat.getIntegerInstance().format(this.starPoints.get(2))+"*\r\n:star::star::star: *"+NumberFormat.getIntegerInstance().format(this.starPoints.get(3))+"*";
		  tEmbed.addField("Phase "+this.phase, stars, false);
		  
		  tEmbed.addField("Platoon points", "1-4: *"+NumberFormat.getIntegerInstance().format(this.platoonPoints.get(1))+"*\r\n5-6: *"+NumberFormat.getIntegerInstance().format(this.platoonPoints.get(6))+"*", false);
		  
		  if( this.affectedTerritories != null ) {
			  String affected = "";
			  for( Integer at = 0; at != this.affectedTerritories.size(); ++at ) {
				  String sameTerr = this.territoryID.substring(0, 3);
				  String affdTerr = this.affectedTerritories.get(at).substring(0, 3);
				  affected += at != 0 ? ", " : "";
				  affected += sameTerr.equalsIgnoreCase(affdTerr) ? "***"+this.affectedTerritories.get(at)+"***" : "*"+this.affectedTerritories.get(at)+"*";
			  }
			  tEmbed.addField("Affected territories", affected, false);
		  }
		  
		  tEmbed.addField("Special ability", "*"+this.ability+"*", false);
		  		  
		  for( Integer i = 0; i != this.combatMissions; ++i ) {
			  Integer points = this.combatType == 2 ? 3 : 6;
			  tEmbed.addField("CM"+(i+1)+" : Combat Mission", "Points "+points+"/"+points+": *"+NumberFormat.getIntegerInstance().format(this.missionPoints.get(points))+"*", false);
		  }
		  
		  if( this.specialMission != null ) {
			 String smission = "Rewards: *"+this.specialMission+"*\r\nRequired: *"+this.requiredUnits+"*\r\nPoints 3/3: *"+NumberFormat.getIntegerInstance().format(this.missionPoints.get(3))+"*";
			 tEmbed.addField("SM1 : Special Mission", smission, false);
			 tEmbed.setColor(Color.ORANGE);
		  }
		  
		  return tEmbed;		  
	  }
	  
	  private boolean getClosest(String name) {
		  
		  Connection conn = null;
		  PreparedStatement stmt = null;
		  ResultSet rs = null;
		
		  try {
			  conn = StaticVars.getJdbcConnection();
		
			  stmt = conn.prepareStatement(SQL_FIND_TERRITORY);
		
			  stmt.setString(1,name);
			  stmt.setString(2,"%"+name+"%");
		
			  logger.debug("Executing query : "+stmt.toString());		
			  rs = stmt.executeQuery();
		
			  if(rs.next()) {
				  
				  this.territoryID=rs.getString("territoryID");
				  this.territoryName=rs.getString("territoryName");
				  this.tbName=rs.getString("tbName");
				  this.phase=rs.getInt("phase");
				  this.combatType=rs.getInt("combatType");
				  this.starPoints.add(0);
				  this.starPoints.add(rs.getInt("starPoints1"));
				  this.starPoints.add(rs.getInt("starPoints2"));
				  this.starPoints.add(rs.getInt("starPoints3"));
				  this.ability=rs.getString("ability");
				  
				  if( rs.getString("affectedTerritories") != null ) {
					  String[] splitAT = rs.getString("affectedTerritories").split(":");
					  for( Integer at = 0; at != splitAT.length; ++at ) {
						  this.affectedTerritories.add( splitAT[at] );
					  }
				  }
				  
				  if( rs.getString("requiredUnits") != null ) {
					  String[] splitRU = rs.getString("requiredUnits").split(":");
					  for( Integer ru = 0; ru != splitRU.length; ++ru ) {
						  this.requiredUnits.add( splitRU[ru] );
					  }
				  }
				  
				  this.specialMission=rs.getString("specialMission");
				  this.combatMissions=rs.getInt("combatMissions");
				  this.missionPoints.add(0);
				  this.missionPoints.add(rs.getInt("missionPoints1"));
				  this.missionPoints.add(rs.getInt("missionPoints2"));
				  this.missionPoints.add(rs.getInt("missionPoints3"));
				  this.missionPoints.add(rs.getInt("missionPoints4"));
				  this.missionPoints.add(rs.getInt("missionPoints5"));
				  this.missionPoints.add(rs.getInt("missionPoints6"));
				  this.platoonPoints.add(0);
				  this.platoonPoints.add(rs.getInt("platoonPoints1"));
				  this.platoonPoints.add(rs.getInt("platoonPoints2"));
				  this.platoonPoints.add(rs.getInt("platoonPoints3"));
				  this.platoonPoints.add(rs.getInt("platoonPoints4"));
				  this.platoonPoints.add(rs.getInt("platoonPoints5"));
				  this.platoonPoints.add(rs.getInt("platoonPoints6"));
				  this.minDeployStar.add(0);
				  this.minDeployStar.add(rs.getInt("minDeployStar1"));
				  this.minDeployStar.add(rs.getInt("minDeployStar2"));
				  this.minDeployStar.add(rs.getInt("minDeployStar3"));
				  this.minGPStar3=rs.getInt("minGPStar3");
				  this.notes=rs.getString("notes");
				  
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
