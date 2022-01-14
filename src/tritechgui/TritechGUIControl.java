package tritechgui;

import javax.swing.JFrame;

import tritechgemini.fileio.MultiFileCatalog;

/**
 * controller class. May as well follow basic MVC rules. 
 * @author dg50
 *
 */
public class TritechGUIControl {
	
	private TritechDisplayPanel tritechDisplayPanel;
	
	private MultiFileCatalog multiFileCatalog;

	public TritechDisplayPanel getTritechDisplayPanel() {
		return tritechDisplayPanel;
	}

	public MultiFileCatalog getMultiFileCatalog() {
		return multiFileCatalog;
	}

	public TritechGUIControl(JFrame mainFrame) {
		
		multiFileCatalog = new MultiFileCatalog();
		
		tritechDisplayPanel = new TritechDisplayPanel(this, mainFrame);
		
	}

}
