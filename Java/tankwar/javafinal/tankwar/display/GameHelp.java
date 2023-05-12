package javafinal.tankwar.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;


public class GameHelp {
	
	public static final String[] HELP = new String[]{
		"Game Help: ",
		"J - Shoot a normal bullet",
		"K - Shoot a bomb bullet",
		"P - Pause / Start",
		"ESC - Back to the main menu",
		"H - Popup / Close the window"
	};
	
	private Image image = null;
	private int w;
	private int h;
	private boolean show;
	
	public GameHelp(int w, int h) {
		this.w = w;
		this.h = h;
	}
	
	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return h;
	}
	
	public void setVisible(boolean show) {
		this.show = show;
	}
	
	public boolean isVisible() {
		return show;
	}

	public Image getHelpImage() {
		if (image == null) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			img = g.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
			g.dispose();
			g = img.createGraphics();
			
			g.setColor(new Color(0, 0, 0, 80));		//set the background
			//g.fill3DRect(0, 0, w, h, false);
			g.fillRoundRect(0, 0, w, h, 5, 5);
			
			Font f = new Font("Arial", Font.PLAIN, 14);
			g.setFont(f);
			g.setColor(new Color(255,100,50));
			int x_off = 20, y_off = 25;
			for (int j = 0; j < HELP.length; j++) {
				g.drawString(HELP[j], x_off, y_off + j * (f.getSize() + 5));
			}

			g.dispose();
			image = img;
		}
		
		return image;
	}
}
