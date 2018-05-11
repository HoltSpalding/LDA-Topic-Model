package holtslda

import scala.io.Source
import java.io._
import java.lang.String
import breeze.linalg._
import breeze.optimize._
import breeze.stats.distributions._
import breeze.numerics._
import util.control.Breaks._
import scala.collection.mutable.ListBuffer
import collection.mutable.HashMap
import breeze.plot._
import pb._


//TODO CHANGE EVERYTHING TO DOUBLES

//Num topics is K, 
class LDA(private val K: Int = 2, private val num_docs: Int = 5, 
		  private val iterations: Int = 3, private val alpha: Int = 1,
			private val eta: Double = 0.001) {
	var word_topic_count_mat = DenseMatrix.zeros[Int](K,9999);
	var doc_topic_count_mat = DenseMatrix.zeros[Int](num_docs,K);
	var token_topic_assignment_list = DenseMatrix.zeros[Int](num_docs,9999); 
	var doc_term_mat = DenseMatrix.zeros[Int](num_docs,9999); 
	var hashtable = new HashMap[String,Int]()  { override def default(key:String) = -1 }
	val randgen = new scala.util.Random
	var theta = DenseMatrix.zeros[Double](num_docs,K);
	var phi = DenseMatrix.zeros[Double](K,9999);
	val docslist = new ListBuffer[String]()

	//the max is 10,0000
	//recurse through files in specified folder
	def parsefiles() : Unit = println("Done parsing files");  {
		//println("hello")
		try{
			var resourcesDir = getListOfFiles("src/main/resources");
			for ((filename,count) <- resourcesDir.zipWithIndex) {
			//	println("filenames " + filename)
				//val stream : InputStream = getClass.getResourceAsStream("/"+filename);
				val fileiter : Iterator[String] = scala.io.Source.fromFile("src/main/resources/" + filename).getLines()
				println(fileiter)
				docslist += filename;
				var whilecount = 0;
				while(fileiter.hasNext && whilecount < 9990) {
			
					//println("here")
	        		var line = fileiter.next();
	        		var splitLine = (line).split(" ");
	        		//println(splitLine.size)
	        		for ((x,countword) <- splitLine.zipWithIndex) {
						//println(x + " " + hashcode(x));
						//var y = sample
						println(x)
						breakable {

							if (countword > 9990) {
								break
							}
							else {

								if (hashtable.size > 9990) {
									println("hashmap full")
									System.exit(1);
								}
								var randKsample = randgen.nextInt(_: Int);
								//randKsample += 1;
								var y = randKsample(2) + 1; 
								//println("y " + y);
								word_topic_count_mat(y-1, hashcode(x)) += 1;
								doc_topic_count_mat(count,y-1) += 1;
								//println("wc" + whilecount)
								//println("y:" + y + " count:" + count + " countword:" + countword);
								token_topic_assignment_list(count,countword) = y;

			        			doc_term_mat(count,countword) = hashcode(x);
			        			whilecount += 1;

			        			//println("end")
		        			}
	        			}
	        		}
	        		//TODO correct line and get all lines so u know how many workds there are
	        		/*
	        		println(word_topic_count_mat);
	        		println(doc_topic_count_mat);
	        		println(line);
	        		*/
	        	}
			}
			//println(token_topic_assignment_list);
		}
		catch {
			case _: Throwable => println("Files not parsable, please ensure your source documents are in the resources folder in plain text format");
		}
	}


	def learnparams() : Unit = println("Done learning"); {
		/*
		println("ttal " + token_topic_assignment_list);
		println("wtcm " + word_topic_count_mat);
		println("dtcm " + doc_topic_count_mat);
		println("dt " + doc_term_mat);
		*/
		var pbcount = iterations * num_docs * token_topic_assignment_list.cols
    	var pb = new ProgressBar(pbcount)
    	pb.showSpeed = true
   
		for(i <- 1 to iterations) {
			 
    
			/*
			println("Iteration: " + i);
			println("ttal " + token_topic_assignment_list);
		println("wtcm " + word_topic_count_mat);
		println("dtcm " + doc_topic_count_mat);
		println("dt " + doc_term_mat);
		*/

			for(d <- 0 until num_docs) { //rows in each token_topic_assignment_list
				//println("document: " + d);
				for(w <- 0 until token_topic_assignment_list.cols) {
				//	println("token: " + w);
					pb += 1
					breakable {
						if (token_topic_assignment_list(d,w) == 0) {
							break
						} 
						else {
							var topic0 = token_topic_assignment_list(d,w);
							var wid = doc_term_mat(d,w);

							doc_topic_count_mat(d,topic0-1) = doc_topic_count_mat(d,topic0-1) - 1;
							word_topic_count_mat(topic0-1, wid) = word_topic_count_mat(topic0-1, wid) - 1;

							var denom_a = convert(sum(doc_topic_count_mat(d,::)),Double) + K * alpha;
							var denom_b = convert(sum(word_topic_count_mat(*,::)),Double) + (word_topic_count_mat.rows * eta);
							//println("da " +  denom_a);
							//println("db " + denom_b);
							
							var p1 = (convert(word_topic_count_mat(::,wid),Double) + eta) 
							//println("p1" + p1);
							var convdt = convert(doc_topic_count_mat,Double);
							var wat = (convdt(d,::) + alpha.toDouble);
							var p2 = (denom_b *:* wat.t); 
							//println("p2 " + p2);
							var p3 = p1 / p2/ denom_a;
							//println("p3 " + p3)
							
							var p_z = p3/sum(p3);
							//println("pz: " + p_z);
							
							//TODO why .draw iterates to next mem loc, try dirichlet.scala
							var mult = new Multinomial(p_z);
							var topic1 = mult.draw;//sample based on multinomial
							
							token_topic_assignment_list(d,w) = topic1 + 1;
							doc_topic_count_mat(d,topic1.toInt) = doc_topic_count_mat(d,topic1.toInt) + 1;
							word_topic_count_mat(topic1.toInt,wid) = word_topic_count_mat(topic1.toInt,wid) + 1;

							if(topic0 != topic1) {
								//println("doc:"  + d + " token:" + w + " topic:" + topic0 + "=>" + token_topic_assignment_list(d,w));
							}
							
						}
					}
					
				}
			}
		}
		
		doc_topic_count_mat = doc_topic_count_mat + alpha;
		var rows = convert(sum(doc_topic_count_mat(*,::)),Double);
		var rowsums = DenseMatrix.zeros[Double](num_docs,K);
	//	println("rows" + rows);
	//	println("rowsums" + rowsums);
	
		for(i <- 0 until rowsums.rows) {
			for(j <- 0 until rowsums.cols) {
				rowsums(i,j) = rows(i);
			}
		}
			//	println("rowsums" + rowsums);

	
		theta = convert(doc_topic_count_mat,Double) / rowsums;

		var convwt = convert(word_topic_count_mat,Double) + eta;
		var rowswt = sum(convwt(*,::));
		var rowsumswt = DenseMatrix.zeros[Double](K,9999);
		for(i <- 0 until rowsumswt.rows) {
			for(j <- 0 until rowsumswt.cols) {
				rowsumswt(i,j) = rowswt(i);
			}
		}
		phi = convwt / rowsumswt;

		
	}


	def train() : Unit = println("Done training"); {
		parsefiles();
		//println(token_topic_assignment_list);
		learnparams();
		println("Documents can be partitioned into these 2 topics:");
		println("Topic 1		Topic 2");
		for (i <- 0 until docslist.length) {
			println(docslist(i) + " " + theta(i,::))
		}
		for (i <-0 until phi.rows) {
			var t = Array[Double](0.0,0.0,0.0);
			println(t(0),t(1),t(2))
			for (j <- 0 until phi.cols) {
				breakable {
					if (phi(i,j) > t(0)) {
						t(0) = j.toDouble;
						break;
					}
					if (phi(i,j) > t(1)) {
						t(1) = j.toDouble;
						break;
					}
					if (phi(i,j) > t(2)) {
						t(2) = j.toDouble;
						break;
					}
				}
				
			}
			var tops = Array[String]("","","");
			println(tops(0),tops(1),tops(2))

			for ((k,v) <- hashtable) {
				if (t(0) == v) { tops(0) = k}
				if (t(1) == v) { tops(1) = k}
				if (t(2) == v) { tops(2) = k}
			}
			println("Topic " + i + ": " + tops(0) + " " + tops(1) + " " + tops(2) + " ")
		}
		//println(theta);
		println(phi);
		var t = Array[Double](0.0,0.0,0.0);
		/*t(0) = argmax(phi(0,::));
		t(1) = argmax(phi(1,::));
		t(2) = argmax(phi(2,::));*/ //this produces and and the
		t(0) = argmax(phi(::,0));
		t(1) = argmax(phi(::,1));
		t(2) = argmax(phi(::,2));
		var tops = Array[String]("","","");
			
			
			for ((k,v) <- hashtable) {
				if (t(0) == v) { tops(0) = k}
				if (t(1) == v) { tops(1) = k}
				if (t(2) == v) { tops(2) = k}
			}
			println(tops(0),tops(1),tops(2))

	} 

	def hashcode(token: String): Int = {
		if (hashtable.contains(token)) {
			hashtable(token)
		}
		else {
			hashtable.put(token, hashtable.size);
			hashtable.size - 1
		}
	}

	def getListOfFiles(dir: String):List[String] = {
	    val d = new File(dir)
	    if (d.exists && d.isDirectory) {
	        d.listFiles.filter(_.isFile).map(_.getName).toList
	    } else {
	        List[String]()
	    }
	}


	def display(tokens_per_topic: Int = 3): Unit = println("Displaying..."); {
		//display how many docs in each topic, pie chart
		/*val f2 = Figure()
		f2.subplot(0) += pie(phi);
		f2.saveas("image.png")	*/	
		var pb = new ProgressBar(1000)
		for (i <- 0 until K) {
			//println("Topic: " + i + " ****************")
		}
	}


}
