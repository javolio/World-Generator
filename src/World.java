import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import javax.swing.JButton;

public class World implements Runnable {
	protected final int WIDTH,HEIGHT, // m; 40,000,000 and 20,000,000 for Earth
			ZRANGE=255, // m between max and min height
			OFFSET, // m between two independent points
			POINTS=8, // minimum number of independent points
			WATERLEVEL=127,HUMIDITY=16,PRECIPRANGE=200,SEED;
	protected int year,maxSlope,endYear;
	protected Point[][] map;
	protected final ArrayList<Civilization> CIVS,FUTURE,ALL;
	protected final HashMap<String,Civilization> NAMES;
	protected JButton[] controllers;
	protected String history;
	protected Random generator;
	protected AvolioNoiseMaker2D elevationNoiseMaker;
	
	//Initialize the world, but don't map anything
	public World(int seed,int width,int height) {
		WIDTH=width;
		HEIGHT=height;
		OFFSET=Math.max(Math.min(WIDTH,HEIGHT)/POINTS,1);
		SEED=seed;
		map=new Point[WIDTH][HEIGHT];
		CIVS=new ArrayList<Civilization>();
		FUTURE=new ArrayList<Civilization>();
		ALL=new ArrayList<Civilization>();
		NAMES=new HashMap<String,Civilization>();
		year=0;
		history="";
		generator=new Random(seed);
		elevationNoiseMaker=new AvolioNoiseMaker2D(generator.nextLong(),8,8,new CosineInterpolatedNoiseMaker2D(generator.nextLong()));
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
				map[x][y]=new Point(x,y,(int) (elevationNoiseMaker.get((double) x/OFFSET,(double) y/OFFSET)*ZRANGE));
				if (map[x][y].z>max)
					max=map[x][y].z;
				if (map[x][y].z<min)
					min=map[x][y].z;
			}
		double range=max-min;
		System.out.println("Min: "+min+"\tMax: "+max);
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1)
				map[x][y].z=(int) ((map[x][y].z-min)*(ZRANGE/range));
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
				map[x][y].temp=(int) (((1-Math.abs(1-y*2./HEIGHT))*.6-map[x][y].z*.4/ZRANGE+.4)*150-25);
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
						else if (p.z<0)
							System.out.println("!!!!\t"+p.z);
				}
			}
		int s1,tried=0;
		boolean good;
		for (int i=0;i<WIDTH*HEIGHT/10000&&tried<100;i++,tried++) {
			s1=generator.nextInt(potential.size());
			Point p=potential.get(s1);
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
					map[x][y].precipitation=PRECIPRANGE;
					for (Point p:map[x][y].adjacent) {
						if (p.z>WATERLEVEL) {
							cur.add(p);
							p.precipitation=PRECIPRANGE-(p.z-WATERLEVEL)/HUMIDITY;
						}
					}
				}
		for (int precip=PRECIPRANGE;precip>0;precip--) {
			Point[] parr=cur.toArray(new Point[0]);
			for (Point p1:parr) {
				if (p1.precipitation==precip) {
					for (Point p2:p1.adjacent) {
						int precipitation=p1.precipitation-(p2.z-WATERLEVEL)/HUMIDITY-1;
						if (p2.x<p1.x) {
							if (p2.y!=p1.y)
								precipitation--;
						} else if (p2.x==p1.x) {
							precipitation-=2;
						} else {
							if (p2.y!=p1.y)
								precipitation-=3;
							else
								precipitation-=4;
						}
						if (p2.precipitation<precipitation) {
							p2.precipitation=precipitation;
							cur.add(p2);
						}
					}
					cur.remove(p1);
				}
			}
		}
		System.out.println("Precipitation Complete");
		
		//Biomes
		for (int y=0;y<HEIGHT;y+=1)
			for (int x=0;x<WIDTH;x+=1)
				map[x][y].biome=getBiome(map[x][y]);
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
		if (good.size()>0&&generator.nextInt(100-length)!=0) {
			Point next=good.get(generator.nextInt(good.size()));
			if (next.river==-1) {
				next.river=p.river;
				runRiver(next,length+1);
			}
		}
		
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
			if (p.precipitation<PRECIPRANGE/2)
				return Biome.TUNDRA;
			else
				return Biome.TAIGA;
		} else if (t<60) {
			if (p.precipitation<PRECIPRANGE/5)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<PRECIPRANGE/2)
				return Biome.GRASSLAND;
			else if (p.precipitation<PRECIPRANGE*4/5)
				return Biome.CONIFEROUS_FOREST;
			else
				return Biome.MIXED_FOREST;
		} else if (t<80) {
			if (p.precipitation<PRECIPRANGE/5)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<PRECIPRANGE/2)
				return Biome.GRASSLAND;
			else if (p.precipitation<PRECIPRANGE*4/5)
				return Biome.CONIFEROUS_FOREST;
			else
				return Biome.BROADLEAF_FOREST;
		} else {
			if (p.precipitation<PRECIPRANGE/5)
				if (p.z<192)
					return Biome.DESERT;
				else
					return Biome.ROCK;
			else if (p.precipitation<PRECIPRANGE/2)
				return Biome.GRASSLAND;
			else if (p.precipitation<PRECIPRANGE*4/5)
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
