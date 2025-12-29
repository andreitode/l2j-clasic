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

import org.classiclude.Config;
import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.sql.CrestTable;
import org.classiclude.gameserver.model.Crest;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class AllyCrest extends ServerPacket
{
	private final int _crestId;
	private final byte[] _data;
	
	public AllyCrest(int crestId)
	{
		_crestId = crestId;
		final Crest crest = CrestTable.getInstance().getCrest(crestId);
		_data = crest != null ? crest.getData() : null;
	}
	
	public AllyCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ALLIANCE_CREST.writeId(this, buffer);
		buffer.writeInt(Config.SERVER_ID);
		buffer.writeInt(_crestId);
		if (_data != null)
		{
			buffer.writeInt(_data.length);
			buffer.writeBytes(_data);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
