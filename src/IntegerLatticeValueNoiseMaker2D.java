

import java.util.Random;

public class IntegerLatticeValueNoiseMaker2D {
	public final long SEED;
	public static final int RANDRANGE=0x400000;
	private int[] values;
	
	public IntegerLatticeValueNoiseMaker2D(long seed) {
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
	
	public double get(int x,int y) {
		return (double) values[(values[x%RANDRANGE]+y)%RANDRANGE]/(RANDRANGE-1); // [0, 1)
	}
}
