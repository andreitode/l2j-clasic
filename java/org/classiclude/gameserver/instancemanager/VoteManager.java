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
package org.classiclude.gameserver.instancemanager;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.classiclude.Config;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.data.sql.IndividualVoteTable;
import org.classiclude.gameserver.data.xml.VoteSiteData;
import org.classiclude.gameserver.enums.VoteSite;
import org.classiclude.gameserver.handler.VoteHandler;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.votesystem.IndividualVote;
import org.classiclude.gameserver.model.votesystem.IndividualVoteResponse;
import org.classiclude.gameserver.model.votesystem.RewardVote;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.ItemList;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;

public final class VoteManager extends VoteHandler
{
	private ScheduledFuture<?> _updateIndividualVotes;
	
	private Map<String, IndividualVote[]> _foundVoters;
	
	public VoteManager()
	{
		_foundVoters = new ConcurrentHashMap<>();
		loadVotes();
		stopAutoTasks();
		
		if (Config.CUSTOM_VOTE_ENABLE)
		{
			_updateIndividualVotes = ThreadPool.scheduleAtFixedRate(new AutoUpdateIndividualVotesTask(), 300000, Config.CUSTOM_VOTE_UPDATE);
		}
	}
	
	private void stopAutoTasks()
	{
		if (_updateIndividualVotes != null)
		{
			_updateIndividualVotes.cancel(true);
			_updateIndividualVotes = null;
		}
	}
	
	@SuppressWarnings("null")
	public void getReward(Player player, int ordinalSite)
	{
		String ip = existIp(player);
		if (ip == null)
		{
			return;
		}
		IndividualVoteResponse ivr = getIndividualVoteResponse(ordinalSite, ip, player.getAccountName());
		if (ivr == null)
		{
			player.sendMessage("We were unable to verify your vote with: " + VoteSiteData.getInstance().getSiteName(ordinalSite) + ", please try again");
			return;
		}
		if (getTimeRemaining(new IndividualVote(ip, ivr.getVoteSiteTime(), ordinalSite, false)) < 0)
		{
			player.sendMessage("We were unable to verify your vote with: " + VoteSiteData.getInstance().getSiteName(ordinalSite) + ", please try again");
			return;
		}
		if (!ivr.getIsVoted())
		{
			player.sendMessage(String.format("You haven't vote on %s yet!", VoteSiteData.getInstance().getSiteName(ordinalSite)));
			return;
		}
		if (!checkIndividualAvailableVote(player, ordinalSite))
		{
			player.sendMessage(String.format("You can get the reward again on %s at %s", VoteSiteData.getInstance().getSiteName(ordinalSite), getTimeRemainingWithSampleFormat(player, ordinalSite)));
			return;
		}
		
		IndividualVote[] aiv;
		if (!_foundVoters.containsKey(ip))
		{
			aiv = new IndividualVote[VoteSite.values().length];
			aiv[ordinalSite] = new IndividualVote(ip, ivr.getDiffTime(), ivr.getVoteSiteTime(), ordinalSite, true);
			_foundVoters.put(ip, aiv);
			for (RewardVote reward : VoteSiteData.getInstance().getRewards(ordinalSite))
			{
				player.getInventory().addItem("VoteSystem", reward.getItemId(), reward.getItemCount(), player, null);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(reward.getItemId());
				sm.addInt(reward.getItemCount());
				player.sendPacket(sm);
			}
			player.sendMessage(String.format("%s: Thank you for voting for our server, your reward has been delivered.", VoteSiteData.getInstance().getSiteName(ordinalSite)));
			player.sendPacket(new ItemList(player, true));
		}
		else
		{
			IndividualVote iv = (_foundVoters.get(ip) != null) ? _foundVoters.get(ip)[ordinalSite] : null;
			if ((iv == null) || ((iv != null) && !iv.getAlreadyRewarded()))
			{
				_foundVoters.get(ip)[ordinalSite] = new IndividualVote(ip, ivr.getDiffTime(), ivr.getVoteSiteTime(), ordinalSite, true);
				for (RewardVote reward : VoteSiteData.getInstance().getRewards(ordinalSite))
				{
					player.getInventory().addItem("VoteSystem", reward.getItemId(), reward.getItemCount(), player, null);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
					sm.addItemName(reward.getItemId());
					sm.addLong(reward.getItemCount());
					player.sendPacket(sm);
				}
				player.sendMessage(String.format("%s: Thank you for voting for our server, your reward has been delivered.", VoteSiteData.getInstance().getSiteName(ordinalSite)));
				player.sendPacket(new ItemList(player, true));
			}
			else
			{
				player.sendMessage(String.format("%s: Alredy rewarded Available on %s", VoteSiteData.getInstance().getSiteName(ordinalSite), getTimeRemainingWithSampleFormat(player, ordinalSite)));
			}
		}
	}
	
