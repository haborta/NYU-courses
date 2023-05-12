package javafinal.tankwar.map;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import javafinal.tankwar.model.Wall;

public class RandomMap {

	public static final String[] WORDS = {
			"|o|", "囧", "Y", "江", "M", "国",
			"恒", "K", "和", "海", "好", "磊", "H", "吠", "哭", "畅",
			"想", "+", "网", "络", "R", "武", "发", "X", "陳", "鑫", "V", "A"};

	public static int[][] invoke(int width, int height, String str) {

		//width and height are the size of the map where we try to create all the walls
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics grap = image.getGraphics();

		grap.setColor(Color.BLACK);
		grap.fillRect(0, 0, width, height);

		//draw the white string
		grap.setColor(Color.WHITE);
		grap.setFont(new Font("Arial", Font.BOLD, 12));
		grap.drawString(str,
				(width - grap.getFontMetrics().stringWidth(str)) / 2, height / 2);

		BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = temp.getGraphics();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.ORANGE);
		int w = image.getWidth();
		int h = image.getHeight();

		int x1 = w, y1 = h, x2 = 0, y2 = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (image.getRGB(x, y) == -1) {
					if (x < x1) x1 = x;
					if (x > x2) x2 = x;
					if (y < y1) y1 = y;
					if (y > y1) y2 = y;
				}
			}
		}

		int[][] maps = new int[x2 - x1 + 1][y2 - y1 + 1];

		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				if (image.getRGB(x, y) == -1) {
					maps[x - x1][y - y1] = -1;
				}
			}
		}

		return maps;
	}

	public static HashMap<Integer, Wall> generate(Image[] images, int rows, int cols,
												   int width, int height, int x_offset, int y_offset) {
		//get the words
		String str = WORDS[((int) (Math.random() * 10000)) % WORDS.length];
		int[][] maps = invoke(width, height, str);

		HashMap<Integer, Wall> wallMap = new HashMap<Integer, Wall>(16, 0.85F);
		int key = -1;

		//create the walls
		Wall wall;
		int x_off = width / maps.length;
		int y_off = height / maps[0].length;
		int t, xx, yy, r = y_off / rows, c = x_off / cols;

		for (int x = 0; x < maps.length; x++) {
			for (int y = 0; y < maps[0].length; y++) {
				if (maps[x][y] == -1) {
					t = ((int)(Math.random() * 10000)) % images.length;
					xx = x_offset + x * x_off;
					yy = y_offset + y * y_off;
					//add the wall sets
					for (int j = 0; j < r; j++) {
						for (int i = 0; i < c; i++) {
							wall = new Wall(images[t], t, key,
									xx + i * cols, yy + j * rows, rows, cols);
							wallMap.put(key--, wall);
						}
					}
				}
			}
		}

//		Wall wall;
//		HashMap<Integer, Wall> wallMap = new HashMap<>();
//		int wall_rows = height / rows;
//		int wall_cols = width / cols;
//		System.out.println("walls size: " + wall_rows + " " + wall_cols);
//		int key = -1;
//		for (int i = 0; i < wall_rows - 1; i++) {
//			for (int j = 0; j < wall_cols - 1; j++) {
//				boolean setWall = random.nextBoolean();
//				if (!setWall) {
//					continue;
//				}
//				int x = i * cols + 2 * x_offset;
//				int y = j * rows + 2 * y_offset;
//				int rand = random.nextInt(images.length);
//				System.out.println("coordinates: " + x + " " + y);
//				//add the wall
//				for (int r = 0; r < rows; r++) {
//					for (int c = 0; c < cols; c++) {
//						wall = new Wall(images[rand], rand, key, x, y, rows, cols);
//						wallMap.put(key--, wall);
//					}
//				}
//
//			}
//		}

		return wallMap;
	}
}
