import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;

import java.awt.AWTEvent;
import ij.IJ;
import ij.process.ImageProcessor;

public class PluginHSV_ implements PlugIn, DialogListener {	
	private GenericDialog gui;
	private ImagePlus image;
	private ImageProcessor processor, processorBkp;
	private double HMin, HMax, SMin, SMax, VMin, VMax;
	
	public void run(String arg) {		
		image = IJ.getImage();
		processor = image.getProcessor();
		processorBkp = image.duplicate().getProcessor();	
		if (image.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("In order to run this plugin, the image must be Type RGB");
		}
		else this.showGUI();
		IJ.run(arg);		
	}
	
	public void showGUI(){
		HMin = SMin = VMin = 0;
		HMax = SMax = VMax = 255;
		this.gui = new GenericDialog("Image HSV adjust");
		this.gui.addDialogListener(this);		
		gui.addMessage("HUE");
		gui.addSlider("H Min",0,255,HMin,1);		
		gui.addSlider("H Max",0,255,HMax,1);
		gui.addMessage("Saturation");
		gui.addSlider("S Min",0,255,SMin,1);		
		gui.addSlider("S Max",0,255,SMax,1);
		gui.addMessage("Value");
		gui.addSlider("V Min",0,255,VMin,1);		
		gui.addSlider("V Max",0,255,VMax,1);
		gui.showDialog();
		if (gui.wasOKed()) {			
			IJ.log("Saved");
		}
		if (gui.wasCanceled()) {
			image.setProcessor(processorBkp);		
			image.updateAndDraw();
			IJ.showMessage("No changes saved");			
		}
	}
		
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {		
		HMin = this.gui.getNextNumber();
		HMax = this.gui.getNextNumber();
		SMin = this.gui.getNextNumber();
		SMax = this.gui.getNextNumber();
		VMin = this.gui.getNextNumber();
		VMax = this.gui.getNextNumber();		
		changeImage();	
		return true;		
	}
	
	private int[] getHuePixel(int[] pixelValue) {		
		double R, G, B, H = 0d;	
		int MAX = getMaxPixelValue(pixelValue);
		R = pixelValue[0]; G = pixelValue[1]; B = pixelValue[2];
		
		if (MAX == R && G >= B)	H = (60 * ((G - B)/(HMax - HMin))) + 0;
		if (MAX == R && G < B) 	H = (60 * ((G - B)/(HMax - HMin))) + 360;
		if (MAX == G) H = (60 * ((B - R)/(HMax - HMin))) + 120;
		if (MAX == B) H = (60 * ((R - G)/(HMax - HMin))) + 240;
		
		return pixelToReturn(H, (HMax/255) * 360, (HMin/255) * 360, pixelValue)	;	
	}	
	
	private int[] getSaturatedPixel(int[] pixels) {
		double S;
		if (SMax ==0) S = 0;		
		else S = (SMax - SMin) / SMax;
		return pixelToReturn(S, (SMax/255) * 360, (SMin/255) * 360, pixels);	
	}
	
	private int[] pixelToReturn(double index, double high, double low, int[] pixels) {
		if (index >= low && index <= high) return pixels;
		else {
			int whitePixels[] = {255,255,255};
			return whitePixels;
		}
	}
	
	private void changeImage() {		
		int x, y, pixelValue[] = {0,0,0};
		double  V;
		for (x = 0; x < image.getWidth(); x++) {
			for (y = 0; y < image.getHeight(); y++) {
				pixelValue = processorBkp.getPixel(x, y, pixelValue);
				pixelValue = getHuePixel(pixelValue);
				pixelValue = getSaturatedPixel(pixelValue);
				V = VMax;
				pixelValue = pixelToReturn(V, (VMax/255) * 360, (VMin/255) * 360, pixelValue);				
				processor.putPixel(x, y, pixelValue);
			}
		}
		image.updateAndDraw();
	} 
	
	private int getMaxPixelValue(int[] pixels) {
		int MAX = 0;
		for (int i = 0; i < 3; i++)
			if (pixels[i] >= MAX) MAX = pixels[i];					
		return MAX;
	}	

}
	