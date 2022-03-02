package tritechgui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 * Real simple GUI to display Tritech Gemini image data from one, eventually many, files. 
 * @author dg50
 *
 */
public class TritechGUIMain {
	
	private JFrame mainFrame;

	public static void main(String[] args) {
		
//		if (args == null || args.length == 0) {
//			args = new String[1];
//			args[0] = "C:\\ProjectData\\RobRiver\\log_2021-12-12-000518.glf";
//		}
		new TritechGUIMain().run(args);
		
		
		
	}

	private void run(String[] args) {
		mainFrame = new JFrame("Tritech GUI");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(800,600);
		TritechGUIControl tritechControl = new TritechGUIControl(mainFrame);
		mainFrame.setVisible(true);
		
		ArrayList<String> fileList = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			File f = new File(args[i]);
			if (f.exists()) {
				fileList.add(args[i]);
			}
		}
		if (fileList.size() > 0) {
			String[] files = fileList.toArray(new String[fileList.size()]);
			System.out.println(files);
			tritechControl.getMultiFileCatalog().catalogFiles(files);
		}
		else {
			tritechControl.runAcquisition();
		}
		
	}
	
}
