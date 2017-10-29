package fr.jedistar.classes;

import java.util.ArrayList;
import java.util.List;

public class Territory {
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
		  description += ":star: "+this.starPoints.get(1)+"\r\n";
		  description += ":star::star: "+this.starPoints.get(2)+"\r\n";
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
	  
}
