package javafinal.tankwar.display;

import javafinal.tankwar.Battlefield;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class GameResult {

	private int w;
	private int h;

	private int score;

	public GameResult(int score) {
		this.w = Battlefield.windowWidth;
		this.h = Battlefield.windowHeight;

		this.score = score;
	}

	public void draw(Graphics g) {

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, w, h);

		String str = "Sorry, you have been beaten.";

		Font f = new Font("Arial", Font.BOLD, 20);
		g.setColor(Color.WHITE);
		g.setFont(f);
		g.drawString(str, (w - g.getFontMetrics(f).stringWidth(str)) / 2, h / 2 - 150);

		str = "Current Score: " + score;

		f = new Font("Arial", Font.BOLD, 20);
		g.setColor(new Color(250, 180, 20));
		g.setFont(f);
		g.drawString(str, (w - g.getFontMetrics(f).stringWidth(str)) / 2, h / 2);

		str = "Please press \"ESCAPE\" to return!";

		f = new Font("Arial", Font.BOLD, 15);

		g.setColor(Color.gray);
		int rectWidth = g.getFontMetrics(f).stringWidth(str) + 20;
		int rectHeight = 25;
		g.fill3DRect((w - rectWidth) / 2, h / 2 + 80, rectWidth, rectHeight, true);


		g.setColor(Color.WHITE);
		g.setFont(f);
		int x2 = (w - g.getFontMetrics(f).stringWidth(str)) / 2;
		int y2 = h / 2 + 100;		//center - up space
		g.drawString(str, x2, y2);

	}
}

