package org.classiclude.commons.util.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * @author lasarus
 */
public class ExtFilter implements FileFilter
{
	private final String _ext;
	
	public ExtFilter(String ext)
	{
		_ext = ext;
	}
	
	@Override
	public boolean accept(File f)
	{
		if ((f == null) || !f.isFile())
		{
			return false;
		}
		return f.getName().toLowerCase().endsWith(_ext);
	}
}
