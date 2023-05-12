package javafinal.tankwar;

import javax.swing.*;
import java.awt.*;

public class Predefined {

	static Toolkit TK = Toolkit.getDefaultToolkit();
	public static ImageIcon loadImageIcon(String _file) {
		return new ImageIcon(Predefined.class.getResource("/imageResource/" +_file));
	}

	//head, offset, blood, bomb
	public static int[] playerArgs = new int[] {Predefined.DIRECTION_U, 1, 10, 5};
	//head, offset, blood, bomb, seeds
	public static int[] enemyArgs = new int[] {Predefined.DIRECTION_D, 1, 1, 0, 50, 500};
	public static short[][] playerDamage = new short[][] {					//player
			new short[] {4, 1},			//normal bullets
			new short[] {4, 2}			//bomb bullets
			//new short[] {5, 5}			//missile bullets
	};
	public static short[][] enemyDamage = new short[][] {					//enemy
			new short[] {4, 1},
			new short[] {4, 2}
			//new short[] {5, 5}
	};

	public static final int DIRECTION_U = 1 << 0;
	public static final int DIRECTION_R = 1 << 1;
	public static final int DIRECTION_RU = 1 << 2;
	public static final int DIRECTION_RD = 1<< 3;

	public static final int DIRECTION_D = 1 << 4;
	public static final int DIRECTION_L = 1 << 5;
	public static final int DIRECTION_LU = 1 << 6;
	public static final int DIRECTION_LD = 1 << 7;

	public static final short X_OFFSET = 3;
	public static final short Y_OFFSET = 3;

	public static final int T_WIDTH = 48;
	public static final int T_HEIGHT = 48;

	public static final Font iFont = new Font("Arial", Font.PLAIN, 12);
	public static final Color iColor = Color.RED;
}
