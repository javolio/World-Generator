import javax.swing.JLabel;
import javax.swing.SwingConstants;

//A threaded label that continually updates with the current year
@SuppressWarnings("serial")
public class YearLabel extends JLabel implements Runnable {
	private World w;
	
	public YearLabel(World w) {
		super("Year: "+w.year);
		setHorizontalAlignment(SwingConstants.CENTER);
		this.w=w;
		new Thread(this).start();
	}
	
	public void run() {
		while (true) {
			setText("YEAR: "+w.year);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	
}
