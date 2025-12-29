package org.classiclude.gameserver.network.serverpackets.mentoring;

import java.util.ArrayList;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.CategoryType;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ListMenteeWaiting extends ServerPacket
{
	private static final int PLAYERS_PER_PAGE = 64;
	
	private final List<Player> _possibleCandiates = new ArrayList<>();
	private final int _page;
	
	public ListMenteeWaiting(int page, int minLevel, int maxLevel)
	{
		_page = page;
		for (Player player : World.getInstance().getPlayers())
		{
			if ((player.getLevel() >= minLevel) && (player.getLevel() <= maxLevel) && !player.isMentee() && !player.isMentor() && !player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
			{
				_possibleCandiates.add(player);
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.LIST_MENTEE_WAITING.writeId(this, buffer);
		buffer.writeInt(1); // always 1 in retail
		if (_possibleCandiates.isEmpty())
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			return;
		}
		
		buffer.writeInt(_possibleCandiates.size());
		buffer.writeInt(_possibleCandiates.size() % PLAYERS_PER_PAGE);
		for (Player player : _possibleCandiates)
		{
			if ((1 <= (PLAYERS_PER_PAGE * _page)) && (1 > (PLAYERS_PER_PAGE * (_page - 1))))
			{
				buffer.writeString(player.getName());
				buffer.writeInt(player.getActiveClass());
				buffer.writeInt(player.getLevel());
			}
		}
	}
}
