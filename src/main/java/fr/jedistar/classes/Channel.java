/**
 * 
 */
package fr.jedistar.classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.jedistar.StaticVars;
import fr.jedistar.commands.SetUpCommand;

/**
 * @author Nathan
 *
 */
public class Channel {

	final static Logger logger = LoggerFactory.getLogger(SetUpCommand.class);

	private static final String SAVE_CHANNEL_REQUEST = "INSERT INTO guild (channelID, guildID, tbAssistant, webhook, alertRole) VALUES ( ?,?,?,?,? ) ON DUPLICATE KEY UPDATE guildID=?, tbAssistant=?, webhook=?, alertRole=?";
	private static final String SELECT_CHANNEL_REQUEST = "SELECT * FROM guild WHERE channelID=?";
	private static final String DEACTIVATE_TBASSISTANT_BY_GUILD_REQUEST = "UPDATE guild SET tbAssistant=0 WHERE guildID=?;";
	
	public String channelID;
	public Integer guildID;
	public boolean tbAssistant;
	public String webhook;
	public String alertRole;
	public boolean saved;
	
	public Channel( String channelID ) {
		this.channelID = channelID;
		this.guildID = 0;
		this.tbAssistant = false;
		this.webhook = null;
		this.alertRole = null;
		
		this.saved = this.getChannelByID( channelID );
	}
	
	
	public boolean saveChannel() {
		this.saved = this.saveNewChannel();
		return this.saved;
	}
	
	
	private boolean saveNewChannel() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			if( this.tbAssistant ) {
				this.deactivateTBAForGuild();
			}
			
			stmt = conn.prepareStatement(SAVE_CHANNEL_REQUEST);
			
			//INSERT
			stmt.setString(1,this.channelID);
			stmt.setInt(2,this.guildID);
			stmt.setBoolean(3,this.tbAssistant);
			stmt.setString(4,this.webhook);
			stmt.setString(5,this.alertRole);
			//UPDATE
			stmt.setInt(6,this.guildID);
			stmt.setBoolean(7,this.tbAssistant);
			stmt.setString(8,this.webhook);
			stmt.setString(9,this.alertRole);			
			
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
	
	
	private boolean deactivateTBAForGuild() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(DEACTIVATE_TBASSISTANT_BY_GUILD_REQUEST);
			stmt.setInt(1,this.guildID);
			
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
	
	
	public boolean getChannelByID( String channelID ) {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SELECT_CHANNEL_REQUEST);
			
			stmt.setString(1,channelID);
			
			logger.debug("Executing query : "+stmt.toString());
			rs = stmt.executeQuery();
			
			if(rs.next()) {
				this.channelID = rs.getString("channelID");
				this.guildID = rs.getInt("guildID");
				this.tbAssistant = rs.getBoolean("tbAssistant");
				this.webhook = rs.getString("webhook");
				this.alertRole = rs.getString("alertRole");
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

	
}
