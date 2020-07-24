package com.romatre.controller;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.github.chen0040.fpm.AssocRuleMiner;
import com.github.chen0040.fpm.apriori.Apriori;
import com.github.chen0040.fpm.data.ItemSet;
import com.github.chen0040.fpm.data.ItemSets;
import com.github.chen0040.fpm.data.MetaData;
import com.github.chen0040.fpm.fpg.FPGrowth;
import com.romatre.model.Tweet;
import com.romatre.model.TweetMessage;
import com.romatre.service.SparkBatchConsumer;
import com.romatre.utils.MapUtil;

import ml.sentiment.model.SentimentResult;
import scala.Tuple2;

@Controller
public class SparkController implements ApplicationListener<SessionDisconnectEvent>{

	@Autowired
	SparkBatchConsumer sparkBatchConsumerService;
	
	private final Logger logger = LoggerFactory.getLogger(SparkController.class);

	@RequestMapping(value = "/compareTrends", method = RequestMethod.GET)
	public ModelAndView compareTrends(@RequestParam(name = "hashtag") String hashtag) {
		
		long start = System.currentTimeMillis();
		
		JavaPairRDD<Tweet, SentimentResult> actualTrend = sparkBatchConsumerService.hashtagAnalysis(hashtag);
		
		logger.error(String.format("#### -> Actual trend elaborated" ));
		
		JavaPairRDD<Tweet, SentimentResult> olderTrent = sparkBatchConsumerService.getOldestTrend(hashtag);
		
		logger.error(String.format("#### -> Older trend elaborated" ));
//		
//		List<Tuple2<SentimentResult, Double>> actualStats = sparkBatchConsumerService.getSentimentStats(actualTrend);
//		
//		logger.error(String.format("#### -> Actual trend stats elaborated" ));
//		
		List<Tuple2<SentimentResult, Double>> olderStats = sparkBatchConsumerService.getSentimentStats(olderTrent);
		
		logger.error(String.format("#### -> Older trend stats elaborated" ));
		
//		Map<String,String> actualMap = new HashMap<String,String>();
//		for(Tuple2<SentimentResult, Double> t : actualStats) {
//			String sentiment = t._1.getSentimentType();
//			
//			DecimalFormat df = new DecimalFormat("#.##");
//			String stat = df.format(t._2*100)+"%";
//
//			actualMap.put(sentiment, stat);
//		}
//		
		Map<String,String> olderMap = new HashMap<String,String>();
		for(Tuple2<SentimentResult, Double> t : olderStats) {
			String sentiment = t._1.getSentimentType();
			
			DecimalFormat df = new DecimalFormat("#.##");
			String stat = df.format(t._2*100)+"%";

			olderMap.put(sentiment, stat);
		}
		
		List<Map<String,String>> l = new LinkedList<Map<String,String>>();
//		l.add(actualMap);
		l.add(olderMap);
		
		ModelAndView model = new ModelAndView("filteredHashtag", "map", l);
		
		logger.error(String.format("#### -> Processing Time: " + ((System.currentTimeMillis() - start)/1000) + "s") );

		return model;
	}
	
	@RequestMapping(value = "/filteringdHashtagBatch", method = RequestMethod.GET)
	public ModelAndView hashtagFiltred(@RequestParam(name = "hashtag") String hashtag){

		try {
			long start = System.currentTimeMillis();
			 
			JavaPairRDD<Tweet, SentimentResult> analysis = sparkBatchConsumerService.hashtagAnalysis(hashtag);
			
			logger.error(String.format("#### -> Tweets with sentiment count: " + analysis.count() ));

			List<Tuple2<SentimentResult, Double>> stats = sparkBatchConsumerService.getSentimentStats(analysis);

			List<Tuple2<Tweet, SentimentResult>> tweet2sentiment = sparkBatchConsumerService.getTweet2Sentiment(analysis);
			
			List<Tuple2<String, Double>> locationStatistics = sparkBatchConsumerService.getLocationStatistics(analysis);
			
			Map<String,String> map1 = new HashMap<String,String>();
			for(Tuple2<Tweet, SentimentResult> r : tweet2sentiment) {
				String tweetText = r._1.getText();
				String sentiment = r._2.getSentimentType();
				
				map1.put(tweetText,sentiment);
//				logger.error(String.format("#### -> " + r._1 + "" + r._2.getSentimentType() ));
			}
			
			Map<String,String> map2 = new HashMap<String,String>();
			logger.error(String.format("#### SIZE STATS -> " + stats.size() ));
			
			for(Tuple2<SentimentResult, Double> t : stats) {
				String sentiment = t._1.getSentimentType();
				
				DecimalFormat df = new DecimalFormat("#.##");
				String stat = df.format(t._2*100)+"%";

				map2.put(sentiment, stat);
//				logger.error(String.format("#### -> " + sentiment + "" + stat ));
			}
			
			Map<String,String> map3 = new HashMap<String,String>();
			for(Tuple2<String, Double> z : locationStatistics) {
				String location = z._1;
		
				DecimalFormat df = new DecimalFormat("#.##");
				String stat = df.format(z._2*100)+"%";
				
				map3.put(location, stat);
//				logger.error(String.format("#### -> " + location + "" + stat ));
			}
			
			List<Map<String,String>> l = new LinkedList<Map<String,String>>();
			l.add(map1);
			l.add(map2);
			l.add(map3);
			
			ModelAndView model = new ModelAndView("filteredHashtag", "map", l);
			
			logger.error(String.format("#### -> Processing Time: " + ((System.currentTimeMillis() - start)/1000) + "s") );

			return model;
			
		} catch (Exception e) {
			System.out.println("Error, maybe i don't found tweet's with this hashtag");
			return new ModelAndView("index");
		}
		
	}
	
