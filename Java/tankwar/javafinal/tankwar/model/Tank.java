package javafinal.tankwar.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.Predefined;
import javafinal.tankwar.map.TankMap;

public class Tank {
	
	public static final int PLAYER_TANK = 0;
	public static final int ENEMY_TANK = 1;
	
	public static final Font font = new Font("Arial", Font.PLAIN, 10);

	protected static Battlefield BF = null;
	protected static TankMap MAP = null;
	//add serial number to identify every tank in the map
	protected int serial;
	protected Image[] images;
	protected int x;
	protected int y;
	protected int type;
	protected int rows;			//number of rows
	protected int cols;			//number of cols
	protected int head = Predefined.DIRECTION_U;
	protected int direction = 0;	//move direction
	protected int offset = 1;
	protected int blood;			//current number of bloods
	private int life = -1;				//total number of bloods
	protected boolean alive = true;

	protected int bomb = 1;
	protected int missile = 0;

	protected Tank(Battlefield bf, Image[] images, int type,
				int serial, int x, int y, int cols, int rows) {
		this(bf, images, type, serial, x, y, cols, rows, 0, 2);
	}

	protected Tank(Battlefield bf, Image[] images, int type,
			int serial, int x, int y, int cols, int rows, int head, int blood) {
		BF = bf;
		MAP = BF.getMap();
		this.images = images;
		this.type = type;
		this.serial = serial;
		this.cols = cols;
		this.rows = rows;
		this.x = x;
		this.y = y;
		this.head = head;
		this.blood = blood;
		//setMapBit(x, y, rows, cols, serial);
	}

	public int getMoveDirection() {
		int d = 0, direct = direction;		//copy the direction
		if ((direct & Predefined.DIRECTION_U) != 0) {		//move up
			d = Predefined.DIRECTION_U;
			if ((direct & Predefined.DIRECTION_L) != 0) d = Predefined.DIRECTION_LU;
			if ((direct & Predefined.DIRECTION_R) != 0) d = Predefined.DIRECTION_RU;
		} 
		else if ((direct & Predefined.DIRECTION_R) != 0) {	//move right
			d = Predefined.DIRECTION_R;
			if ((direct & Predefined.DIRECTION_U) != 0) d = Predefined.DIRECTION_RU;
			if ((direct & Predefined.DIRECTION_D) != 0) d = Predefined.DIRECTION_RD;
		}
		else if ((direct & Predefined.DIRECTION_D) != 0) {	//move down
			d = Predefined.DIRECTION_D;
			if ((direct & Predefined.DIRECTION_L) != 0) d = Predefined.DIRECTION_LD;
			if ((direct & Predefined.DIRECTION_R) != 0) d = Predefined.DIRECTION_RD;
		}
		else if ((direct & Predefined.DIRECTION_L) != 0) {	//move left
			d = Predefined.DIRECTION_L;
			if ((direct & Predefined.DIRECTION_U) != 0) d = Predefined.DIRECTION_LU;
			if ((direct & Predefined.DIRECTION_D) != 0) d = Predefined.DIRECTION_LD;
		}
		
		return d;
	}
	

	public void draw(Graphics g) {
		if (alive) {
		int x = this.x * MAP.getXoffset();
		int y = this.y * MAP.getYoffset();
		boolean ch = true, down = false;
		switch (head) {
		case Predefined.DIRECTION_U:	g.drawImage(images[0], x, y, null); down = true; break;
		case Predefined.DIRECTION_RU:	g.drawImage(images[1], x, y, null); down = true; break;
		case Predefined.DIRECTION_R:	g.drawImage(images[2], x, y, null); break;
		case Predefined.DIRECTION_RD:	g.drawImage(images[3], x, y, null); break;
		case Predefined.DIRECTION_D:	g.drawImage(images[4], x, y, null); break;
		case Predefined.DIRECTION_LD:	g.drawImage(images[5], x, y, null); break;
		case Predefined.DIRECTION_L:	g.drawImage(images[6], x, y, null); break;
		case Predefined.DIRECTION_LU:	g.drawImage(images[7], x, y, null); down = true; break;
		default : ch = false;
		}
		//draw the blood
		if (ch) {
			if (type == 0) {
				g.setColor(new Color(200, 120, 0));
			} else {
				g.setColor(Color.RED);
			}
			int w = (cols - 2) * MAP.getXoffset();
			int h = 4;
			x = x + MAP.getXoffset();
			y = down ? y + (rows + 1) * MAP.getYoffset() : y - 2 * MAP.getYoffset();
			g.drawRect(x, y, w, h);
			
			//draw the blood slider
			if (life == -1) life = blood;
			g.fillRect(x + 1, y, blood * (w - 1) / life, h);
			
			//draw the life number
			g.setColor(Color.WHITE);
			g.setFont(font);
			String str = blood + "/" + life + "," + bomb;
			g.drawString(str,
					x + (w - g.getFontMetrics().stringWidth(str)) / 2,
					y + font.getSize() / 2);
		}
		
		move();
		}
	}

