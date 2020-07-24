package com.romatre.model;

import java.io.Serializable;

import org.bson.Document;

public class Tweet implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 13934129L;
	private long id;
    private String text;
    private String hashtag;
    private String lang;
    private User user;
    private int retweetCount;
    private int favoriteCount;

    public Tweet(long id, String text, String hashtag, String lang, User user, int retweetCount, int favoriteCount) {
        this.id = id;
        this.text = text == null ? "" : text;
        this.hashtag = hashtag == null ? "" : hashtag;
        this.lang = lang == null ? "" : lang;
        this.user = user;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
	public String getHashtag() {
		return hashtag;
	}
	
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
    
    @Override
    public boolean equals(Object o) {
    	Tweet other = (Tweet)o;
    	
    	if(this.text.equals(other.getText())) {
    		return true;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
    	return (int) (this.text.hashCode()+this.id);
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", text='" + text + '\'' +
                 ", hashtag='" + hashtag + '\'' +
                ", lang='" + lang + '\'' +
                ", user=" + user +
                ", retweetCount=" + retweetCount +
                ", favoriteCount=" + favoriteCount +
                '}';
    }
    
    public Document getTweetAsDocument() {
    	
    	Document tweetDoc = new Document("_id", this.id)
    			.append("text", this.text)
    			.append("hashtag",this.hashtag)
    			.append("lang", this.lang)
    			.append("retweetCount", this.retweetCount)
    			.append("favouriteCount", this.favoriteCount)
    			.append("user", new Document(
    						"_id", this.user.getId())
    					.append("name", this.user.getName())
    					.append("followersCount", this.user.getFollowersCount())
    					.append("location", this.user.getLocation())
    					.append("screenName", this.user.getScreenName())
    			);
    	;
    	
    	return tweetDoc;
    }
}