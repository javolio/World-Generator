import java.util.ArrayList;
import java.util.HashMap;

public class Civilization implements Comparable<Civilization> {
	
	protected int startYear, //The year the civilization starts their first city
			aggression, //0-100
			strength, //0-100, the military/combat ability of the civilization
			populationDensity, //max population per map tile
			growthRate, //modifies how fast cities grow, 0-100
			maxSlope,minSlope,maxTemp,minTemp,maxPrecip,minPrecip,maxElevation,minElevation, //Maximum and minimum allowed values for a location
			slopeWeight,tempWeight,precipWeight,elevationWeight,distanceWeight, //how much of the total this counts for
			riverBonus,seaBonus,deltaBonus, //how much better a location is if it is adjacent to these things
			desiredDistance; //distance at which another city is considered to not be in this city's space
	protected float hue,sat; //hue and saturation values of the civ's color
	protected String name;
	protected final World WORLD;
	protected ArrayList<City> cities,future;
	protected Biome[] restrictedBiomes;
	protected HashMap<String,Integer> relationships,baseRelationships;
	protected boolean alive,started;
	
	/*public Civilization(World world,String name,int startYear,int aggression,int strength,int populationDensity,int growthRate,int maxSlope,int minSlope,int maxTemp,int minTemp,int maxPrecip,int minPrecip,int slopeWeight,int tempWeight,int precipWeight,int riverBonus,int seaBonus,int deltaBonus,int desiredDistance,int distanceWeight,Biome[] restrictedBiomes) {
		WORLD=world;
		cities=new ArrayList<City>();
		future=new ArrayList<City>();
		this.name=name;
		this.startYear=startYear;
		this.aggression=aggression;
		this.strength=strength;
		this.populationDensity=populationDensity;
		this.growthRate=growthRate;
		this.maxSlope=maxSlope;
		this.minSlope=minSlope;
		this.maxTemp=maxTemp;
		this.minTemp=minTemp;
		this.maxPrecip=maxPrecip;
		this.minPrecip=minPrecip;
		this.desiredDistance=desiredDistance;
		this.slopeWeight=slopeWeight;
		this.tempWeight=tempWeight;
		this.precipWeight=precipWeight;
		this.distanceWeight=distanceWeight;
		this.riverBonus=riverBonus;
		this.seaBonus=seaBonus;
		this.deltaBonus=deltaBonus;
		this.restrictedBiomes=restrictedBiomes;
		relationships=new HashMap<String,Integer>();
		enemies=new ArrayList<Civilization>();
		friends=new ArrayList<Civilization>();
		alive=false;
		started=false;
	}*/

	public Civilization(World w) {
		WORLD=w;
		cities=new ArrayList<City>();
		future=new ArrayList<City>();
		relationships=new HashMap<String,Integer>();
		baseRelationships=new HashMap<String,Integer>();
		alive=false;
		started=false;
	}
	
