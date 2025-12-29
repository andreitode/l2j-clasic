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
package org.classiclude.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.gameserver.enums.ShortcutType;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.interfaces.IRestorable;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.item.type.EtcItemType;
import org.classiclude.gameserver.network.serverpackets.ExAutoSoulShot;
import org.classiclude.gameserver.network.serverpackets.ShortCutInit;
import org.classiclude.gameserver.network.serverpackets.ShortCutRegister;

public class ShortCuts implements IRestorable
{
	private static final Logger LOGGER = Logger.getLogger(ShortCuts.class.getName());
	private static final int MAX_SHORTCUTS_PER_BAR = 12;
	private final Player _owner;
	private final Map<Integer, Shortcut> _shortCuts = new ConcurrentHashMap<>();
	
	public ShortCuts(Player owner)
	{
		_owner = owner;
	}
	
	public Collection<Shortcut> getAllShortCuts()
	{
		return _shortCuts.values();
	}
	
	public Shortcut getShortCut(int slot, int page)
	{
		Shortcut sc = _shortCuts.get(slot + (page * MAX_SHORTCUTS_PER_BAR));
		// Verify shortcut
		if ((sc != null) && (sc.getType() == ShortcutType.ITEM) && (_owner.getInventory().getItemByObjectId(sc.getId()) == null))
		{
			deleteShortCut(sc.getSlot(), sc.getPage());
			sc = null;
		}
		return sc;
	}
	
	public synchronized void registerShortCut(Shortcut shortcut)
	{
		// Verify shortcut
		if (shortcut.getType() == ShortcutType.ITEM)
		{
			final Item item = _owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null)
			{
				return;
			}
			shortcut.setSharedReuseGroup(item.getSharedReuseGroup());
		}
		registerShortCutInDb(shortcut, _shortCuts.put(shortcut.getSlot() + (shortcut.getPage() * MAX_SHORTCUTS_PER_BAR), shortcut));
	}
	
	private void registerShortCutInDb(Shortcut shortcut, Shortcut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,sub_level,class_index) values(?,?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType().ordinal());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, shortcut.getSubLevel());
			statement.setInt(8, _owner.getClassIndex());
			statement.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not store character shortcut: " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param slot
	 * @param page
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		final Shortcut old = _shortCuts.remove(slot + (page * MAX_SHORTCUTS_PER_BAR));
		if ((old == null) || (_owner == null))
		{
			return;
		}
		deleteShortCutFromDb(old);
		if (old.getType() == ShortcutType.ITEM)
		{
			final Item item = _owner.getInventory().getItemByObjectId(old.getId());
			if ((item != null) && (item.getItemType() == EtcItemType.SOULSHOT) && _owner.removeAutoSoulShot(item.getId()))
			{
				_owner.sendPacket(new ExAutoSoulShot(item.getId(), false, 0));
			}
		}
		
		_owner.sendPacket(new ShortCutInit(_owner));
		
		for (int shotId : _owner.getAutoSoulShot())
		{
			_owner.sendPacket(new ExAutoSoulShot(shotId, true, 0));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		for (Shortcut shortcut : _shortCuts.values())
		{
			if ((shortcut.getType() == ShortcutType.ITEM) && (shortcut.getId() == objectId))
			{
				deleteShortCut(shortcut.getSlot(), shortcut.getPage());
				break;
			}
		}
	}
	
	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(Shortcut shortcut)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?"))
		{
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not delete character shortcut: " + e.getMessage(), e);
		}
	}
	
	@Override
	public boolean restoreMe()
	{
		_shortCuts.clear();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, slot, page, type, shortcut_id, level, sub_level FROM character_shortcuts WHERE charId=? AND class_index=?"))
		{
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, _owner.getClassIndex());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final int slot = rset.getInt("slot");
					final int page = rset.getInt("page");
					final int type = rset.getInt("type");
					final int id = rset.getInt("shortcut_id");
					final int level = rset.getInt("level");
					final int subLevel = rset.getInt("sub_level");
					_shortCuts.put(slot + (page * MAX_SHORTCUTS_PER_BAR), new Shortcut(slot, page, ShortcutType.values()[type], id, level, subLevel, 1));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not restore character shortcuts: " + e.getMessage(), e);
			return false;
		}
		
		// Verify shortcuts
		for (Shortcut sc : getAllShortCuts())
		{
			if (sc.getType() == ShortcutType.ITEM)
			{
				final Item item = _owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
				else if (item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 * @param skillId the skill Id to search and update.
	 * @param skillLevel the skill level to update.
	 * @param skillSubLevel the skill sub level to update.
	 */
	public synchronized void updateShortCuts(int skillId, int skillLevel, int skillSubLevel)
	{
		// Update all the shortcuts for this skill
		for (Shortcut sc : _shortCuts.values())
		{
			if ((sc.getId() == skillId) && (sc.getType() == ShortcutType.SKILL))
			{
				final Shortcut newsc = new Shortcut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, skillSubLevel, 1);
				_owner.sendPacket(new ShortCutRegister(newsc, _owner));
				_owner.registerShortCut(newsc);
			}
		}
	}
}
