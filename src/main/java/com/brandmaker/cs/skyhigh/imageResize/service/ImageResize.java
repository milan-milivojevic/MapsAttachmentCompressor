package com.brandmaker.cs.skyhigh.imageResize.service;

public interface ImageResize {
	
	void resizeScheduled();
	
	void runResize();

	public int getTotalNodes();
	
	public int getProcessedNodes();

}
