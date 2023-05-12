package javafinal.tankwar.generator;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.model.Tank;
import javafinal.tankwar.tank.PlayerTank;
import javafinal.tankwar.tank.EnemyTank;

public class TankGenerator {
	
	//direction, offset, blood, bomb, missile
	private static int serial = 1;
	public static final int DEFAULT_TANK_OUTPUT = 10;
	private static int tanknum = 0;
	
	public static void reset() {
		serial = 1;
		tanknum = 0;
	}
	
	public static int getTankOutput() {
		return tanknum;
	}

	public static PlayerTank createCenterPlayerTank(Battlefield bf,
                                                    int w, int h, int[] cfg) {
		int rows = (h / bf.getMap().getYoffset());
		int cols = (w / bf.getMap().getXoffset());
		PlayerTank pTank = new PlayerTank(bf, Battlefield.playerImages, Tank.PLAYER_TANK, serial++,
				bf.getMap().getCols() / 2,
				bf.getMap().getRows() - rows, rows, cols);
		pTank.setHead(cfg[0]);
		pTank.setOffset(cfg[1]);
		pTank.setBlood(cfg[2]);
		pTank.setBomb(cfg[3]);
		return pTank;
	}

	public static EnemyTank createEnemyTank(Battlefield bf,
											 int w, int h, int[] cfg) {
		//how many rows and cols that the enemy tank takes
		short rows = (short) (h / bf.getMap().getYoffset());
		short cols = (short) (w / bf.getMap().getXoffset());
		//randomly select one kind of enemy tanks
		int i = (int) (Math.random() * 1000) % Battlefield.enemyImages.length;
		int x = ((int) (Math.random() * 10000)) % (bf.getMap().getCols() - cols);
		EnemyTank etank = new EnemyTank(bf,
				Battlefield.enemyImages[i], Tank.ENEMY_TANK, serial++, x, 0, rows, cols);
		etank.setHead(cfg[0]);
		etank.setOffset(cfg[1]);
		etank.setBlood(cfg[2]);
		etank.setBomb(cfg[3]);
		etank.setSeeds(new int[] {cfg[4], cfg[5]});
		tanknum++;
		return etank;
	}
}
