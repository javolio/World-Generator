

public class VariableOctaveSpectralSynthesisNoiseMaker2D implements ContinuousNoiseMaker2D {
	ContinuousNoiseMaker2D continuousNoise;
	ContinuousNoiseMaker2D octaveNoise;
	protected int m;
	protected double scale,stretch;
	
	public VariableOctaveSpectralSynthesisNoiseMaker2D(ContinuousNoiseMaker2D continuousNoise,ContinuousNoiseMaker2D octaveNoise,int maxDepth,double stretch) {
		this.continuousNoise=continuousNoise;
		this.octaveNoise=octaveNoise;
		m=maxDepth;
		scale=2-1/(1<<m-1);
		this.stretch=stretch;
	}
	
	@Override
	public double get(double x,double y) {
		double sum=0;
		double d=getDepth(x,y);
		for (int i=0;i<d;i++)
			sum+=continuousNoise.get(x*(1<<i),y*(1<<i))/(1<<i);
		sum+=continuousNoise.get(x*(1<<(int) d+1),y*(1<<(int) d+1))/(1<<(int) d+1)*(d-(int) d);
		return sum/scale;
	}
	
	public double getDepth(double x,double y) {
		return octaveNoise.get(x/stretch,y/stretch)*(m-1)+1;
	}
}
