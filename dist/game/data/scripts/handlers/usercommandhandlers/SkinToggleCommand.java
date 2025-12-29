package handlers.usercommandhandlers;

import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.serverpackets.CharInfo;

public class SkinToggleCommand implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"skinoff",
		"skinon"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equalsIgnoreCase("skinoff"))
		{
			activeChar.setHideAppearance(true);
			activeChar.sendMessage("You have disabled the display of appearances.");
			
		}
		else if (command.equalsIgnoreCase("skinon"))
		{
			activeChar.setHideAppearance(false);
			activeChar.sendMessage("You have enabled the display of appearances.");
			
		}
		activeChar.sendPacket(new CharInfo(activeChar, false));
		
		World.getInstance().getVisibleObjects(activeChar, Player.class).forEach(nearbyPlayer ->
		{
			activeChar.sendPacket(new CharInfo(nearbyPlayer, false));
		});
		activeChar.broadcastUserInfo();
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
