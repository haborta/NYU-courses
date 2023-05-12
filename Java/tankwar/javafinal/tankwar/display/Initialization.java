package javafinal.tankwar.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.Predefined;

public class Initialization {
	
	private Font font1 = new Font("Arial", Font.BOLD, 40);
	private Font font2 = new Font("Arial", Font.BOLD, 15);
	private Color color = new Color(250, 140, 40);

	private Image image;
	private int w;				//width of the screen
	private int h;				//height of the screen

	public Initialization() {
		this.w = Battlefield.windowWidth;
		this.h = Battlefield.windowHeight;
		this.image = Predefined.loadImageIcon("tank_bg.png").getImage();
	}

	public void draw(Graphics g) {

		g.drawImage(image, 0, 0, null);
		//title string
		String str1 = "Battle City: Tank War";
		g.setColor(color);
		g.setFont(font1);
		int x1 = (w - g.getFontMetrics(font1).stringWidth(str1)) / 2;
		int y1 = h / 2 - 50;		//center - up space
		g.drawString(str1, x1, y1);

		String str2 = "Please press \"ENTER\" to start the game!";
		String str3 = "Please press \"SPACE\" to view the ranking list!";

		g.setColor(Color.gray);
		int rectWidth = g.getFontMetrics(font2).stringWidth(str3) + 20;
		int rectHeight = 50;
		g.fill3DRect((w - rectWidth) / 2, h / 2 + 80, rectWidth, rectHeight, true);

		g.setColor(Color.BLACK);
		g.setFont(font2);
		int x2 = (w - g.getFontMetrics(font2).stringWidth(str2)) / 2;
		int y2 = h / 2 + 100;		//center - up space
		g.drawString(str2, x2, y2);

		g.setColor(Color.BLACK);
		g.setFont(font2);
		int x3 = (w - g.getFontMetrics(font2).stringWidth(str3)) / 2;
		int y3 = h / 2 + 120;		//center - up space
		g.drawString(str3, x3, y3);
	}
}


