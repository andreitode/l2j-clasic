package org.classiclude.gameserver.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ScriptDocument
{
	private static final Logger LOGGER = Logger.getLogger(ScriptDocument.class.getName());
	
	private Document _document;
	private final String _name;
	
	public ScriptDocument(String name, InputStream input)
	{
		_name = name;
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			final DocumentBuilder builder = factory.newDocumentBuilder();
			_document = builder.parse(input);
		}
		catch (SAXException sxe)
		{
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
			{
				x = sxe.getException();
			}
			LOGGER.warning(getClass().getSimpleName() + ": " + x.getMessage());
		}
		catch (ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			LOGGER.log(Level.WARNING, "", pce);
		}
		catch (IOException ioe)
		{
			// I/O error
			LOGGER.log(Level.WARNING, "", ioe);
		}
	}
	
	public Document getDocument()
	{
		return _document;
	}
	
	/**
	 * @return Returns the _name.
	 */
	public String getName()
	{
		return _name;
	}
	
	@Override
	public String toString()
	{
		return _name;
	}
}
