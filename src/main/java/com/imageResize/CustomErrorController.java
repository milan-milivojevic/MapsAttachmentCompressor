package com.imageResize;

import org.springframework.boot.web.servlet.error.ErrorController;

public class CustomErrorController implements ErrorController{

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
