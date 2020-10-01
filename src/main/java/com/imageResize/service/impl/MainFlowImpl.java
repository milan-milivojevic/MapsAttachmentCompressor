package com.imageResize.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.imageResize.service.ImageResize;
import com.imageResize.service.MainFlow;

@EnableScheduling
@Service
public class MainFlowImpl implements MainFlow {
	
	@Autowired
	ImageResize imageResize;

	@Override
	//@Scheduled(cron = "0 0 1 * * *")
	public void resizeScheduler() throws Exception {
		System.out.println("working");
		imageResize.runResize();
	};
}
