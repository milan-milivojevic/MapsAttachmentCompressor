package com.imageResize.service;

import java.io.IOException;

public interface ImageResize {
	
	void resizeScheduled();
	
	void runResize();

	public int getTotalNodes();
	
	public int getProcessedNodes();

}
