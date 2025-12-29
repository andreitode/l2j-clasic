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

import java.util.Collections;
import java.util.List;

import org.classiclude.gameserver.data.SpawnTable;
import org.classiclude.gameserver.handler.AdminCommandHandler;
import org.classiclude.gameserver.handler.IAdminCommandHandler;
import org.classiclude.gameserver.instancemanager.DBSpawnManager;
import org.classiclude.gameserver.model.Spawn;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.spawns.NpcSpawnTemplate;
import org.classiclude.gameserver.model.spawns.SpawnGroup;
import org.classiclude.gameserver.model.spawns.SpawnTemplate;
import org.classiclude.gameserver.model.zone.type.SpawnTerritory;
import org.classiclude.gameserver.util.BuilderUtil;
import org.classiclude.gameserver.util.Util;

/**
 * @author Mobius
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete", // supports range parameter
		"admin_delete_group" // for territory spawns
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.contains("group"))
		{
			handleDeleteGroup(activeChar);
		}
		else if (command.startsWith("admin_delete"))
		{
			final String[] split = command.split(" ");
			handleDelete(activeChar, (split.length > 1) && Util.isDigit(split[1]) ? Integer.parseInt(split[1]) : 0);
		}
		return true;
	}
	
	private void handleDelete(Player player, int range)
	{
		if (range > 0)
		{
			World.getInstance().forEachVisibleObjectInRange(player, Npc.class, range, target -> deleteNpc(player, target));
			return;
		}
		
		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteNpc(player, obj.asNpc());
		}
		else
		{
			BuilderUtil.sendSysMessage(player, "Incorrect target.");
		}
	}
	
	private void handleDeleteGroup(Player player)
	{
		final WorldObject obj = player.getTarget();
		if (obj instanceof Npc)
		{
			deleteGroup(player, obj.asNpc());
		}
		else
		{
			BuilderUtil.sendSysMessage(player, "Incorrect target.");
		}
	}
	
	private void deleteNpc(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTemplate npcSpawnTemplate = spawn.getNpcSpawnTemplate();
			final SpawnGroup group = npcSpawnTemplate != null ? npcSpawnTemplate.getGroup() : null;
			List<SpawnTerritory> territories = group != null ? group.getTerritories() : Collections.emptyList();
			if (territories.isEmpty())
			{
				final SpawnTemplate spawnTemplate = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate() : null;
				if (spawnTemplate != null)
				{
					territories = spawnTemplate.getTerritories();
				}
			}
			if (territories.isEmpty())
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (DBSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					DBSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
				BuilderUtil.sendSysMessage(player, "Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			}
			else
			{
				AdminCommandHandler.getInstance().useAdminCommand(player, AdminDelete.ADMIN_COMMANDS[1], true);
			}
		}
	}
	
	private void deleteGroup(Player player, Npc target)
	{
		final Spawn spawn = target.getSpawn();
		if (spawn != null)
		{
			final NpcSpawnTemplate npcSpawnTemplate = spawn.getNpcSpawnTemplate();
			final SpawnGroup group = npcSpawnTemplate != null ? npcSpawnTemplate.getGroup() : null;
			List<SpawnTerritory> territories = group != null ? group.getTerritories() : Collections.emptyList();
			boolean simpleTerritory = false;
			if (territories.isEmpty())
			{
				final SpawnTemplate spawnTemplate = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate() : null;
				if (spawnTemplate != null)
				{
					territories = spawnTemplate.getTerritories();
					simpleTerritory = true;
				}
			}
			if (territories.isEmpty())
			{
				BuilderUtil.sendSysMessage(player, "Incorrect target.");
			}
			else
			{
				target.deleteMe();
				spawn.stopRespawn();
				if (DBSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					DBSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
				
				if (group != null)
				{
					for (NpcSpawnTemplate template : group.getSpawns())
					{
						template.despawn();
					}
				}
				else if (simpleTerritory && (npcSpawnTemplate != null))
				{
					npcSpawnTemplate.despawn();
				}
				
				BuilderUtil.sendSysMessage(player, "Deleted " + target.getName() + " group from " + target.getObjectId() + ".");
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
