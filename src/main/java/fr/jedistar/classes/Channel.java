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

import com.vdurmont.emoji.EmojiManager;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.impl.ImplReaction;

import fr.jedistar.commands.SetUpCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.PendingAction;
import fr.jedistar.listener.JediStarBotReactionAddListener;

/**
 * @author shittybill
 *
 */
public class Channel {

	final static Logger logger = LoggerFactory.getLogger(SetUpCommand.class);

	private static final String INSERT_CHANNEL_REQUEST = "INSERT INTO guild VALUES (?, ?, ?, ?)";
	private static final String SELECT_CHANNEL_REQUEST = "SELECT * FROM guild WHERE channelID=?";
	private static final String UPDATE_CHANNEL_REQUEST = "UPDATE guild SET guildID=?, tbAssistant=?, webhook=? WHERE channelID=?";
	private static final String DEACTIVATE_TBASSISTANT_BY_GUILD_REQUEST = "UPDATE guild SET tbAssistant=0 WHERE guildID=?;";
	
	public String channelID;
	public Integer guildID;
	public boolean tbAssistant;
	public String webhook;
	public boolean saved;
	
	public Channel( String channelID ) {
		this.channelID = channelID;
		this.guildID = 0;
		this.tbAssistant = false;
		this.webhook = null;
		
		this.saved = this.getChannelByID( channelID );
	}
	
	public boolean saveChannel() {
		return this.saved ? this.updateChannel() : this.insertChannel();
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
	
	private boolean updateChannel() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			if( this.tbAssistant ) {
				this.deactivateTBAForGuild();
			}			
			
			stmt = conn.prepareStatement(UPDATE_CHANNEL_REQUEST);
			
			//UPDATE
			stmt.setInt(1,this.guildID);
			stmt.setBoolean(2,this.tbAssistant);
			stmt.setString(3,webhook);
			stmt.setString(4,this.channelID);
			
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
	
	
	private boolean insertChannel() {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(INSERT_CHANNEL_REQUEST);
			
			//INSERT
			stmt.setString(1,this.channelID);
			stmt.setInt(2,this.guildID);
			stmt.setBoolean(3,this.tbAssistant);
			stmt.setString(4,webhook);
			
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
