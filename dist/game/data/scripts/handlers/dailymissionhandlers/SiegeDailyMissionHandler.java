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
package handlers.dailymissionhandlers;

import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.enums.DailyMissionStatus;
import org.classiclude.gameserver.handler.AbstractDailyMissionHandler;
import org.classiclude.gameserver.model.DailyMissionDataHolder;
import org.classiclude.gameserver.model.DailyMissionPlayerEntry;
import org.classiclude.gameserver.model.SiegeClan;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.events.Containers;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.sieges.OnCastleSiegeStart;
import org.classiclude.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author UnAfraid
 */
public class SiegeDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _minLevel;
	private final int _maxLevel;
	
	public SiegeDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_maxLevel = holder.getParams().getInt("maxLevel", Byte.MAX_VALUE);
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_CASTLE_SIEGE_START, (OnCastleSiegeStart event) -> onSiegeStart(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case AVAILABLE:
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private void onSiegeStart(OnCastleSiegeStart event)
	{
		event.getSiege().getAttackerClans().forEach(this::processSiegeClan);
		event.getSiege().getDefenderClans().forEach(this::processSiegeClan);
	}
	
	private void processSiegeClan(SiegeClan siegeClan)
	{
		final Clan clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
		if (clan != null)
		{
			clan.getOnlineMembers(0).forEach(player ->
			{
				if ((player.getLevel() < _minLevel) || (player.getLevel() > _maxLevel))
				{
					return;
				}
				
				final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
				entry.setStatus(DailyMissionStatus.AVAILABLE);
				storePlayerEntry(entry);
			});
		}
	}
}
