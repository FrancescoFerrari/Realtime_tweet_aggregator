package com.romatre.service;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import com.romatre.config.SparkConfiguration;
import com.romatre.model.Tweet;
import com.romatre.utils.Doc2Tweet;
import com.romatre.utils.HashTagsFilter;

import ml.sentiment.classifier.SentimentAnalyzer;
import ml.sentiment.model.SentimentResult;
import scala.Tuple2;

@Service
public class SparkBatchConsumer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 19381239L;
	private final SentimentAnalyzer sentimentAnalyzer;

//	private SparkConf sparkConf;

	private final Logger logger = LoggerFactory.getLogger(getClass());

//	private ReadConfig tweetsReadConfig;

	public SparkBatchConsumer() {

//		this.sparkConf = SparkConfiguration.getSparkConf();
//		setReadConfig(SparkConfiguration.getSparkConf());

		this.sentimentAnalyzer = new SentimentAnalyzer();
		this.sentimentAnalyzer.initialize();
	}

	private ReadConfig setReadConfig(SparkConf sparkConf) {
		Map<String, String> tweetsReadOverrides = new HashMap<>();
		tweetsReadOverrides.put("collection", "tweets");
		tweetsReadOverrides.put("readPreference.name", "secondaryPreferred");
		return ReadConfig.create(sparkConf).withOptions(tweetsReadOverrides);
	}

	public JavaPairRDD<Tweet, SentimentResult> getOldestTrend(String hashtag) {
		SparkConf conf = SparkConfiguration.getSparkConf();
		JavaSparkContext sparkContext = new JavaSparkContext(conf);
		
		Map<String, String> olderTweetsReadOverrides = new HashMap<>();
		olderTweetsReadOverrides.put("collection", "oldTweets");
		olderTweetsReadOverrides.put("readPreference.name", "secondaryPreferred");
		ReadConfig olderTweetReadConfig = ReadConfig.create(conf).withOptions(olderTweetsReadOverrides);
		
		JavaMongoRDD<Document> tweetsRdd = MongoSpark.load(sparkContext, olderTweetReadConfig);

		logger.error(String.format("#### -> OlderTweetsRdd count: " + tweetsRdd.count()));

		JavaRDD<Document> docTweetsJavaRdd;

		docTweetsJavaRdd = tweetsRdd.filter(doc -> HashTagsFilter.containsHashTags(doc.getString("hashtag"), hashtag));

		logger.error(String.format("#### -> OlderTweetsFiltered count: " + docTweetsJavaRdd.count()));

		JavaRDD<Tweet> tweetsJavaRdd = docTweetsJavaRdd.map( doc -> Doc2Tweet.convert(doc));

		logger.error(String.format("#### -> Data from Mongo fetched"));

		return tweetsJavaRdd.mapToPair( 
				tweet -> new Tuple2<Tweet,SentimentResult>(
						tweet, this.sentimentAnalyzer.getSentimentResult(tweet.getText())));
	}
	
	public JavaPairRDD<Tweet, SentimentResult> hashtagAnalysis(String hashtag) {
		SparkConf conf = SparkConfiguration.getSparkConf();
		JavaSparkContext sparkContext = new JavaSparkContext(conf);
		JavaMongoRDD<Document> tweetsRdd = MongoSpark.load(sparkContext, setReadConfig(conf));

		logger.error(String.format("#### -> TweetsRdd count: " + tweetsRdd.count()));

		JavaRDD<Document> docTweetsJavaRdd;

		docTweetsJavaRdd = tweetsRdd.filter(doc -> HashTagsFilter.containsHashTags(doc.getString("hashtag"), hashtag));

		logger.error(String.format("#### -> TweetsFiltered count: " + docTweetsJavaRdd.count()));

		JavaRDD<Tweet> tweetsJavaRdd = docTweetsJavaRdd.map( doc -> Doc2Tweet.convert(doc));

		logger.error(String.format("#### -> Data from Mongo fetched"));

		return tweetsJavaRdd.mapToPair( 
				tweet -> new Tuple2<Tweet,SentimentResult>(
						tweet, this.sentimentAnalyzer.getSentimentResult(tweet.getText())));
	}

	public List<Tuple2<SentimentResult, Double>> getSentimentStats(JavaPairRDD<Tweet, SentimentResult> tweet2sentiment) {

		double tot = tweet2sentiment.count();

		logger.error(String.format("#### -> TWEET2SENTIMENT COUNT: " + tot));
		
		JavaPairRDD<SentimentResult, Integer> sentiment2String = tweet2sentiment.mapToPair(
				r -> new Tuple2<SentimentResult,Integer>(r._2,1)).reduceByKey((a,b) -> a + b); 

		logger.error(String.format("#### -> sentiment2String COUNT: " + sentiment2String.count()));
		
		JavaRDD<Tuple2<SentimentResult, Double>> sentiment2Tot = sentiment2String.map( r -> new Tuple2<SentimentResult, Double>(r._1,  (r._2/tot)));

		logger.error(String.format("#### -> sentiment2Tot COUNT: " + sentiment2Tot.count()));
		
		logger.error(String.format("#### -> Stats completed"));

		return sentiment2Tot.collect();
	}

	public List<Tuple2<Tweet, SentimentResult>> getTweet2Sentiment(JavaPairRDD<Tweet, SentimentResult> tweet2sentiment) {

		logger.error(String.format("#### -> Tweets to sentiments associated"));

		return tweet2sentiment.collect();
	}

	// For FPM and Apriori
	public List<Tuple2<Integer, Tweet>> top10FavCount() {
		SparkConf conf = SparkConfiguration.getSparkConf();
		JavaSparkContext sparkContext = new JavaSparkContext(conf);
		JavaMongoRDD<Document> tweetsRdd = MongoSpark.load(sparkContext, setReadConfig(conf));

		JavaRDD<Document> docTweetsJavaRdd = tweetsRdd.filter(doc -> doc.getInteger("retweetCount") != 0 && !doc.getString("hashtag").equals(""));
		
		logger.error(String.format("#### -> TweetsJavaRdd count: " + docTweetsJavaRdd.count()));

		JavaRDD<Tweet> tweetsJavaRdd = docTweetsJavaRdd.map(doc -> Doc2Tweet.convert(doc));

		JavaPairRDD<Integer,Tweet> tweets2order= tweetsJavaRdd
				.mapToPair(	tweet -> 
				new Tuple2<Integer,Tweet>(
						tweet.getRetweetCount(),tweet));

		JavaPairRDD<Integer, Tweet> tweetsOrdered = tweets2order.sortByKey(false);

		logger.error(String.format("#### -> Data from Mongo fetched"));
		logger.error(String.format("#### -> ORDERED: %s", tweetsOrdered.take(10)));
		return tweetsOrdered.take(10);
	}

	public List<List<String>> preprocessing(List<Tuple2<Integer, Tweet>> top20FavCount){
		List<List<String>> dataset= new ArrayList<>();

		for(Tuple2<Integer, Tweet> tupla : top20FavCount) {
			if( tupla._2.getHashtag()!=null) {
				List<String> hashtags=Arrays.asList(tupla._2.getHashtag().split("#"));
				dataset.add(hashtags);
			}
		}

		return dataset;
	}

	public List<Tuple2<String, Double>> getLocationStatistics(JavaPairRDD<Tweet, SentimentResult> tweet2sentiment) {

		double tot = tweet2sentiment.count();
		JavaPairRDD<String, Integer> locationStat;

		locationStat = tweet2sentiment.mapToPair(
				r -> new Tuple2<String,Integer>(r._1.getUser().getLocation(),1)).reduceByKey((a,b) -> a + b);

		JavaRDD<Tuple2<String, Double>> statTotal = locationStat.map( r -> new Tuple2<String, Double>(r._1, (r._2/tot)));

		logger.info(String.format("#### -> Location Stats completed"));

		return statTotal.collect();
	}
	

	//most popular Hashtags Batch
		public JavaRDD<String> TopHashtag() {
			SparkConf conf = SparkConfiguration.getSparkConf();
			JavaSparkContext sparkContext = new JavaSparkContext(conf);
			JavaMongoRDD<Document> tweetsRdd = MongoSpark.load(sparkContext, setReadConfig(conf));

			JavaRDD<Tweet> tweets = tweetsRdd.map(doc -> Doc2Tweet.convert(doc));
			JavaRDD<String> hashtag =
					tweets.flatMap(tweet -> (Iterator<String>)Arrays.asList(tweet.getText().split(" ")))
					.filter(word -> word.startsWith("#") && word.length() > 1);
			System.out.println("mentions.count() " + hashtag.count());
			return hashtag;
		}


		//Count how many times an hashtag is used 

		public JavaPairRDD<String, Integer> countHashtag() {
			JavaRDD<String> hashtag = TopHashtag();
			JavaPairRDD<String, Integer> hashtagCount =
					hashtag.mapToPair(hash -> new Tuple2<>(hash, 1))
					.reduceByKey((x, y) -> x + y);
			return hashtagCount;
		}


		public List<Tuple2<Integer, String>> top20Hashtags() {
			JavaPairRDD<String, Integer> counts = countHashtag();
			List<Tuple2<Integer, String>> mostMentioned =
					counts.mapToPair(pair -> new Tuple2<>(pair._2(), pair._1()))
					.sortByKey(false)
					.take(20);
			return mostMentioned;
		}


		//to see the mention of a tweet
		public JavaPairRDD<String, Integer> mentionOnTweet() {
			SparkConf conf = SparkConfiguration.getSparkConf();
			JavaSparkContext sparkContext = new JavaSparkContext(conf);
			JavaMongoRDD<Document> tweetsRdd = MongoSpark.load(sparkContext, setReadConfig(conf));

			JavaRDD<Tweet> tweets = tweetsRdd.map(doc -> Doc2Tweet.convert(doc));
			tweets = tweets.filter(t -> t != null);
			JavaPairRDD<String, Integer> mentions =
					tweets.flatMap(tweet -> Arrays.asList(tweet.getText().split(" ")).iterator())
					.filter(word -> word.startsWith("@") && word.length() > 1).
					mapToPair(mention -> new Tuple2<>(mention, 1)).reduceByKey((x, y) -> x + y);
			System.out.println("mentions.count() " + mentions.count());
			return mentions;
		}

		//Count how many times a person is mentioned 
		public JavaPairRDD<String, Integer> countMentions() {
			JavaPairRDD<String, Integer> mentions = mentionOnTweet();
//			JavaPairRDD<String, Integer> mentionCount =
//					mentions.mapToPair(mention -> new Tuple2<>(mention, 1))
//					.reduceByKey((x, y) -> x + y);
			return mentions;
		}




		public List<Tuple2<Integer, String>> top20mentions() {
			JavaPairRDD<String, Integer> counts = countMentions();
			List<Tuple2<Integer, String>> mostMentioned =
					counts.mapToPair(pair -> new Tuple2<>(pair._2(), pair._1()))
					.sortByKey(false)
					.take(20);
			return mostMentioned;
		}
	
}








