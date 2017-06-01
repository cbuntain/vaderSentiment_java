# Java-based implementation of VADER Sentiment Analysis

This repository contains a Java-based implementation of Hutto and Gilbert's Python-based VADER: A Parsimonious Rule-based Model for Sentiment Analysis of Social Media Text, from:

	VADER: A Parsimonious Rule-based Model for Sentiment Analysis of Social Media Text
	(by C.J. Hutto and Eric Gilbert)
	Eighth International Conference on Weblogs and Social Media (ICWSM-14). Ann Arbor, MI, June 2014.

## Original Python Version

Hutto and Gilbert's version can be found on GitHub [here](https://github.com/cjhutto/vaderSentiment)

## Usage

The SentimentIntensityAnalyzer class has a static method, getDefaultAnalyzer(), that returns an instance of SentimentIntensityAnalyzer using the default VADER lexicon.
You can use this object and its polarity_scores() method to get the polarity score for a given string. 

__NOTE:__ This code does not provide sentence identification or any other natural language utilities.

The polarity_scores() function returns a Java Map object with the positive, negative, neutral, and compound polarity scores.

### Scala Usage

The following example shows how to use this package in Scala, which is my primary language right now.

	import edu.umd.hcil.vader.SentimentIntensityAnalyzer

	val analyzer = SentimentIntensityAnalyzer.getDefaultAnalyzer

	analyzer.polarity_scores("VADER is smart, handsome, and funny.").get("compound")

Here's a brief example on using this code to get the compound value for a file full of tweets

	val source = scala.io.Source.fromFile("tweets.txt")
	for ( line <- source.getLines ) {
	    val score = analyzer.polarity_scores(line).get("compound")
	    
	    System.out.println(String.format("%s\t%s\n", line, score.toString))
	}

## Acks

I also relied heavily on Chris Humphreys's [p2j](https://github.com/chrishumphreys/p2j) Python-to-Java translation toolkit to handle a lot of the mundane translations.