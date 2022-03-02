package tritechgui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import javax.swing.JPanel;

import tritechgemini.detect.BackgroundSub;
import tritechgemini.detect.DetectedRegion;
import tritechgemini.detect.RegionDetector;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;

/**
 * Panel for displaying the actual sonar image(s)
 * @author dg50
 *
 */
public class TritechSonarPanel extends JPanel {

	private GeminiImageRecordI[] geminiRecord;

	private BufferedImage[] bufferedImage;

	private FanImageData[] fanData;

	private FanImageData[] backgroundFan;
	
	private ImageCoordinates[] imageCoordinates;

	private ImageFanMaker fanMaker = new FanPicksFromData(4);

	private boolean drawGrid = true;

	private Color gridColour = new Color(255,0,0,160);

	private int nBearingGrid = 5;

	private int nRangeGrid = 4;

	private BackgroundSub[] backgroundSub = new BackgroundSub[4];
	
	private ArrayList<DetectedRegion> detections[] = new ArrayList[4];
	
	private RegionDetector regionDetector;

	transient public static final String deg="\u00B0";

	private TritechDisplayPanel tritechDisplayPanel;

	public TritechSonarPanel(TritechDisplayPanel tritechDisplayPanel) {
		this.tritechDisplayPanel = tritechDisplayPanel;
		//		backgroundSub = new BackgroundSub();
		setToolTipText("Gemini image display");
		regionDetector = new RegionDetector();
//		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (geminiRecord == null || bufferedImage == null) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON); 
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
		
		if (fanData == null) {
			return;
		}
		if (imageCoordinates == null || imageCoordinates.length != fanData.length) {
			imageCoordinates = new ImageCoordinates[fanData.length];
			for (int i = 0; i < fanData.length; i++) {
				imageCoordinates[i] = new ImageCoordinates();
			}
		}
		

		if (fanData.length == 0) {
			return;
		}
		if (fanData.length == 1) {
			drawSonarImage(g2d, 0, fanData[0], bufferedImage[0], 0, 0, getWidth(), getHeight());
		}
		if (fanData.length == 2) {
			drawSonarImage(g2d, 0, fanData[0], bufferedImage[0], 0, getHeight()*1/3, getWidth()*5/9, getHeight());
			drawSonarImage(g2d, 1, fanData[1], bufferedImage[1], getWidth()*4/9, 0, getWidth(), getHeight()*2/3);
		}

	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (imageCoordinates == null) {
			return null;
		}
		for (int i = 0; i < imageCoordinates.length; i++) {
			String tip = getToolTipText(event, i);
			if (tip != null) {
				return tip;
			}
		}
		return null;
	}
	
	/**
	 * Get a tooltip text for a specified image. 
	 * @param event
	 * @param imageIndex 
	 * @return null if its outside the image. 
	 */
	public String getToolTipText(MouseEvent event, int imageIndex) {
		ImageCoordinates imagePos = imageCoordinates[imageIndex];
		if (imagePos == null) {
			return null;
		}
		if (imagePos.imageX1 == imagePos.imageX2 || imagePos.imageY1 == imagePos.imageY2 || fanData[imageIndex] == null || bufferedImage[imageIndex] == null) {
			return null;
		}
		int x = event.getX();
		int y = event.getY();
		double xMid = (imagePos.imageX1 + imagePos.imageX2)/2;
		double ang = Math.toDegrees(Math.atan2(x-xMid, imagePos.imageY1-y));
		if (Math.abs(ang) > 60) {
			return null;
		}
		double r = Math.sqrt((x-xMid)*(x-xMid) + (y-imagePos.imageY1)*(y-imagePos.imageY1) ); // distance in screen pixels
		if (r > Math.abs(imagePos.imageY1-imagePos.imageY2)) {
			return null;
		}
		r *= fanData[imageIndex].getGeminiRecord().getMaxRange() / (imagePos.imageY1-imagePos.imageY2);
//		r *= bufferedImage[imageIndex].getHeight();
//		r /= (imagePos.imageY1-imagePos.imageY2); // distance in image pixels. 
//		r *= imagePos.imageScale; // distance in metres. 
		short[][] fanValues = fanData[imageIndex].getImageValues();
		if (fanValues == null) {
			return null;
		}
		int nFanX = fanValues.length;
		int nFanY = fanValues[0].length;

		int imPixX = (int) ((x-imagePos.imageX1) * (double) nFanX / (double) (imagePos.imageX2-imagePos.imageX1));
		int imPixY = nFanY - (int) ((y - imagePos.imageY2) * (double) nFanY / (double) (imagePos.imageY1-imagePos.imageY2));
		//		String tip = String.format("%3.1f, %3.1f, %d/%d, %d/%d", ang, r, imPixX, bufferedImage.getWidth(), imPixY, bufferedImage.getHeight() );
		if (imPixX < 0 || imPixX >= nFanX) {
			return null;
		}		
		if (imPixY < 0 || imPixY >= nFanY) {
			return null;
		}
		short val = fanValues[imPixX][imPixY];

		String tip = String.format("Sonar %d, %3.1f%s, %3.1fm, val=%d", fanData[imageIndex].getGeminiRecord().getDeviceId(),
				ang, deg, r, val);
//		if (backgroundFan != null) {
//			short[][] backFan = backgroundFan[imageIndex].getImageValues();
//			tip += String.format(", bg=%d", backFan[imPixX][imPixY]);
//		}

		return tip;
	}

