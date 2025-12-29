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

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class PledgeSkillList extends ServerPacket
{
	private final Collection<Skill> _skills;
	private final Collection<SubPledgeSkill> _subSkills;
	
	public static class SubPledgeSkill
	{
		int _subType;
		int _skillId;
		int _skillLevel;
		
		public SubPledgeSkill(int subType, int skillId, int skillLevel)
		{
			_subType = subType;
			_skillId = skillId;
			_skillLevel = skillLevel;
		}
	}
	
	public PledgeSkillList(Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_SKILL_LIST.writeId(this, buffer);
		buffer.writeInt(_skills.size());
		buffer.writeInt(_subSkills.size()); // Squad skill length
		for (Skill sk : _skills)
		{
			buffer.writeInt(sk.getDisplayId());
			buffer.writeShort(sk.getDisplayLevel());
			buffer.writeShort(0); // Sub level
		}
		for (SubPledgeSkill sk : _subSkills)
		{
			buffer.writeInt(sk._subType); // Clan Sub-unit types
			buffer.writeInt(sk._skillId);
			buffer.writeShort(sk._skillLevel);
			buffer.writeShort(0); // Sub level
		}
	}
}
