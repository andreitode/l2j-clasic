package org.classiclude.gameserver.scripting;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for classes that are meant to be implemented by scripts.<br>
 * @author KenM
 */
public abstract class ManagedScript
{
	private static final Logger LOGGER = Logger.getLogger(ManagedScript.class.getName());
	
	private final Path _scriptFile;
	private long _lastLoadTime;
	private boolean _isActive;
	
	public ManagedScript()
	{
		_scriptFile = Path.of(ScriptEngineManager.getInstance().getCurrentLoadingScript().toUri());
		setLastLoadTime(System.currentTimeMillis());
	}
	
	/**
	 * Attempts to reload this script and to refresh the necessary bindings with it ScriptControler.<br>
	 * Subclasses of this class should override this method to properly refresh their bindings when necessary.
	 * @return true if and only if the script was reloaded, false otherwise.
	 */
	public boolean reload()
	{
		try
		{
			ScriptEngineManager.getInstance().executeScript(_scriptFile);
			return true;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed to reload script!", e);
			return false;
		}
	}
	
	public abstract boolean unload();
	
	public void setActive(boolean status)
	{
		_isActive = status;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	/**
	 * @return Returns the scriptFile.
	 */
	public Path getScriptFile()
	{
		return _scriptFile;
	}
	
	/**
	 * @param lastLoadTime The lastLoadTime to set.
	 */
	protected void setLastLoadTime(long lastLoadTime)
	{
		_lastLoadTime = lastLoadTime;
	}
	
	/**
	 * @return Returns the lastLoadTime.
	 */
	protected long getLastLoadTime()
	{
		return _lastLoadTime;
	}
	
	public abstract String getScriptName();
}
