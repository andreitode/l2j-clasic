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
package org.classiclude.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.classiclude.Config;
import org.classiclude.commons.util.IXmlReader;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.holders.ItemHolder;

/**
 * @author Mobius
 */
public class AttendanceRewardData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AttendanceRewardData.class.getName());
	private final List<ItemHolder> _rewards = new ArrayList<>();
	private int _rewardsCount = 0;
	
	protected AttendanceRewardData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		if (Config.ENABLE_ATTENDANCE_REWARDS)
		{
			_rewards.clear();
			parseDatapackFile("data/AttendanceRewards.xml");
			_rewardsCount = _rewards.size();
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _rewardsCount + " rewards.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": Disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "item", rewardNode ->
		{
			final StatSet set = new StatSet(parseAttributes(rewardNode));
			final int itemId = set.getInt("id");
			final int itemCount = set.getInt("count");
			if (ItemData.getInstance().getTemplate(itemId) == null)
			{
				LOGGER.info(getClass().getSimpleName() + ": Item with id " + itemId + " does not exist.");
			}
			else
			{
				_rewards.add(new ItemHolder(itemId, itemCount));
			}
		}));
	}
	
	public List<ItemHolder> getRewards()
	{
		return _rewards;
	}
	
	public int getRewardsCount()
	{
		return _rewardsCount;
	}
	
	public static AttendanceRewardData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AttendanceRewardData INSTANCE = new AttendanceRewardData();
	}
}
