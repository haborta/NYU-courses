package javafinal.tankwar.tank;
import java.awt.Image;
import java.awt.event.KeyEvent;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.generator.BulletGenerator;
import javafinal.tankwar.model.Bullet;
import javafinal.tankwar.model.Tank;
import javafinal.tankwar.Predefined;

public class PlayerTank extends Tank {

	public PlayerTank(Battlefield bf, Image[] images, int t, int serial,
					  int x, int y, int rows, int cols) {
		super(bf, images, t, serial, x, y, rows, cols, Predefined.DIRECTION_U, 5);
	}
	
	public PlayerTank(Battlefield bf, Image[] images, int t, int serial,
					  int x, int y, int rows, int cols, int head, int blood) {
		super(bf, images, t, serial, x, y, rows, cols, head, blood);
	}
	
	public void keyPress(KeyEvent e) {
		if (! alive) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_A:
			direction |= Predefined.DIRECTION_L;
			break;
		case KeyEvent.VK_S:
			direction |= Predefined.DIRECTION_D;
			break;
		case KeyEvent.VK_D:
			direction |= Predefined.DIRECTION_R;
			break;
		case KeyEvent.VK_W:
			direction |= Predefined.DIRECTION_U;
			break;
		}
	}
	
	public void keyRelease(KeyEvent e) {
		if (! alive) return;		//do nothing for a dead tank
		switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				direction &= ~Predefined.DIRECTION_L;
				break;
			case KeyEvent.VK_S:
				direction &= ~Predefined.DIRECTION_D;
				break;
			case KeyEvent.VK_D:
				direction &= ~Predefined.DIRECTION_R;
				break;
			case KeyEvent.VK_W:
				direction &= ~Predefined.DIRECTION_U;
				break;
			case KeyEvent.VK_J:
				Bullet nbt = BulletGenerator.createPlayerBullet(BF,
							this, Bullet.NORMAL_BULLET, 0, 0, head);
				nbt.setX(x + (cols - nbt.getCols()) / 2);
				nbt.setY(y + (rows - nbt.getRows()) / 2);
				BF.addSynBullet(nbt);
				break;
			case KeyEvent.VK_K:
				if (bomb > 0) {
					bomb--;
					Bullet bbt = BulletGenerator.createPlayerBullet(BF,
							this, Bullet.BOMB_BULLET, 0, 0, head);
					bbt.setX(x + (cols - bbt.getCols()) / 2);
					bbt.setY(y + (rows - bbt.getRows()) / 2);
					BF.addSynBullet(bbt);
				}
				break;
		}
	}
}
