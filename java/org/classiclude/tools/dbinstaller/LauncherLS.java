package org.classiclude.tools.dbinstaller;

import java.awt.HeadlessException;
import java.io.File;

import javax.swing.UIManager;

import org.classiclude.tools.dbinstaller.console.DBInstallerConsole;
import org.classiclude.tools.dbinstaller.gui.DBConfigGUI;

/**
 * Contains main class for Database Installer If system doesn't support the graphical UI, start the installer in console mode.
 * @author mrTJO
 */
public class LauncherLS extends AbstractDBLauncher
{
	public static void main(String[] args) throws Exception
	{
		final File file = new File(LauncherLS.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		final String dir = file.getParent() + "/" + "sql/login/";
		final String defDatabase = "classiclude";
		
		if ((args != null) && (args.length > 0))
		{
			new DBInstallerConsole(defDatabase, dir, getArg("-h", args), getArg("-p", args), getArg("-u", args), getArg("-pw", args), getArg("-d", args), getArg("-m", args));
			return;
		}
		
		try
		{
			// Set OS Look And Feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			// Ignore.
		}
		
		try
		{
			new DBConfigGUI(defDatabase, dir);
		}
		catch (HeadlessException e)
		{
			new DBInstallerConsole(defDatabase, dir);
		}
	}
}
