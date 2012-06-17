import java.util.ArrayList;

public class City implements Comparable<City> {
	protected final ArrayList<Point> LAND;
	protected final Point CENTER;
	protected Civilization owner;
	protected int population,populationDensity,startYear/*,growth,growthRate*/,maxPopulation,id,maxSize;
	protected boolean sea,river;
	
	//Initializes a new city
	public City(Civilization owner,Point start,int population,int id,int maxSize) {
		LAND=new ArrayList<Point>();
		LAND.add(start);
		CENTER=start;
		//growthRate=owner.growthRate;
		//growth=owner.getPreference(start)*growthRate/100;
		this.population=population;
		this.owner=owner;
		this.id=id;
		startYear=owner.WORLD.year;
		start.owner=this;
		this.maxSize=maxSize;
		populationDensity=owner.populationDensity;
		maxPopulation=maxSize*owner.populationDensity;
		sea=false;
		river=false;
		for (Point p:start.adjacent)
			if (p.biome==Biome.SEA)
				sea=true;
			else if (p.biome==Biome.RIVER)
				river=true;
	}
	
	//Expand to a new point
	public void expand() {
		ArrayList<Point> possible=new ArrayList<Point>();
		Point[] adj;
		Point p;
		for (int i=0;i<LAND.size();i++) {
			p=LAND.get(i);
			adj=p.adjacent;
			for (Point p2:adj)
				if (!possible.contains(p2))
					possible.add(p2);
		}
		Point best=possible.get(0);
		int max=owner.getExpansion(this,best);
		for (Point p2:possible)
			if (!LAND.contains(p2)&&owner.getExpansion(this,p2)>max) {
				best=p2;
				max=owner.getExpansion(this,p2);
			}
		if (max>0) {
			//growth+=growthRate*max/100;
			best.owner=this;
			LAND.add(best);
			if (!river||!sea)
				for (Point p2:best.adjacent)
					if (p2.biome==Biome.RIVER)
						river=true;
					else if (p2.biome==Biome.SEA)
						sea=true;
		} else {
			maxPopulation=LAND.size()*populationDensity;
		}
	}
	
	//Called every year
	public void tick() {
		if (LAND.size()<maxSize) {
			population+=1+population*owner.growthRate/100;
			if (population>LAND.size()*owner.populationDensity) {
				expand();
			}
		}
		if (population>maxPopulation)
			population=maxPopulation;
		
	}
	
	private static int rand1(int seed) {
		return Math.abs((seed*149+6581)*19319+28513)&1048575;
	}
	
	private static int rand2(int seed) {
		return Math.abs((seed*167+6823)*18947+24121)&1048575;
	}
	
	//Attack another city
	public void attack(City c) {
		int m1=owner.aggression*population/100, //size of attacker's military, [0,pop]
		m2=c.owner.aggression*c.population/100, //size of defender's military, [0,pop]
		p1=owner.strength+owner.getBonus(c.CENTER)-CENTER.distanceTo(c.CENTER)*owner.distanceWeight/100+rand1(CENTER.x*rand1(CENTER.y+rand1(owner.WORLD.year)))%100, //[0,200]-distance*w (could be far negative)
		p2=c.owner.strength+c.owner.getBonus(c.CENTER)+rand2(c.CENTER.x*rand2(c.CENTER.y+rand2(c.owner.WORLD.year)))%100; //[0,200]
		if (p1<1)
			p1=1;
		if (p2<1)
			p2=1;
		int attackPower=m1*p1, //[0,300*amil]-[0,amil*distance*distanceWeight/100]
		defendPower=m2*p2, //[0,300*dmil]
		outcome=attackPower-defendPower; //[-300*dmil-amil*distance*distanceWeight/100,300*amil]
		if (outcome>0) { //You won!
			owner.WORLD.addEvent(owner.WORLD.year+": A city belonging to "+c.owner.name+" at ("+c.CENTER.x+", "+c.CENTER.y+") was razed by "+owner.name+".");
			population-=defendPower/p1;
			c.owner.removeCity(c);
			c.owner.changeRelationship(-100,owner); //The defender likes the attacker less
			int r;
			for (Civilization e:owner.WORLD.CIVS)
				if (e.alive&&e!=c.owner&&e!=owner) {
					r=e.relationships.get(c.owner.name)!=null?e.relationships.get(c.owner.name):0;
					e.changeRelationship(-(r>>6),owner); //Everyone else's relationship with the attacker changes, depending on how much they like the defender 
				}
			for (Point p:c.LAND)
				//Free c's LAND
				p.owner=null;
		} else if (outcome<0) { //You lost :(
			owner.WORLD.addEvent(owner.WORLD.year+": A city belonging to "+c.owner.name+" at ("+c.CENTER.x+", "+c.CENTER.y+") has beaten off an attack by "+owner.name+".");
			c.population-=attackPower/p2;
			population-=m1;
			c.owner.changeRelationship(-50,owner); //The defender likes the attacker less
			owner.changeRelationship(-100,c.owner); //The attacker likes the defender less
		} else { //Stalemate
			owner.WORLD.addEvent(owner.WORLD.year+": A city belonging to "+c.owner.name+" at ("+c.CENTER.x+", "+c.CENTER.y+") has survived an attack by "+owner.name+".");
			c.population-=m2;
			population-=m1;
			c.owner.changeRelationship(-70,owner); //The defender likes the attacker less
			owner.changeRelationship(-70,c.owner); //The attacker likes the defender less
		}
	}
	
	public int compareTo(City c) {
		if (CENTER.x/25==c.CENTER.x/25)
			return CENTER.y-c.CENTER.y!=0?CENTER.y-c.CENTER.y:CENTER.x-c.CENTER.x;
		else
			return CENTER.x-c.CENTER.x!=0?CENTER.x-c.CENTER.x:CENTER.y-c.CENTER.y;
	}
}
