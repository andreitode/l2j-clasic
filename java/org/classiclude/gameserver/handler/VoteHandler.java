/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.classiclude.gameserver.handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import org.classiclude.Config;
import org.classiclude.gameserver.data.xml.VoteSiteData;
import org.classiclude.gameserver.enums.VoteSite;
import org.classiclude.gameserver.model.votesystem.IndividualVoteResponse;
import org.classiclude.gameserver.model.votesystem.VoteUtil;

public class VoteHandler
{
	public static final Logger LOGGER = Logger.getLogger(VoteHandler.class.getName());
	
	protected static String getNetWorkResponse(String URL, int ordinal)
	{
		if ((ordinal == VoteSite.L2NETWORK.ordinal()) && ("".equals(Config.CUSTOM_VOTE_API_L2NET) || "".equals(Config.CUSTOM_VOTE_URL_L2NET) || "".equals(Config.CUSTOM_VOTE_UNA_L2NET)))
		{
			return "";
		}
		
		StringBuffer response = new StringBuffer();
		try
		{
			String API_URL = Config.CUSTOM_VOTE_URL_L2NET;
			String detail = URL;
			String postParameters = "";
			postParameters += "apiKey=" + VoteUtil.between("apiKey=", detail, "&type=");
			postParameters += "&type=" + VoteUtil.between("&type=", detail, "&player");
			String beginIndexPlayer = "&player=";
			String player = detail.substring(detail.indexOf(beginIndexPlayer) + beginIndexPlayer.length());
			
			if ((player != null) && !player.equals(""))
			{
				postParameters += "&player=" + player;
			}
			
			byte[] postData = postParameters.getBytes(Charset.forName("UTF-8"));
			URL url = URI.create(API_URL).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Length", Integer.toString(postData.length));
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setDoOutput(true);
			
			DataOutputStream os = new DataOutputStream(con.getOutputStream());
			os.write(postData);
			os.flush();
			os.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();
			
			return response.toString();
			
		}
		catch (Exception e)
		{
			LOGGER.warning(VoteUtil.Sites[ordinal] + " Say: An error ocurred " + e.getCause());
			return "";
		}
	}
	
