

import java.util.Random;

public abstract class NoiseMaker2D {
	public final long SEED;
	public static final int RANDRANGE=0x400000;
	private int[] values;
	
	public NoiseMaker2D(long seed) {
		SEED=seed;
		
		//Force uniform distribution by having every number
		values=new int[RANDRANGE];
		for (int i=0;i<RANDRANGE;i++)
			values[i]=i;
		
		//Shuffle the array
		Random gen=new Random(seed);
		int t,r;
		for (int i=0;i<RANDRANGE;i++) {
			t=values[i];
			r=gen.nextInt(RANDRANGE);
			values[i]=values[r];
			values[r]=t;
		}
	}
	
	public double get(double x,double y) {
		return (double) values[(values[(int) x%RANDRANGE]+(int) y)%RANDRANGE]/RANDRANGE; // [0, 1)
	}
}
