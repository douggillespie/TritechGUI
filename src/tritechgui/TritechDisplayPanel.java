package tritechgui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tritechgemini.fileio.CatalogObserver;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;

public class TritechDisplayPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private GeminiImageRecordI geminiImage[];
	
	private TritechSonarPanel tritechSonarPanel;

	private JFrame mainFrame;
	
	private JSlider dataSlider;
	
	private JLabel infoStrip;

	private TritechGUIControl tritechGUIControl;
	
	private DateFormat dateFormat;

	private int[] sonarList;
	
	private HashMap<Integer, Integer> availableSonars = new HashMap();

	public TritechDisplayPanel(TritechGUIControl tritechGUIControl, JFrame mainFrame) {
		this.tritechGUIControl = tritechGUIControl;
		this.mainFrame = mainFrame;
		
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		mainFrame.setLayout(new BorderLayout());
		tritechSonarPanel = new TritechSonarPanel(this);
		mainFrame.add(tritechSonarPanel, BorderLayout.CENTER);
		dataSlider = new JSlider(0, 100);
		mainFrame.add(dataSlider, BorderLayout.SOUTH);
		dataSlider.addChangeListener(new SliderObserver());
		dataSlider.setValue(0);
		infoStrip = new JLabel(" ");
		mainFrame.add(infoStrip, BorderLayout.NORTH);
		tritechGUIControl.getMultiFileCatalog().addObserver(new CatObsever());
	
	}

	public GeminiImageRecordI[] getGeminiImages() {
		return geminiImage;
	}

	private class SliderObserver implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			int ind = dataSlider.getValue();
			showImage(ind);
		}
		
	}
	
	private class CatObsever implements CatalogObserver {


		@Override
		public void catalogChanged() {
			setSliderLimits();
			MultiFileCatalog cat = tritechGUIControl.getMultiFileCatalog();
			sonarList = cat.getSonarIDs();
		}
		
	}

	public void setSliderLimits() {
		MultiFileCatalog cat = tritechGUIControl.getMultiFileCatalog();
		if (cat == null) {
			return;
		}
		int n = cat.getMaxDeviceRecords();
		dataSlider.setMaximum(n-1);
	}

	public void showImage(int ind) {
		MultiFileCatalog cat = tritechGUIControl.getMultiFileCatalog();
		if (cat == null || sonarList == null) {
			return;
		}
		GeminiImageRecordI[] images = new GeminiImageRecordI[sonarList.length];
		for (int i = 0; i < sonarList.length; i++) {
			images[i] = cat.getSonarRecord(sonarList[i], ind);
		}
		showImages(images);
	}
	
	public void showLiveImage(GeminiImageRecordI geminiImage) {
		int sonarInd = getLiveSonarindex(geminiImage.getDeviceId());
		this.geminiImage[sonarInd] = geminiImage;
		showImages(this.geminiImage);
	}
	
	public int getLiveSonarindex(int sonarId) {
		int nSonar = checkLiveSonars(sonarId);
		for(int i = 0; i < nSonar; i++) {
			if (sonarList[i] == sonarId) {
				return i;
			}
		}
		return -1;
	}
	private int checkLiveSonars(int sonarId) {
		if (availableSonars.containsKey(sonarId)) {
			return availableSonars.size();
		}
		availableSonars.put(sonarId, sonarId);
		// need to add a sonar
		if (sonarList == null) {
			sonarList = new int[availableSonars.size()];
			geminiImage = new GeminiImageRecordI[availableSonars.size()];
		}
		else {
			sonarList = Arrays.copyOf(sonarList, availableSonars.size());
			geminiImage = Arrays.copyOf(geminiImage, availableSonars.size());
		}
		sonarList[sonarList.length-1] = sonarId;
		return availableSonars.size();
	}

	private void showImages(GeminiImageRecordI[] records) {
		this.geminiImage = records;
		tritechSonarPanel.setGeminiRecord(records);
		sayImageInfo();
	}

	private void sayImageInfo() {
		if (infoStrip == null) {
			return;
		}
		if (geminiImage == null || geminiImage.length == 0) {
			infoStrip.setText(" ");
			return;
		}
		GeminiImageRecordI anImage = geminiImage[0];
		if (anImage == null) {
			infoStrip.setText(" ");
			return;
		}
		if (anImage.getFilePath() != null) {
		File f = new File(anImage.getFilePath());
		String infoString = String.format(" %s, rec %d, %s, range %3.1fm", f.getName(), 
				anImage.getRecordNumber(), formatTime(anImage.getRecordTime()), anImage.getMaxRange());
		infoStrip.setText(infoString);
		}
	}
	
	public String formatTime(long time) {
		Date d = new Date(time);
		return dateFormat.format(d);
	}
	
	

}
