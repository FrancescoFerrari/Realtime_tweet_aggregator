package com.romatre.app;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.romatre.config.MongoConfiguration;
import com.romatre.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDatasetBatch {

	public static void uploadTweets() throws IOException{
		
		final Logger logger = LoggerFactory.getLogger(MongoDatasetBatch.class);
		MongoClient mongoClient = new MongoClient( MongoConfiguration.SERVER , MongoConfiguration.PORT );
		MongoDatabase database = mongoClient.getDatabase(MongoConfiguration.DATABASE);
		MongoCollection<Document> collection = database.getCollection("oldTweets");
		long start = System.currentTimeMillis();
		
//		for(int z = 21; z < 24 ; z++) {
//			for(int j = 10; j < 60 ; j++) {
		for(int z = 0; z < 0 ; z++) {
			for(int j = 0; j < 0 ; j++) {
				File file = new File("/Users/danielebarbato/Downloads/2018/10/01/" +z + "/" + j + ".json");
				Scanner scanner = new Scanner(file);

				while(scanner.hasNext()){
					try {
						String msg = scanner.nextLine();

						JSONObject json = new JSONObject(msg);
						JSONObject entities = (JSONObject) json.get("entities");
						JSONArray hashtagsJSON = (JSONArray) entities.get("hashtags");

						int lenHash = Math.toIntExact(hashtagsJSON.length());

						long id = (long) json.get("id");
						String hashtags="";

						for(int i=0; i < lenHash ; i++) {

							JSONObject textHashtag = (JSONObject) hashtagsJSON.getJSONObject(i);
							String hashtag = (String) textHashtag.get("text");

							if(hashtag != "" && hashtag != null) {
								hashtags +="#"+hashtag;
							}
						}
						String fullText = (String) json.get("text");
				
						JSONObject userJSON = (JSONObject) json.get("user");
						int userIDInt = (int) userJSON.get("id");
						long userID = Long.valueOf(userIDInt);
						String userName = (String) userJSON.get("name");
						String userScreenName = (String) userJSON.get("screen_name");
						String userLocation = (String) userJSON.get("location");
						int userFollower= (int) userJSON.get("followers_count");

						User user = new User(userID,userName,userScreenName, userLocation,userFollower);
						com.romatre.model.Tweet tweetDocument = new com.romatre.model.Tweet(id, fullText, hashtags, "N/D", user, 0, 0);
						collection.insertOne(tweetDocument.getTweetAsDocument());

					}
					catch (Exception e) {
//						e.printStackTrace();
					}
				}

				scanner.close();
			}
		}
		logger.error(String.format("#### -> Processing Time: " + ((System.currentTimeMillis() - start)/1000) + "s") );
		mongoClient.close();
	}


	public static void main(String[] args) {
		try {
			uploadTweets();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
