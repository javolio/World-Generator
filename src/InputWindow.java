import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.*;

//Gets the parameters for the world from the user and starts everything up
@SuppressWarnings("serial")
public class InputWindow extends JFrame implements ActionListener {
	private JButton clear,create;
	private JTextField[] textFields;
	
	public InputWindow() {
		super("World Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel=new JPanel();
		setContentPane(panel);
		panel.setLayout(new GridLayout(0,2));
		JLabel[] labels=new JLabel[3];
		textFields=new JTextField[3];
		labels[0]=new JLabel("Seed: ");
		labels[1]=new JLabel("Width: ");
		labels[2]=new JLabel("Height: ");
		for (int i=0;i<3;i++) {
			labels[i].setHorizontalAlignment(SwingConstants.RIGHT);
			panel.add(labels[i]);
			panel.add(textFields[i]=new JTextField());
		}
		panel.add(clear=new JButton("CLEAR"));
		panel.add(create=new JButton("CREATE WORLD"));
		clear.addActionListener(this);
		create.addActionListener(this);
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==clear) {
			for (JTextField t:textFields)
				t.setText("");
		} else if (e.getSource()==create) {
			boolean good=true;
			int s,w,h;
			try {
				s=Integer.parseInt(textFields[0].getText());
			} catch (NumberFormatException err) {
				textFields[0].setText("");
				good=false;
				s=0;
			}
			try {
				w=Integer.parseInt(textFields[1].getText());
			} catch (NumberFormatException err) {
				textFields[1].setText("");
				good=false;
				w=0;
			}
			try {
				h=Integer.parseInt(textFields[2].getText());
			} catch (NumberFormatException err) {
				textFields[2].setText("");
				good=false;
				h=0;
			}
			if (good) {
				World world=new World(s,w,h);
				Loader l=null;
				try {
					l=new Loader(world);
				} catch (FileNotFoundException e1) {
					good=false;
				}
				if (good) {
					try {
						l.loadAll();
					} catch (FileNotFoundException e1) {
					}
					world.mapWorld();
					JFrame frame=new JFrame("Map");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					JPanel panel=new JPanel();
					frame.setContentPane(panel);
					panel.setLayout(new BorderLayout());
					SwitchablePanel p=new SwitchablePanel(world);
					panel.add(p,BorderLayout.NORTH);
					panel.add(new ButtonPanel(p,world),BorderLayout.SOUTH);
					frame.pack();
					frame.setVisible(true);
					frame.setSize(new Dimension(SwitchablePanel.WIDTH+6,SwitchablePanel.HEIGHT+132));
					frame.setResizable(false);
					setVisible(false);
					dispose();
				}
			}
		}
	}
}
