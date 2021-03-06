package fr.jedistar.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.utils.GuildUnitsSWGOHGGDataParser;
import fr.jedistar.utils.JaroWinklerDistance;


public class ModsCommand implements JediStarBotCommand {

	final static Logger logger = LoggerFactory.getLogger(JediStarBotCommand.class);

	private static final int MAX_LENGTH = 1950;
	private static final int MAX_ANSWERS = 3;

	private final String COMMAND;
	
	private static String APPROX_MATCHES_MESSAGE;

	private static String CHAR_MESSAGE;

	private static String HELP;
	private static String ERROR_MESSAGE;
	private static String PARAMS_ERROR;
	private static String ACCESS_ERROR;
	private static String JSON_ERROR;
	private static String MESSAGE_TOO_LONG;
	
	private final static Color EMBED_COLOR = Color.GREEN;
	
	private static String CHARACTERS_URL="http://swgoh.gg/characters/%s/";
	private static String CHARACTERS_SEPARATOR="-";


	//Nom des �l�ments dans le JSON de mods
	private final static String JSON_DATA = "data";
	private final static String JSON_NAME = "name";
	private final static String JSON_CHAR_NAME = "cname";
	private final static String JSON_SHORT = "short";
	private final static String JSON_SET1 = "set1";
	private final static String JSON_SET2 = "set2";
	private final static String JSON_SET3 = "set3";
	private final static String JSON_SQUARE = "square";
	private final static String JSON_ARROW = "arrow";
	private final static String JSON_DIAMOND = "diamond";
	private final static String JSON_TRIANGLE = "triangle";
	private final static String JSON_CIRCLE = "circle";
	private final static String JSON_CROSS = "cross";

	private static String JSON_URI = null;
	
	//Variables JSON de param�tres
	private final static String JSON_ERROR_MESSAGE = "errorMessage";
	private final static String JSON_MODS_COMMAND = "modsCommandParameters";
	private final static String JSON_MODS_COMMAND_COMMAND = "command";
	
	private final static String JSON_MODS_MESSAGES = "messages";
	private final static String JSON_MODS_MESSAGES_APPROX_MATCHES = "approxMatches";
	private final static String JSON_MODS_MESSAGES_CHAR_MODS = "characterMods";
	private final static String JSON_MODS_MESSAGES_HELP = "help";
	
	private final static String JSON_MODS_ERROR_MESSAGES = "errorMessages";
	private final static String JSON_MODS_ERROR_MESSAGES_PARAMS = "paramsError";
	private final static String JSON_MODS_ERROR_MESSAGES_ACCESS = "accessError";
	private final static String JSON_MODS_ERROR_MESSAGES_JSON = "jsonError";
	private final static String JSON_MODS_ERROR_MESSAGES_TOO_LONG = "tooLong";
	
	private final static String SQL_FIND_CHARS = "SELECT image FROM characters WHERE name=?";

	public static void setJsonUri(String uri) {
		JSON_URI = uri;
	}

