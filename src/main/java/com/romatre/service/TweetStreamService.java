package com.romatre.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.social.twitter.api.*;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TweetStreamService{
	private final Twitter twitter;
	private final Gson gson;
	
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value(value = "${spring.kafka.template.topics}")
	private List<String> topics;

	public TweetStreamService(Twitter twitter) {
		this.twitter = twitter;
		this.gson = new Gson();
	}

	public void run() {

		List<StreamListener> listeners = new ArrayList<StreamListener>();

		StreamListener streamListener = new StreamListener() {

			@Override
			public void onTweet(Tweet tweet) {
				
//				logger.info("Sending: User '{}', Tweeted : {}, from ; {}", tweet.getUser().getName() , tweet.getText(), tweet.getUser().getLocation());
				
				if(tweet.getLanguageCode().equals("en")) {
					
					String jsonStr = gson.toJson(tweet);
			        
//			        logger.error(String.format("#### -> " + jsonStr));
			        
					kafkaTemplate.send(topics.get(0), jsonStr);
					
				}
			}

			@Override
			public void onDelete(StreamDeleteEvent deleteEvent) {
				logger.debug("onDelete");
			}

			@Override
			public void onLimit(int numberOfLimitedTweets) {
				logger.debug("onLimit");
			}

			@Override
			public void onWarning(StreamWarningEvent warningEvent) {
				logger.debug("onLimit");
			}

		};

		listeners.add(streamListener);
		twitter.streamingOperations().sample(listeners);
	}
}
