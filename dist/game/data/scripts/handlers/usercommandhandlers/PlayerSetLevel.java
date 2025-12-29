package handlers.usercommandhandlers;

import java.util.StringTokenizer;

import org.classiclude.Config;
import org.classiclude.gameserver.data.xml.ExperienceData;
import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.model.actor.Player;

public class PlayerSetLevel implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"setlevel"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!Config.ENABLE_SET_LEVEL_COMMAND)
		{
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(target);
		if (!st.hasMoreTokens())
		{
			activeChar.sendMessage("You must specify a level.");
			return false;
		}
		
		String val = st.nextToken();
		try
		{
			int level = Integer.parseInt(val);
			int maxLevel = ExperienceData.getInstance().getMaxLevel();
			
			if ((level >= 1) && (level <= maxLevel))
			{
				long pXp = activeChar.getExp();
				long tXp = ExperienceData.getInstance().getExpForLevel(level);
				
				if (pXp > tXp)
				{
					activeChar.getStat().setLevel((byte) level);
					activeChar.removeExpAndSp(pXp - tXp, 0);
					activeChar.sendMessage("Your level has been set to " + level + ". Removed " + (pXp - tXp) + " exp.");
				}
				else if (pXp < tXp)
				{
					activeChar.addExpAndSp(tXp - pXp, 0);
					activeChar.sendMessage("Your level has been set to " + level + ". Added " + (tXp - pXp) + " exp.");
				}
				
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You must specify a level between 1 and " + maxLevel + ".");
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			activeChar.sendMessage("You must specify a valid level number.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
