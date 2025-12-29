package org.classiclude.gameserver.script;

public class ParserNotCreatedException extends Exception
{
	public ParserNotCreatedException()
	{
		super("Parser could not be created!");
	}
}
