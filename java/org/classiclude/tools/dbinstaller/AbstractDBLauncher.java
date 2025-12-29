package org.classiclude.tools.dbinstaller;

/**
 * Abstract Database Launcher.
 * @author Zoey76
 */
public abstract class AbstractDBLauncher
{
	protected static String getArg(String arg, String[] args)
	{
		try
		{
			int i = 0;
			do
			{
				// Nothing is missing here.
			}
			while (!arg.equalsIgnoreCase(args[i++]));
			return args[i];
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
