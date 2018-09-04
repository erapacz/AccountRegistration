package com.capgemini.registration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Controller
public class CredentialsController implements WebMvcConfigurer {
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/terms").setViewName("terms");
	}
	
	@GetMapping("/credentials")
	public String getCredentials() {
		return "credentials";
	}
	
	@PostMapping("/credentials")
	public String verify() {	
		return "redirect:/terms";
	}

}
