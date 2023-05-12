package javafinal.tankwar.model;

import java.awt.Graphics;
import java.awt.Image;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.generator.ExplosionGenerator;
import javafinal.tankwar.map.TankMap;
import javafinal.tankwar.Predefined;


public class Bullet {
	
	public static final short NORMAL_BULLET = 0;
	public static final short BOMB_BULLET = 1;
	
	public static Battlefield BF = null;
	public static TankMap MAP = null;
	
	private Image image;
	private Tank mtank;
	private int tankType;
	private int x;
	private int y;
	private short rows;
	private short cols;
	private int offset = 1;
	private short t;
	private int blood = 1;	//one normal bullet takes one blood of the tank
	private int direction = 0;
	private boolean alive = true;

	public Bullet(Battlefield bf, Image image, short t, Tank mtank, int tankType,
				int x, int y, short rows, short cols, int direction) {
		this(bf, image, t, mtank, tankType, x, y, rows, cols, direction, 1);
	}

	public Bullet(Battlefield bf, Image image, short t, Tank mtank, int tankType,
				int x, int y, short rows, short cols, int direction, int blood) {
		BF = bf;
		MAP = bf.getMap();
		this.image = image;
		this.t = t;
		this.mtank = mtank;
		this.tankType = tankType;
		this.x = x;
		this.y = y;
		this.rows = rows;
		this.cols = cols;
		this.direction = direction;
		this.blood = blood;
	}

	public void draw(Graphics g) {
		g.drawImage(image, (x - cols / 2) * MAP.getXoffset(),
				(y - rows / 2) * MAP.getYoffset(), null);
		move();
	}

	private void move() {
		switch (direction) {
		case Predefined.DIRECTION_U:	y -= offset; break;
		case Predefined.DIRECTION_RU:	x += offset; y -= offset; break;
		case Predefined.DIRECTION_R:	x += offset; break;
		case Predefined.DIRECTION_RD:	x += offset; y += offset; break;
		case Predefined.DIRECTION_D:	y += offset; break;
		case Predefined.DIRECTION_LD:	x -= offset; y += offset; break;
		case Predefined.DIRECTION_L:	x -= offset; break;
		case Predefined.DIRECTION_LU:	x -= offset; y -= offset; break;	
		}
		
		//the bullet has out of the map.
		if ((x < 0 || x >= (MAP.getCols() - 1)) 
					|| (y < 0 || y >= (MAP.getRows() - 1))) {
			setAlive(false);
			return;
		}
		//check if the bullet has hit something.
		int bit = MAP.getBit(x, y);
		if (bit == mtank.getSerial()) {
			return;
		}
		if (bit < 0) {							//hit the wall or the bullet
			if (bit != TankMap.BIT_BULLET) {
				Wall w = MAP.getWall(bit);
				if (w != null && w.couldSmash(this)) {
					MAP.clearWall(w);
					w.setAlive(false);				//clear the Wall if it could be mashed.
				}
			}
			setAlive(false);
		} else if (bit > 0) {						//hit the tank.
			Tank tank = BF.getTankBySerial(bit);	//get the tank that was hit
			if (tank == null)		return;
			if (tank.getType() != tankType) {			//not same tank type (not teammate)
				if (tank.getBlood() > blood) {	//take its blood
					tank.setBlood(tank.getBlood() - blood);
				} else {							//the tank is destroyed
					tank.setAlive(false);

					//count the amount of the beaten enemies
					if (tank.getType() == 1) {
						BF.enemy++;
						//System.out.println("Beat " + BF.enemy + "th enemy");
					}
					//add an explosion the battlefield
					BF.addExplosion(ExplosionGenerator.createExplosion(BF, x, y,
							t == BOMB_BULLET ? Explosion.SUPER_EXPLOSION : Explosion.SMALL_EXPLOSION));
				}
				setAlive(false);
			}
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public short getRows() {
		return rows;
	}
	
	public short getCols() {
		return cols;
	}

	public int getBlood() {
		return blood;
	}

	public void setBlood(int blood) {
		this.blood = blood;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public Tank getMasterTank() {
		return mtank;
	}
	
	public int getType() {
		return t;
	}
	
	public int getTankType() {
		return tankType;
	}
}
