package org.classiclude.gameserver.data.xml;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.classiclude.commons.util.IXmlReader;

/**
 * @author YourName
 */
public class SkillRoutes implements IXmlReader
{
	private final Map<Integer, Map<Integer, String>> _skillRoutes = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, String>> _skillDescriptions = new ConcurrentHashMap<>();
	
	protected SkillRoutes()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_skillRoutes.clear();
		parseDatapackFile("data/skillRoutes.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equals(listNode.getNodeName()))
			{
				for (Node skillRoutesNode = listNode.getFirstChild(); skillRoutesNode != null; skillRoutesNode = skillRoutesNode.getNextSibling())
				{
					if ("skillRoutes".equals(skillRoutesNode.getNodeName()))
					{
						for (Node skillRouteNode = skillRoutesNode.getFirstChild(); skillRouteNode != null; skillRouteNode = skillRouteNode.getNextSibling())
						{
							if ("skillRoute".equals(skillRouteNode.getNodeName()))
							{
								int skillId = -1;
								int routeId = -1;
								String name = "";
								String description = "";
								
								for (Node detailNode = skillRouteNode.getFirstChild(); detailNode != null; detailNode = detailNode.getNextSibling())
								{
									switch (detailNode.getNodeName())
									{
										case "skillId":
											skillId = Integer.parseInt(detailNode.getTextContent());
											break;
										case "routeId":
											routeId = Integer.parseInt(detailNode.getTextContent());
											break;
										case "name":
											name = detailNode.getTextContent();
											break;
										case "description":
											description = detailNode.getTextContent();
											break;
									}
								}
								
								if ((skillId != -1) && (routeId != -1))
								{
									_skillRoutes.computeIfAbsent(skillId, k -> new ConcurrentHashMap<>()).put(routeId, name);
									_skillDescriptions.computeIfAbsent(skillId, k -> new ConcurrentHashMap<>()).put(routeId, description);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public String getRouteName(int skillId, int routeId)
	{
		if (_skillRoutes.containsKey(skillId))
		{
			return _skillRoutes.get(skillId).getOrDefault(routeId, "Sin ruta");
		}
		return "Sin ruta";
	}
	
	public int getRouteId(int skillId, String routeName)
	{
		if (_skillRoutes.containsKey(skillId))
		{
			for (Map.Entry<Integer, String> entry : _skillRoutes.get(skillId).entrySet())
			{
				if (entry.getValue().equalsIgnoreCase(routeName))
				{
					return entry.getKey();
				}
			}
		}
		return -1;
	}
	
	public String getRouteDescription(int skillId, int routeId)
	{
		if (_skillDescriptions.containsKey(skillId))
		{
			return _skillDescriptions.get(skillId).getOrDefault(routeId, "Sin descripción");
		}
		return "Sin descripción";
	}
	
	public static SkillRoutes getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillRoutes INSTANCE = new SkillRoutes();
	}
}