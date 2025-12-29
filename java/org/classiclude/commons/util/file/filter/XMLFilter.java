package org.classiclude.commons.util.file.filter;

/**
 * Specialized {@link ExtFilter} class.<br>
 * Accepts files ending with ".xml" only.
 * @author mrTJO
 */
public class XMLFilter extends ExtFilter
{
	public XMLFilter()
	{
		super(".xml");
	}
}
