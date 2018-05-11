package holtprog

import holtslda._
import WikiParser._
import scala.io.Source
import java.io._
import java.lang.String
import breeze.linalg._
import breeze.optimize._
import breeze.stats.distributions._
import breeze.numerics._



object Main extends App {
	//val numberofdocs = WikiParser.httpreq()
	//println("numberofdocs" + numberofdocs);
	val numberofdocs = 2;
 	val LDA1 = new LDA(K=3,num_docs=numberofdocs, iterations=50);
 	LDA1.train();
 	//LDA1.display();
 //	LDA1.test()list
 

//Given an article, it will get all related articles and read them in. 
//Give a way to get all articles of a given type 
 
}

