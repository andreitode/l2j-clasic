package org.classiclude.tools.dbinstaller;

import java.sql.Connection;

/**
 * @author mrTJO
 */
public interface DBOutputInterface
{
	void setProgressIndeterminate(boolean value);
	
	void setProgressMaximum(int maxValue);
	
	void setProgressValue(int value);
	
	void setFrameVisible(boolean value);
	
	void appendToProgressArea(String text);
	
	Connection getConnection();
	
	int requestConfirm(String title, String message, int type);
	
	void showMessage(String title, String message, int type);
}
