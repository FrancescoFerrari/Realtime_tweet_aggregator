package com.romatre.utils;

import org.bson.Document;

import com.romatre.model.Tweet;
import com.romatre.model.User;

public class Doc2Tweet {

	public static final Tweet convert(Document doc) {

		Document userdoc= (Document)doc.get("user");

		if(userdoc != null) {

			String location="";

			if(userdoc.get("location")!=null)
				location=userdoc.get("location").toString();

			User user = new User(
					Long.parseLong(userdoc.get("_id").toString()),
					userdoc.get("name").toString(), 
					userdoc.get("screenName").toString(), 
					location, 
					Integer.parseInt(userdoc.get("followersCount").toString()));

			return new Tweet(
					Long.parseLong(doc.get("_id").toString()), 
					doc.get("text").toString(), 
					doc.get("hashtag").toString(), 
					doc.get("lang").toString(), 
					user, 
					Integer.parseInt(doc.get("retweetCount").toString()), 
					Integer.parseInt(doc.get("favouriteCount").toString()));
		}

		else {
			return null;
		}
	}
}
