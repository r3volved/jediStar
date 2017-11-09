CREATE DATABASE IF NOT EXISTS jedistar CHARACTER SET UTF8;

CREATE USER IF NOT EXISTS 'jedistar'@'localhost' IDENTIFIED BY 'JeDiStArBoT';

GRANT ALL ON jedistar.* TO 'jedistar'@'localhost';

USE jedistar;

CREATE TABLE IF NOT EXISTS guild
(
	channelID VARCHAR(50) PRIMARY KEY,
	guildID INT NOT NULL,
	tbAssistant BOOLEAN DEFAULT 0,
	webhook VARCHAR(128) NULL,
	alertRole VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS tbEventLog
(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	date TIMESTAMP NOT NULL,
	phase INT NOT NULL,
	tbName VARCHAR(32) NOT NULL 
);

CREATE TABLE IF NOT EXISTS tbTerritoryLog
(
	id INT NOT NULL,
	guildID INT NOT NULL,
	territoryID VARCHAR(8) NOT NULL,
	phase INT NOT NULL,
	CM1 TEXT NULL,
	CM2 TEXT NULL,
	SM1 TEXT NULL,
	platoons TEXT NULL,
	PRIMARY KEY( id, territoryID, guildID )
);

CREATE TABLE IF NOT EXISTS characters
(
	name VARCHAR(64) PRIMARY KEY,
	baseID VARCHAR(64),
	url VARCHAR(128),
	image VARCHAR(128),
	power INTEGER,
	description VARCHAR(512),
	combatType INTEGER,
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ships
(
	name VARCHAR(64) PRIMARY KEY,
	baseID VARCHAR(64),
	url VARCHAR(128),
	image VARCHAR(128),
	power INTEGER,
	description VARCHAR(512),
	combatType INTEGER,
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guildUnits
(
	guildID INTEGER NOT NULL,
	player VARCHAR(32) NOT NULL,
	charID VARCHAR(64) NOT NULL,
	rarity INTEGER,
	combatType INTEGER,
	power INTEGER,
	level INTEGER,
	expiration TIMESTAMP,
	PRIMARY KEY (guildID,player,charID)
);

CREATE TABLE IF NOT EXISTS commandHistory
(
	command VARCHAR(32),
	ts TIMESTAMP,
	userID VARCHAR(64),
	userName VARCHAR(32) NOT NULL,
	serverID VARCHAR(128),
	serverName VARCHAR(128),
	serverRegion VARCHAR(64),
	PRIMARY KEY (command,ts,userID)
);

CREATE TABLE IF NOT EXISTS territoryData (
  territoryID VARCHAR(8) NOT NULL,
  territoryName VARCHAR(64) DEFAULT NULL,
  tbName VARCHAR(32) DEFAULT NULL,
  phase INT(11) DEFAULT NULL,
  combatType INT(11) DEFAULT NULL,
  starPoints1 INT(11) DEFAULT NULL,
  starPoints2 INT(11) DEFAULT NULL,
  starPoints3 INT(11) DEFAULT NULL,
  ability VARCHAR(64) DEFAULT NULL,
  affectedTerritories VARCHAR(32) DEFAULT NULL,
  requiredUnits VARCHAR(64) DEFAULT NULL,
  specialMission VARCHAR(32) DEFAULT NULL,
  specialPoints INT(11) DEFAULT NULL,
  combatMissions INT(11) DEFAULT NULL,
  missionPoints1 INT(11) DEFAULT NULL,
  missionPoints2 INT(11) DEFAULT NULL,
  missionPoints3 INT(11) DEFAULT NULL,
  missionPoints4 INT(11) DEFAULT NULL,
  missionPoints5 INT(11) DEFAULT NULL,
  missionPoints6 INT(11) DEFAULT NULL,
  platoonPoints1 INT(11) DEFAULT NULL,
  platoonPoints2 INT(11) DEFAULT NULL,
  platoonPoints3 INT(11) DEFAULT NULL,
  platoonPoints4 INT(11) DEFAULT NULL,
  platoonPoints5 INT(11) DEFAULT NULL,
  platoonPoints6 INT(11) DEFAULT NULL,
  minDeployStar1 INT(11) DEFAULT NULL,
  minDeployStar2 INT(11) DEFAULT NULL,
  minDeployStar3 INT(11) DEFAULT NULL,
  minGPStar3 INT(11) DEFAULT NULL,
  notes TEXT,
  PRIMARY KEY (territoryID)
);


CREATE TABLE squads (
  squadID int(10) UNSIGNED NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL,
  raid varchar(255) NOT NULL,
  phase int(11) NOT NULL,
  char1 varchar(255) NOT NULL,
  char2 varchar(255) NOT NULL,
  char3 varchar(255) NOT NULL,
  char4 varchar(255) NOT NULL,
  char5 varchar(255) NOT NULL,
  notes text NOT NULL
);


