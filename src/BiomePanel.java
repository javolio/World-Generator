import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
@Deprecated
public class BiomePanel extends JPanel {
	private final int W,H=600;
	private final double SCALE;
	private World w;
	
	public BiomePanel(World w) {
		this.w=w;
		W=(int) (1.*w.WIDTH*H/w.HEIGHT);
		SCALE=1.*H/w.HEIGHT;
		setPreferredSize(new Dimension(W,H));
		System.out.println();
	}
	
	public void paintComponent(Graphics page) {
		Biome b;
		for (int y1=0;y1<w.HEIGHT;y1++)
			for (int x1=0;x1<w.WIDTH;x1++) {
				b=w.map[x1][y1].biome;
				switch (b) {
				case ICE:
					page.setColor(Color.LIGHT_GRAY);
					break;
				case SEA:
				case RIVER:
					page.setColor(Color.BLUE);
					break;
				case TUNDRA:
					page.setColor(Color.WHITE);
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
				page.fillRect((int) (x1*SCALE),(int) (y1*SCALE),(int) SCALE,(int) SCALE);
			}
	}
}
