package org.classiclude.commons.util.file.filter;

/**
 * Specialized {@link ExtFilter} class.<br>
 * Accepts <b>files</b> ending with ".sql" only.
 * @author Zoey76
 */
public class SQLFilter extends ExtFilter
{
	public SQLFilter()
	{
		super(".sql");
	}
}
