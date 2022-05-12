package tritechgui.acquisition;

import geminisdk.structures.GemStatusPacket;

public class SonarStatusData {

	public int sonarId;
	
	public GemStatusPacket lastStatusPacket;
	
	int totalImages;
	
	public SonarStatusData(int sonarId) {
		this.sonarId = sonarId;
	}

}
