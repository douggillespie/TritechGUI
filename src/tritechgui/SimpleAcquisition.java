package tritechgui;

import geminisdk.GenesisSerialiser;
import geminisdk.Svs5Commands;
import geminisdk.Svs5StandardCallback;
import geminisdk.GenesisSerialiser.GlfLib;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.ConfigOnline;
import geminisdk.structures.GemStatusPacket;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.PingMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechgemini.imagedata.GLFImageRecord;

public class SimpleAcquisition {

	private TritechGUIControl guiControl;
	private Svs5Commands svs5Commands;
	private GlfLib gSerialiser;

	public SimpleAcquisition(TritechGUIControl guiControl) {
		this.guiControl = guiControl;
		
		gSerialiser = GenesisSerialiser.getLibrary();
		svs5Commands = new Svs5Commands();
		String sv5Inf = gSerialiser.svs5GetLibraryVersionInfo();
		System.out.println(sv5Inf);
	}

	public void start() {

		long ans1 = gSerialiser.svs5StartSvs5(new GeminiCallback());
		long err;
		
		ChirpMode chirpMode = new ChirpMode(ChirpMode.CHIRP_AUTO);
		err = svs5Commands.setConfiguration(chirpMode);
		System.out.println("setConfiguration chirpMode returned " + err);


		RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
		err = svs5Commands.setConfiguration(rfConfig);
		System.out.println("setConfiguration returned " + err);
		
	
//		SimulateADC simADC = new SimulateADC(true);
//		err = svs5Commands.setConfiguration(simADC);
//		System.out.println("Simulate returned " + err);

		PingMode pingMode = new PingMode();
		err = svs5Commands.setConfiguration(pingMode);
		System.out.println("setConfiguration pingMode returned " + err);

		ConfigOnline cOnline = new ConfigOnline(true);
		err = svs5Commands.setConfiguration(cOnline);
		System.out.println("setOnline returned " + err);

		GeminiRange range = new GeminiRange(2.);
		err = svs5Commands.setConfiguration(range, 0);
		err += svs5Commands.setConfiguration(range, 1);
		System.out.println("setRange returned " + err);

				
	}
	
	public void stop() {

		ConfigOnline cOnline = new ConfigOnline(true);
		long err = svs5Commands.setConfiguration(cOnline);
		System.out.println("setOnline off returned " + err);

		long ans2 = gSerialiser.svs5StopSvs5();
		System.out.printf("SvS5 stopped with code %d\n", ans2);
		
	}
	
	public class GeminiCallback extends Svs5StandardCallback {

		public GeminiCallback() {
			super();
			setVerbose(false);
		}

		@Override
		public void setFrameRate(int framesPerSecond) {
//			GeminiRange range = new GeminiRange(0);
//			svs5Commands.getConfiguration(range.defaultCommand(), range, 853);
			System.out.println("Frame rate is " + framesPerSecond);
		}

		@Override
		public void newGLFLiveImage(GLFImageRecord glfImage) {
			guiControl.getTritechDisplayPanel().showLiveImage(glfImage);
		}

		@Override
		public void newStatusPacket(GemStatusPacket statusPacket) {
			// TODO Auto-generated method stub

			GeminiRange range;

//			range = new GeminiRange(1.5);
//			long err = svs5Commands.setConfiguration(range,0);
//			err += svs5Commands.setConfiguration(range,1);
//			System.out.printf("setRange returned %d; ", err);
//			
////			range = new GeminiRange(2);
//			long rErr = svs5Commands.getConfiguration(range.defaultCommand(), range, 0);
//			double r1 = range.range;
//			rErr += svs5Commands.getConfiguration(range.defaultCommand(), range, 1);
//			double r2 = range.range;
//				System.out.printf("Range err %d, value %3.1f and %3.1f\n", rErr, r1,r2);
			
		}
		
	}

}
