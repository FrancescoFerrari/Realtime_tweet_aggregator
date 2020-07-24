package com.romatre.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import com.romatre.service.SparkConsumer;
import com.romatre.service.TweetStreamService;

@SpringBootApplication(scanBasePackages={"com.romatre.controller","com.romatre.service","com.romatre.config"})
public class KafkaTwitterAppConsumer extends SpringBootServletInitializer implements CommandLineRunner{
	
	@Autowired
	SparkConsumer sparkConsumerService;
	
	@Autowired
	TweetStreamService tweetStreamService;
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(KafkaTwitterAppConsumer.class);
    }
    
	public static void main(String[] args) {
		SpringApplication.run(KafkaTwitterAppConsumer.class, args);
	}
	
	@Override
	public void run(String... strings) throws Exception {
		tweetStreamService.run();
		sparkConsumerService.run();

	}

}
