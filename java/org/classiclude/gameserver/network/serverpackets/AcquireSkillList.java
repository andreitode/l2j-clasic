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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.xml.SkillTreeData;
import org.classiclude.gameserver.model.SkillLearn;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.holders.ItemHolder;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Sdw, Mobius
 */
public class AcquireSkillList extends ServerPacket
{
	private Player _player;
	private Collection<SkillLearn> _learnable;
	
	public AcquireSkillList(Player player)
	{
		if (!player.isSubclassLocked()) // Changing class.
		{
			_player = player;
			
			if (player.isTransformed())
			{
				_learnable = Collections.emptyList();
			}
			else
			{
				_learnable = SkillTreeData.getInstance().getAvailableSkills(player, player.getClassId(), false, false);
				_learnable.addAll(SkillTreeData.getInstance().getNextAvailableSkills(player, player.getClassId(), false, false));
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (_player == null)
		{
			return;
		}
		
		ServerPackets.ACQUIRE_SKILL_LIST.writeId(this, buffer);
		buffer.writeShort(_learnable.size());
		for (SkillLearn skill : _learnable)
		{
			buffer.writeInt(skill.getSkillId());
			buffer.writeShort(skill.getSkillLevel()); // Main writeD, Classic writeH.
			buffer.writeLong(skill.getLevelUpSp());
			buffer.writeByte(skill.getGetLevel());
			buffer.writeByte(0); // Skill dual class level.
			
			buffer.writeByte(skill.getRequiredItems().size());
			for (ItemHolder item : skill.getRequiredItems())
			{
				buffer.writeInt(item.getId());
				buffer.writeLong(item.getCount());
			}
			
			final List<Skill> removeSkills = new LinkedList<>();
			for (int id : skill.getRemoveSkills())
			{
				final Skill removeSkill = _player.getKnownSkill(id);
				if (removeSkill != null)
				{
					removeSkills.add(removeSkill);
				}
			}
			
			buffer.writeByte(removeSkills.size());
			for (Skill removed : removeSkills)
			{
				buffer.writeInt(removed.getId());
				buffer.writeShort(removed.getLevel()); // Main writeD, Classic writeH.
			}
		}
	}
}
