package javafinal.tankwar;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

import javafinal.tankwar.display.*;
import javafinal.tankwar.display.BattleCanvas;
import javafinal.tankwar.generator.BulletGenerator;
import javafinal.tankwar.generator.TankGenerator;
import javafinal.tankwar.map.RandomMap;
import javafinal.tankwar.map.TankMap;
import javafinal.tankwar.model.Bullet;
import javafinal.tankwar.model.Explosion;
import javafinal.tankwar.model.Tank;
import javafinal.tankwar.model.Wall;
import javafinal.tankwar.tank.PlayerTank;
import javafinal.tankwar.tank.EnemyTank;


public class Battlefield extends JFrame {

	private static final long serialVersionUID = -1105359425320038750L;
	public static final int GAME_OVERED = -1;
	public static final int GAME_PUSHED = 0;
	public static final int GAME_INITIALIZATION = 1;
	public static final int GAME_MAP_SELECT = 2;
	public static final int GAME_RUNNING = 3;
	public static final int GAME_RANK = 4;
	
	public static Object lock = new Object();
	public static Dimension w_size = new Dimension(640, 512);

	public static int windowWidth = 0;
	public static int windowHeight = 0;
	//image resource
	public static Image[] background = null;
	public static Image[] playerImages = null;
	public static Image[][] enemyImages = null;
	public static Image[] bulletImages = null;
	public static Image[] wallImages = null;
	public static Image[][] explosionImages = null;
	
	public int bgWidth = 0;
	public int bgHeight = 0;
	
	//components
	private BattleCanvas canvas;
	private TankMap tankMap = null;
	private PlayerTank player = null;
	private HashMap<Integer, Tank> tanks = null;
	private ArrayList<Bullet> bullets = null;
	private ArrayList<Explosion> explosions = null;
	
	//user interface
	private Initialization init = null;
	private GameResult gresult = null;
	private GameHelp gameHelp = null;
	private GameRank grank = null;

	private int state = GAME_INITIALIZATION;
	private int bg = 0;					//canvas background index

	//double buffer draw
	private Image bufferImage = null;
	private Graphics bufferScreen = null;
	public Container container;

	public Battlefield battleField;

	public int enemy = 0;
	private int score = 0;
	private Client user;
	public UserLogin userLogin;

	
	public Battlefield(Client user, UserLogin userLogin) {
		battleField = this;
		this.user = user;
		this.userLogin = userLogin;
		windowWidth = w_size.width;
		windowHeight = w_size.height;
		userLogin.setVisible(false);
		setTitle("Battle City: Tank War");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container = getContentPane();
		container.setLayout(new BorderLayout());
		setResizable(false);
		canvas = new BattleCanvas();
		container.add(canvas);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}


