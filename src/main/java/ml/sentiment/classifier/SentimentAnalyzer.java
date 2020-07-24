package ml.sentiment.classifier;

import java.io.Serializable;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.romatre.app.MongoDatasetBatch;

import ml.sentiment.model.ValueSentiment;
import ml.sentiment.model.SentimentResult;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer implements Serializable{

	/*
	 * "Very negative" = 0 "Negative" = 1 "Neutral" = 2 "Positive" = 3
	 * "Very positive" = 4
	 */
	
	private static final long serialVersionUID = 12831921L;

	static Properties props;
	static StanfordCoreNLP pipeline;

	public void initialize() {
	// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and sentiment
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public SentimentResult getSentimentResult(String text) {

		SentimentResult sentimentResult = new SentimentResult();
		ValueSentiment sentimentClass = new ValueSentiment();

		if (text != null && text.length() > 0) {
			
			// run all Annotators on the text
			Annotation annotation = pipeline.process(text);

			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				// this is the parse tree of the current sentence
				Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
				SimpleMatrix sm = RNNCoreAnnotations.getPredictions(tree);
				String sentimentType = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

				sentimentClass.setVeryPositive((double)Math.round(sm.get(4) * 100d));
				sentimentClass.setPositive((double)Math.round(sm.get(3) * 100d));
				sentimentClass.setNeutral((double)Math.round(sm.get(2) * 100d));
				sentimentClass.setNegative((double)Math.round(sm.get(1) * 100d));
				sentimentClass.setVeryNegative((double)Math.round(sm.get(0) * 100d));
				
				sentimentResult.setSentimentScore(RNNCoreAnnotations.getPredictedClass(tree));
				sentimentResult.setSentimentType(sentimentType);
				sentimentResult.setSentimentClass(sentimentClass);
			}

		}


		return sentimentResult;
	}
	
}