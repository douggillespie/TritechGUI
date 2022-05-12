package tritechgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class TritechMenu {

	private TritechGUIControl tritechControl;
	

	public TritechMenu(TritechGUIControl tritechControl) {
		this.tritechControl = tritechControl;
	}
	
	public JMenuBar createMenu() {
		JMenuBar menuBar  = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem quit = new JMenuItem("Exit");
		fileMenu.add(quit);
		quit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tritechControl.quit();
			}
		});
		
		
		
		
		return menuBar;
	}

}
