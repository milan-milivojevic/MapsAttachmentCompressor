package com.imageResize.service;

import java.io.IOException;

public interface ImageResize {
	
	void resizeScheduled();
	
	void resizeTest() throws IOException, Exception;

	public int getTotalNodes();
	
	public int getProcessedNodes();

}
