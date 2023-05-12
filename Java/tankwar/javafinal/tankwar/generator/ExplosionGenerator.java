package javafinal.tankwar.generator;

import javafinal.tankwar.Battlefield;
import javafinal.tankwar.model.Explosion;

public class ExplosionGenerator {
	
	//rows, cols, offset
	private static short[][] exp = new short[][] {
		new short[] {30, 30, 0},
		new short[] {42, 42, 0},
	};

	//create a new explosion .
	public static Explosion createExplosion(Battlefield bf, int x, int y, int t) {
		return new Explosion(bf, Battlefield.explosionImages[t], t,
				x, y, exp[t][0], exp[t][1], exp[t][2]);
	}
}
