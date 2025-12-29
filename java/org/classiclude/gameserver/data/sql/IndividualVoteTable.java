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
package org.classiclude.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.gameserver.enums.VoteSite;
import org.classiclude.gameserver.model.votesystem.IndividualVote;

public final class IndividualVoteTable
{
	private static final Logger LOGGER = Logger.getLogger(IndividualVoteTable.class.getName());
	private final Map<String, IndividualVote[]> _votes;
	private Statement st;
	private Connection con;
	
	private IndividualVoteTable()
	{
		_votes = new HashMap<>();
		loadVotes();
	}
	
	public void loadVotes()
	{
		
		_votes.clear();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded FROM individualvotes");
			ResultSet rs = ps.executeQuery();)
		{
			IndividualVote[] ivs = new IndividualVote[VoteSite.values().length];
			while (rs.next())
			{
				IndividualVote iv = new IndividualVote(rs.getString("voterIp"), rs.getLong("diffTime"), rs.getLong("votingTimeSite"), rs.getInt("voteSite"), rs.getBoolean("alreadyRewarded"));
				if (_votes.containsKey(iv.getVoterIp()))
				{
					if (_votes.get(iv.getVoterIp())[iv.getVoteSite()] == null)
					{
						ivs[iv.getVoteSite()] = iv;
						_votes.replace(iv.getVoterIp(), ivs);
					}
				}
				else
				{
					ivs[iv.getVoteSite()] = iv;
					_votes.put(iv.getVoterIp(), ivs);
					
				}
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void SaveVotes(Map<String, IndividualVote[]> votes)
	{
		
		if (votes == null)
		{
			return;
		}
		if (votes.size() == 0)
		{
			return;
		}
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO individualvotes(voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE " + "voterIp = VALUES(voterIp), voteSite = VALUES(voteSite), diffTime = VALUES(diffTime), votingTimeSite = VALUES(votingTimeSite),alreadyRewarded = VALUES(alreadyRewarded)");)
		{
			
			for (Map.Entry<String, IndividualVote[]> ivm : votes.entrySet())
			{
				for (IndividualVote iv : ivm.getValue())
				{
					if (iv == null)
					{
						continue;
					}
					ps.setString(1, iv.getVoterIp());
					ps.setInt(2, iv.getVoteSite());
					ps.setLong(3, iv.getDiffTime());
					ps.setLong(4, iv.getVotingTimeSite());
					ps.setBoolean(5, iv.getAlreadyRewarded());
					ps.addBatch();
				}
			}
			ps.executeBatch();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void SaveVote(IndividualVote vote)
	{
		
		if (vote == null)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO individualvotes(voterIp,voteSite,diffTime,votingTimeSite,alreadyRewarded) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE" + "voterIp = VALUES(voterIp), voteSite = VALUES(voteSite), diffTime = VALUES(diffTime), votingTimeSite = VALUES(votingTimeSite), alreadyRewarded = VALUES(alreadyRewarded)");)
		{
			ps.setString(1, vote.getVoterIp());
			ps.setInt(2, vote.getVoteSite());
			ps.setLong(3, vote.getDiffTime());
			ps.setLong(4, vote.getVotingTimeSite());
			ps.setBoolean(5, vote.getAlreadyRewarded());
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void DeleteVotes(HashSet<IndividualVote> deleteVotes)
	{
		if (deleteVotes == null)
		{
			return;
		}
		if (deleteVotes.size() == 0)
		{
			return;
		}
		try
		{
			con = DatabaseFactory.getConnection();
			st = con.createStatement();
			for (IndividualVote iv : deleteVotes)
			{
				String sql = String.format("Delete from individualvotes where voterIp = '%s' AND voteSite = %s", iv.getVoterIp(), iv.getVoteSite());
				st.addBatch(sql);
			}
			int[] result = st.executeBatch();
			st.close();
			con.close();
			LOGGER.info(result.length + " Innecesary votes has been deleted");
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public Map<String, IndividualVote[]> getVotesDB()
	{
		return _votes;
	}
	
	public static final IndividualVoteTable getInstance()
	{
		return SingleHolder.INSTANCE;
	}
	
	private static final class SingleHolder
	{
		protected static final IndividualVoteTable INSTANCE = new IndividualVoteTable();
	}
}