	private void drawSonarImage(Graphics2D g2d, int imageIndex, FanImageData fanData, BufferedImage image, int dx1, int dy1, int dx2, int dy2) {

		if (fanData == null || image == null) {
			System.out.println("Null data for image index " + imageIndex);
			return;
		}
		double imageWidM = image.getWidth() * fanData.getMetresPerPixX();
		double imageHeiM = image.getHeight() * fanData.getMetresPerPixY();

		boolean xSmallest = Math.abs(dx2-dx1)/imageWidM < Math.abs(dy2-dy1)/imageHeiM;
		int x1, x2, y1, y2;
		if (xSmallest) {
			x1 = dx1;
			x2 = dx2;
			y2 = dy1;
			y1 = y2 + (int) Math.round(Math.abs(x2-x1) * imageHeiM / imageWidM);
			int yGap = (Math.abs(dy2-dy1)-Math.abs(y2-y1)) / 2;
			y1 += yGap;
			y2 += yGap;
			imageCoordinates[imageIndex].imageScale = fanData.getMetresPerPixX();
		}
		else {
			y1 = dy2;
			y2 = dy1;
			x1 = dx1;
			x2 = dx1+(int) Math.round(Math.abs(y2-y1 )* imageWidM / imageHeiM);
			int xGap = ((dx2-dx1)-(x2-x1))/2;
			x1 += xGap;
			x2 += xGap;
			imageCoordinates[imageIndex].imageScale = fanData.getMetresPerPixY();
		}

		imageCoordinates[imageIndex].imageX1 = x1;
		imageCoordinates[imageIndex].imageX2 = x2;
		imageCoordinates[imageIndex].imageY1 = y1;
		imageCoordinates[imageIndex].imageY2 = y2;
		g2d.drawImage(image, x1, y1, x2, y2, 0, 0,
				image.getWidth(), image.getHeight(), null);
		
		int xVertex = (x1+x2)/2;
		int yVertex = (y1-1);
		int rangePixels = Math.abs(y2-y1);

		if (drawGrid ) {
			g2d.setColor(gridColour);
			GeminiImageRecordI gemRec = fanData.getGeminiRecord();
			double[] bearingTable = gemRec.getBearingTable();
			int nBear = bearingTable.length;
			double bearRange = bearingTable[nBear-1] - bearingTable[0];
			double bearStep = bearRange / (nBearingGrid-1);
			int len = y1-y2;
			for (int i = 0; i < nBearingGrid; i++) {
				double bear = bearingTable[0] + i*bearStep;
				x1 = (int) (xVertex + Math.sin(bear) * len);
				y1 = (int) (yVertex - Math.cos(bear) * len);
				g2d.drawLine(xVertex, yVertex, x1, y1);
			}

			int a1d = (int) Math.toDegrees(bearingTable[0]);
			int a2d = (int) Math.toDegrees(bearingTable[nBear-1]);
			double rMostAngle = bearingTable[nBear-1];
			FontMetrics fm = g2d.getFontMetrics();
			for (int i = 0; i < nRangeGrid; i++) {
				g2d.setColor(gridColour);
				int r =(i+1) * len / nRangeGrid;
				//				r = len;
				int x = xVertex-r;
				int y = yVertex-r;
				int w = 2*r;
				int h =  r*2;
				g2d.drawArc(x, y, w, h, a1d+90, a2d-a1d);

				AffineTransform currTrans = g2d.getTransform();
				double d = (i+1) * fanData.getGeminiRecord().getMaxRange() / nRangeGrid;
				String str = String.format(" %3.1fm", d);
				Color col = g2d.getColor();
				g2d.setColor(new Color(col.getRed(), col.getBlue(), col.getGreen()));


				// this method rotates everything, then draws relative to origin. 
				// draw rotated text. for this we need the actual line end 
				//				g2d.setTransform(AffineTransform.getRotateInstance(-rMostAngle, 
				//						x0, y0));
				//				g2d.drawString(str,x0,y0-r);
				//				Font f = g2d.getFont();

				// this one calculates the position, then rotates about the position and
				// draws just there. 
				x = (int) (xVertex-r*Math.sin(rMostAngle));
				y = (int) (yVertex-r*Math.cos(rMostAngle));
				g2d.setTransform(AffineTransform.getRotateInstance(-rMostAngle, 
						x, y));
				LineMetrics lm = fm.getLineMetrics(str, g2d);
				g2d.drawString(str, x, y+lm.getAscent()/2-1);


				g2d.setTransform(currTrans);
			}
		}
		ArrayList<DetectedRegion> dets = detections[imageIndex];
		if (dets != null) {
			for (int i = 0; i < dets.size(); i++) {
				DetectedRegion region = dets.get(i);
				double a1 = region.getMinBearing();
				double a2 = region.getMaxBearing(); 
				double r1 = region.getMinRange();
				double r2 = region.getMaxRange();
				int[] xr = new int[4];
				int[] yr = new int[4];
				double rScale = 1./fanData.getMetresPerPixX();
				rScale = rangePixels/fanData.getGeminiRecord().getMaxRange();
				int x0 = (x1+x2)/2;
				int y0 = y1-1;
				xr[0] = (int) (xVertex-rScale*r1*Math.sin(a1));
				xr[1] = (int) (xVertex-rScale*r2*Math.sin(a1));
				xr[2] = (int) (xVertex-rScale*r2*Math.sin(a2));
				xr[3] = (int) (xVertex-rScale*r1*Math.sin(a2));
				yr[0] = (int) (yVertex-rScale*r1*Math.cos(a1));
				yr[1] = (int) (yVertex-rScale*r2*Math.cos(a1));
				yr[2] = (int) (yVertex-rScale*r2*Math.cos(a2));
				yr[3] = (int) (yVertex-rScale*r1*Math.cos(a2));
				g2d.drawPolygon(xr, yr, 4);
			}
		}
		
		// write info in the top left corner of the display. 
		g2d.setColor(Color.BLACK);
		int xt = imageCoordinates[imageIndex].imageX1;
		int yt = imageCoordinates[imageIndex].imageY2;
		FontMetrics fm = g2d.getFontMetrics();
		xt += fm.charWidth(' ');
		GeminiImageRecordI gemRec = fanData.getGeminiRecord();
		String str = String.format("Sonar %d, record %d", gemRec.getDeviceId(), gemRec.getRecordNumber());
		g2d.drawString(str, xt, yt);
		str = tritechDisplayPanel.formatTime(gemRec.getRecordTime());
		yt += fm.getHeight();
		g2d.drawString(str, xt, yt);		
		
	}

