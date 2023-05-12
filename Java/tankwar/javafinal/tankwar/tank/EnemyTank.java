package javafinal.tankwar.tank;

import java.awt.Graphics;
import java.awt.Image;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.generator.BulletGenerator;
import javafinal.tankwar.model.Bullet;
import javafinal.tankwar.model.Tank;

public class EnemyTank extends Tank {
	
	private int[] seeds = new int[] {20, 50};
	
	private int moveInterval = 20;
	private int shotInterval = 50;
	
	public EnemyTank(Battlefield bf, Image[] images, int t, int serial,
					 int x, int y, int rows, int cols) {
		super(bf, images, t, serial, x, y, rows, cols);
	}
	
	public EnemyTank(Battlefield bf, Image[] images, int t,
					 int serial, int x, int y, int rows, int cols, int head, int blood) {
		super(bf, images, t, serial, x, y, rows, cols, head, blood);
	}
	
	@Override
	public void draw(Graphics g) {
		super.draw(g);
		if (moveInterval-- == 0) {
			int t = ((int) (Math.random() * 10000)) % 7;
			direction = 1 << t;
			moveInterval = ((int) (Math.random() * 1000)) % seeds[0];
		}

		if (shotInterval-- == 0) {
			Bullet bbt = BulletGenerator.createEnemyBullet(BF,
					this, Bullet.NORMAL_BULLET, 0, 0, head);
			bbt.setX(x + (cols - bbt.getCols()) / 2);
			bbt.setY(y + (rows - bbt.getRows()) / 2);
			BF.addBullet(bbt);
			shotInterval = ((int) ((Math.random() + 1) * 10000)) % seeds[1];
		}
	}

	public void setSeeds(int[] seeds) {
		this.seeds = seeds;
	}
}
