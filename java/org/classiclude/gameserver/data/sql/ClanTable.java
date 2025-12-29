/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.classiclude.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.classiclude.Config;
import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.classiclude.gameserver.data.xml.ClanHallData;
import org.classiclude.gameserver.enums.UserInfoType;
import org.classiclude.gameserver.instancemanager.FortManager;
import org.classiclude.gameserver.instancemanager.FortSiegeManager;
import org.classiclude.gameserver.instancemanager.IdManager;
import org.classiclude.gameserver.instancemanager.SiegeManager;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.clan.Clan.WarState;
import org.classiclude.gameserver.model.clan.ClanMember;
import org.classiclude.gameserver.model.clan.ClanPrivilege;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerClanCreate;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerClanDestroy;
import org.classiclude.gameserver.model.residences.ClanHall;
import org.classiclude.gameserver.model.siege.Fort;
import org.classiclude.gameserver.model.siege.FortSiege;
import org.classiclude.gameserver.model.siege.Siege;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.classiclude.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.classiclude.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;
import org.classiclude.gameserver.util.EnumIntBitmask;
import org.classiclude.gameserver.util.Util;

/**
 * This class loads the clan related data.
 */
public class ClanTable
{
	private static final Logger LOGGER = Logger.getLogger(ClanTable.class.getName());
	private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();
	
	protected ClanTable()
	{
		// forums has to be loaded before clan data, because of last forum id used should have also memo included
		if (Config.ENABLE_COMMUNITY_BOARD)
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		// Get all clan ids.
		final List<Integer> cids = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT clan_id FROM clan_data"))
		{
			while (rs.next())
			{
				cids.add(rs.getInt("clan_id"));
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error restoring ClanTable.", e);
		}
		
		// Create clans.
		for (int cid : cids)
		{
			final Clan clan = new Clan(cid);
			_clans.put(cid, clan);
			if (clan.getDissolvingExpiryTime() != 0)
			{
				scheduleRemoveClan(clan.getId());
			}
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Restored " + cids.size() + " clans from the database.");
		allianceCheck();
		restoreClanWars();
		
		ThreadPool.scheduleAtFixedRate(this::updateClanRanks, 1000, 1200000); // 20 minutes.
	}
	
	/**
	 * Gets the clans.
	 * @return the clans
	 */
	public Collection<Clan> getClans()
	{
		return _clans.values();
	}
	
	/**
	 * Gets the clan count.
	 * @return the clan count
	 */
	public int getClanCount()
	{
		return _clans.size();
	}
	
	/**
	 * @param clanId
	 * @return
	 */
	public Clan getClan(int clanId)
	{
		return _clans.get(clanId);
	}
	
	public Clan getClanByName(String clanName)
	{
		for (Clan clan : _clans.values())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
			{
				return clan;
			}
		}
		return null;
	}
	
	/**
	 * Creates a new clan and store clan info to database
	 * @param player
	 * @param clanName
	 * @return NULL if clan with same name already exists
	 */
	public Clan createClan(Player player, String clanName)
	{
		if (player == null)
		{
			return null;
		}
		
		if (player.getLevel() < 10)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		if (player.getClanId() != 0)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
			return null;
		}
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		if (!Util.isAlphaNumeric(clanName) || (clanName.length() < 2))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return null;
		}
		if (clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
			return null;
		}
		
		if (getClanByName(clanName) != null)
		{
			// clan name is already taken
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			return null;
		}
		
