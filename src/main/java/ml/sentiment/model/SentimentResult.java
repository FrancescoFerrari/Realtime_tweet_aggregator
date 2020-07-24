package ml.sentiment.model;

import java.io.Serializable;

public class SentimentResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12138912L;
	double sentimentScore;
	String sentimentType;
	ValueSentiment sentimentClass;

	public double getSentiment() {
		return sentimentScore;
	}

	public double getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(double sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	public String getSentimentType() {
		return sentimentType;
	}

	public void setSentimentType(String sentimentType) {
		this.sentimentType = sentimentType;
	}

	public ValueSentiment getSentimentClass() {
		return sentimentClass;
	}

	public void setSentimentClass(ValueSentiment sentimentClass) {
		this.sentimentClass = sentimentClass;
	}

	@Override
	public int hashCode() {
		return (int) (this.sentimentScore+this.sentimentType.hashCode());
	}
	
	@Override
	public boolean equals(Object o) {
		SentimentResult other = (SentimentResult)o;
		if(this.sentimentType.equals(other.sentimentType))
			return true;
		return false;
	}
	

}