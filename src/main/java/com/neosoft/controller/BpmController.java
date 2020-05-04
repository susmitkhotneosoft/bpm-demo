package com.neosoft.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.neosoft.beans.UserBean;
import com.neosoft.service.IBpmService;

@RestController
public class BpmController {

	@Autowired
	private IBpmService bpmService;
	
	@PostMapping("/onboard")
	public String startOnboarding(@RequestBody UserBean userBean) {
		this.bpmService.onboard(userBean);
		return "Onboarding completed successfully";
	}
}
