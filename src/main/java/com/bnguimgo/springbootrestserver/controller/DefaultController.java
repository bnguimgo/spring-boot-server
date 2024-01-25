package com.bnguimgo.springbootrestserver.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth/*")
public class DefaultController {
	
	private static Logger logger = LoggerFactory.getLogger(DefaultController.class);
	
	@GetMapping(value = "/")
	public ResponseEntity<String> pong() 
	{
		logger.info("Démarrage des services OK .....");
	    return new ResponseEntity<String>("Réponse du serveur: "+HttpStatus.OK.name(), HttpStatus.OK);
	}

	@GetMapping(value = "/hello")
	public String helloWorld()
	{
		logger.info("Appel du service  helloWorld OK .....");
		return "Hello World";
	}
	
	@GetMapping("/server")
	public	@ResponseBody String ping() {
		logger.info("Réponse du serveur sur /server : "+ HttpStatus.OK);
    	return "{\"status\":\"OK\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}";
	}

}