import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
@Deprecated
public class HeightPanel extends JPanel {
	private final int W,H=600,DELTA;
	private final double SCALE;
	private World w;
	
	public HeightPanel(World w) {
		this.w=w;
		W=(int) (1.*w.WIDTH*H/w.HEIGHT);
		DELTA=255/w.RANGE;
		SCALE=1.*H/w.HEIGHT;
		setPreferredSize(new Dimension(W,H));
		System.out.println();
	}
	
	public void paintComponent(Graphics page) {
		//page.drawRect(0,0,50,50);
		for (int y1=0;y1<w.HEIGHT;y1++)
			for (int x1=0;x1<w.WIDTH;x1++) {
				if (w.map[x1][y1].z<=w.WATERLEVEL)
					page.setColor(new Color(0,0,64+DELTA*w.map[x1][y1].z));
				else if (w.map[x1][y1].river!=-1)
					page.setColor(new Color(0,0,Math.min(64+DELTA*w.map[x1][y1].z,255)));
				else if (w.map[x1][y1].owner!=null)
					page.setColor(Color.RED);
				else
					page.setColor(new Color(DELTA*w.map[x1][y1].z,DELTA*w.map[x1][y1].z,DELTA*w.map[x1][y1].z));
				page.fillRect((int) (x1*SCALE),(int) (y1*SCALE),(int) SCALE,(int) SCALE);
			}
	}
}