	@RequestMapping(value = "/FPM", method = RequestMethod.GET)
	public ModelAndView FPM(ItemSet itemSet){

		long start = System.currentTimeMillis();
		
		List<List<String>> dataset = sparkBatchConsumerService.preprocessing(sparkBatchConsumerService.top10FavCount());

		AssocRuleMiner method =  new FPGrowth();
		method.setMinSupportLevel(2);

		MetaData metaData = new MetaData(dataset);
		
		// obtain all frequent item sets with support level not below 2
		ItemSets frequent_item_sets = method.minePatterns(dataset, metaData.getUniqueItems());
		
		Map<String, Integer> set = new HashMap<String, Integer>();
		frequent_item_sets.stream().forEach(item -> {
			StringBuilder builder = new StringBuilder();
			List<String> hashtags = item.getItems();
			builder.append("{ ");
			for(String s : hashtags) {
				builder.append(", " + s);
			}
			builder.append(" }");
			set.put(builder.toString(), item.getSupport());
		});

		// obtain the max frequent item sets
		ItemSets r = method.findMaxPatterns(dataset, metaData.getUniqueItems());
		Map<String,Integer> max_frequent_item_sets = MapUtil.sortByValue(set);

		logger.error(String.format("#### -> Processing Time: " + ((System.currentTimeMillis() - start)/1000) + "s") );
		
		return new ModelAndView("FPM2", "max_frequent_item_sets", max_frequent_item_sets);	
	}

	@RequestMapping(value = "/Apriori", method = RequestMethod.GET)
	public ModelAndView Apriori(){

		long start = System.currentTimeMillis();
		
		List<List<String>> dataset = sparkBatchConsumerService.preprocessing(sparkBatchConsumerService.top10FavCount());

		AssocRuleMiner method = new Apriori();
		method.setMinSupportLevel(2);

		MetaData metaData = new MetaData(dataset);

		// obtain all frequent item sets with support level not below 2
		ItemSets frequent_item_sets = method.minePatterns(dataset, metaData.getUniqueItems());
		Map<String, Integer> set = new HashMap<String, Integer>();
		frequent_item_sets.stream().forEach(item -> {
			StringBuilder builder = new StringBuilder();
			List<String> hashtags = item.getItems();
			builder.append("{ ");
			for(String s : hashtags) {
				builder.append(", " + s);
			}
			builder.append(" }");
			set.put(builder.toString(), item.getSupport());
		});

		// obtain the max frequent item sets
		ItemSets r = method.findMaxPatterns(dataset, metaData.getUniqueItems());
		Map<String,Integer> max_frequent_item_sets = MapUtil.sortByValue(set);

		logger.error(String.format("#### -> Processing Time: " + ((System.currentTimeMillis() - start)/1000) + "s") );
		
		return new ModelAndView("FPM2", "max_frequent_item_sets", max_frequent_item_sets);	
	}
	
	@RequestMapping(value = "/topMentioned", method = RequestMethod.GET)
	public ModelAndView topMentioned(){
		
		
		List<Tuple2<Integer,String>> topMentioned = sparkBatchConsumerService.top20mentions();
		
		logger.error(String.format("#### SIZE TOP MENTIONED -> " + topMentioned.size() ));
		
		Map<Integer,String> mapTopMentioned = new HashMap<Integer,String>();
		
		for(Tuple2<Integer, String> t : topMentioned) {
			mapTopMentioned.put(t._1(), t._2());

		}
		List<Map<Integer,String>> l = new LinkedList<Map<Integer,String>>();
		l.add(mapTopMentioned);
		
		
		return new ModelAndView("filteredHashtag","map",l);
		
	}

	@MessageMapping("/fetch")
	@SendTo("/topic/tweets")
	public String tweets() throws Exception{
		return "Hello World!";
	}

	@Override
	public void onApplicationEvent(SessionDisconnectEvent event){
		logger.debug("Event SessionDisconnectEvent");
	}

	@Autowired
	private SimpMessagingTemplate template;

	// the business logic can call this to update all connected clients
	public void sendEventFromJava(TweetMessage tweetMessage){
		logger.debug("Sending event from java: "+tweetMessage);
		this.template.convertAndSend("/topic/tweets", tweetMessage);
	}

}
