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
package handlers.admincommandhandlers;

import org.classiclude.gameserver.data.xml.FakePlayerData;
import org.classiclude.gameserver.handler.IAdminCommandHandler;
import org.classiclude.gameserver.instancemanager.FakePlayerChatManager;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.util.BuilderUtil;

/**
 * @author Mobius
 */
public class AdminFakePlayers implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fakechat"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_fakechat"))
		{
			final String[] words = command.substring(15).split(" ");
			if (words.length < 3)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //fakechat playername fpcname message");
				return false;
			}
			final Player player = World.getInstance().getPlayer(words[0]);
			if (player == null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Player not found.");
				return false;
			}
			final String fpcName = FakePlayerData.getInstance().getProperName(words[1]);
			if (fpcName == null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Fake player not found.");
				return false;
			}
			String message = "";
			for (int i = 0; i < words.length; i++)
			{
				if (i < 2)
				{
					continue;
				}
				message += (words[i] + " ");
			}
			FakePlayerChatManager.getInstance().sendChat(player, fpcName, message);
			BuilderUtil.sendSysMessage(activeChar, "Your message has been sent.");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
