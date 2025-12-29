package org.classiclude.commons.util.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Specialized {@link FileFilter} class.<br>
 * Accepts <b>files</b> ending with ".htm" and ".html" only.
 * @author Zoey76
 */
public class HTMLFilter implements FileFilter
{
	@Override
	public boolean accept(File f)
	{
		if ((f == null) || !f.isFile())
		{
			return false;
		}
		final String name = f.getName().toLowerCase();
		return name.endsWith(".htm") || name.endsWith(".html");
	}
}