	//load all the image resources
	public void loadResource() {
		bufferImage = createImage(w_size.width, w_size.height);
		bufferScreen = bufferImage.getGraphics();
		addKeyListener(new TankKeyListener());
		
		//load the background.
		ImageIcon bg = Predefined.loadImageIcon("bg-green.gif");
		background = new Image[] {
			Predefined.loadImageIcon("bg-grass.jpg").getImage(),
			Predefined.loadImageIcon("bg-sand.jpg").getImage()
		};
		bgWidth = bg.getIconWidth();
		bgHeight = bg.getIconHeight();
		
		//player tank
		playerImages = new Image[] {

				Predefined.loadImageIcon("tank/4/tank-u.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-ru.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-r.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-rd.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-d.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-ld.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-l.gif").getImage(),
				Predefined.loadImageIcon("tank/4/tank-lu.gif").getImage()
		};

		//enemy tank
		enemyImages = new Image[][] {
			new Image[] {
				Predefined.loadImageIcon("tank/1/tank-u.png").getImage(),
			 	Predefined.loadImageIcon("tank/1/tank-ru.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-r.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-rd.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-d.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-ld.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-l.png").getImage(),
				Predefined.loadImageIcon("tank/1/tank-lu.png").getImage()
			},
			new Image[] {
				Predefined.loadImageIcon("tank/2/tank-u.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-ru.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-r.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-rd.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-d.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-ld.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-l.png").getImage(),
				Predefined.loadImageIcon("tank/2/tank-lu.png").getImage()
			},
			new Image[] {
				Predefined.loadImageIcon("tank/3/tank-u.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-ru.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-r.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-rd.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-d.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-ld.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-l.gif").getImage(),
				Predefined.loadImageIcon("tank/3/tank-lu.gif").getImage()
			}
		};

		//bullet
		bulletImages = new Image[] {
			Predefined.loadImageIcon("bullet/normal.png").getImage(),
			Predefined.loadImageIcon("bullet/bomb.png").getImage()
		};

		//wall
		wallImages = new Image[] {
			Predefined.loadImageIcon("wall/grass.gif").getImage(),
			Predefined.loadImageIcon("wall/wood.gif").getImage(),
			Predefined.loadImageIcon("wall/brick.gif").getImage(),
			Predefined.loadImageIcon("wall/iron.gif").getImage()
		};

		//explosion
		explosionImages = new Image[][] {
			new Image[] {
				Predefined.loadImageIcon("explosion/1.png").getImage(),
				Predefined.loadImageIcon("explosion/2.png").getImage(),
				Predefined.loadImageIcon("explosion/3.png").getImage(),
				Predefined.loadImageIcon("explosion/4.png").getImage(),
				Predefined.loadImageIcon("explosion/5.png").getImage(),
				Predefined.loadImageIcon("explosion/6.png").getImage()
			},
			new Image[] {
				Predefined.loadImageIcon("explosion/big/1.png").getImage(),
				Predefined.loadImageIcon("explosion/big/2.png").getImage(),
				Predefined.loadImageIcon("explosion/big/3.png").getImage(),
				Predefined.loadImageIcon("explosion/big/4.png").getImage(),
				Predefined.loadImageIcon("explosion/big/5.png").getImage(),
				Predefined.loadImageIcon("explosion/big/6.png").getImage(),
				Predefined.loadImageIcon("explosion/big/7.png").getImage(),
				Predefined.loadImageIcon("explosion/big/8.png").getImage(),
				Predefined.loadImageIcon("explosion/big/9.png").getImage(),
				Predefined.loadImageIcon("explosion/big/10.png").getImage(),
				Predefined.loadImageIcon("explosion/big/11.png").getImage(),
				Predefined.loadImageIcon("explosion/big/12.png").getImage(),
				Predefined.loadImageIcon("explosion/big/13.png").getImage(),
				Predefined.loadImageIcon("explosion/big/14.png").getImage(),
				Predefined.loadImageIcon("explosion/big/15.png").getImage(),
				Predefined.loadImageIcon("explosion/big/16.png").getImage(),
				Predefined.loadImageIcon("explosion/big/17.png").getImage(),
				Predefined.loadImageIcon("explosion/big/18.png").getImage(),
				Predefined.loadImageIcon("explosion/big/19.png").getImage(),
				Predefined.loadImageIcon("explosion/big/20.png").getImage(),
				Predefined.loadImageIcon("explosion/big/21.png").getImage(),
				Predefined.loadImageIcon("explosion/big/22.png").getImage(),
				Predefined.loadImageIcon("explosion/big/23.png").getImage(),
				Predefined.loadImageIcon("explosion/big/24.png").getImage(),
				Predefined.loadImageIcon("explosion/big/25.png").getImage(),
				Predefined.loadImageIcon("explosion/big/26.png").getImage(),
				Predefined.loadImageIcon("explosion/big/27.png").getImage(),
				Predefined.loadImageIcon("explosion/big/28.png").getImage(),
				Predefined.loadImageIcon("explosion/big/29.png").getImage(),
				Predefined.loadImageIcon("explosion/big/30.png").getImage()
			}
		};
		//create help image
		gameHelp = new GameHelp(250, 130);

		//user interface
		init = new Initialization();

		grank = new GameRank(user, false);

		short width = Predefined.X_OFFSET, height = Predefined.Y_OFFSET;

		tankMap = new TankMap(windowHeight / height, windowWidth / width,
					width, height, null);
		
		canvas.setBufferImage(bufferImage);	//loading tips

		start();
	}
	
