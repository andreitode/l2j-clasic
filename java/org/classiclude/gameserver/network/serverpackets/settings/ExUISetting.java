package org.classiclude.gameserver.network.serverpackets.settings;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.variables.PlayerVariables;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Mobius
 */
public class ExUISetting extends ServerPacket
{
	public static final String SPLIT_VAR = "	";
	
	private final byte[] _uiKeyMapping;
	
	public ExUISetting(Player player)
	{
		if (player.getVariables().hasVariable(PlayerVariables.UI_KEY_MAPPING))
		{
			_uiKeyMapping = player.getVariables().getByteArray(PlayerVariables.UI_KEY_MAPPING, SPLIT_VAR);
		}
		else
		{
			_uiKeyMapping = null;
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UI_SETTING.writeId(this, buffer);
		if (_uiKeyMapping != null)
		{
			buffer.writeInt(_uiKeyMapping.length);
			buffer.writeBytes(_uiKeyMapping);
		}
		else
		{
			buffer.writeInt(0);
		}
	}
}
