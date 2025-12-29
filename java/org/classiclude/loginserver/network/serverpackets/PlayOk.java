package org.classiclude.loginserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.loginserver.SessionKey;
import org.classiclude.loginserver.network.LoginClient;

public class PlayOk extends LoginServerPacket
{
	private final int _playOk1;
	private final int _playOk2;
	
	public PlayOk(SessionKey sessionKey)
	{
		_playOk1 = sessionKey.playOkID1;
		_playOk2 = sessionKey.playOkID2;
	}
	
	@Override
	protected void writeImpl(LoginClient client, WritableBuffer buffer)
	{
		buffer.writeByte(0x07);
		buffer.writeInt(_playOk1);
		buffer.writeInt(_playOk2);
	}
}