	public TankMap getMap() {
		return tankMap;
	}
	
	//start to play the game
	public void play() {
		score = 0;
		enemy = 0;
		gresult = null;				//reset the game result user interface
		player = null;				//clear the player
		if (tanks != null) tanks.clear();
		if (bullets != null) bullets.clear();
		if (explosions != null) explosions.clear();
		tankMap.clear();			//clear the map
		System.gc();				//start the garbage collection
		create();

		state = GAME_RUNNING;
	}

	private void create() {
		bg = ((int) (Math.random() * 10000)) % background.length;
		
		//player tank
		int tankWidth = Predefined.T_WIDTH, tankHeight = Predefined.T_HEIGHT;
		tanks = new HashMap<Integer, Tank>();
		TankGenerator.reset();

		player = TankGenerator.createCenterPlayerTank(this, tankWidth, tankHeight, Predefined.playerArgs);
		tanks.put(player.getSerial(), player);
		
		//enemy tank
		int tankNum = TankGenerator.DEFAULT_TANK_OUTPUT;
		EnemyTank etank;
		for (int j = 0; j < tankNum; j++) {
			etank = TankGenerator.createEnemyTank(this, tankWidth, tankHeight, Predefined.enemyArgs);
			tanks.put(etank.getSerial(), etank);
		}

		//set the walls
		HashMap<Integer, Wall> wmap = RandomMap.generate(
				wallImages, 8, 8,
				tankMap.getCols() - 2 * player.getCols(),
				tankMap.getRows() - 2 * player.getRows(), player.getCols(), player.getRows());
		tankMap.setWalls(wmap);
		
		//bullets
		BulletGenerator.playerDamage = Predefined.playerDamage;
		BulletGenerator.enemyDamage = Predefined.enemyDamage;
		bullets = new ArrayList<Bullet>();

		//explosion
		explosions = new ArrayList<Explosion>();
	}
	
	//start the game
	private void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (tanks != null && tanks.size() < 10 && player != null) {
						//keep generating new enemy tanks
						EnemyTank etank = TankGenerator.createEnemyTank(battleField, Predefined.T_WIDTH, Predefined.T_HEIGHT, Predefined.enemyArgs);
						tanks.put(etank.getSerial(), etank);
					}
					if (state == GAME_PUSHED) {
						synchronized (lock) {
							try {lock.wait();} catch (InterruptedException e) {break;}
						}
					}

					try {Thread.sleep(30);} catch (InterruptedException e) {break;}

					switch (state) {
						case GAME_RANK:
							grank.draw(bufferScreen);
							break;
						case GAME_INITIALIZATION:
							init.draw(bufferScreen);
							break;
						case GAME_MAP_SELECT:
							break;
						case GAME_RUNNING:
							runningDraw();
							break;
						case GAME_OVERED:
							if (gresult != null) {
								gresult.draw(bufferScreen);
							}
							break;
					}
					
					//draw the help window
					if (gameHelp.isVisible()) 
						bufferScreen.drawImage(gameHelp.getHelpImage(),
							(w_size.width - gameHelp.getWidth()) / 2,
							(w_size.height - gameHelp.getHeight()) / 2, null);
					
