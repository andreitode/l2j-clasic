package org.classiclude.gameserver.script;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Luis Arias
 */
public class ScriptEngine
{
	public static final Map<String, ParserFactory> PARSER_FACTORIES = new HashMap<>();
	
	protected static Parser createParser(String name) throws ParserNotCreatedException
	{
		ParserFactory s = PARSER_FACTORIES.get(name);
		if (s == null) // shape not found
		{
			try
			{
				Class.forName("org.classiclude.gameserver.script." + name);
				// By now the static block with no function would
				// have been executed if the shape was found.
				// the shape is expected to have put its factory
				// in the hashtable.
				s = PARSER_FACTORIES.get(name);
				if (s == null) // if the shape factory is not there even now
				{
					throw new ParserNotCreatedException();
				}
			}
			catch (ClassNotFoundException e)
			{
				// We'll throw an exception to indicate that
				// the shape could not be created
				throw new ParserNotCreatedException();
			}
		}
		return s.create();
	}
}
