World-Generator
===============

A procedural world generator, focusing on civilization growth and conflict. Includes elevation, precipitation, temperature, and biomes. Civilizations are read in from detailed files describing their relationships and preferred settlement locations. New cities are founded and destroyed over time, and relationships between civilizations change.

================================

Basic usage is fairly simple. Run "World Generator.jar". Fill in a seed (any integer up to 2,147,483,647 should work), and the height and width, in pixels, of the world. Click "Create World". The world generation will take a while, especially on slow computers or with big worlds.

After the world is generated, there are a bunch of visualizers available, so you can inspect various aspects. Settlement will initially be blank. You can have the world advance over any number of years.

After it has advanced, you can export the world. The exported file will list all civilizations, their relationships with other civilizations, and their cities. It will also list the entire history of the world.

While there are 69 civilizations already included, it's possible to change them around, remove some, or add new ones. If you remove a civilization, remove all references to it in other civilization files, under relationships. Each civilization also needs to be in "Civ List.DAT". All these files can be opened as regular text files.