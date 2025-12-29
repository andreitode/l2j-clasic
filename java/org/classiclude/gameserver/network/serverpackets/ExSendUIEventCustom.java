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
package org.classiclude.gameserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author xBaus
 */

public class ExSendUIEventCustom extends ServerPacket
{
	public static final int COUNT_DOWN = 0; // dropType;0;(id;min;max;chance)
	public static final int COUNT_UP = 1;
	public static final int OPEN_FISH_WND = 99;
	public static final int INITIALIZE_FISH_STATUS = 100;
	public static final int UPDATE_FISH_STATUS = 101;
	public static final int CLOSE_FISH_WND = 102;
	
	private final int _type;
	private final int _param1;
	private final int _param2;
	private final String _param3;
	private final String _param4;
	private final String _param5;
	private final String _param6;
	private final String _param7;
	private final String _param8;
	
	public ExSendUIEventCustom(int type)
	{
		this(type, 0, 0, "", "", "", "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1)
	{
		this(type, param1, 0, "", "", "", "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2)
	{
		this(type, param1, param2, "", "", "", "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3)
	{
		this(type, param1, param2, param3, "", "", "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3, String param4)
	{
		this(type, param1, param2, param3, param4, "", "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3, String param4, String param5)
	{
		this(type, param1, param2, param3, param4, param5, "", "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3, String param4, String param5, String param6)
	{
		this(type, param1, param2, param3, param4, param5, param6, "", "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3, String param4, String param5, String param6, String param7)
	{
		this(type, param1, param2, param3, param4, param5, param6, param7, "");
	}
	
	public ExSendUIEventCustom(int type, int param1, int param2, String param3, String param4, String param5, String param6, String param7, String param8)
	{
		_type = type;
		_param1 = param1;
		_param2 = param2;
		_param3 = param3;
		_param4 = param4;
		_param5 = param5;
		_param6 = param6;
		_param7 = param7;
		_param8 = param8;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SEND_UIEVENT.writeId(this, buffer);
		
		buffer.writeInt(-1); // objid
		buffer.writeInt(_type);
		buffer.writeInt(_param1);
		buffer.writeInt(_param2);
		buffer.writeString(_param3);
		buffer.writeString(_param4);
		buffer.writeString(_param5);
		buffer.writeString(_param6);
		buffer.writeString(_param7);
		buffer.writeString(_param8);
	}
}