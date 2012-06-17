import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
@Deprecated
public class CompositePanel extends JPanel implements Runnable {
	private final int W,H=600;
	private final double DELTA;
	private final double SCALE;
	private World w;
	private Thread animator;
	private int year;
	
	public CompositePanel(World w) {
		this.w=w;
		W=(int) (1.*w.WIDTH*H/w.HEIGHT);
		DELTA=255/w.RANGE;
		SCALE=1.*H/w.HEIGHT;
		setPreferredSize(new Dimension(W,H));
		year=w.year;
		animator=new Thread(this);
		animator.start();
	}
	
	public void paintComponent(Graphics page) {
		Biome b;
		for (int y1=0;y1<w.HEIGHT;y1++)
			for (int x1=0;x1<w.WIDTH;x1++) {
				b=w.map[x1][y1].biome;
				if (w.map[x1][y1].owner!=null)
					if (w.map[x1][y1].owner.owner.name.equals("Elves"))
						page.setColor(Color.ORANGE);
					else
						page.setColor(Color.RED);
				else
					switch (b) {
					case ICE:
						page.setColor(Color.WHITE);
						break;
					case SEA:
					case RIVER:
						page.setColor(new Color(0,0,(int) Math.min(64+DELTA*w.map[x1][y1].z,255)));
						break;
					case TUNDRA:
						page.setColor(new Color((int) Math.min(DELTA*w.map[x1][y1].z+64,255),(int) Math.min(DELTA*w.map[x1][y1].z+64,255),(int) Math.min(DELTA*w.map[x1][y1].z+64,255)));
						break;
					case TAIGA:
						page.setColor(new Color((int) (32+DELTA*w.map[x1][y1].z/4.),(int) (64+DELTA*w.map[x1][y1].z/2.),(int) (32+DELTA*w.map[x1][y1].z/4.)));
						break;
					case DESERT:
						page.setColor(new Color((int) (64+DELTA*w.map[x1][y1].z/2.),(int) (32+DELTA*w.map[x1][y1].z/4.),0));
						break;
					case GRASSLAND:
						page.setColor(new Color((int) Math.min(48+DELTA*w.map[x1][y1].z*2/3.,255),(int) Math.min(64+DELTA*w.map[x1][y1].z,255),0));
						break;
					case CONIFEROUS_FOREST:
						page.setColor(new Color(0,(int) (DELTA*w.map[x1][y1].z),48));
						break;
					case MIXED_FOREST:
						page.setColor(new Color(0,(int) (DELTA*w.map[x1][y1].z),24));
						break;
					case BROADLEAF_FOREST:
						page.setColor(new Color(0,(int) (DELTA*w.map[x1][y1].z),0));
						break;
					}
				page.fillRect((int) (x1*SCALE),(int) (y1*SCALE),(int) SCALE,(int) SCALE);
			}
	}
	
	public void run() {
		while (true) {
			if (year!=w.year) {
				year=w.year;
				this.repaint();
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
	}
}
