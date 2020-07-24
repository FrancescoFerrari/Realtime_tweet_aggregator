package com.romatre.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.romatre.config.SparkConfiguration;
import com.romatre.controller.SparkController;
import com.romatre.model.TweetMessage;
import com.romatre.utils.HashTagsFilter;

import scala.Tuple2;

@Service
public class SparkConsumer {

	private SparkConf sparkConf;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value(value = "${spring.kafka.consumer.bootstrap-servers}")
	private String bootstrapServers;

	@Value(value = "${spring.kafka.sparkconsumer.group-id}")
	private String groudID;
	
	@Value(value = "${spring.kafka.consumer.auto-offset-reset}")
	private String valueOffsetReset;
	
	@Value(value = "${spring.kafka.consumer.client-id-config}")
	private String clientIdConfig;
	
	private final SparkController sparkController;
	public JavaStreamingContext streamingContext;

	public SparkConsumer(SparkController sparkController) {
		this.sparkController = sparkController;
		this.sparkConf = SparkConfiguration.getSparkStreamingConf();
		
		this.streamingContext = new JavaStreamingContext(this.sparkConf, SparkConfiguration.BATCH_INTERVAL);
	}

	private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groudID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, valueOffsetReset);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientIdConfig);

		return props;
	}

	public void run() {
		
		JavaInputDStream<ConsumerRecord<Long, String>> stream =
				KafkaUtils.createDirectStream(
						streamingContext,
						LocationStrategies.PreferConsistent(),
						ConsumerStrategies.<Long, String>Subscribe(Arrays.asList("tweets"), consumerConfigs())
						);

		JavaDStream<String> lines = stream.map(stringStringConsumerRecord -> stringStringConsumerRecord.value());
		
        // Count the tweets and print
        lines
                .count()
                .map(cnt -> "Popular hash tags in last 60 seconds (" + cnt + " total tweets):")
                .print();

        lines.flatMap(text -> HashTagsFilter.hashTagsFromTweet(text))
                .mapToPair(hashTag -> new Tuple2<>(hashTag, 1))
                .reduceByKey((a, b) -> Integer.sum(a, b))

                .mapToPair(stringIntegerTuple2 -> stringIntegerTuple2.swap())
                .foreachRDD(rrdd -> {
                	logger.error("---------------------------------------------------------------");
                    List<Tuple2<Integer, String>> sorted;
                    JavaPairRDD<Integer, String> counts = rrdd.sortByKey(false);
                    sorted = counts.collect();
                    sorted.forEach( record -> {
                    	logger.info(String.format(" %s (%d)", record._2, record._1));
                    	this.sparkController.sendEventFromJava(new TweetMessage(String.format(" %s (%d)", record._2, record._1)));
                    });
                });

        // Start the computation

		streamingContext.start();

		try {
			streamingContext.awaitTermination();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