					//refresh the canvas
					canvas.repaint();
				}
			}
		}).start();
	}

	public void runningDraw() {
		//draw the background.
		drawBackground(bufferScreen);
		
		//player tank
		if (player.isAlive()) {
			player.draw(bufferScreen);
		} else {
			state = GAME_OVERED;
			gresult = new GameResult(score);
		}

		Iterator<Entry<Integer, Tank>> eit = tanks.entrySet().iterator();
		Tank etank;
		boolean _new = false;
		while (eit.hasNext()) {
			Entry<Integer, Tank> e = eit.next();
			etank = e.getValue();
			if (! etank.isAlive()) {
				eit.remove();			//the tank is dead
				//_new = true;
			}
			else etank.draw(bufferScreen);
		}
		//bullets
		synchronized (bullets) {
			Iterator<Bullet> bit = bullets.iterator();
			Bullet btemp;
			while (bit.hasNext()) {
				btemp = bit.next();
				if (! btemp.isAlive()) bit.remove();
				else btemp.draw(bufferScreen);
			}
		}

		tankMap.draw(bufferScreen);
		
		//explosion
		Iterator<Explosion> expit = explosions.iterator();
		Explosion exp;
		while (expit.hasNext()) {
			exp = expit.next();
			if (exp.isAlive()) exp.draw(bufferScreen);
			else expit.remove();
		}
		
		//draw the game info
		drawGameInfo(bufferScreen);
	}

	private void drawGameInfo(Graphics g) {
		score = enemy;
		String str = "Current Score: " + score + ", Enemy beaten: " + enemy + ", Press h for Help";
		g.setFont(Predefined.iFont);
		g.setColor(Predefined.iColor);
		g.drawString(str,
			w_size.width - g.getFontMetrics().stringWidth(str) - 10,
			w_size.height - Predefined.iFont.getSize());
	}

	public Tank getTankBySerial(int serial) {
		return tanks.get(serial);
	}

	public void addSynBullet(final Bullet e) {
		synchronized (bullets) {
			bullets.add(e);
		}
	}

	public void addBullet(final Bullet e) {
		bullets.add(e);
	}

	public void addExplosion(final Explosion exp) {
		explosions.add(exp);
	}

	public void drawBackground(Graphics g) {
		int x = 0, y = 0;
		do {
			bufferScreen.drawImage(background[bg], x, y, this);
			x += bgWidth;
			if (x >= getWidth()) {
				x = 0;
				y += bgHeight;
			}
			if (y >= getHeight()) break;
		} while (true);
	}

	private class TankKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (state == GAME_RANK) {
					state = GAME_INITIALIZATION;
					return;
				}
				if (state == GAME_PUSHED) synchronized (lock) {lock.notify();}
				if (state != GAME_OVERED) {
					int option = JOptionPane.showConfirmDialog(null,"Your current score is " + score +
							". Are you sure to quit the game？", "Tips", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (option == JOptionPane.OK_OPTION) {
						state = GAME_INITIALIZATION;
						user.sendScore(score);
					}else {
						synchronized (lock) {
							lock.notify();
							state = GAME_RUNNING;
						}
					}
				}
				if (state == GAME_OVERED) {
					state = GAME_INITIALIZATION;
					user.sendScore(score);
				}
				return;
			}
			
			//start/stop witch
			if (e.getKeyCode() == KeyEvent.VK_P) {
				switch (state) {
				case GAME_PUSHED:		//stop, make it start
					synchronized (lock) {
						lock.notify();
						state = GAME_RUNNING;
					}
					break;
				case GAME_RUNNING: state = GAME_PUSHED; break;
				}
				return;
			}

			//game help window
			if (e.getKeyCode() == KeyEvent.VK_H) {
				gameHelp.setVisible(!gameHelp.isVisible());
			}
			
			switch (state) {
			case GAME_INITIALIZATION:
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					user.sendRankRequest();
					boolean hasRank = !(user.allScores.equals(""));
					state = GAME_RANK;
					grank = new GameRank(user, hasRank);
					grank.draw(bufferScreen);
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					battleField.play();
				}
				break;
			case GAME_MAP_SELECT: break;
			case GAME_RUNNING:
				player.keyPress(e);
				break;
			case GAME_OVERED:
				user.sendScore(score);
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {			
			switch (state) {
			case GAME_RUNNING:	player.keyRelease(e); break;
			}
		}
	}
}
