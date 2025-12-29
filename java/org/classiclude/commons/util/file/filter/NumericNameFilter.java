package org.classiclude.commons.util.file.filter;

import java.io.File;

/**
 * Specialized {@link XMLFilter} class.<br>
 * Accepts <b>files</b> matching "numbers".xml only.
 * @author UnAfraid
 */
public class NumericNameFilter extends XMLFilter
{
	@Override
	public boolean accept(File f)
	{
		return super.accept(f) && f.getName().matches("\\d+\\.xml");
	}
}
