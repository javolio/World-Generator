import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.*;

public class Loader {
	protected World w;
	protected String template;
	
	//Loads the template
	public Loader(World w) throws FileNotFoundException {
		this.w=w;
		Scanner scan;
		scan=new Scanner(new File("Civilizations/TEMPLATE.CIV"));
		scan.useDelimiter("\\Z");
		template=scan.next();
	}
	
	//Not used, but loads a new world with parameters from a file, as well as the template
	public Loader() throws FileNotFoundException {
		Scanner scan=new Scanner(new File("WORLD"));
		int s=scan.nextInt();
		scan.nextLine();
		int w=scan.nextInt();
		scan.nextLine();
		int h=scan.nextInt();
		this.w=new World(s,w,h);
		scan=new Scanner(new File("Civilizations/TEMPLATE.CIV"));
		scan.useDelimiter("\\Z");
		template=scan.next();
	}
	
	//Loads all the information about a civilization, given the name
	public Civilization loadCiv(String name) throws FileNotFoundException {
		Scanner scan=new Scanner(new File("Civilizations/"+name+".CIV"));
		scan.useDelimiter("\\Z");
		String source=scan.next();
		Civilization c=new Civilization(w);
		getColor(c,source);
		c.name=get("Name:.*?\"(.*?)\"",source);
		c.aggression=Integer.parseInt(get("Aggression:.*?(-?\\d*)",source));
		c.strength=Integer.parseInt(get("Strength:.*?(-?\\d*)",source));
		c.populationDensity=Integer.parseInt(get("Population Density:.*?(-?\\d*)",source));
		c.growthRate=Integer.parseInt(get("Growth Rate:.*?(-?\\d*)",source));
		c.maxSlope=Integer.parseInt(get("Max Slope:.*?(-?\\d*)",source));
		c.minSlope=Integer.parseInt(get("Min Slope:.*?(-?\\d*)",source));
		c.maxTemp=Integer.parseInt(get("Max Temperature:.*?(-?\\d*)",source));
		c.minTemp=Integer.parseInt(get("Min Temperature:.*?(-?\\d*)",source));
		c.maxPrecip=Integer.parseInt(get("Max Precipitation:.*?(-?\\d*)",source));
		c.minPrecip=Integer.parseInt(get("Min Precipitation:.*?(-?\\d*)",source));
		c.maxElevation=Integer.parseInt(get("Max Elevation:.*?(-?\\d*)",source));
		c.minElevation=Integer.parseInt(get("Min Elevation:.*?(-?\\d*)",source));
		c.desiredDistance=Integer.parseInt(get("Desired Distance:.*?(-?\\d*)",source));
		c.slopeWeight=Integer.parseInt(get("Slope Importance:.*?(-?\\d*)",source));
		c.tempWeight=Integer.parseInt(get("Temperature Importance:.*?(-?\\d*)",source));
		c.precipWeight=Integer.parseInt(get("Precipitation Importance:.*?(-?\\d*)",source));
		c.elevationWeight=Integer.parseInt(get("Elevation Importance:.*?(-?\\d*)",source));
		c.distanceWeight=Integer.parseInt(get("Distance Importance:.*?(-?\\d*)",source));
		c.riverBonus=Integer.parseInt(get("River Bonus:.*?(-?\\d*)",source));
		c.seaBonus=Integer.parseInt(get("Sea Bonus:.*?(-?\\d*)",source));
		c.deltaBonus=Integer.parseInt(get("Delta Bonus:.*?(-?\\d*)",source));
		c.startYear=Integer.parseInt(get("Starting Year:.*?(-?\\d*)",source));
		c.restrictedBiomes=getBiomes(get("Restricted Biomes:.*?\\{(.*?)}",source));
		c.relationships=getRelationships(get("Relationships:.*?\\{(.*?)}",source));
		for (String n:c.relationships.keySet())
			c.baseRelationships.put(n,c.relationships.get(n));
		return c;
	}
	
	//Use regex to check for the desired attribute, and return group 1
	public String get(String regex,String source) {
		Matcher m=Pattern.compile(regex,Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(source);
		if (m.find())
			return m.group(1);
		else { //If it cannot be found, it is taken from the template
			m=Pattern.compile(regex).matcher(template);
			m.find();
			return m.group(1);
		}
	}
	
	//Loads the restricted biome list
	public Biome[] getBiomes(String source) {
		ArrayList<Biome> restricted=new ArrayList<Biome>();
		String r;
		Matcher m;
		Biome[] v=Biome.values();
		for (int i=0;i<11;i++) { //Check each biome
			switch (i) {
			case 0:
				r="Sea";
				break;
			case 1:
				r="River";
				break;
			case 2:
				r="Ice";
				break;
			case 3:
				r="Tundra";
				break;
			case 4:
				r="Taiga";
				break;
			case 5:
				r="Desert";
				break;
			case 6:
				r="Grassland";
				break;
			case 7:
				r="Coniferous Forest";
				break;
			case 8:
				r="Mixed Forest";
				break;
			case 10:
				r="Broadleaf Forest";
				break;
			default:
				r="Rock";
				break;
			}
			m=Pattern.compile(r,Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(source);
			if (m.find())
				restricted.add(v[i]);
		}
		return restricted.toArray(new Biome[0]);
	}
	
	//Loads all relationships
	private HashMap<String,Integer> getRelationships(String source) {
		HashMap<String,Integer> relationships=new HashMap<String,Integer>();
		Matcher m=Pattern.compile("\"(.*?)\":.*?(-?\\d+)",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(source);
		while (m.find()) {
			relationships.put(m.group(1),Integer.parseInt(m.group(2)));
		}
		return relationships;
	}
	
	//Loads the color
	private void getColor(Civilization c,String source) {
		Matcher m=Pattern.compile("Hue:.*?(-?\\.?\\d*).*?Sat:.*?(-?\\.?\\d*)",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(source);
		if (m.find()) {
			c.hue=Float.parseFloat(m.group(1));
			c.sat=Float.parseFloat(m.group(2));
		} else {
			m=Pattern.compile("Hue:.*?(-?\\.?\\d*).*?Sat:.*?(-?\\.?\\d*)").matcher(template);
			m.find();
			c.hue=Float.parseFloat(m.group(1));
			c.sat=Float.parseFloat(m.group(2));
		}
	}
	
	//Load all civilizations in Civ List.DAT
	public void loadAll() throws FileNotFoundException {
		Scanner scan=new Scanner(new File("Civ List.DAT"));
		Civilization c;
		while (scan.hasNext()) {
			c=loadCiv(scan.nextLine());
			w.FUTURE.add(c);
			w.ALL.add(c);
			w.NAMES.put(c.name,c);
		}
		Collections.sort(w.ALL);
	}
	
}
