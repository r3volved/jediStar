CREATE DATABASE IF NOT EXISTS jedistar CHARACTER SET UTF8;

CREATE USER IF NOT EXISTS 'jedistar'@'localhost' IDENTIFIED BY 'JeDiStArBoT';

GRANT ALL ON jedistar.* TO 'jedistar'@'localhost';

USE jedistar;

CREATE TABLE IF NOT EXISTS guild
(
	channelID VARCHAR(50) PRIMARY KEY,
	guildID INT NOT NULL,
	tbAssistant BOOLEAN DEFAULT 0,
	webhook VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS tbEventLog
(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	date TIMESTAMP NOT NULL,
	phase INT NOT NULL,
	tbName VARCHAR(32) NOT NULL 
);


INSERT INTO tbEventLog (id, date, phase, tbName) VALUES 
('1', '2017-10-23 17:00:00', '0', 'Hoth - Imperial Invasion'),
('2', '2017-11-02 17:00:00', '0', 'Hoth - Imperial Invasion');


CREATE TABLE IF NOT EXISTS tbTerritoryLog
(
	id INT NOT NULL,
	guildID INT NOT NULL,
	territoryID VARCHAR(4) NOT NULL,
	phase INT NOT NULL,
	CM1 TEXT NULL,
	CM2 TEXT NULL,
	SM1 TEXT NULL,
	Platoons TEXT NULL,
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
  territoryID VARCHAR(4) NOT NULL,
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


INSERT INTO territoryData (territoryID, territoryName, tbName, phase, combatType, starPoints1, starPoints2, starPoints3, ability, affectedTerritories, requiredUnits, specialMission, combatMissions, missionPoints1, missionPoints2, missionPoints3, missionPoints4, missionPoints5, missionPoints6, platoonPoints1, platoonPoints2, platoonPoints3, platoonPoints4, platoonPoints5, platoonPoints6, minDeployStar1, minDeployStar2, minDeployStar3, minGPStar3, notes) VALUES
('HO1A', 'Rebel Base', 'Hoth - Imperial Invasion', 1, 1, 885000, 6580000, 45600000, 'Rebel Guerilla Strike', 'HO2A:HO2B', 'Phoenix', '7 Guild Event Tokens', 2, 24000, 51000, 91000, 144000, 211000, 291000, 100000, 100000, 100000, 100000, 150000, 150000, -2215000, 3480000, 42500000, 42500000, null),
('HO2A', 'Ion Cannon', 'Hoth - Imperial Invasion', 2, 2, 1900000, 19800000, 55000000, 'Ion Cannon Blast', 'HO3A:HO4A:HO5A', 'Rebels:HRSoldier', null, 2, 43000, 72000, 115000, 172000, 243000, 329000, 120000, 120000, 120000, 120000, 180000, 180000, -3240000, 14660000, 49860000, 90670000, 'These platoons affect the next 3 ship territories! In order to maximize star count everyone will require the ion cannon during thier ship battles.'),
('HO2B', 'Overlook', 'Hoth - Imperial Invasion', 2, 1, 1900000, 15400000, 43800000, 'Rebel Supply Lines', 'HO3B:HO3C', 'Rogue One', '8 Guild Event Tokens', 1, 43000, 72000, 115000, 172000, 243000, 329000, 120000, 120000, 120000, 120000, 180000, 180000, -1090000, 12410000, 40810000, 0, null),
('HO3A', 'Rear Airspace', 'Hoth - Imperial Invasion', 3, 2, 1920000, 16500000, 26300000, 'Rebel Strafing Run', 'HO4B:HO4C', null, null, 1, 96000, 203000, 371000, 0, 0, 0, 140000, 140000, 140000, 140000, 140000, 140000, -3720000, 10860000, 20660000, 20660000, null),
('HO3B', 'Rear Trenches', 'Hoth - Imperial Invasion', 3, 1, 3510000, 27600000, 64800000, 'Rebel Supply Lines', 'HO4B:HO4C', 'Rebels:HRScout', null, 2, 65000, 96000, 142000, 203000, 280000, 372000, 140000, 140000, 140000, 140000, 210000, 210000, -3970000, 20120000, 57320000, 105290000, null),
('HO3C', 'Power Generator', 'Hoth - Imperial Invasion', 3, 1, 3510000, 22400000, 52200000, 'Planetary Shield', 'HO3B:HO3C:HO4B:HO4C:HO5B:HO5C', '5* HRSoldier:5* CHS', 'Rebel Officer Leia Organa', 1, 65000, 96000, 142000, 203000, 280000, 372000, 140000, 140000, 140000, 140000, 210000, 210000, -720000, 18170000, 47970000, 0, 'These platoons affect both of *this* phase\'s middle and lower territories! In order to maximize our star count please do not engage either territory\'s battles until some platoons are full. **These platoons also affect the next two phase\'s middle and lower territories.'),
('HO4A', 'Forward Airspace', 'Hoth - Imperial Invasion', 4, 2, 2176000, 18700000, 29800000, 'Rebel Strafing Run', 'HO5B:HO5C', null, null, 1, 144000, 271000, 478000, 0, 0, 0, 160000, 160000, 160000, 160000, 160000, 160000, -5984000, 10540000, 21640000, 21640000, null),
('HO4B', 'Forward Trenches', 'Hoth - Imperial Invasion', 4, 1, 5220000, 34700000, 78100000, 'Rebel Supply Lines', 'HO5B:HO5C', 'Rebels:HRSoldier', null, 2, 76000, 111000, 163000, 232000, 319000, 423000, 160000, 160000, 160000, 160000, 240000, 240000, -3500000, 25980000, 69380000, 127060000, null),
('HO4C', 'Outer Pass', 'Hoth - Imperial Invasion', 4, 1, 5220000, 28300000, 62600000, 'Rebel Guerilla Strike', 'HO5B:HO5C', 'CHS:ROLO', '20 Guild Event Tokens', 1, 76000, 111000, 163000, 232000, 319000, 423000, 160000, 160000, 160000, 160000, 240000, 240000, 300000, 23380000, 57680000, 0, null),
('HO5A', 'Contested Airspace', 'Hoth - Imperial Invasion', 5, 2, 18000000, 34000000, 50000000, 'Rebel Strafing Run', 'HO6B:HO6C', null, null, 1, 208000, 335000, 536000, 0, 0, 0, 180000, 180000, 180000, 180000, 180000, 180000, 6520000, 22520000, 38520000, 38520000, null),
('HO5B', 'Snowfields', 'Hoth - Imperial Invasion', 5, 1, 14100000, 49300000, 89800000, 'AT-AT Assault', 'HO5B', 'Rebels:HRScout:CLS:Phoenix', '20 Guild Event Tokens', 2, 90000, 128000, 185000, 261000, 356000, 470000, 180000, 180000, 180000, 180000, 270000, 270000, 3840000, 39040000, 79540000, 145380000, 'These platoons affect *this* territory! In order to maximize our star count please do not engage this territory\'s battles until some platoons are full.'),
('HO5C', 'Forward Stronghold', 'Hoth - Imperial Invasion', 5, 1, 11100000, 41000000, 71600000, 'Rebel Guerilla Strike', 'HO6B:HO6C', null, null, 1, 90000, 128000, 185000, 261000, 356000, 470000, 180000, 180000, 180000, 180000, 270000, 270000, 5340000, 35240000, 65840000, 106690000, null),
('HO6A', 'Imperial Fleet Staging Area', 'Hoth - Imperial Invasion', 6, 2, 21600000, 40800000, 60000000, 'Orbital Bombardment', 'HO6B:HO6C', null, null, 1, 359000, 458000, 614000, 0, 0, 0, 200000, 200000, 200000, 200000, 200000, 200000, 2450000, 21650000, 40850000, 40850000, 'These platoons affect both of *this* phase\'s lower territories! In order to maximize our star count please do not engage either territory\'s battles until some platoons are full.'),
('HO6B', 'Imperial Flank', 'Hoth - Imperial Invasion', 6, 1, 31000000, 72000000, 100000000, 'AT-AT Assault', 'HO6B', 'Rebels:7* Rogue One', null, 2, 152000, 191000, 249000, 327000, 424000, 541000, 200000, 200000, 200000, 200000, 300000, 300000, 14400000, 55400000, 83400000, 155900000, 'These platoons affect *this* territory! In order to maximize our star count please do not engage this territory\'s battles until some platoons are full.'),
('HO6C', 'Imperial Landing', 'Hoth - Imperial Invasion', 6, 1, 26400000, 59300000, 81500000, 'AT-AT Assault', 'HO6C', '7* CHS:7* ROLO', '30 Guild Event Tokens', 1, 152000, 191000, 249000, 327000, 424000, 541000, 200000, 200000, 200000, 200000, 300000, 300000, 17400000, 50300000, 72500000, 72500000, 'These platoons affect *this* territory! In order to maximize our star count please do not engage this territory\'s battles until some platoons are full.');



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


INSERT INTO squads (squadID, name, raid, phase, char1, char2, char3, char4, char5, notes) VALUES
(3, 'Auto LightZader', 'rancor', 0, 'VADER', 'GRANDMOFFTARKIN', 'JYNERSO', 'TIEFIGHTERPILOT', 'DARTHSIDIOUS', 'Zeta: Vader'),
(4, 'Imperial Grancor Maneuver', 'rancor', 0, 'GRANDADMIRALTHRAWN', 'JYNERSO', 'SHORETROOPER', 'GRANDMOFFTARKIN', 'DIRECTORKRENNIC', ''),
(5, 'Rebel Power', 'rancor', 0, 'WEDGEANTILLES', 'JYNERSO', 'BAZEMALBUS', 'BISTAN', 'SCARIFREBEL', ''),
(6, 'Rebel Power Alt', 'rancor', 0, 'WEDGEANTILLES', 'JYNERSO', 'CASSIANANDOR', 'BISTAN', 'SCARIFREBEL', ''),
(7, 'SuperstaR2D2', 'rancor', 0, 'WEDGEANTILLES', 'R2D2_LEGENDARY', 'JYNERSO', 'CASSIANANDOR', 'BISTAN', ''),
(8, 'C.L.S.Teebo, D.I.S.C.O', 'rancor', 0, 'TEEBO', 'COMMANDERLUKESKYWALKER', 'CT7567', 'QUIGONJINN', 'R2D2_LEGENDARY', ''),
(9, 'CLS JynTwiggs', 'rancor', 0, 'TEEBO', 'COMMANDERLUKESKYWALKER', 'JYNERSO', 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', ''),
(10, 'Auto Rancgarborator', 'rancor', 0, 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', 'JYNERSO', 'HANSOLO', 'COMMANDERLUKESKYWALKER', 'Zeta: rhan'),
(11, 'Tenacity Jedi', 'haat', 1, 'AAYLASECURA', 'ANAKINKNIGHT', 'IMAGUNDI', 'QUIGONJINN', 'AHSOKATANO', ''),
(12, 'Counter Jedi', 'haat', 1, 'IMAGUNDI', 'AAYLASECURA', 'ANAKINKNIGHT', 'AHSOKATANO', 'GRANDMASTERYODA', ''),
(13, 'Qway Gon Fishin\'', 'haat', 1, 'QUIGONJINN', 'EZRABRIDGERS3', 'BARRISSOFFEE', 'ANAKINKNIGHT', 'IMAGUNDI', 'Zeta: QGJ'),
(14, 'Kenobi Sink', 'haat', 1, 'IMAGUNDI', 'GENERALKENOBI', 'JAWASCAVENGER', 'MAUL', 'FIRSTORDERTIEPILOT', ''),
(15, 'Retribution Party', 'haat', 1, 'ANAKINKNIGHT', 'GENERALKENOBI', 'JAWASCAVENGER', 'MAUL', 'FIRSTORDERTIEPILOT', ''),
(16, 'Rogue Teebo', 'haat', 1, 'TEEBO', 'JYNERSO', 'CASSIANANDOR', 'TIEFIGHTERPILOT', 'BARRISSOFFEE', 'Zeta: Barriss'),
(17, 'Nihilus', 'haat', 1, 'DARTHNIHILUS', 'BARRISSOFFEE', 'MAUL', 'SAVAGEOPRESS', 'COUNTDOOKU', 'Zeta: Barriss'),
(18, 'No Retreat', 'haat', 1, 'TEEBO', 'COMMANDERLUKESKYWALKER', 'FIRSTORDEROFFICERMALE', 'OLDBENKENOBI', 'BARRISSOFFEE', ''),
(19, 'No Retreat alt', 'haat', 1, 'No Retreat alt	TEEBO', 'COMMANDERLUKESKYWALKER', 'FIRSTORDEROFFICERMALE', 'BODHIROOK', 'EWOKELDER', ''),
(20, 'Zylo', 'haat', 1, 'KYLOREN', '', '', '', '', 'Zeta: Kylo'),
(21, 'Droids', 'haat', 2, 'HK47', 'IG88', 'IG86SENTINELDROID', 'CHIEFNEBIT', 'JAWAENGINEER', ''),
(22, 'Standard Clones', 'haat', 2, 'CC2224', 'CT210408', 'CT5555', 'CT7567', 'CLONESERGEANTPHASEI', 'Zeta: Cody'),
(23, 'Dathcha Zody', 'haat', 2, 'CC2224', 'CT210408', 'CT5555', 'CLONESERGEANTPHASEI', 'DATHCHA', 'Zeta: Cody'),
(24, 'Elder Zody', 'haat', 2, 'CC2224', 'CT210408', 'CT5555', 'CLONESERGEANTPHASEI', 'EWOKELDER', 'Zeta: Cody'),
(25, 'Imperial Troopers', 'haat', 2, 'VEERS', 'STORMTROOPER', 'MAGMATROOPER', 'DEATHTROOPER', 'SNOWTROOPER', 'Zeta: Veers'),
(26, 'Srp ZODY', 'haat', 2, 'CC2224', 'CT210408', 'CT5555', 'CLONESERGEANTPHASEI', 'SCARIFREBEL', 'Zeta: Cody, Fives'),
(27, 'Elder Zader', 'haat', 2, 'VADER', 'DARTHSIDIOUS', 'TUSKENSHAMAN', 'EWOKELDER', 'PHASMA', 'Zeta: Vader, Sidious'),
(28, 'Viva la Resistance!', 'haat', 2, 'FINN', 'REY', 'POE', 'RESISTANCEPILOT', 'RESISTANCETROOPER', 'Zeta: Finn'),
(29, 'The Fourth Order', 'haat', 2, 'PHASMA', 'KYLOREN', 'FIRSTORDEROFFICERMALE', 'FIRSTORDERTROOPER', 'FIRSTORDERTIEPILOT', 'Zeta: Phasma, Kylo, FOST, FOTP'),
(30, 'Scavanger Zody', 'haat', 2, 'CC2224', 'JAWASCAVENGER', 'CT210408', 'CT5555', 'CLONESERGEANTPHASEI', 'Zeta: Cody'),
(31, 'Imperial Grand Maneuver', 'haat', 3, 'GRANDADMIRALTHRAWN', 'JYNERSO', 'SHORETROOPER', 'GRANDMOFFTARKIN', 'DIRECTORKRENNIC', 'Zeta: Thrawn'),
(32, 'Viva la Poe!', 'haat', 3, 'FINN', 'POE', 'R2D2_LEGENDARY', 'RESISTANCEPILOT', 'RESISTANCETROOPER', 'Zeta: Finn'),
(33, 'Chirpatine', 'haat', 3, 'CHIEFCHIRPA', 'EMPERORPALPATINE', 'STORMTROOPERHAN', 'ROYALGUARD', 'SUNFAC', ''),
(34, 'Tiepatine', 'haat', 3, 'EMPERORPALPATINE', 'TIEFIGHTERPILOT', 'SUNFAC', 'STORMTROOPERHAN', 'ROYALGUARD', ''),
(35, 'Rogue 1', 'haat', 3, 'JYNERSO', 'CASSIANANDOR', 'BAZEMALBUS', 'CHIRRUTIMWE', 'BISTAN', 'Zeta: Jyn'),
(36, 'Darth Teebo', 'haat', 3, 'TEEBO', 'JYNERSO', 'BAZEMALBUS', 'CHIRRUTIMWE', 'DARTHNIHILUS', ''),
(37, 'Teebotine', 'haat', 3, 'TEEBO', 'EMPERORPALPATINE', 'TIEFIGHTERPILOT', 'STORMTROOPERHAN', 'ROYALGUARD', ''),
(38, 'RTFP', 'haat', 3, 'TEEBO', 'JYNERSO', 'BAZEMALBUS', 'CHIRRUTIMWE', 'TIEFIGHTERPILOT', ''),
(39, 'Chirpathrawn', 'haat', 3, 'CHIEFCHIRPA', 'EMPERORPALPATINE', 'GRANDADMIRALTHRAWN', 'SUNFAC', 'ROYALGUARD', 'Zeta: Thrawn'),
(40, 'Murder Bears', 'haat', 3, 'CHIEFCHIRPA', 'TEEBO', 'WICKET', 'LOGRAY', 'PAPLOO', 'Zeta: Chirpa, Wicket, Paploo'),
(41, 'Zavage', 'haat', 3, 'SAVAGEOPRESS', '', '', '', '', 'Zeta: Savage'),
(42, 'Standard Rebels', 'haat', 4, 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', 'ADMINISTRATORLANDO', 'ADMIRALACKBAR', 'PRINCESSLEIA', ''),
(43, 'Princess Zody', 'haat', 4, 'CC2224', 'CT5555', 'CT210408', 'CLONESERGEANTPHASEI', 'PRINCESSLEIA', 'Zeta: Cody, Fives'),
(44, 'Ima Gun Kill Ya', 'haat', 4, 'CC2224', 'CT5555', 'CT210408', 'CLONESERGEANTPHASEI', 'IMAGUNDI', ''),
(45, 'Chaze and Friends', 'haat', 4, 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', 'BAZEMALBUS', 'CHIRRUTIMWE', 'HANSOLO', ''),
(46, 'Phasma Rebels', 'haat', 4, 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', 'ADMIRALACKBAR', 'ADMINISTRATORLANDO', 'PHASMA', ''),
(47, 'First Order', 'haat', 4, 'PHASMA', 'FIRSTORDEROFFICERMALE', 'KYLOREN', 'FIRSTORDERTROOPER', 'FIRSTORDERTIEPILOT', ''),
(48, 'Viva la R2!', 'haat', 4, 'FINN', 'POE', 'REY', 'RESISTANCETROOPER', 'R2D2_LEGENDARY', 'Zeta: Finn, Rey'),
(49, 'Rebels Shot First', 'haat', 4, 'WEDGEANTILLES', 'BIGGSDARKLIGHTER', 'R2D2_LEGENDARY', 'HANSOLO', 'EZRABRIDGERS3', 'Zeta: rhan'),
(50, 'Imperial Troopers', 'haat', 4, 'VEERS', 'STORMTROOPER', 'MAGMATROOPER', 'DEATHTROOPER', 'SNOWTROOPER', 'Zeta: Veers'),
(51, 'JuggerMaul', 'haat', 4, 'DARTHSIDIOUS', 'EMPERORPALPATINE', 'MAUL', 'SITHASSASSIN', 'EWOKELDER', '');
