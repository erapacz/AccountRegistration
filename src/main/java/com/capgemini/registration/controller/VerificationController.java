package com.capgemini.registration.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.capgemini.registration.model.RegistrationDetails;
import com.capgemini.registration.model.RegistrationLog;
import com.capgemini.registration.model.VerificationDetails;
import com.capgemini.registration.service.RegDetailsServiceImpl;
import com.capgemini.registration.service.RegLogServiceImpl;

@Controller
public class VerificationController {

	
	@Autowired
	RegLogServiceImpl regLogServiceImpl;
	
	@Autowired
	RegDetailsServiceImpl regDetServiceImpl;
	
	@GetMapping("/verification")
	public String getVerification(Model model) {
		model.addAttribute("client", new VerificationDetails());
		return "verification";
	}
	
	@PostMapping("/verification")
	public String verify(@Valid @ModelAttribute("client") VerificationDetails client, BindingResult bindingResult, Model model ) {	
		model.addAttribute("client", client);
		
		if(bindingResult.hasErrors()) {
			return "verification";
		}
		String accNum = client.getAccNum();
		
		String url = "http://localhost:8082/verify/account/"+accNum;
		RestTemplate restTemplate = new RestTemplate();
		VerificationDetails server = restTemplate.getForObject(url, VerificationDetails.class);
	     
	    System.out.println("Server: "+server.toString());
	    System.out.println("Client: "+client.toString());
	    
	    boolean invalid = false;
	    String incorrect = "";
	    RegistrationDetails registration;
	    try {
	    	registration = regDetServiceImpl.getRegDetailsByCustId(server.getCustomerId());
	    } catch(Exception e) {
	    	registration = new RegistrationDetails();
	    	registration.setCustomerId(server.getCustomerId());
	    }
	    RegistrationLog log = new RegistrationLog();
	 
	    if(server.getSsn() == null ) {
	    	System.out.println("Invalid Account Number");
	    	model.addAttribute("accNumError", "Invalid Account Number");
	    	invalid = true;
	    	incorrect = "ACCNUM";
	    }
	    else {
		    if(!client.getSsn().equals(server.getSsn())) {
		    	System.out.println("Invalid SSN");
		    	model.addAttribute("ssnError", "Invalid SSN");
		    	invalid = true;
		    	incorrect += "SSN ";
		    }
		    if(!client.getDob().equals(server.getDob())) {
		       	System.out.println("Invalid DOB");
		       	model.addAttribute("dobError", "Invalid DOB");
		       	invalid = true;
		       	incorrect += "DOB ";
			}
		    if(!client.getMaiden().equals(server.getMaiden())){
				System.out.println("Invalid Maiden Name");
				model.addAttribute("maidenError", "Invalid Maiden Name");
				invalid = true;
				incorrect += "MAIDEN ";
			}
	    }
	    
	    if(invalid == true) {
	    	log.setAttempt(incorrect);
	    	log.setStatus("fail");
	    	registration.setAttempts(registration.getAttempts() + 1);
	    	regDetServiceImpl.saveRegDetails(registration);
	    	log.setRegistrationId(registration.getRegistrationId());
	    	regLogServiceImpl.saveRegLog(log);
	    	if(registration.getAttempts() >= 6)
	    		return "locked";
	    	return "verification";
	    }
	    
	    log.setStatus("pass");
	    regDetServiceImpl.saveRegDetails(registration);
	    log.setRegistrationId(registration.getRegistrationId());
	    regLogServiceImpl.saveRegLog(log);
	    model.addAttribute("credentials", new RegistrationDetails());
	    return "credentials";
	}
}