	/**
	 * Set the gemini record to show. 
	 * @param records
	 */
	public void setGeminiRecord(GeminiImageRecordI[] records) {
		this.geminiRecord = records.clone();
		this.makeImage(records);
//		System.out.println("Repaint now");
		repaint();
	}

	private void makeImage(GeminiImageRecordI[] record) {
//		bufferedImage = new BufferedImage[record.length];
		FanImageData[] newFanData = new FanImageData[record.length];
		BufferedImage[] newBufferedImages = new BufferedImage[record.length];
		backgroundFan = new FanImageData[record.length];
		if (record == null) {
//			bufferedImage = null;
			return;
		}
		for (int i = 0; i < record.length; i++) {
			if (record[i] == null) {
				continue;
			}
			byte[] cleanData = record[i].getImageData();
			if (backgroundSub[i] ==null) {
				backgroundSub[i] = new BackgroundSub();
			}
			cleanData = backgroundSub[i].removeBackground(cleanData, true);
			newFanData[i] = fanMaker.createFanData(record[i], getWidth(), getHeight(), cleanData);
			detections[i] = regionDetector.detectRegions(record[i], cleanData, 70, 30, 8);
//			if (backgroundSub[i] != null && backgroundSub.getBackground() != null) {
//				backgroundFan = fanMaker.createFanData(record, getWidth(), getHeight(), backgroundSub.getBackground());
//			}
//			else {
//				backgroundFan = null;
//			}
			if (newFanData == null) {
				return;
			}
			short[][] data = newFanData[i].getImageValues();
			if (data == null) {
				return;
			}
			this.geminiRecord = record;
			int nX = data.length;
			int nY = data[0].length;
			newBufferedImages[i] = new BufferedImage(nX, nY, BufferedImage.TYPE_4BYTE_ABGR);
			WritableRaster raster = newBufferedImages[i].getRaster();
			//		Color transParent = new Color(0,0,0,0);
			//		Color pixCol = new Color(0,0,0,0);
			int[] transparent = {0,0,0,0};
			int[] coloured = {0,0,0,255};
			for (int ix = 0; ix < nX; ix++) {
				for (int iy = 0; iy < nY; iy++) {
					short val = data[ix][iy];
					if (val < 0) {
						raster.setPixel(ix, iy, transparent);
					}
					else {
						val &= 0xFF;
						coloured[0] = 0;
						coloured[1] = val;
						coloured[2] = sqrt255(val);
						//					coloured[3] = val;
						raster.setPixel(ix, iy, coloured);
					}
				}
			}
		}
		fanData = newFanData;
		bufferedImage = newBufferedImages;
	}

	/**
	 * Get the sqrt of 255 on a scale of 1:255. 
	 * @param val
	 * @return
	 */
	private int sqrt255(int val) {
		return (int) Math.sqrt(val*255);
	}
	
	private class ImageCoordinates {
		/*
		 * final coordinates of drawn image, so can use these
		 * in tooltips which work out a pixel in the image from
		 * the mouse coordinate. 
		 */
		int imageX1, imageX2, imageY1, imageY2;

		private double imageScale;
	}
}
