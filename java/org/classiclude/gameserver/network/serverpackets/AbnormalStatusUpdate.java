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

import java.util.ArrayList;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.skill.BuffInfo;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class AbnormalStatusUpdate extends ServerPacket
{
	private final List<BuffInfo> _effects = new ArrayList<>();
	
	public void addSkill(BuffInfo info)
	{
		if (!info.getSkill().isHealingPotionSkill())
		{
			_effects.add(info);
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ABNORMAL_STATUS_UPDATE.writeId(this, buffer);
		buffer.writeShort(_effects.size());
		for (BuffInfo info : _effects)
		{
			if ((info != null) && info.isInUse())
			{
				buffer.writeInt(info.getSkill().getDisplayId());
				buffer.writeShort(info.getSkill().getDisplayLevel());
				buffer.writeShort(info.getSkill().getSubLevel());
				buffer.writeInt(info.getSkill().getAbnormalType().getClientId());
				writeOptionalInt(info.getSkill().isAura() || info.getSkill().isToggle() ? -1 : info.getTime(), buffer);
			}
		}
	}
}