	public ModsCommand() {
		super();
		
		JSONObject parameters = StaticVars.jsonSettings;

		//messages de base
		ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

		//Param�tres propres aux mods
		JSONObject modsParams = parameters.getJSONObject(JSON_MODS_COMMAND);
		
		COMMAND = modsParams.getString(JSON_MODS_COMMAND_COMMAND);

		//Messages
		JSONObject messages = modsParams.getJSONObject(JSON_MODS_MESSAGES);
		APPROX_MATCHES_MESSAGE = messages.getString(JSON_MODS_MESSAGES_APPROX_MATCHES);
		CHAR_MESSAGE = messages.getString(JSON_MODS_MESSAGES_CHAR_MODS);
		HELP = messages.getString(JSON_MODS_MESSAGES_HELP);
		
		//Messages d'erreur
		JSONObject errorMessages = modsParams.getJSONObject(JSON_MODS_ERROR_MESSAGES);
		PARAMS_ERROR = errorMessages.getString(JSON_MODS_ERROR_MESSAGES_PARAMS);
		ACCESS_ERROR = errorMessages.getString(JSON_MODS_ERROR_MESSAGES_ACCESS);
		JSON_ERROR = errorMessages.getString(JSON_MODS_ERROR_MESSAGES_JSON);
		MESSAGE_TOO_LONG = errorMessages.getString(JSON_MODS_ERROR_MESSAGES_TOO_LONG);
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}

	
	@Override
	public CommandAnswer answer(DiscordAPI api, List<String> params,Message messageRecu,boolean isAdmin) {

		if(params.size() == 0) {
			return error(HELP);
		}
		
		String requestedCharacterName = String.join(" ",params).toLowerCase();
		
		try {
			JSONObject modsJsonRoot = getHttpJsonFile();
			
			JSONArray dataArray = modsJsonRoot.getJSONArray(JSON_DATA);
			
			List<Match> exactMatches = new ArrayList<Match>();
			List<Match> approxMatches = new ArrayList<Match>();
				
			String currentMatchCharName = null;
			boolean singleMatch = true;
			
			GuildUnitsSWGOHGGDataParser.parseCharacters();
			
			//Itérer sur les personnages présents dans le json
			for(int i=0;i<dataArray.length();i++) {
				JSONObject charData = dataArray.getJSONObject(i);
				
				try {
					//Comparer le nom du personnage avec la recherche
					String charName = charData.getString(JSON_CHAR_NAME).toLowerCase();			
					String charShortName = charData.getString(JSON_SHORT).toLowerCase();
					Double jaroWinkler = new JaroWinklerDistance().apply(requestedCharacterName, charName);			

					Match match = new Match();
					match.score = jaroWinkler;
					match.value = formatMessageForChar(charData);
					match.charName = charData.getString(JSON_CHAR_NAME);
					match.variantName = charData.getString(JSON_NAME);		

					//Correspondances exactes
					if(requestedCharacterName.length() > 2 && (charName.contains(requestedCharacterName) || requestedCharacterName.contains(charName))) {
						exactMatches.add(match);

						singleMatch = singleMatch && (currentMatchCharName == null || currentMatchCharName.equals(match.charName));
						currentMatchCharName = match.charName;
					}
					else if(charShortName.contains(requestedCharacterName)) {
						exactMatches.add(match);

						singleMatch = singleMatch && currentMatchCharName != null && currentMatchCharName.equals(match.charName);
						currentMatchCharName = match.charName;
					}
					//Correspondance approximative				
					else if(jaroWinkler > 0) {
						approxMatches.add(match);
					}
				}
				catch(JSONException e) {
					logger.warn("Error in mods JSON for character :");
					logger.warn(charData.toString());
				}
			}
			
			String message = "";
						
			Collections.sort(exactMatches);
			
			//Liste de noms uniques dans exactMatches
			List<String> chars = new ArrayList<String>();
			for(Match match : exactMatches) {
				if(!chars.contains(match.charName)) {
					chars.add(match.charName);
				}
			}
			chars.sort(String::compareToIgnoreCase);
			
		
			//Si trop de réponses, on renvoie simplement la liste de noms
			if(!singleMatch && chars.size() > MAX_ANSWERS) {
				message = MESSAGE_TOO_LONG;
				
				
				for(String charName : chars) {
					message += charName+"\r\n";
				}
			}
			else {
				//sinon, on renvoi la réponse détaillée
				if(!exactMatches.isEmpty()) {
					
					Map<String,List<Match>> variantsPerChar = new HashMap<String,List<Match>>();
					for(Match match : exactMatches) {
						
						if(variantsPerChar.get(match.charName) == null) {
							variantsPerChar.put(match.charName,new ArrayList<Match>());		
						}
						
						variantsPerChar.get(match.charName).add(match);
						
					}
					
					for(String charName : variantsPerChar.keySet()) {
						List<Match> variantsForThisChar = variantsPerChar.get(charName);
						String url = getCharacterURL(charName);
						String portraitUrl = getCharacterPortraitURL(charName);
						EmbedBuilder embed = new EmbedBuilder();
						embed.setColor(EMBED_COLOR);
						embed.setAuthor(charName, url, portraitUrl);
						embed.setThumbnail(portraitUrl);

						for(Match match : variantsForThisChar) {
							String title = variantsForThisChar.size() == 1 ? "-" : match.variantName;
							embed.addField(title, match.value, true);
						}
						
						embed.addField("-","Mods advised by [Crouching Rancor](http://apps.crouchingrancor.com)\r\nBot designed by [JediStar](https://jedistar.jimdo.com)", false);
						//On contourne le chemin de réponse habituel pour pouvoir retourner plusieurs embeds d'un coup
						messageRecu.reply(null, embed);
					}
					
				}
				
			}
			
			
			//Si pas de corresp. exactes, on renvoie les correspondances approx., en baissant progressivement le niveau de tolérance
			if(exactMatches.isEmpty() && !approxMatches.isEmpty()) {
				message += APPROX_MATCHES_MESSAGE;
						
				Set<String> approxChars = new HashSet<String>();
				for(Match match : approxMatches) {
					approxChars.add(match.charName);
				}
				
				Collections.sort(approxMatches);
					
				boolean nothingFound = true;
				
				for(Double currentThreshold = 0.7 ; currentThreshold > 0 && nothingFound; currentThreshold -= 0.2) {
					for(Match approx : approxMatches) {
						
						if(message.length() + approx.charName.length() > MAX_LENGTH) {
							break;
						}
						
						if(approx.score < currentThreshold) {
							break;
						}
						
						//Utiliser la liste approxChars pour ne pas renvoyer deux fois la m�me r�ponse
						if(approxChars.contains(approx.charName)) {
							message += approx.charName+"\r\n";			
							nothingFound = false;
							approxChars.remove(approx.charName);
						}
						
					}
				}
				
			}
	
			return new CommandAnswer(message,null);
		}
		catch (MalformedURLException|UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return error(PARAMS_ERROR);
		} 
		catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return error(ACCESS_ERROR);
		}
		catch (JSONException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return error(JSON_ERROR);
		}
	}

	/**
	 * Lit le fichier JSON via HTTP
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private JSONObject getHttpJsonFile() throws MalformedURLException, IOException, UnsupportedEncodingException {
				
		String json = GuildUnitsSWGOHGGDataParser.retrieveJSONfromURL(JSON_URI);
		return new JSONObject(json);
		
	}
	
	/**
	 * Remplit le message avec les données contenues dans le JsonObject
	 * @param charData
	 * @return le message formaté
	 */
	private String formatMessageForChar(JSONObject charData) {
		return String.format(CHAR_MESSAGE,
								charData.get(JSON_SET1),
								charData.get(JSON_SET2),
								charData.get(JSON_SET3),
								charData.get(JSON_SQUARE),
								charData.get(JSON_ARROW),
								charData.get(JSON_DIAMOND),
								charData.get(JSON_TRIANGLE),
								charData.get(JSON_CIRCLE),
								charData.get(JSON_CROSS)
							);
	}
	
	private String getCharacterURL(String charName) {
		return String.format(CHARACTERS_URL, charName.replace(" ", CHARACTERS_SEPARATOR).toLowerCase());
	}
	
	private String getCharacterPortraitURL(String charName) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_FIND_CHARS);
			
			stmt.setString(1, charName);
			
			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			if(rs.next()) 
			{
				return rs.getString(1);
			}
			
			return null;
		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private CommandAnswer error(String errorMessage) {
		String message = ERROR_MESSAGE +"**"+ errorMessage + "**\r\n\r\n"+ HELP;
		
		return new CommandAnswer(message, null);
	}
	
	private class Match implements Comparable<Match>{

		public String value;
		public String charName;
		public Double score;
		public String variantName;
		
		@Override
		public int compareTo(Match other) {			
			return -1 * (int) ((this.score - other.score)*1000);
		}
		
	}

}
