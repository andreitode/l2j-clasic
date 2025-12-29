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

import java.util.List;

import org.classiclude.Config;
import org.classiclude.gameserver.geoengine.pathfinding.GeoLocation;
import org.classiclude.gameserver.geoengine.pathfinding.PathFinding;
import org.classiclude.gameserver.handler.IAdminCommandHandler;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.util.BuilderUtil;

public class AdminPathNode implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_path_find"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_path_find"))
		{
			if (Config.PATHFINDING < 1)
			{
				BuilderUtil.sendSysMessage(activeChar, "PathFinding is disabled.");
				return true;
			}
			
			if (activeChar.getTarget() != null)
			{
				final List<GeoLocation> path = PathFinding.getInstance().findPath(activeChar.getX(), activeChar.getY(), (short) activeChar.getZ(), activeChar.getTarget().getX(), activeChar.getTarget().getY(), (short) activeChar.getTarget().getZ(), activeChar.getInstanceWorld(), true);
				if (path == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "No Route!");
					return true;
				}
				for (GeoLocation a : path)
				{
					BuilderUtil.sendSysMessage(activeChar, "x:" + a.getX() + " y:" + a.getY() + " z:" + a.getZ());
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "No Target!");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
