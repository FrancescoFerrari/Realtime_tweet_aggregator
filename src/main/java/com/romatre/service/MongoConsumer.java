package com.romatre.service;

import java.io.IOException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.romatre.config.MongoConfiguration;
import com.romatre.model.User;

import org.springframework.social.twitter.api.*;

@Service
public class MongoConsumer {
	
	private final MongoClient mongoClient;
	private final Gson gson;
	
	public MongoConsumer() {
		this.mongoClient = new MongoClient( MongoConfiguration.SERVER , MongoConfiguration.PORT );
		this.gson = new Gson();
	}
//	private long start = System.currentTimeMillis();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @KafkaListener(topics = "${spring.kafka.template.topics}", groupId = "${spring.kafka.mongoconsumer.group-id}")
    public void consume(String message) throws IOException {
    
//        logger.error(String.format("#### -> MongoDB: Consumed message in: " + ((System.currentTimeMillis() - start)) + "ms") );
//        start = System.currentTimeMillis();
        
		MongoDatabase database = mongoClient.getDatabase(MongoConfiguration.DATABASE);
		MongoCollection<Document> collection = database.getCollection(MongoConfiguration.TABLE);
		
		Tweet tweet = this.gson.fromJson(message, Tweet.class);
		
		User user = new User(tweet.getUser().getId(), tweet.getUser().getName(), tweet.getUser().getScreenName(), tweet.getUser().getLocation(), tweet.getUser().getFollowersCount());
        
		String hashtags="";
		for(HashTagEntity hashtag : tweet.getEntities().getHashTags())
			hashtags+="#"+hashtag.getText();
		
		int retweet = tweet.getRetweetedStatus() != null ? tweet.getRetweetedStatus().getRetweetCount() : 0;
		com.romatre.model.Tweet tweetDocument = new com.romatre.model.Tweet(tweet.getId(), tweet.getText(), hashtags, tweet.getLanguageCode(), user, retweet, tweet.getFavoriteCount());
        
		try { 
			collection.insertOne(tweetDocument.getTweetAsDocument()); 
		} catch (Exception e) {
			System.out.println("Duplicate key");
		}
		
    }
}









