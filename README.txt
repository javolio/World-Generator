World-Generator
===============

A procedural world generator, focusing on civilization growth and conflict. Includes elevation, precipitation, temperature, and biomes. Civilizations are read in from detailed files describing their relationships and preferred settlement locations. New cities are founded and destroyed over time, and relationships between civilizations change.

================================

Basic usage is fairly simple. Run "World Generator.jar". Fill in a seed (any integer up to 2,147,483,647 should work), and the height and width, in pixels, of the world. Click "Create World". The world generation will take a while, especially on slow computers or with big worlds.

After the world is generated, there are a bunch of visualizers available, so you can inspect various aspects. Settlement will initially be blank. You can have the world advance over any number of years.

After it has advanced, you can export the world. The exported file will list all civilizations, their relationships with other civilizations, and their cities. It will also list the entire history of the world.

While there are 69 civilizations already included, it's possible to change them around, remove some, or add new ones. If you remove a civilization, remove all references to it in other civilization files, under relationships. Each civilization also needs to be in "Civ List.DAT". All these files can be opened as regular text files.

Here's an outline of a sample .CIV file:

Name:"Bugbears",		The name of the civilization
Category:"Goblinoids",		What category of creature they are. This isn't actually used, I just have it here for reference.
Starting Year:3200,		The year the civilization founds their first city.
Color:{				The civilization's color on the map, with luminance of .5.
Hue:.0625				The hue, between 0 and 1.
Sat:.25				The saturation, between 0 and 1.
}
Aggression:80			How aggressive the civilization is, between 0 and 100. This determines how often the civilization will attack their enemies. Also determines how much of their population is in the military.
Strength:70			How powerful a civilization's military is. Similar to a CR. Generally, less than 100.
Population Density:750		How many people can live in one square of a city. 1500 or more is pretty dense; 500 or less is somewhat sparse.
Growth Rate:70			How quickly the population grows. 100 is very high.
Max Slope:4			The steepest slope the civilization will live on. 5 is very steep; 0 is almost perfectly flat. On the visualizer, purple is 0, blue is 1, teal is 2, green is 3, yellow is 4, and red is 5+, and very rare.
Min Slope:0			The minimum slope the civilization will live on.
Max Temperature:125		The highest temperature the civilization will tolerate, roughly in fahrenheit.
Min Temperature:-25		The lowest temperature the civilization will tolerate. The civilization's ideal temperature is the average of the max and min temperatures.
Max Precipitation:100		The highest precipitation the civilization will tolerate, with 100 being the highest possible. All oceans are at 100.
Min Precipitation:0		The lowest precipitation the civilization will tolerate. The civilization's ideal precipitation is the average of the max and min temperatures.
Max Elevation:255		The highest elevation the civilization will tolerate, with 100 being the highest possible. Heights go from 0-255. 127 and below is ocean, but may be frozen over. 192 is considered to be in the mountains.
Min Elevation:0			The lowest elevation the civilization will tolerate. The civilization's ideal precipitation is the average of the max and min elevations.
Desired Distance:100		How far away they prefer to settle new cities.
Slope Importance:10		How important slope is to them. The sum of the importances plus the highest of the three bonuses must add up to 100.
Temperature Importance:10	How important temperature is to them.
Precipitation Importance:0	How important precipitation is to them.
Elevation Importance:0		How important elevation is to them.
Distance Importance:60		How important distance is to them.
River Bonus:10			How much more they like a location if it's next to a river, but not an ocean.
Sea Bonus:10			How much more they like a location if it's next to an ocean, but not a river.
Delta Bonus:20			How much more they like a location if it's next to a river and an ocean.
Restricted Biomes:{		The list of biomes they refuse to settle in. The possible biomes are Sea, River, Ice, Tundra, Taiga, Desert, Grassland, Coniferous Forest, Mixed Forest, Broadleaf Forest, and Rock. Ice is just frozen-over ocean.
Sea,
River,
Ice,
}
Relationships:{			Their basic relationships with other civilizations, between -1000 and 1000. They will naturally return to this baseline over time. Negative numbers indicate a dislike. Lower than -50 means they are enemies. Higher than 50 means they are friends. If a civilization is not listed here, the base relationship is 0. Note that these are one-way. A civilization may hate a civilization that doesn't hate it.
"Goblins":300,
"Hobgoblins":300,
"Blues":200,

"Bretons":-100,
"Nords":-100,
"Imperials":-100,
"Redguard":-100,
"Sand People":-100,
"Sea Dwellers":-100,

"Wild Elves":-100,
"Wood Elves":-100,
"High Elves":-100,
"Drow":-100,
"Frost Elves":-100,
"Gray Elves":-100,
"Sea Elves":-100,

"Hill Dwarves":-200,
"Mountain Dwarves":-200,
"Savasi":-200,
"Deep Dwarves":-200,
"Duergar":-200,

"Rock Gnomes":-100,
"Forest Gnomes":-100,
"Svirfneblin":-100,

"Lightfoot Halflings":-100,
"Tallfellow Halflings":-100,
"Rhulisti Halflings":-100,
"Deep Halflings":-100,
"Rhul-thaun Halflings":-100,

"Common Orcs":200,
"Jungle Orcs":200,
"Desert Orcs":200,
"Skraal":200,
"Aquatic Orcs":100,

"Plains Minotaur":100,
"Arctic Minotaur":100,
}