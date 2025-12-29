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
package handlers.voicedcommandhandlers;

import java.util.StringTokenizer;

import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.data.xml.AdminData;
import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.instancemanager.PunishmentManager;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.punishment.PunishmentAffect;
import org.classiclude.gameserver.model.punishment.PunishmentTask;
import org.classiclude.gameserver.model.punishment.PunishmentType;
import org.classiclude.gameserver.model.skill.AbnormalVisualEffect;
import org.classiclude.gameserver.util.BuilderUtil;
import org.classiclude.gameserver.util.Util;

public class ChatAdmin implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"banchat",
		"chatban",
		"unbanchat",
		"chatunban"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			return false;
		}
		
		switch (command)
		{
			case "banchat":
			case "chatban":
			{
				if (params == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: .banchat name [minutes]");
					return true;
				}
				final StringTokenizer st = new StringTokenizer(params);
				if (st.hasMoreTokens())
				{
					final String name = st.nextToken();
					long expirationTime = 0;
					if (st.hasMoreTokens())
					{
						final String token = st.nextToken();
						if (Util.isDigit(token))
						{
							expirationTime = Integer.parseInt(token);
						}
					}
					
					final int objId = CharInfoTable.getInstance().getIdByName(name);
					if (objId > 0)
					{
						final Player player = World.getInstance().getPlayer(objId);
						if ((player == null) || !player.isOnline())
						{
							BuilderUtil.sendSysMessage(activeChar, "Player not online!");
							return false;
						}
						if (player.isChatBanned())
						{
							BuilderUtil.sendSysMessage(activeChar, "Player is already punished!");
							return false;
						}
						if (player == activeChar)
						{
							BuilderUtil.sendSysMessage(activeChar, "You can't ban yourself!");
							return false;
						}
						if (player.isGM())
						{
							BuilderUtil.sendSysMessage(activeChar, "You can't ban a GM!");
							return false;
						}
						if (AdminData.getInstance().hasAccess(command, player.getAccessLevel()))
						{
							BuilderUtil.sendSysMessage(activeChar, "You can't ban moderator!");
							return false;
						}
						
						PunishmentManager.getInstance().startPunishment(new PunishmentTask(objId, PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN, System.currentTimeMillis() + (expirationTime * 1000 * 60), "Chat banned by moderator", activeChar.getName()));
						if (expirationTime > 0)
						{
							BuilderUtil.sendSysMessage(activeChar, "Player " + player.getName() + " chat banned for " + expirationTime + " minutes.");
						}
						else
						{
							BuilderUtil.sendSysMessage(activeChar, "Player " + player.getName() + " chat banned forever.");
						}
						player.sendMessage("Chat banned by moderator " + activeChar.getName());
						player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player not found!");
						return false;
					}
				}
				break;
			}
			case "unbanchat":
			case "chatunban":
			{
				if (params == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: .unbanchat name");
					return true;
				}
				final StringTokenizer st = new StringTokenizer(params);
				if (st.hasMoreTokens())
				{
					final String name = st.nextToken();
					final int objId = CharInfoTable.getInstance().getIdByName(name);
					if (objId > 0)
					{
						final Player player = World.getInstance().getPlayer(objId);
						if ((player == null) || !player.isOnline())
						{
							BuilderUtil.sendSysMessage(activeChar, "Player not online!");
							return false;
						}
						if (!player.isChatBanned())
						{
							BuilderUtil.sendSysMessage(activeChar, "Player is not chat banned!");
							return false;
						}
						
						PunishmentManager.getInstance().stopPunishment(objId, PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN);
						BuilderUtil.sendSysMessage(activeChar, "Player " + player.getName() + " chat unbanned.");
						player.sendMessage("Chat unbanned by moderator " + activeChar.getName());
						player.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player not found!");
						return false;
					}
				}
				break;
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