	//Returns a int between 0 and 100, or -1 for invalid location
	public int getPreference(Point p) {
		Biome b=p.biome;
		for (Biome b2:restrictedBiomes)
			if (b==b2)
				return -1;
		int pref=0;
		if (slopeWeight!=0) {
			int t=p.slope;
			if (t<minSlope||t>maxSlope)
				return -1;
			if (minSlope==0)
				pref+=t*slopeWeight/maxSlope;
			else {
				int x=2*slopeWeight/(maxSlope-minSlope)*(t-minSlope)-slopeWeight;
				pref+=(x<=0?x:-x)+slopeWeight;
			}
			
		}
		if (tempWeight!=0) {
			int t=p.temp;
			if (t<minTemp||t>maxTemp)
				return -1;
			int x=2*tempWeight/(maxTemp-minTemp)*(t-minTemp)-tempWeight;
			pref+=(x<=0?x:-x)+tempWeight;
		}
		if (precipWeight!=0) {
			int t=p.precipitation;
			if (t<minPrecip||t>maxPrecip)
				return -1;
			int x=2*precipWeight/(maxPrecip-minPrecip)*(t-minPrecip)-precipWeight;
			pref+=(x<=0?x:-x)+precipWeight;
		}
		if (elevationWeight!=0) {
			int t=p.z;
			if (t<minElevation||t>maxElevation)
				return -1;
			int x=2*elevationWeight/(maxElevation-minElevation)*(t-minElevation)-elevationWeight;
			pref+=(x<=0?x:-x)+elevationWeight;
		}
		boolean river=false,sea=false;
		for (Point p2:p.adjacent) {
			if (p2.biome==Biome.RIVER)
				river=true;
			if (p2.biome==Biome.SEA)
				sea=true;
		}
		pref+=river&&sea?deltaBonus:river?riverBonus:sea?seaBonus:0;
		if (cities.size()>0) {
			int minDist=p.distanceTo(cities.get(0).CENTER),d;
			for (City c:cities) {
				//pref-=10+Math.pow(desiredDistance-p.distanceTo(c.CENTER),2)*distanceWeight/Math.pow(desiredDistance,2);
				d=p.distanceTo(c.CENTER);
				if (d<minDist)
					minDist=d;
			}
			if (minDist<=desiredDistance)
				//pref-=(1.*desiredDistance*desiredDistance/minDist*minDist*distanceWeight-1)*a;
				pref+=minDist*minDist*distanceWeight/(desiredDistance*desiredDistance);
			else
				pref+=-(minDist-desiredDistance)*(minDist-desiredDistance)/(desiredDistance*desiredDistance)*distanceWeight+distanceWeight;
		} else
			pref+=distanceWeight;
		return pref;
	}
	
	//Similar to getPreference, but for when a city is getting larger
	public int getExpansion(City c,Point p) {
		Biome b=p.biome;
		for (Biome b2:restrictedBiomes)
			if (b==b2)
				return -1;
		int pref=0;
		if (slopeWeight!=0) {
			int t=p.slope;
			if (t<minSlope||t>maxSlope)
				return -1;
			if (minSlope==0)
				pref+=t*slopeWeight/maxSlope;
			else {
				int x=2*slopeWeight/(maxSlope-minSlope)*(t-minSlope)-slopeWeight;
				pref+=(x<=0?x:-x)+slopeWeight;
			}
		}
		if (tempWeight!=0) {
			int t=p.temp;
			if (t<minTemp||t>maxTemp)
				return -1;
			int x=2*tempWeight/(maxTemp-minTemp)*(t-minTemp)-tempWeight;
			pref+=(x<=0?x:-x)+tempWeight;
		}
		if (precipWeight!=0) {
			int t=p.precipitation;
			if (t<minPrecip||t>maxPrecip)
				return -1;
			int x=2*precipWeight/(maxPrecip-minPrecip)*(t-minPrecip)-precipWeight;
			pref+=(x<=0?x:-x)+precipWeight;
		}
		if (elevationWeight!=0) {
			int t=p.z;
			if (t<minElevation||t>maxElevation)
				return -1;
			int x=2*elevationWeight/(maxElevation-minElevation)*(t-minElevation)-elevationWeight;
			pref+=(x<=0?x:-x)+elevationWeight;
		}
		boolean river=false,sea=false;
		for (Point p2:p.adjacent) {
			if (p2.biome==Biome.RIVER)
				river=true;
			if (p2.biome==Biome.SEA)
				sea=true;
		}
		pref+=sea&&river&&!c.sea&&!c.river?deltaBonus:river&&!c.river?riverBonus:sea&&!c.sea?seaBonus:0;
		//Comment this out to make cities snake around rivers and coastlines
		pref+=((desiredDistance>>4)-Math.sqrt((c.CENTER.x-p.x)*(c.CENTER.x-p.x)+(c.CENTER.y-p.y)*(c.CENTER.y-p.y)))*distanceWeight/100;
		return pref;
	}
	
