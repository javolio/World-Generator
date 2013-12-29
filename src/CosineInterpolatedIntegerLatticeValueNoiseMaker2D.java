

public class CosineInterpolatedIntegerLatticeValueNoiseMaker2D implements ContinuousNoiseMaker2D {
	IntegerLatticeValueNoiseMaker2D latticeNoise;
	
	public CosineInterpolatedIntegerLatticeValueNoiseMaker2D(IntegerLatticeValueNoiseMaker2D latticeNoise) {
		this.latticeNoise=latticeNoise;
	}
	
	@Override
	public double get(double x,double y) {
		double left=cosineInterpolate(latticeNoise.get((int) x,(int) y),latticeNoise.get((int) x,(int) y+1),y-(int) y);
		double right=cosineInterpolate(latticeNoise.get((int) x+1,(int) y),latticeNoise.get((int) x+1,(int) y+1),y-(int) y);
		return cosineInterpolate(left,right,x-(int) x);
	}
	
	private double cosineInterpolate(double a,double b,double x) {
		double f=(1-Math.cos(x*Math.PI))*.5;
		return a*(1-f)+b*f;
	}
	
}