	public void move() {
		int d = getMoveDirection();
		if (d == 0) return;
		head = d;
		int off;
		switch (d) {
		case Predefined.DIRECTION_U:
			off = getUpMoveOffset(x, y); if (off != 0) moveUp(off); break;
		case Predefined.DIRECTION_RU:
			off = getRightMoveOffset(x, y); if (off != 0)	moveRight(off);
			off = getUpMoveOffset(x, y); if (off != 0)	moveUp(off);
			break;
		case Predefined.DIRECTION_R:	
			off = getRightMoveOffset(x, y); if (off != 0)	moveRight(off); break;
		case Predefined.DIRECTION_RD:	
			off = getRightMoveOffset(x, y); if (off != 0)	moveRight(off);
			off = getDownMoveOffset(x, y); if (off != 0)	moveDown(off);
			break;
		case Predefined.DIRECTION_D:	
			off = getDownMoveOffset(x, y); if (off != 0)	moveDown(off); break;
		case Predefined.DIRECTION_LD:
			off = getDownMoveOffset(x, y); if (off != 0)	moveDown(off);
			off = getLeftMoveOffset(x, y); if (off != 0) 	moveLeft(off);
			break;
		case Predefined.DIRECTION_L:	
			off = getLeftMoveOffset(x, y); if (off != 0) 	moveLeft(off); break;
		case Predefined.DIRECTION_LU:	
			off = getLeftMoveOffset(x, y); if (off != 0) 	moveLeft(off);
			off = getUpMoveOffset(x, y); if (off != 0) moveUp(off);
			break;
		}
	}

	protected int getUpMoveOffset(int x, int y) {
		int off = offset;
		if (y - offset < 0) off = y;
		if (MAP.checkMapBit(x, y - off, off, cols, TankMap.BIT_BLANK)) return off;
		return 0;
	}

	private void moveUp(int off) {
		MAP.setMapBit(x, y + rows - off, off, cols, TankMap.BIT_BLANK);
		y -= off;
		MAP.setMapBit(x, y, off, cols, serial);
	}

	protected int getRightMoveOffset(int x, int y) {
		int off = offset;
		if (x + offset > (MAP.getCols() - cols)) off = MAP.getCols() - cols - x;
		if (MAP.checkMapBit(x + cols, y, rows, off, TankMap.BIT_BLANK)) return off;
		return 0;
	}

	private void moveRight(int off) {
		MAP.setMapBit(x, y, rows, off, TankMap.BIT_BLANK);
		x += off;
		MAP.setMapBit(x + cols - off, y, rows, off, serial);
	}

	protected int getDownMoveOffset(int x, int y) {
		int off = offset;
		if (y + offset > (MAP.getRows() - rows)) off = MAP.getRows() - rows - y;
		if (MAP.checkMapBit(x, y + rows, off, cols, TankMap.BIT_BLANK)) return off;
		return 0;
	}

	private void moveDown(int off) {
		MAP.setMapBit(x, y, off, cols, TankMap.BIT_BLANK);
		y += off;
		MAP.setMapBit(x, y + rows - off, off, cols, serial);
	}

	protected int getLeftMoveOffset(int x, int y) {
		int off = offset;
		if (x - offset < 0) off = x;
		if (MAP.checkMapBit(x - off, y, rows, off, TankMap.BIT_BLANK)) return off;
		return 0;
	}

	private void moveLeft(int off) {
		MAP.setMapBit(x + cols - off, y, rows, off, TankMap.BIT_BLANK);
		x -= off;
		MAP.setMapBit(x, y, rows, off, serial);
	}
	
	public int getSerial() {
		return serial;
	}
	
	public void setSerial(int serial) {
		this.serial = serial;
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
	
	public int getCols() {
		return cols;
	}
	
	public int getRows() {
		return rows;
	}

	public int getBlood() {
		return blood;
	}
	
	public void setHead(int head) {
		this.head = head;
	}

	public void setBlood(int blood) {
		this.blood = blood;
	}

	public void setBomb(int bomb) {
		this.bomb = bomb;
	}
	
	public void setMissile(int missile) {
		this.missile = missile;
	}

	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		if (alive == false) MAP.setMapBit(x, y, rows, cols, TankMap.BIT_BLANK);
		this.alive = alive;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
}