	//Similar to getPreference, but used to determine how much of a terrain advantage the civilization has at a Point
	public int getBonus(Point p) {
		Biome b=p.biome;
		for (Biome b2:restrictedBiomes)
			if (b==b2)
				return -1;
		int pref=0;
		if (slopeWeight!=0) {
			int t=p.slope;
			if (t<minSlope||t>maxSlope)
				return -1;
			if (minSlope==0)
				pref+=t*slopeWeight/maxSlope;
			else {
				int x=2*slopeWeight/(maxSlope-minSlope)*(t-minSlope)-slopeWeight;
				pref+=(x<=0?x:-x)+slopeWeight;
			}
		}
		if (tempWeight!=0) {
			int t=p.temp;
			if (t<minTemp||t>maxTemp)
				return -1;
			int x=2*tempWeight/(maxTemp-minTemp)*(t-minTemp)-tempWeight;
			pref+=(x<=0?x:-x)+tempWeight;
		}
		if (precipWeight!=0) {
			int t=p.precipitation;
			if (t<minPrecip||t>maxPrecip)
				return -1;
			int x=2*precipWeight/(maxPrecip-minPrecip)*(t-minPrecip)-precipWeight;
			pref+=(x<=0?x:-x)+precipWeight;
		}
		if (elevationWeight!=0) {
			int t=p.z;
			if (t<minElevation||t>maxElevation)
				return -1;
			int x=2*elevationWeight/(maxElevation-minElevation)*(t-minElevation)-elevationWeight;
			pref+=(x<=0?x:-x)+elevationWeight;
		}
		boolean river=false,sea=false;
		for (Point p2:p.adjacent) {
			if (p2.biome==Biome.RIVER)
				river=true;
			if (p2.biome==Biome.SEA)
				sea=true;
		}
		pref+=river&&sea?deltaBonus:river?riverBonus:sea?seaBonus:0;
		return pref;
	}
	
	//Called every year
	public void tick() {
		for (City c:future) {
			cities.add(c);
			WORLD.addEvent(WORLD.year+": A city was founded at ("+c.CENTER.x+", "+c.CENTER.y+") by the "+name+".");
		}
		future.clear();
		for (City c:cities)
			c.tick();
		for (String n:relationships.keySet()) {
			if (WORLD.NAMES.get(n)==null)
				System.out.println("****"+name+" "+n+"****");
			else if (WORLD.NAMES.get(n).alive)
				if (-relationships.get(n)*aggression>(rand2(WORLD.year+WORLD.NAMES.get(n).cities.size())&1048575))
					attack(WORLD.NAMES.get(n));
		}
		if ((WORLD.year&7)==0) //This determines how quickly their relationships go back to normal. Right now they stabilize by 1 point every 8 years
			for (String n:relationships.keySet())
				if (WORLD.NAMES.get(n).alive)
					if (relationships.get(n)<(baseRelationships.get(n)==null?0:baseRelationships.get(n)))
						relationships.put(n,relationships.get(n)+1);
					else if (relationships.get(n)>(baseRelationships.get(n)==null?0:baseRelationships.get(n)))
						relationships.put(n,relationships.get(n)-1);
		if (rand3(WORLD.year+startYear*cities.size())%(cities.size()*64+1)==0) //This determines how often they will found new cities
			addCity();
	}
	
