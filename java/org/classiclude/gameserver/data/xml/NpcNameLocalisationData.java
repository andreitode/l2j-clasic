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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.classiclude.Config;
import org.classiclude.commons.util.IXmlReader;
import org.classiclude.gameserver.model.StatSet;

/**
 * @author Mobius
 */
public class NpcNameLocalisationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(NpcNameLocalisationData.class.getName());
	
	private static final Map<String, Map<Integer, String[]>> NPC_NAME_LOCALISATIONS = new ConcurrentHashMap<>();
	private static String _lang;
	
	protected NpcNameLocalisationData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		NPC_NAME_LOCALISATIONS.clear();
		
		if (Config.MULTILANG_ENABLE)
		{
			for (String lang : Config.MULTILANG_ALLOWED)
			{
				final File file = new File("data/lang/" + lang + "/NpcNameLocalisation.xml");
				if (!file.isFile())
				{
					continue;
				}
				
				NPC_NAME_LOCALISATIONS.put(lang, new ConcurrentHashMap<>());
				_lang = lang;
				parseDatapackFile("data/lang/" + lang + "/NpcNameLocalisation.xml");
				final int count = NPC_NAME_LOCALISATIONS.get(lang).values().size();
				if (count == 0)
				{
					NPC_NAME_LOCALISATIONS.remove(lang);
				}
				else
				{
					LOGGER.log(Level.INFO, getClass().getSimpleName() + ": Loaded localisations for [" + lang + "].");
				}
			}
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "localisation", localisationNode ->
		{
			final StatSet set = new StatSet(parseAttributes(localisationNode));
			NPC_NAME_LOCALISATIONS.get(_lang).put(set.getInt("id"), new String[]
			{
				set.getString("name"),
				set.getString("title")
			});
		}));
	}
	
	/**
	 * @param lang
	 * @param id
	 * @return a String Array[] that contains NPC name and title or Null if is does not exist.
	 */
	public String[] getLocalisation(String lang, int id)
	{
		final Map<Integer, String[]> localisations = NPC_NAME_LOCALISATIONS.get(lang);
		if (localisations != null)
		{
			return localisations.get(id);
		}
		return null;
	}
	
	public boolean hasLocalisation(int id)
	{
		for (Map<Integer, String[]> data : NPC_NAME_LOCALISATIONS.values())
		{
			if (data.containsKey(id))
			{
				return true;
			}
		}
		return false;
	}
	
	public static NpcNameLocalisationData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcNameLocalisationData INSTANCE = new NpcNameLocalisationData();
	}
}
