package com.romatre.config;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;

public class SparkConfiguration {

	public static final String APPNAME = "SparkConsumer";
	public static final Duration BATCH_INTERVAL = Durations.seconds(15);

	private static SparkConf sparkConf;
	public static final SparkConf getSparkConf() {
		if(sparkConf == null) {
			SparkConf sparkConf = new SparkConf().
					setAppName(SparkConfiguration.APPNAME).
					setMaster("local[*]").
					set("spark.mongodb.input.uri", "mongodb://localhost:27017/tweets.tweets").
					set("spark.driver.allowMultipleContexts","true").
					set("spark.default.parallelism", "100");
			return sparkConf;
		}
		return sparkConf;
	}

	//	private static SparkConf sparkStreamingConf;
	public static final SparkConf getSparkStreamingConf() {
		//		if(sparkStreamingConf == null) {
		SparkConf sparkStreamingConf = new SparkConf().
				setAppName("context").
				setMaster("local[*]").
				set("spark.executor.memory","1g").
				set("spark.driver.allowMultipleContexts","true").
				set("spark.default.parallelism", "100");
		return sparkStreamingConf;
		//		}
		//		return sparkStreamingConf;
	}
}
