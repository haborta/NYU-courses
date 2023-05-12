package javafinal.tankwar.generator;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.model.Bullet;
import javafinal.tankwar.model.Tank;

public class BulletGenerator {
	
	//rows, cols
	private static short[][] rect = {
		new short[]{3, 3},
		new short[]{4, 4}
	};
	
	//offset, blood
	public static short[][] playerDamage = {
		new short[] {4, 1},			//normal bullets
		new short[] {5, 5}			//bomb bullets
	};
	
	public static short[][] enemyDamage = {
		new short[] {4, 1},			//normal bullets
		new short[] {5, 5}			//bomb bullets
	};

	public static Bullet createPlayerBullet(Battlefield battleField,
					Tank mtank, short type, int x, int y, int direction) {
		Bullet e = new Bullet(battleField, Battlefield.bulletImages[type],
				type, mtank, Tank.PLAYER_TANK, x, y, rect[type][0], rect[type][1], direction);
		e.setOffset(playerDamage[type][0]);
		e.setBlood(playerDamage[type][1]);
		return e;
	}

	public static Bullet createEnemyBullet(Battlefield battleField,
					Tank mtank, short t, int x, int y, int direction) {
		Bullet e = new Bullet(battleField, Battlefield.bulletImages[t],
				t, mtank, Tank.ENEMY_TANK, x, y, rect[t][0], rect[t][1], direction);
		e.setOffset(enemyDamage[t][0]);
		e.setBlood(enemyDamage[t][1]);
		return e;
	}
}
