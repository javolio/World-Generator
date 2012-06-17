import java.awt.*;
import javax.swing.*;

//The visualizer
@SuppressWarnings("serial")
public class SwitchablePanel extends JPanel implements Runnable {
	private World w;
	private int year;
	private Display mode;
	private boolean civs,grid;
	
	public SwitchablePanel(World w) {
		this.w=w;
		setPreferredSize(new Dimension(w.WIDTH,w.HEIGHT));
		year=w.year;
		mode=Display.COMPOSITE;
		civs=true;
		grid=true;
		new Thread(this).start();
	}
	
	public void paintComponent(Graphics page) {
		Biome b;
		switch (mode) {
		case CIVLIST:
			page.setColor(Color.BLACK);
			page.fillRect(0,0,w.WIDTH,w.HEIGHT);
			for (int x1=10,y1=20,i=0;i<w.ALL.size();i++) {
				Civilization c=w.ALL.get(i);
				page.setColor(Color.getHSBColor(c.hue,c.sat,.5F));
				page.drawString(c.name,x1,y1);
				y1+=20;
				if (y1>=w.HEIGHT) {
					y1=20;
					x1+=w.WIDTH>>2;
				}
			}
			break;
		case HEIGHT:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					page.setColor(new Color(w.map[x1][y1].z,w.map[x1][y1].z,w.map[x1][y1].z));
					page.fillRect(x1,y1,1,1);
				}
			break;
		case PRECIPITATION:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					page.setColor(Color.getHSBColor((float) (w.map[x1][y1].precipitation*27./10)/360,1,(float) .5));
					page.fillRect(x1,y1,1,1);
				}
			break;
		case TEMPERATURE:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					page.setColor(Color.getHSBColor((float) (270-(w.map[x1][y1].temp+25)*27./15)/360,1,(float) .5));
					page.fillRect(x1,y1,1,1);
				}
			break;
		case BIOME:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					b=w.map[x1][y1].biome;
					switch (b) {
					case ICE:
						page.setColor(Color.WHITE);
						break;
					case SEA:
					case RIVER:
						page.setColor(Color.BLUE);
						break;
					case TUNDRA:
						page.setColor(Color.LIGHT_GRAY);
						break;
					case TAIGA:
						page.setColor(Color.CYAN);
						break;
					case DESERT:
						page.setColor(new Color(255,238,170));
						break;
					case GRASSLAND:
						page.setColor(Color.YELLOW);
						break;
					case CONIFEROUS_FOREST:
						page.setColor(Color.GREEN);
						break;
					case MIXED_FOREST:
						page.setColor(new Color(0,160,0));
						break;
					case BROADLEAF_FOREST:
						page.setColor(new Color(0,64,0));
						break;
					case ROCK:
						page.setColor(Color.GRAY);
						break;
					}
					page.fillRect(x1,y1,1,1);
				}
			break;
		case SETTLEMENT:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					if (w.map[x1][y1].owner!=null)
						page.setColor(Color.getHSBColor(w.map[x1][y1].owner.owner.hue,w.map[x1][y1].owner.owner.sat,(float) .5));
					else
						page.setColor(Color.BLACK);
					page.fillRect(x1,y1,1,1);
				}
			break;
		case SLOPE:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					if (w.map[x1][y1].slope<=10)
						page.setColor(Color.getHSBColor((float) ((10-w.map[x1][y1].slope)*27.)/360,1,(float) .5));
					else
						page.setColor(Color.getHSBColor(0,1,(float) .5));
					page.fillRect(x1,y1,1,1);
				}
			break;
		case WATER:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					b=w.map[x1][y1].biome;
					switch (b) {
					case SEA:
						page.setColor(new Color(0,0,w.map[x1][y1].z));
						break;
					case RIVER:
						page.setColor(Color.WHITE);
						break;
					default:
						page.setColor(Color.BLACK);
						break;
					}
					page.fillRect(x1,y1,1,1);
				}
			break;
		case COMPOSITE:
		default:
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					b=w.map[x1][y1].biome;
					switch (b) {
					case ICE:
						page.setColor(Color.WHITE);
						break;
					case SEA:
					case RIVER:
						page.setColor(new Color(0,0,64+w.map[x1][y1].z/2));
						break;
					case TUNDRA:
						page.setColor(new Color(128+w.map[x1][y1].z/2,128+w.map[x1][y1].z/2,128+w.map[x1][y1].z/2));
						break;
					case TAIGA:
						page.setColor(Color.getHSBColor(.4167F,1,(float) (.25+w.map[x1][y1].z/2./255)));
						break;
					case DESERT:
						page.setColor(Color.getHSBColor(.1667F,.5F,(float) ((64.+w.map[x1][y1].z)/255)));
						break;
					case GRASSLAND:
						page.setColor(Color.getHSBColor(.25F,1,(float) (.5+w.map[x1][y1].z/2./255)));
						break;
					case CONIFEROUS_FOREST:
						page.setColor(Color.getHSBColor(.3889F,1,(float) (.25+w.map[x1][y1].z/2./255)));
						break;
					case MIXED_FOREST:
						page.setColor(Color.getHSBColor(.3611F,1,(float) (.25+w.map[x1][y1].z/2./255)));
						break;
					case BROADLEAF_FOREST:
						page.setColor(Color.getHSBColor(.3333F,1,(float) (.25+w.map[x1][y1].z/2./255)));
						break;
					case ROCK:
						page.setColor(new Color(64+w.map[x1][y1].z/2,64+w.map[x1][y1].z/2,64+w.map[x1][y1].z/2));
						break;
					}
					page.fillRect(x1,y1,1,1);
				}
			break;
		}
		if (civs&&mode!=Display.SETTLEMENT&&mode!=Display.CIVLIST) {
			for (int y1=0;y1<w.HEIGHT;y1++)
				for (int x1=0;x1<w.WIDTH;x1++) {
					if (w.map[x1][y1].owner!=null) {
						page.setColor(Color.getHSBColor(w.map[x1][y1].owner.owner.hue,w.map[x1][y1].owner.owner.sat,.5F));
						page.fillRect(x1,y1,1,1);
					}
				}
		}
		if (grid&&mode!=Display.CIVLIST) {
			page.setColor(new Color(255,255,255,64));
			for (int y1=25;y1<w.HEIGHT;y1+=25) {
				if (y1%100==0) {
					page.setColor(new Color(255,0,0,64));
					page.fillRect(0,y1,w.WIDTH,1);
					page.setColor(new Color(255,255,255,64));
				} else
					page.fillRect(0,y1,w.WIDTH,1);
			}
			for (int x1=25;x1<w.WIDTH;x1+=25) {
				if (x1%100==0) {
					page.setColor(new Color(255,0,0,64));
					page.fillRect(x1,0,1,w.HEIGHT);
					page.setColor(new Color(255,255,255,64));
				} else
					page.fillRect(x1,0,1,w.HEIGHT);
			}
		}
	}
	
	public void run() {
		while (true) {
			if (year!=w.year) {
				year=w.year;
			}
			synchronized (this) {
				repaint();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void setMode(Display d) {
		synchronized (this) {
			mode=d;
			repaint();
		}
	}
	
	public synchronized void toggleCivs() {
		civs=!civs;
	}
	
	public synchronized void toggleGrid() {
		grid=!grid;
	}
}
