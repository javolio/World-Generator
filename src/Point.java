public class Point {
	protected int x,y,z,river,precipitation,slope,temp;
	protected boolean riverSource;
	protected City owner;
	protected Biome biome;
	protected Point[] adjacent;
	
	public Point(int x,int y,int z) {
		this.x=x;
		this.y=y;
		this.z=z;
		river=-1;
		precipitation=0;
	}
	
	//Calculate the distance to another Point
	public int distanceTo(Point p) {
		return (int) Math.sqrt(Math.pow(p.x-x,2)+Math.pow(p.y-y,2));
	}
	
	/*public void setPrecipitation(int precipitation) {
		this.precipitation=Math.max(precipitation,0);
	}*/

	/*public double exactDistanceTo(Point p) {
		return Math.sqrt(Math.pow(p.x-x,2)+Math.pow(p.y-y,2));
	}*/
}
