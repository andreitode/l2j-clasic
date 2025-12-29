package org.classiclude.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.clan.Clan.WarState;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class PledgeReceiveWarList extends ServerPacket
{
	private final Clan _clan;
	private final int _tab; // 0 = Declared, 1 = Under Attack
	
	public PledgeReceiveWarList(Clan clan, int tab)
	{
		_clan = clan;
		_tab = tab;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_RECEIVE_WAR_LIST.writeId(this, buffer);
		buffer.writeInt(_tab);
		Collection<Integer> warList;
		Set<Integer> combinedList = new LinkedHashSet<>();
		combinedList.addAll(_clan.getWarList());
		combinedList.addAll(_clan.getAttackerList());
		warList = new ArrayList<>(combinedList);
		buffer.writeInt(warList.size());
		
		for (Integer clanId : warList)
		{
			final Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan == null)
			{
				continue;
			}
			
			buffer.writeString(clan.getName());
			
			WarState warState = _clan.getWarState(clan.getId());
			if (warState == null)
			{
				warState = (_tab == 0) ? WarState.BLOOD_DECLARATION : WarState.DECLARATION;
			}
			buffer.writeInt(warState.ordinal());
			buffer.writeInt(0); // Time if friends to start remaining
			buffer.writeInt(0); // Score
			buffer.writeInt(0); // @TODO: Recent change in points
			buffer.writeInt(0); // Friends to start war left
		}
	}
}
