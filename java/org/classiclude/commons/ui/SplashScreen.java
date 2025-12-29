package org.classiclude.commons.ui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JWindow;

/**
 * @author Mobius
 */
public class SplashScreen extends JWindow
{
	Image image;
	
	/**
	 * @param path of image file
	 * @param time in milliseconds
	 * @param parent frame to set visible after time ends
	 */
	public SplashScreen(String path, long time, JFrame parent)
	{
		setBackground(new Color(0, 255, 0, 0)); // Transparency.
		image = Toolkit.getDefaultToolkit().getImage(path);
		final ImageIcon imageIcon = new ImageIcon(image);
		setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
		
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				setVisible(false);
				if (parent != null)
				{
					// Make parent visible.
					parent.setVisible(true);
					// Focus parent window.
					parent.toFront();
					parent.setState(Frame.ICONIFIED);
					parent.setState(Frame.NORMAL);
				}
				dispose();
			}
		}, imageIcon.getIconWidth() > 0 ? time : 100);
	}
	
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, null);
	}
}
