import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;

public class World implements Runnable {
	protected final int WIDTH,HEIGHT, // m, 40,000,000 and 20,000,000 for Earth
			RANGE=255, // m between max and min height
			OFFSET=64, // m distance between two independent points, should be a fraction of W
			WATERLEVEL=127,HUMIDITY=16,SEED;
	protected int year,maxSlope,endYear;
	protected Point[][] map;
	protected final ArrayList<Civilization> CIVS,FUTURE,ALL;
	protected final HashMap<String,Civilization> NAMES;
	protected JButton[] controllers;
	protected String history;
	
	//Initialize the world, but don't map anything
	public World(int seed,int width,int height) {
		WIDTH=width;
		HEIGHT=height;
		SEED=seed;
		map=new Point[WIDTH][HEIGHT];
		CIVS=new ArrayList<Civilization>();
		FUTURE=new ArrayList<Civilization>();
		ALL=new ArrayList<Civilization>();
		NAMES=new HashMap<String,Civilization>();
		year=0;
		history="";
	}
	
	public void setControllers(JButton[] c) {
		controllers=c;
	}
	
	//Map the world
	public void mapWorld() {
		System.out.println("Start Generation");
		
		//Elevation
		int max=0,min=0;
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1) {
				map[x][y]=new Point(x,y,(int) elevationNoise(x,y));
				if (map[x][y].z>max)
					max=map[x][y].z;
				if (map[x][y].z<min)
					min=map[x][y].z;
			}
		int range=max-min;
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1)
				map[x][y].z=(map[x][y].z-min)*RANGE/range;
		System.out.println("Elevation Complete");
		
		//Adjacency
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1) {
				map[x][y].adjacent=getAdjacentPoints(map[x][y]);
			}
		System.out.println("Adjacency Complete");
		
		//Slope and temperature
		int slope;
		max=0;
		Point[] adj;
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1) {
				map[x][y].temp=(int) (((1-Math.abs(1-y*2./HEIGHT))*3./5-map[x][y].z*2./5/RANGE+2./5)*150-25);
				slope=0;
				adj=map[x][y].adjacent;
				for (Point p:adj)
					slope+=Math.abs(p.z-map[x][y].z);
				map[x][y].slope=slope/adj.length;
				if (max<map[x][y].slope)
					max=map[x][y].slope;
			}
		System.out.println("Slope Complete");
		
		//Rivers
		ArrayList<Point> potential=new ArrayList<Point>();
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1) {
				if (map[x][y].z<=WATERLEVEL&&map[x][y].temp>=40) {
					for (Point p:map[x][y].adjacent)
						if (p.z>WATERLEVEL)
							potential.add(p);
				}
			}
		int s1,s2=SEED,tried=0;
		boolean good;
		for (int i=0;i<WIDTH*HEIGHT/10000&&tried<100;i++,tried++) {
			s1=rand3(s2);
			s2=rand4(s1);
			Point p=potential.get(s1%potential.size());
			if (p.river==-1&&p.z>WATERLEVEL) {
				good=false;
				for (Point p2:p.adjacent)
					if (p2.z<=WATERLEVEL)
						good=true;
				if (good) {
					tried=0;
					p.riverSource=true;
					p.river=i;
					runRiver(p,1);
				} else
					i--;
			} else
				i--;
		}
		System.out.println("Rivers Complete");
		
		//Precipitation
		Set<Point> cur=new HashSet<Point>();
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1)
				if (map[x][y].z<=WATERLEVEL) {
					map[x][y].precipitation=100;
					for (Point p:map[x][y].adjacent) {
						if (p.z>WATERLEVEL) {
							cur.add(p);
							p.precipitation=100-(p.z-WATERLEVEL)/HUMIDITY;
						}
					}
				}
		for (int precip=100;precip>0;precip--) {
			Point[] parr=cur.toArray(new Point[0]);
			for (Point curP:parr) {
				if (curP.precipitation==precip) {
					for (Point p2:curP.adjacent) {
						if (p2.precipitation==0) {
							p2.precipitation=curP.precipitation-(p2.z-WATERLEVEL)/HUMIDITY-1;
							cur.add(p2);
						}
					}
					cur.remove(curP);
				}
			}
		}
		System.out.println("Precipitation Complete");
		
		//Biomes
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1)
				map[x][y].biome=getBiome(map[x][y]);
	}
	
	//Take the 2D elevation noise function, and transform it into perlin noise
	protected double elevationNoise(double x,double y) {
		return elevation(x,y)-elevation(x*2,y*2)/2+elevation(x*4,y*4)/4+elevation(x*8,y*8)/8+elevation(x*16,y*16)/16+elevation(x*32,y*32)/32/*+elevation(x*64,y*64)/64+elevation(x*128,y*128)/128*/;
	}
	
	private double elevation(double x,double y) {
		int s1=rand1(SEED);
		for (int i=0;i<=x;i+=OFFSET)
			s1=rand1(s1);
		int s2=rand1(s1);
		for (int i=0;i<=y;i+=OFFSET) {
			s1=rand2(s1);
			s2=rand2(s2);
		}
		return cosineInterpolate(cosineInterpolate(s1,rand2(s1),y%OFFSET/OFFSET),cosineInterpolate(s2,rand2(s2),y%OFFSET/OFFSET),x%OFFSET/OFFSET);
	}
	
	//Recursively runs a river from a point
	private void runRiver(Point p,int length) {
		Point[] adj=p.adjacent;
		boolean coastal;
		ArrayList<Point> good=new ArrayList<Point>();
		for (int i=0;i<adj.length;i++) { //Find acceptable points
			coastal=false;
			for (Point p2:adj[i].adjacent)
				if (p2.z<=WATERLEVEL||p2.river!=-1&&p2!=p)
					coastal=true;
			if (!coastal&&adj[i].river==-1)
				good.add(adj[i]);
		}
		if (good.size()>0&&rand3(good.size()+length+p.z*p.x)%(100-length)!=0) {
			Point next=good.get(rand3(p.z*p.x*p.y)%good.size());
			if (next.river==-1) {
				next.river=p.river;
				runRiver(next,length+1);
			}
		}
		
	}
	
	private int rand1(int seed) { //X Height
		return Math.abs((seed*193+7309)*15739+26497)&1048575;
	}
	
	private int rand2(int seed) { //Y Height
		return Math.abs((seed*181+6997)*17837+24851)&1048575;
	}
	
	public int rand3(int seed) { //Rivers
		return Math.abs((seed*233+7159)*16223+25357)&1048575;
	}
	
	public int rand4(int seed) { //Rivers
		return Math.abs((seed*241+7129)*17599+23173)&1048575;
	}
	
	private double cosineInterpolate(double a,double b,double x) {
		double f=(1-Math.cos(x*Math.PI))*.5;
		return a*(1-f)+b*f;
	}
	
	@Deprecated
	public void printMap() {
		for (int y=0;y<HEIGHT;y++) {
			for (int x=0;x<WIDTH;x++)
				System.out.print(map[x][y].z+" ");
			System.out.println();
		}
	}
	
	//Gets an array containing all Points adjacent to p
	public Point[] getAdjacentPoints(Point p) {
		ArrayList<Point> points=new ArrayList<Point>();
		if (p.x>0) {
			if (p.y>0)
				points.add(map[p.x-1][p.y-1]);
			points.add(map[p.x-1][p.y]);
			if (p.y<HEIGHT-1)
				points.add(map[p.x-1][p.y+1]);
		}
		if (p.y>0)
			points.add(map[p.x][p.y-1]);
		if (p.y<HEIGHT-1)
			points.add(map[p.x][p.y+1]);
		if (p.x<WIDTH-1) {
			if (p.y>0)
				points.add(map[p.x+1][p.y-1]);
			points.add(map[p.x+1][p.y]);
			if (p.y<HEIGHT-1)
				points.add(map[p.x+1][p.y+1]);
		}
		return points.toArray(new Point[0]);
	}
	
	//Calculate the Biome at p
	public Biome getBiome(Point p) {
		int t=p.temp;
		if (p.river!=-1)
			return Biome.RIVER;
		else if (p.z<=WATERLEVEL)
			if (t<40)
				return Biome.ICE;
			else
				return Biome.SEA;
		if (p.slope>10)
			return Biome.ROCK;
		if (t<20)
			return Biome.TUNDRA;
		else if (t<40) {
			if (p.precipitation<50)
				return Biome.TUNDRA;
			else
				return Biome.TAIGA;
		} else if (t<60) {
			if (p.precipitation<20)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<50)
				return Biome.GRASSLAND;
			else if (p.precipitation<80)
				return Biome.CONIFEROUS_FOREST;
			else
				return Biome.MIXED_FOREST;
		} else if (t<80) {
			if (p.precipitation<20)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<50)
				return Biome.GRASSLAND;
			else if (p.precipitation<80)
				return Biome.CONIFEROUS_FOREST;
			else
				return Biome.BROADLEAF_FOREST;
		} else {
			if (p.precipitation<20)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<50)
				return Biome.GRASSLAND;
			else if (p.precipitation<80)
				return Biome.MIXED_FOREST;
			else
				return Biome.BROADLEAF_FOREST;
		}
	}
	
	//Advance 1 year
	public synchronized void tick() {
		if (year==5000)
			System.out.println();
		for (int i=0;i<FUTURE.size();i++) {
			if (FUTURE.get(i).startYear<=year) {
				CIVS.add(FUTURE.get(i));
				FUTURE.remove(i--).start();
			}
		}
		for (int i=0;i<CIVS.size();i++) {
			if (!CIVS.get(i).alive) {
				CIVS.get(i).startYear=year+25000/CIVS.get(i).growthRate;
				FUTURE.add(CIVS.remove(i--));
			} else
				CIVS.get(i).tick();
		}
		year++;
	}
	
	//Advance multiple years
	public void tick(int years) {
		endYear+=years;
		new Thread(this).start();
	}
	
	//Adds an event to history
	public void addEvent(String s) {
		history+=s+"\n";
		System.out.println(s);
	}
	
	public void run() {
		for (JButton b:controllers)
			b.setEnabled(false);
		while (year<endYear)
			tick();
		for (JButton b:controllers)
			b.setEnabled(true);
		System.out.println("History through "+year);
	}
	
	//Export the world to a text file
	public void export() {
		BufferedWriter out;
		try {
			/**out=new BufferedWriter(new FileWriter(SEED+"-"+year+".txt"));
			out.write("Civilizations---------------------------------------------------\n");
			ArrayList<Civilization> all=new ArrayList<Civilization>();
			for (Civilization c:CIVS)
				all.add(c);
			for (Civilization c:FUTURE)
				all.add(c);
			Collections.sort(all);
			for (Civilization c:all) {
				out.write(c.name+":\n\tRelationships:\n");
				ArrayList<String> names=new ArrayList<String>();
				for (String n:c.relationships.keySet())
					if (c.relationships.get(n)!=0)
						names.add(n);
				Collections.sort(names);
				for (String n:names) {
					out.write("\t\t"+n+": "+c.relationships.get(n)+"\n");
				}
				out.write("\tCities: "+c.cities.size()+"\n");
				ArrayList<City> cities=new ArrayList<City>();
				cities.addAll(c.cities);
				Collections.sort(cities);
				for (City t:cities) {
					out.write("\t\t("+t.CENTER.x+", "+t.CENTER.y+") - "+t.population+" - "+t.LAND.size()+"\n");
				}
			}
			out.write("History---------------------------------------------------------\n");
			out.write(history+"\n");
			out.close();
			/**/
			out=new BufferedWriter(new FileWriter(SEED+"-"+year+".txt"));
			ArrayList<City> all=new ArrayList<City>();
			for (Civilization civ:CIVS)
				for (City c:civ.cities)
					all.add(c);
			for (Civilization civ:FUTURE)
				for (City c:civ.cities)
					all.add(c);
			Collections.sort(all);
			for (City c:all) {
				out.write("\t\t("+c.CENTER.x+", "+c.CENTER.y+") - "+c.population+" - "+c.owner.name+"\n");
			}
			out.close();/**/
		} catch (IOException e) {
		}
	}
}
