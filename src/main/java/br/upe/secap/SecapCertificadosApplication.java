package br.upe.secap;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ServletComponentScan
public class SecapCertificadosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecapCertificadosApplication.class, args);
	}
	
	@Bean
	VelocityEngine velocityEngine(){
	    VelocityEngine engine = new VelocityEngine();
	    engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
	    engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    engine.init();
	    return engine;
	}
}
