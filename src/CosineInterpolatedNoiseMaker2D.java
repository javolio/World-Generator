

public class CosineInterpolatedNoiseMaker2D extends NoiseMaker2D {
	
	public CosineInterpolatedNoiseMaker2D(long seed) {
		super(seed);
	}
	
	@Override
	public double get(double x,double y) {
		double left=cosineInterpolate(super.get(x,y),super.get(x,y+1),y-(int) y);
		double right=cosineInterpolate(super.get(x+1,y),super.get(x+1,y+1),y-(int) y);
		return cosineInterpolate(left,right,x-(int) x);
	}
	
	private double cosineInterpolate(double a,double b,double x) {
		double f=(1-Math.cos(x*Math.PI))*.5;
		return a*(1-f)+b*f;
	}
	
}