		final Clan clan = new Clan(IdManager.getInstance().getNextId(), clanName);
		final ClanMember leader = new ClanMember(clan, player);
		clan.setLeader(leader);
		leader.setPlayer(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(ClanMember.calculatePledgeClass(player));
		player.setClanPrivileges(new EnumIntBitmask<>(ClanPrivilege.class, true));
		
		_clans.put(clan.getId(), clan);
		
		// should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		PledgeShowMemberListAll.sendAllTo(player);
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(SystemMessageId.YOUR_CLAN_HAS_BEEN_CREATED);
		player.broadcastUserInfo(UserInfoType.RELATION, UserInfoType.CLAN);
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_CREATE))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanCreate(player, clan));
		}
		
		return clan;
	}
	
	public synchronized void destroyClan(int clanId)
	{
		final Clan clan = getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		
		// ClanEntryManager.getInstance().removeFromClanList(clan.getId());
		
		final int castleId = clan.getCastleId();
		if (castleId == 0)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clan);
			}
		}
		
		final int fortId = clan.getFortId();
		if (fortId == 0)
		{
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeAttacker(clan);
			}
		}
		
		final ClanHall hall = ClanHallData.getInstance().getClanHallByClan(clan);
		if (hall != null)
		{
			hall.setOwner(null);
		}
		
		final ClanMember leaderMember = clan.getLeader();
		if (leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayer(), null);
		}
		
		for (ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getObjectId(), 0);
		}
		
		_clans.remove(clanId);
		IdManager.getInstance().releaseId(clanId);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?"))
			{
				ps.setInt(1, clanId);
				ps.setInt(2, clanId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM clan_notices WHERE clan_id=?"))
			{
				ps.setInt(1, clanId);
				ps.execute();
			}
			
			if (fortId != 0)
			{
				final Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					final Clan owner = fort.getOwnerClan();
					if (clan == owner)
					{
						fort.removeOwner(true);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Error removing clan from DB.", e);
		}
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_DESTROY))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanDestroy(leaderMember, clan));
		}
	}
	
	public void scheduleRemoveClan(int clanId)
	{
		ThreadPool.schedule(() ->
		{
			if (getClan(clanId) == null)
			{
				return;
			}
			if (getClan(clanId).getDissolvingExpiryTime() != 0)
			{
				destroyClan(clanId);
			}
		}, Math.max(getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000));
	}
	
	public boolean isAllyExists(String allyName)
	{
		for (Clan clan : _clans.values())
		{
			if ((clan.getAllyName() != null) && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}
	
	public void storeClanWars(int clanId1, int clanId2)
	{
		final Clan clan1 = getClan(clanId1);
		final Clan clan2 = getClan(clanId2);
		
		if (clan2.isAtWarWith(clanId1))
		{
			clan1.setWarState(clan2.getId(), WarState.IN_WAR);
			clan2.setWarState(clan1.getId(), WarState.IN_WAR);
		}
		else
		{
			clan1.setWarState(clan2.getId(), WarState.DECLARATION);
			clan2.setWarState(clan1.getId(), WarState.BLOOD_DECLARATION);
		}
		
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2, war_state1, war_state2) VALUES(?,?,?,?,?,?)"))
		{
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.setInt(3, 0); // Assuming wantspeace1 is not used here
			ps.setInt(4, 0); // Assuming wantspeace2 is not used here
			ps.setString(5, clan1.getWarState(clan2.getId()).name());
			ps.setString(6, clan2.getWarState(clan1.getId()).name());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Error storing clan wars data.", e);
		}
		
		SystemMessage msg = new SystemMessage(SystemMessageId.A_CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_THE_CLAN_S1_IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_YOU_WILL_ONLY_LOSE_A_QUARTER_OF_THE_NORMAL_EXPERIENCE_FROM_DEATH);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		msg = new SystemMessage(SystemMessageId.THE_CLAN_S1_HAS_DECLARED_A_CLAN_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void deleteClanWars(int clanId1, int clanId2)
	{
		final Clan clan1 = getClan(clanId1);
		final Clan clan2 = getClan(clanId2);
		
		if (clan1.getWarState(clan2.getId()) == WarState.IN_WAR)
		{
			clan1.setWarState(clan2.getId(), WarState.BLOOD_DECLARATION);
			clan2.setWarState(clan1.getId(), WarState.DECLARATION);
		}
		else
		{
			clan1.setWarState(clan2.getId(), WarState.DECLARATION);
			clan2.setWarState(clan1.getId(), WarState.BLOOD_DECLARATION);
		}
		
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?"))
		{
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Error removing clan wars data.", e);
		}
		
		SystemMessage msg = new SystemMessage(SystemMessageId.THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		msg = new SystemMessage(SystemMessageId.THE_CLAN_S1_HAS_DECIDED_TO_STOP_THE_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void checkSurrender(Clan clan1, Clan clan2)
	{
		int count = 0;
		for (ClanMember member : clan1.getMembers())
		{
			if ((member != null) && (member.getPlayer().getWantsPeace() == 1))
			{
				count++;
			}
		}
		if (count == (clan1.getMembers().size() - 1))
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteClanWars(clan1.getId(), clan2.getId());
		}
	}
	
	private void restoreClanWars()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement();
			ResultSet rset = statement.executeQuery("SELECT clan1, clan2, war_state1, war_state2 FROM clan_wars"))
		{
			while (rset.next())
			{
				Clan clan1 = getClan(rset.getInt("clan1"));
				Clan clan2 = getClan(rset.getInt("clan2"));
				if ((clan1 != null) && (clan2 != null))
				{
					clan1.setEnemyClan(clan2);
					clan2.setAttackerClan(clan1);
					clan1.setWarState(clan2.getId(), WarState.valueOf(rset.getString("war_state1")));
					clan2.setWarState(clan1.getId(), WarState.valueOf(rset.getString("war_state2")));
				}
				else
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": restorewars one of clans is null clan1:" + clan1 + " clan2:" + clan2);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Error restoring clan wars data.", e);
		}
	}
	
	/**
	 * Check for nonexistent alliances
	 */
	private void allianceCheck()
	{
		for (Clan clan : _clans.values())
		{
			final int allyId = clan.getAllyId();
			if ((allyId != 0) && (clan.getId() != allyId) && !_clans.containsKey(allyId))
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.changeAllyCrest(0, true);
				clan.updateClanInDB();
				LOGGER.info(getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
			}
		}
	}
	
	public List<Clan> getClanAllies(int allianceId)
	{
		final List<Clan> clanAllies = new ArrayList<>();
		if (allianceId != 0)
		{
			for (Clan clan : _clans.values())
			{
				if ((clan != null) && (clan.getAllyId() == allianceId))
				{
					clanAllies.add(clan);
				}
			}
		}
		return clanAllies;
	}
	
	public void shutdown()
	{
		for (Clan clan : _clans.values())
		{
			clan.updateClanInDB();
			clan.getVariables().storeMe();
		}
	}
	
	private void updateClanRanks()
	{
		for (Clan clan : _clans.values())
		{
			clan.setRank(getClanRank(clan));
		}
	}
	
	public int getClanRank(Clan clan)
	{
		if (clan.getLevel() < 3)
		{
			return 0;
		}
		
		int rank = 1;
		for (Clan c : _clans.values())
		{
			if ((clan != c) && ((clan.getLevel() < c.getLevel()) || ((clan.getLevel() == c.getLevel()) && (clan.getReputationScore() <= c.getReputationScore()))))
			{
				rank++;
			}
		}
		
		return rank;
	}
	
	public static ClanTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanTable INSTANCE = new ClanTable();
	}
}