	protected static String getResponse(String Url, int ordinal)
	{
		if ((ordinal == VoteSite.L2NETWORK.ordinal()) && ("".equals(Config.CUSTOM_VOTE_API_L2NET) || "".equals(Config.CUSTOM_VOTE_URL_L2NET) || "".equals(Config.CUSTOM_VOTE_URL_L2NET)))
		{
			return "";
		}
		
		try
		{
			int responseCode = 0;
			URL objUrl = URI.create(Url).toURL();
			HttpURLConnection con = (HttpURLConnection) objUrl.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setConnectTimeout(5000);
			responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK)
			{
				String inputLine;
				StringBuffer response = new StringBuffer();
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = in.readLine()) != null)
				{
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			}
			
		}
		catch (Exception e)
		{
			LOGGER.warning(VoteSiteData.getInstance().getSiteName(ordinal) + " Say: An error ocurred " + e.getCause());
			return "";
		}
		
		return "";
	}
	
	public static IndividualVoteResponse getIndividualVoteResponse(int ordinal, String ip, String AccountName)
	{
		String response = "";
		boolean isVoted = false;
		long voteSiteTime = 0L, diffTime = 0L;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		switch (ordinal)
		{
			case 0:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"already_voted\":", response, ",\"vote_time\""));
				if (isVoted)
				{
					try
					{
						voteSiteTime = format.parse(VoteUtil.between("\"vote_time\":\"", response, "\",\"server_time\"")).getTime();
						diffTime = System.currentTimeMillis() - format.parse(VoteUtil.between("\"server_time\":\"", response, "\"}")).getTime();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
				}
				break;
			
			case 1:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"isvoted\":", response.toString().toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), ",\"votetime").replaceAll("\"", ""));
				if (isVoted)
				{
					try
					{
						voteSiteTime = (Long.parseLong(VoteUtil.between("\"votetime\":", response.toString().toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), ",\"servertime"))) * 1000;
						diffTime = System.currentTimeMillis() - ((Long.parseLong(VoteUtil.between("\"servertime\":", response.toLowerCase().replaceAll("\n", "").replaceAll(" ", ""), "}"))) * 1000);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				break;
			
			case 2:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(response);
				if (isVoted)
				{
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
				}
				break;
			
			case 3:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = ((VoteUtil.between("\"status\":\"", response, "\",\"date\"") != "") && (Integer.parseInt(VoteUtil.between("\"status\":\"", response, "\",\"date\"")) == 1)) ? true : false;
				if (isVoted)
				{
					String dateString = VoteUtil.between("\"date\":\"", response, "\"}]");
					try
					{
						voteSiteTime = System.currentTimeMillis();
						diffTime = System.currentTimeMillis() - format.parse(dateString).getTime();
						diffTime = 0;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}
				break;
			
			case 4:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"voted\":", response, ",\"voteTime\""));
				if (isVoted)
				{
					try
					{
						voteSiteTime = format.parse(VoteUtil.between("\"voteTime\":\"", response, "\",\"hopzoneServerTime\"")).getTime();
						diffTime = System.currentTimeMillis() - format.parse(VoteUtil.between("\"hopzoneServerTime\":\"", response, "\",\"status_code\":")).getTime();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
				}
				break;
			
			case 5:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (!"".equals(response) && (Integer.parseInt(response) == 1)) ? true : false;
				if (isVoted)
				{
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
				}
				break;
			
			case 6:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = ("".equals(response)) ? false : (Integer.parseInt(VoteUtil.between("\"status\":", response, ",\"server_time\"").replaceAll("\"", "")) == 1) ? true : false;
				if (isVoted)
				{
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
					
					try
					{
						voteSiteTime = format.parse(VoteUtil.between("\"date\":\"", response, "\",\"status\"")).getTime();
						diffTime = System.currentTimeMillis() - format.parse(VoteUtil.between("\"server_time\":\"", response, "\",\"hours_since_vote\":")).getTime();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				break;
			case 7:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"is_voted\":", response, ",\"vote_time\""));
				if (isVoted)
				{
					try
					{
						voteSiteTime = (Long.parseLong(VoteUtil.between("\"vote_time\":", response, ",\"server_time\""))) * 1000;
						diffTime = System.currentTimeMillis() - (Long.parseLong(VoteUtil.between("\"server_time\":", response, "}}")) * 1000);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				break;
			
			case 8:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"isVoted\": ", response, ",\"voteTime\""));
				if (isVoted)
				{
					voteSiteTime = Long.parseLong(VoteUtil.between("\"voteTime\": \"", response, "\",\"serverTime\"")) * 1000;
					diffTime = System.currentTimeMillis() - (Long.parseLong(VoteUtil.between("\"serverTime\": ", response, "}}")) * 1000);
				}
				break;
			
			case 9:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(response);
				if (isVoted)
				{
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
				}
				break;
			
			case 10:
				response = getResponse(getIndividualUrl(ordinal, ip, null), ordinal);
				isVoted = (response == "") ? false : Boolean.parseBoolean(VoteUtil.between("\"voted\":", response, "}"));
				if (isVoted)
				{
					voteSiteTime = System.currentTimeMillis();
					diffTime = 0;
				}
				break;
			
		}
		if (!response.equals(""))
		{
			return new IndividualVoteResponse(isVoted, diffTime, voteSiteTime);
		}
		return null;
	}
	
	public static String getIndividualUrl(int ordinal, String ip, String AccountName)
	{
		String url = "";
		ip = (Config.CUSTOM_VOTE_TESTIP.equalsIgnoreCase("off") || Config.CUSTOM_VOTE_TESTIP.equalsIgnoreCase("")) ? ip : Config.CUSTOM_VOTE_TESTIP;
		switch (ordinal)
		{
			case 0:
				// l2.topgameserver.net
				url = String.format("%sAPI_KEY=%s/getData/%s", Config.CUSTOM_VOTE_URL_TGS, Config.CUSTOM_VOTE_API_TGS, ip);
				break;
			case 1:
				// itopz.com
				url = String.format("%s%s/%s/%s", Config.CUSTOM_VOTE_URL_ITZ, Config.CUSTOM_VOTE_API_ITZ, Config.CUSTOM_VOTE_SID_ITZ, ip);
				break;
			
			case 2:
				// l2top.co
				url = String.format("%sVoteCheck.php?id=%s&ip=%s", Config.CUSTOM_VOTE_URL_TCO, Config.CUSTOM_VOTE_SID_TCO, ip);
				break;
			
			case 3:
				// l2votes.com
				url = String.format("%sapi.php?apiKey=%s&ip=%s", Config.CUSTOM_VOTE_URL_L2VOTE, Config.CUSTOM_VOTE_SID_L2VOTE, ip);
				break;
			
			case 4:
				// hopzone.net
				url = String.format("%svote?token=%s&ip_address=%s", Config.CUSTOM_VOTE_URL_HOPZONE, Config.CUSTOM_VOTE_API_HOPZONE, ip);
				break;
			
			case 5:
				// l2network.eu
				url = String.format("%s?a=in&u=%s&ipc=%s", Config.CUSTOM_VOTE_URL_L2NET, Config.CUSTOM_VOTE_UNA_L2NET, ip);
				break;
			
			case 6:
				// top.l2jbrasil.com
				url = String.format("%susername=%s&ip=%s&type=json", Config.CUSTOM_VOTE_URL_BRASIL, Config.CUSTOM_VOTE_UNA_BRASIL, ip);
				break;
			
			case 7:
				// mmotop
				url = String.format("%s%s/ip/%s", Config.CUSTOM_VOTE_URL_MMOTOP, Config.CUSTOM_VOTE_API_MMOTOP, ip);
				break;
			
			case 8:
				// topzone.com
				url = String.format("%svote?token=%s&ip=%s", Config.CUSTOM_VOTE_URL_L2TOPZONE, Config.CUSTOM_VOTE_API_L2TOPZONE, ip);
				break;
			
			case 9:
				// l2servers.com
				url = String.format("%scheckip.php?hash=%s&server_id=%s&ip=%s", Config.CUSTOM_VOTE_URL_L2SERVERS, Config.CUSTOM_VOTE_API_L2SERVERS, Config.CUSTOM_VOTE_SID_L2SERVERS, ip);
				break;
			
			case 10:
				// www.top100arena.com/
				url = String.format("%scheck_ip/%s?ip=%s", Config.CUSTOM_VOTE_URL_TOP100ARENA, Config.CUSTOM_VOTE_SID_TOP100ARENA, ip);
				break;
		}
		
		return url;
	}
}