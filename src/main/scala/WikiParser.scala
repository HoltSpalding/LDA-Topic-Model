package WikiParser
import scala.io.Source
import java.io._
import scalaj.http._
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.collection.mutable.ListBuffer

object WikiParser {
	val docslist = new ListBuffer[String]()

//https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&titles=Natural_Language_Processing&redirects=
	def parselinks() {
		try {
			println("ps")
			var resourcesDir = getListOfFiles("src/main/articles");
			println("resourcesDir" + resourcesDir)
			for ((filename,count) <- resourcesDir.zipWithIndex) {
				println(filename)
				//val stream : InputStream = getClass.getResourceAsStream("/"+filename);
								//println(stream)
				var fileiter : Iterator[String] = scala.io.Source.fromFile("src/main/articles/"+filename).getLines()
											//println("here")
				println(fileiter)
			 	while(fileiter.hasNext) {
			        var line = fileiter.next();
			        var splitLine = (line).split("/");
			        //println(splitLine(4))
			        docslist += splitLine(4);
				} 
			}
			
		}
		catch {
			case _: Throwable => println("cant parse docs");
		}
	}

	def create_extracts(): Array[String] = {
		parselinks();
		//println("len " + docslist.length)
		//println(docslist)
		val len = docslist.length;
		val extracts = new Array[String](len);
		val action = "query"
		val prop = "extracts"
		val format = "json"
		for (i <- 0 until len) {
			var ex_link : String = "https://en.wikipedia.org/w/api.php?format=" +
									format + "&action=" + action + "&prop=" +
									prop + "&titles=" + docslist(i) + "&redirects=";
			extracts(i) = ex_link;
		}
		return extracts;

	}

	def httpreq(): Int = {
		val extracts = create_extracts();
		for (i <- 0 until extracts.length) {
			var response: HttpResponse[String] = Http(extracts(i)).asString
 			//val ressplit = response.body.split("title",0);
 			//println(ressplit.length);
 			var title = response.body.substring(response.body.indexOf("\"title\":"),
 						response.body.indexOf("\",\"extract\"")).replaceAll("\"title\":\"", "").trim().replaceAll(" ", "_") + ".txt";
 			//println(title);
 			var body = response.body.substring(response.body.indexOf("\",\"extract\""), 
 							response.body.length - 5).replaceAll("\",\"extract\":\"","");

 			var doc: Document = Jsoup.parse(body)
 			var s: String = doc.body.text().replaceAll("\n","").replaceAll("[^a-zA-Z0-9 -]","").toLowerCase();  //<body> text only
    		//println(s)
    		val path = "src/main/resources/";
    		var file = new File(path + title)
    		file.createNewFile();
			var bw = new BufferedWriter(new FileWriter(file))
			bw.write(s)
			bw.close();
 			//println(body);
 			//println(response.body.getClass);
 			//println(response.body);
 			//val doc: Document = Jsoup.parse(response.body)
 			//val s: String = doc.body.text()  //<body> text only
    		//println(s)
		}
		return extracts.length
 		//val doc: Document = Jsoup.parse(html)
 		// val s: String = doc.body.text()  //<body> text only
    	//println(s)
	}


	def getListOfFiles(dir: String):List[String] = {
	    val d = new File(dir)
	    if (d.exists && d.isDirectory) {
	        d.listFiles.filter(_.isFile).map(_.getName).toList
	    } else {
	        List[String]()
	    }
	}
/*
 val response: HttpResponse[String] = Http().asString
 println(response.getClass);
 val doc: Document = Jsoup.parse(html)
  val s: String = doc.body.text()  //<body> text only
    println(s)



*/



}