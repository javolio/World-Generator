import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
@Deprecated
public class SlopePanel extends JPanel {
	private final int W,H=600;
	private final double SCALE;
	private World w;
	
	public SlopePanel(World w) {
		this.w=w;
		W=(int) (1.*w.WIDTH*H/w.HEIGHT);
		SCALE=1.*H/w.HEIGHT;
		setPreferredSize(new Dimension(W,H));
		System.out.println();
	}
	
	public void paintComponent(Graphics page) {
		//page.drawRect(0,0,50,50);
		for (int y1=0;y1<w.HEIGHT;y1++)
			for (int x1=0;x1<w.WIDTH;x1++) {
				page.setColor(Color.getHSBColor((float) (200.-w.map[x1][y1].slope*200./w.maxSlope)/255,1,(float) .5));
				page.fillRect((int) (x1*SCALE),(int) (y1*SCALE),(int) SCALE,(int) SCALE);
			}
	}
}
