package tritechgui;

import javax.swing.JFrame;

import tritechgemini.fileio.MultiFileCatalog;
import tritechgui.acquisition.SimpleAcquisition;

/**
 * controller class. May as well follow basic MVC rules. 
 * @author dg50
 *
 */
public class TritechGUIControl {
	
	private TritechDisplayPanel tritechDisplayPanel;
	
	private MultiFileCatalog multiFileCatalog;
	
	private SimpleAcquisition simpleAcquisition;

	private JFrame mainFrame; 

	public TritechDisplayPanel getTritechDisplayPanel() {
		return tritechDisplayPanel;
	}

	public MultiFileCatalog getMultiFileCatalog() {
		return multiFileCatalog;
	}

	public TritechGUIControl(JFrame mainFrame) {
		
		this.mainFrame = mainFrame;
		
		multiFileCatalog = new MultiFileCatalog();
		
		tritechDisplayPanel = new TritechDisplayPanel(this, mainFrame);
		
	}

	public void runAcquisition() {
		simpleAcquisition = new SimpleAcquisition(this);
		simpleAcquisition.start();
	}

	public void quit() {
//		mainFrame.clos.
		System.exit(0);
	}

	public void stopEverything() {
		if (simpleAcquisition != null) {
			simpleAcquisition.stop();
		}
		
	}

}
