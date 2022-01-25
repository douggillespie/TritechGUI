package tritechgui.detect;

import java.util.ArrayList;

import tritechgemini.imagedata.GeminiImageRecordI;

/**
 * Detector which searches for detected regions in data. Uses two thresholds, a "minimum maximum" (thHigh) 
 * and a "minimum edge" thLow. 
 * Should ideally be sent data after background subtraction, can also work with raw raw data. 
 * @author dg50
 *
 */
public class RegionDetector {

	private int MAXSIZE = 200;
	/**
	 * Search for regions using the raw data within the record
	 * @param geminiRecord
	 * @param thHigh
	 * @param thLow
	 * @return
	 */
	public ArrayList<DetectedRegion> detectRegions(GeminiImageRecordI geminiRecord, int thHigh, int thLow, int nConnect) {
		return detectRegions(geminiRecord, geminiRecord.getImageData(), thHigh, thLow, nConnect);
	}


	/**
	 * Search for regions using a different set of raw data, which should probably be a noise reduced version of 
	 * what was in the original record. Must be the same dimension as that in the original. 
	 * @param geminiRecord
	 * @param recordData
	 * @param thHigh
	 * @param thLow
	 * @return
	 */
	public ArrayList<DetectedRegion> detectRegions(GeminiImageRecordI geminiRecord, byte[] recordData, int thHigh, int thLow, int nConnect) {
		/*
		 * First copy the data since it's going to be destroyed.
		 */
		ArrayList<DetectedRegion> detectedRegions = null;
		byte[] data = recordData.clone();
		int nBearing = geminiRecord.getnBeam();
		int nRange = geminiRecord.getnRange();
		int nData = nRange*nBearing;
		/**
		 * Zero data around the edges of the image. 
		 */
		for (int i = 0, j = nBearing*nRange-1; i < nBearing; i++, j--) {
			// this gets the first and last range data
			data[i] = data[j] = 0;
		}
		for (int i = nBearing-1, j = nBearing; j < nData; i+=nBearing, j+= nBearing) {
			// this gets the edges (last and first bearing) at each range 
			data[i] = data[j] = 0;
		}
		/**
		 * Make the mask, connect 4 or connect 8.
		 */
		int[] searchMask;
		if (nConnect == 4) {
			int[] mask = {-1, 1, nBearing, -nBearing};
			searchMask = mask;
		}
		else {
			int[] mask = {-1, 1, nBearing, -nBearing, nBearing-1, nBearing+1, -nBearing-1, -nBearing+1};
			searchMask = mask;
		}
		// can now look for data above threshold
		for (int i = nBearing+1; i < nData-nBearing; i++) {
			if (Byte.toUnsignedInt(data[i]) >= thHigh) {
				//	we have a detection
				DetectedRegion newRegion = new DetectedRegion(geminiRecord, i);
				data[i] = 0; // set point to zero so it's not used again
				expandRegion(newRegion, data, i, thLow, searchMask);
				newRegion.completeRegion();
				if (wantRegion(newRegion)) {
					if (detectedRegions == null) {
						detectedRegions = new ArrayList<>();
					}
					detectedRegions.add(newRegion);
				}
			}
		}
		return detectedRegions;
	}

	/**
	 * Recursive call to more about a point, seeing if any adjacent points are above threshold and 
	 * should be added to the region. As data are added, they are set to zero so that as the 
	 * search mask moves around, it can never add the same point twice. 
	 * @param region Growing region of data. 
	 * @param data data array
	 * @param currInd current search index in the data
	 * @param thLow threshold
	 * @param searchMask search mask for connect 4 or connect 8.  
	 */
	private void expandRegion(DetectedRegion region, byte[] data, int currInd, int thLow, int[] searchMask) {
		if (region.getRegionSize() >= MAXSIZE) {
			return;
		}
		for (int i = 0; i < searchMask.length; i++) {
			int ind = currInd+searchMask[i];
			int dat = Byte.toUnsignedInt(data[ind]);
			if (dat >= thLow) {
				region.addPoint(ind);
				data[ind] = 0;
				expandRegion(region, data, ind, thLow, searchMask);
			}
		}
	}

	/**
	 * Want this region ? Can make decisions based on size, etc. 
	 * @param newRegion
	 * @return true if it's worth keeping. 
	 */
	private boolean wantRegion(DetectedRegion newRegion) {
		return newRegion.getRegionSize() > 10;
	}
}
