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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.classiclude.commons.util.IXmlReader;
import org.classiclude.gameserver.model.item.ItemTemplate;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.options.EnchantOptions;
import org.classiclude.gameserver.util.Util;

/**
 * @author UnAfraid, Mobius
 */
public class EnchantItemOptionsData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnchantItemOptionsData.class.getName());
	
	private final Map<Integer, Map<Integer, EnchantOptions>> _data = new HashMap<>();
	
	protected EnchantItemOptionsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_data.clear();
		parseDatapackFile("data/EnchantItemOptions.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		int counter = 0;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				ITEM: for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						final int itemId = parseInteger(d.getAttributes(), "id");
						final ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
						if (template == null)
						{
							LOGGER.warning(getClass().getSimpleName() + ": Could not find item template for id " + itemId);
							continue ITEM;
						}
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("options".equalsIgnoreCase(cd.getNodeName()))
							{
								final EnchantOptions op = new EnchantOptions(parseInteger(cd.getAttributes(), "level"));
								for (byte i = 0; i < 3; i++)
								{
									final Node att = cd.getAttributes().getNamedItem("option" + (i + 1));
									if ((att != null) && Util.isDigit(att.getNodeValue()))
									{
										final int id = parseInteger(att);
										if (OptionData.getInstance().getOptions(id) == null)
										{
											LOGGER.warning(getClass().getSimpleName() + ": Could not find option " + id + " for item " + template);
											continue ITEM;
										}
										
										Map<Integer, EnchantOptions> data = _data.get(itemId);
										if (data == null)
										{
											data = new HashMap<>();
											_data.put(itemId, data);
										}
										if (!data.containsKey(op.getLevel()))
										{
											data.put(op.getLevel(), op);
										}
										
										op.setOption(i, id);
									}
								}
								counter++;
							}
						}
					}
				}
			}
		}
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _data.size() + " items and " + counter + " options.");
	}
	
	/**
	 * @param itemId
	 * @return if specified item id has available enchant effects.
	 */
	public boolean hasOptions(int itemId)
	{
		return _data.containsKey(itemId);
	}
	
	/**
	 * @param itemId
	 * @param enchantLevel
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(int itemId, int enchantLevel)
	{
		if (!_data.containsKey(itemId) || !_data.get(itemId).containsKey(enchantLevel))
		{
			return null;
		}
		return _data.get(itemId).get(enchantLevel);
	}
	
	/**
	 * @param item
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(Item item)
	{
		return item != null ? getOptions(item.getId(), item.getEnchantLevel()) : null;
	}
	
	/**
	 * Gets the single instance of EnchantOptionsData.
	 * @return single instance of EnchantOptionsData
	 */
	public static EnchantItemOptionsData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemOptionsData INSTANCE = new EnchantItemOptionsData();
	}
}
