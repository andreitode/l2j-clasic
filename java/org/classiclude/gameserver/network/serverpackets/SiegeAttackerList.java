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
package org.classiclude.gameserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.model.SiegeClan;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.siege.Castle;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * Populates the Siege Attacker List in the SiegeInfo Window<br>
 * <br>
 * c = ca<br>
 * d = CastleID<br>
 * d = unknown (0)<br>
 * d = unknown (1)<br>
 * d = unknown (0)<br>
 * d = Number of Attackers Clans?<br>
 * d = Number of Attackers Clans<br>
 * { //repeats<br>
 * d = ClanID<br>
 * S = ClanName<br>
 * S = ClanLeaderName<br>
 * d = ClanCrestID<br>
 * d = signed time (seconds)<br>
 * d = AllyID<br>
 * S = AllyName<br>
 * S = AllyLeaderName<br>
 * d = AllyCrestID<br>
 * @author Mobius
 */
public class SiegeAttackerList extends ServerPacket
{
	private final Castle _castle;
	
	public SiegeAttackerList(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CASTLE_SIEGE_ATTACKER_LIST.writeId(this, buffer);
		buffer.writeInt(_castle.getResidenceId());
		buffer.writeInt(0); // 0
		buffer.writeInt(1); // 1
		buffer.writeInt(0); // 0
		final int size = _castle.getSiege().getAttackerClans().size();
		if (size > 0)
		{
			Clan clan;
			buffer.writeInt(size);
			buffer.writeInt(size);
			for (SiegeClan siegeclan : _castle.getSiege().getAttackerClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}
				buffer.writeInt(clan.getId());
				buffer.writeString(clan.getName());
				buffer.writeString(clan.getLeaderName());
				buffer.writeInt(clan.getCrestId());
				buffer.writeInt(0); // signed time (seconds) (not storated by L2J)
				buffer.writeInt(clan.getAllyId());
				buffer.writeString(clan.getAllyName());
				buffer.writeString(""); // AllyLeaderName
				buffer.writeInt(clan.getAllyCrestId());
			}
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
