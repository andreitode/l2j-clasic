package org.classiclude.gameserver.network.serverpackets.mentoring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.CategoryType;
import org.classiclude.gameserver.instancemanager.MentorManager;
import org.classiclude.gameserver.model.Mentee;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExMentorList extends ServerPacket
{
	private final int _type;
	private final Collection<Mentee> _mentees;
	
	public ExMentorList(Player player)
	{
		if (player.isMentor())
		{
			_type = 1;
			_mentees = MentorManager.getInstance().getMentees(player.getObjectId());
		}
		else if (player.isMentee())
		{
			_type = 2;
			_mentees = Arrays.asList(MentorManager.getInstance().getMentor(player.getObjectId()));
		}
		else if (player.isInCategory(CategoryType.SIXTH_CLASS_GROUP)) // Not a mentor, Not a mentee, so can be a mentor
		{
			_mentees = Collections.emptyList();
			_type = 1;
		}
		else
		{
			_mentees = Collections.emptyList();
			_type = 0;
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MENTOR_LIST.writeId(this, buffer);
		buffer.writeInt(_type);
		buffer.writeInt(0);
		buffer.writeInt(_mentees.size());
		for (Mentee mentee : _mentees)
		{
			buffer.writeInt(mentee.getObjectId());
			buffer.writeString(mentee.getName());
			buffer.writeInt(mentee.getClassId());
			buffer.writeInt(mentee.getLevel());
			buffer.writeInt(mentee.isOnlineInt());
		}
	}
}