	public boolean checkIndividualAvailableVote(Player player, int ordinalSite)
	{
		String ip = existIp(player);
		if (_foundVoters.containsKey(ip))
		{
			IndividualVote[] ivs = _foundVoters.get(ip);
			if (ivs[ordinalSite] == null)
			{
				return true;
			}
			if (ivs[ordinalSite] != null)
			{
				IndividualVote iv = ivs[ordinalSite];
				if (getTimeRemaining(iv) < 0)
				{
					return true;
				}
			}
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	public long getTimeRemaining(IndividualVote iv)
	{
		long timeRemaining = 0L;
		timeRemaining = ((iv.getVotingTimeSite() + (Config.CUSTOM_VOTE_INTERVAL_TOPSITES * 3600000)) - ((iv.getDiffTime() > 0) ? (System.currentTimeMillis() + iv.getDiffTime()) : (System.currentTimeMillis() - iv.getDiffTime())));
		return timeRemaining;
	}
	
	public String getTimeRemainingWithSampleFormat(Player player, int ordinalSite)
	{
		String ip = existIp(player);
		String timeRemainingWithSampleFormat = "";
		if (_foundVoters.containsKey(ip))
		{
			IndividualVote[] ivs = _foundVoters.get(ip);
			if (ivs[ordinalSite] != null)
			{
				IndividualVote iv = ivs[ordinalSite];
				long timeRemaining = getTimeRemaining(iv);
				if (timeRemaining > 0)
				{
					timeRemainingWithSampleFormat = CalculateTimeRemainingWithSampleDateFormat(timeRemaining);
					return timeRemainingWithSampleFormat;
				}
			}
		}
		return timeRemainingWithSampleFormat;
	}
	
	public String CalculateTimeRemainingWithSampleDateFormat(long timeRemaining)
	{
		long t = timeRemaining / 1000;
		int hours = Math.round(((t / 3600) % 24));
		int minutes = Math.round((t / 60) % 60);
		int seconds = Math.round(t % 60);
		return String.format("%sH:%sm:%ss", hours, minutes, seconds);
	}
	
	public String existIp(Player p)
	{
		
		GameClient client = p.getClient();
		if ((client.getConnection() != null) && (client.getPlayer() != null) && !client.isDetached())
		{
			try
			{
				return client.getIp();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	public final void loadVotes()
	{
		_foundVoters = IndividualVoteTable.getInstance().getVotesDB();
	}
	
	public void saveVotes()
	{
		IndividualVoteTable.getInstance().SaveVotes(_foundVoters);
	}
	
	protected synchronized void AutoUpdateIndividualVotes()
	{
		AutoCleanInnecesaryIndividualVotes();
		IndividualVoteTable.getInstance().SaveVotes(_foundVoters);
	}
	
	protected synchronized void AutoCleanInnecesaryIndividualVotes()
	{
		HashSet<IndividualVote> removeVotes = new HashSet<>();
		for (Map.Entry<String, IndividualVote[]> ivs : _foundVoters.entrySet())
		{
			for (IndividualVote individualvote : ivs.getValue())
			{
				if (individualvote == null)
				{
					continue;
				}
				if (getTimeRemaining(individualvote) < 0)
				{
					removeVotes.add(individualvote);
					if (_foundVoters.containsKey(individualvote.getVoterIp()))
					{
						if (_foundVoters.get(individualvote.getVoterIp())[individualvote.getVoteSite()] != null)
						{
							_foundVoters.get(individualvote.getVoterIp())[individualvote.getVoteSite()] = null;
						}
					}
				}
			}
		}
		IndividualVoteTable.getInstance().DeleteVotes(removeVotes);
	}
	
	public void Shutdown()
	{
		AutoCleanInnecesaryIndividualVotes();
		AutoUpdateIndividualVotes();
	}
	
	protected class AutoUpdateIndividualVotesTask implements Runnable
	{
		
		@Override
		public void run()
		{
			AutoUpdateIndividualVotes();
			
		}
		
	}
	
	public static VoteManager getInatance()
	{
		return SingleHolder.INSTANCE;
	}
	
	private static class SingleHolder
	{
		protected static final VoteManager INSTANCE = new VoteManager();
	}
}