	//Determines which city to attack, from which city of their own
	private void attack(Civilization c) {
		if (c.cities.size()>0&&cities.size()>0) {
			City best=cities.get(0),target=c.cities.get(0);
			int attackGuess=aggression*best.population/100*(strength+getBonus(target.CENTER)-best.CENTER.distanceTo(target.CENTER)*distanceWeight);
			int defendGuess=c.aggression*target.population/100*(c.strength+c.getBonus(target.CENTER));
			int max=attackGuess-defendGuess;
			int cur;
			for (City c1:cities)
				for (City c2:c.cities) {
					attackGuess=aggression*c1.population/100*(strength+getBonus(c2.CENTER)-c1.CENTER.distanceTo(c2.CENTER));
					defendGuess=c.aggression*c2.population/100*(c.strength+c.getBonus(c2.CENTER));
					cur=attackGuess-defendGuess;
					if (cur>max) {
						max=cur;
						best=c1;
						target=c2;
					}
				}
			if (max>-50) //This specifies how good a chance of victory they require to attack
				best.attack(target);
		}
	}
	
	//Found a new city at an optimal location
	public void addCity() {
		int max=getPreference(WORLD.map[0][0]);
		ArrayList<Point> best=new ArrayList<Point>();
		best.add(WORLD.map[0][0]);
		for (int y=0;y<WORLD.HEIGHT;y++)
			for (int x=0;x<WORLD.WIDTH;x++)
				if (WORLD.map[x][y].owner==null)
					if (getPreference(WORLD.map[x][y])>max) {
						best.clear();
						best.add(WORLD.map[x][y]);
						max=getPreference(WORLD.map[x][y]);
					} else if (getPreference(WORLD.map[x][y])==max)
						best.add(WORLD.map[x][y]);
		if (max>0) {
			City c=new City(this,best.get(rand(best.size()+cities.size())%best.size()),rand(cities.size()+future.size())%populationDensity+1,cities.size()-1,max/4);
			future.add(c);
		} else {
			for (City c:cities) {
				c.maxPopulation<<=1;
				c.maxSize<<=1;
			}
		}
	}
	
	//Remove a city that has been razed by enemies
	public void removeCity(City c) {
		cities.remove(c);
		if (cities.size()==0) {
			alive=false;
			WORLD.addEvent(WORLD.year+": The "+name+" have been wiped out.");
			startYear=WORLD.year+25000/growthRate+rand3(WORLD.year)&511;
		}
	}
	
	private int rand(int seed) {
		return Math.abs((seed*199+6871)*15511+23293)&1048575;
	}
	
	private static int rand2(int seed) {
		return Math.abs((seed*211+7247)*17737+25657)&1048575;
	}
	
	private static int rand3(int seed) {
		return Math.abs((seed*271+7901)*13933+24509)&1048575;
	}
	
	//Change their relationship with another civilization, and mark in history if any significant change happened 
	public void changeRelationship(int i,Civilization c) {
		if (c.name!=name) {
			int cur;
			if (relationships.get(c.name)!=null)
				cur=relationships.get(c.name);
			else {
				cur=0;
			}
			if (c.alive) {
				if (cur<-50&&cur+i>=-50) {
					WORLD.addEvent(WORLD.year+": The "+c.name+" are no longer enemies of the "+name+".");
				} else if (cur>50&&cur+i<=50) {
					WORLD.addEvent(WORLD.year+": The "+c.name+" are no longer friends of the "+name+".");
				}
				if (cur<=50&&cur+i>50) {
					WORLD.addEvent(WORLD.year+": The "+c.name+" are now friends of the "+name+".");
				} else if (cur>=-50&&cur+i<-50) {
					WORLD.addEvent(WORLD.year+": The "+c.name+" are now enemies of the "+name+".");
				}
			}
			cur+=i;
			if (cur<-1000)
				cur=-1000;
			else if (cur>1000)
				cur=1000;
			relationships.put(c.name,cur);
		}
	}
	
	//Found their first city, and update history
	public void start() {
		alive=true;
		if (started) {
			WORLD.addEvent(WORLD.year+": The "+name+" have returned!");
		} else {
			WORLD.addEvent(WORLD.year+": The "+name+" have founded their first city!");
			started=true;
		}
		addCity();
	}
	
	//Used for exporting
	public int compareTo(Civilization c) {
		return name.compareTo(c.name);
	}
}
