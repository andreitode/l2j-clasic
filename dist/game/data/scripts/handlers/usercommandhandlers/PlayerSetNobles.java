package handlers.usercommandhandlers;

import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.model.actor.Player;

public class PlayerSetNobles implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS =
    {
        "setnobles"
    };
    
    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String target)
    {
        if (activeChar.isNoble())
        {
            activeChar.sendMessage("You are already a noble.");
            return false;
        }

        activeChar.setNoble(true);
        activeChar.broadcastUserInfo();
        activeChar.sendMessage("Congratulations! You are now a noble.");

        return true;
    }
    
    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
