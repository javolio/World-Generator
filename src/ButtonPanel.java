import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//The buttons below the visualizer
@SuppressWarnings("serial")
public class ButtonPanel extends JPanel implements ActionListener {
	private JButton[] modeButtons;
	private JButton tickButton,exportButton,civButton,gridButton,listButton;
	private JTextField years;
	private SwitchablePanel p;
	private World world;
	
	public ButtonPanel(SwitchablePanel p,World w) {
		this.p=p;
		modeButtons=new JButton[8];
		this.add(modeButtons[0]=new JButton("COMPOSITE"));
		this.add(modeButtons[1]=new JButton("BIOME"));
		this.add(modeButtons[2]=new JButton("HEIGHT"));
		this.add(modeButtons[3]=new JButton("SLOPE"));
		this.add(modeButtons[4]=new JButton("TEMPERATURE"));
		this.add(modeButtons[5]=new JButton("PRECIPITATION"));
		this.add(modeButtons[6]=new JButton("SETTLEMENT"));
		this.add(modeButtons[7]=new JButton("WATER"));
		this.add(civButton=new JButton("TOGGLE CIVS"));
		this.add(gridButton=new JButton("TOGGLE GRIDLINES"));
		this.add(listButton=new JButton("CIVILIZATION LIST"));
		this.add(exportButton=new JButton("EXPORT"));
		JLabel l=new JLabel("Years to advance: ");
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(l);
		this.add(years=new JTextField());
		this.add(tickButton=new JButton("GO"));
		this.add(new YearLabel(w));
		for (JButton j:modeButtons)
			j.addActionListener(this);
		tickButton.addActionListener(this);
		exportButton.addActionListener(this);
		years.addActionListener(this);
		civButton.addActionListener(this);
		gridButton.addActionListener(this);
		listButton.addActionListener(this);
		world=w;
		w.setControllers(new JButton[] {tickButton,exportButton});
		setLayout(new GridLayout(0,4));
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source=e.getSource();
		if (source==modeButtons[0]) { //Switch visualizer
			p.setMode(Display.COMPOSITE);
		} else if (source==modeButtons[1]) { //Switch visualizer
			p.setMode(Display.BIOME);
		} else if (source==modeButtons[2]) { //Switch visualizer
			p.setMode(Display.HEIGHT);
		} else if (source==modeButtons[3]) { //Switch visualizer
			p.setMode(Display.SLOPE);
		} else if (source==modeButtons[4]) { //Switch visualizer
			p.setMode(Display.TEMPERATURE);
		} else if (source==modeButtons[5]) { //Switch visualizer
			p.setMode(Display.PRECIPITATION);
		} else if (source==modeButtons[6]) { //Switch visualizer
			p.setMode(Display.SETTLEMENT);
		} else if (source==modeButtons[7]) { //Switch visualizer
			p.setMode(Display.WATER);
		} else if (source==tickButton||source==years&&tickButton.isEnabled()) { //Advance y years
			int y=Integer.parseInt(years.getText());
			world.tick(y);
		} else if (source==exportButton) { //Export the world
			world.export();
		} else if (source==civButton) { //Toggle cities
			p.toggleCivs();
		} else if (source==gridButton) { //Toggle the gridlines
			p.toggleGrid();
		} else if (source==listButton) { //Switch visualizer
			p.setMode(Display.CIVLIST);
		}
		
	}
}
