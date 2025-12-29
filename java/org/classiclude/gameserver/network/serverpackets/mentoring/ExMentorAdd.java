package org.classiclude.gameserver.network.serverpackets.mentoring;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Gnacik, UnAfraid
 */
public class ExMentorAdd extends ServerPacket
{
	final Player _mentor;
	
	public ExMentorAdd(Player mentor)
	{
		_mentor = mentor;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MENTOR_ADD.writeId(this, buffer);
		buffer.writeString(_mentor.getName());
		buffer.writeInt(_mentor.getActiveClass());
		buffer.writeInt(_mentor.getLevel());
	}
}
