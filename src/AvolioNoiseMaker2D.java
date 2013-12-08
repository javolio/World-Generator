

public class AvolioNoiseMaker2D extends CosineInterpolatedNoiseMaker2D {
	protected NoiseMaker2D n;
	protected int m;
	protected double scale,stretch;
	
	public AvolioNoiseMaker2D(long seed,int maxDepth,double stretch,NoiseMaker2D n) {
		super(seed);
		m=maxDepth;
		scale=2-1/(1<<m-1);
		this.n=n;
		this.stretch=stretch;
	}
	
	@Override
	public double get(double x,double y) {
		double sum=0;
		double d=getDepth(x,y);
		for (int i=0;i<d;i++)
			sum+=super.get(x*(1<<i),y*(1<<i))/(1<<i);
		sum+=super.get(x*(1<<(int) d+1),y*(1<<(int) d+1))/(1<<(int) d+1)*(d-(int) d);
		return sum/scale;
	}
	
	public double getDepth(double x,double y) {
		return n.get(x/stretch,y/stretch)*(m-1)+1;
	}
}
