/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawling_aria;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Aria
 * The purpose of the program is to crawl Bahai quotes from this web site: http://www.bahaiquotes.com
 * The program finds quotes, authors, source and pages
 */

public class Crawling_Aria {

    /**
     * @param args the command line arguments
     */
    
    static ArrayList <String> Author=new ArrayList<>();
    static ArrayList <String> Quote=new ArrayList<>();
    static ArrayList <String> Source=new ArrayList<>();
    static ArrayList <String> Pages=new ArrayList<>();
    static int failNum=0;
    static int sucNum=0;
    public static void main(String[] args) throws IOException, SQLException {
        // TODO code application logic here
        processPage("http://www.bahaiquotes.com");
        System.out.println("Number of fails: "+failNum);
        System.out.println("Number of sucesses: "+sucNum);
        
    }
    // http://www.bahaiquotes.com/basepage.php?A to http://www.bahaiquotes.com/basepage.php?Z
    public static void processPage(String URL) throws SQLException, IOException{
		
                ArrayList <String> s = new ArrayList <String>();
		String sql = "select * from Record where URL = '"+URL+"'";
		
			Document doc = Jsoup.connect(URL).get();
                        System.out.println(URL);
			
			Elements questions = doc.select("a[href]");
			for(Element link: questions){
				if(link.attr("href").contains("basepage.php"))
                                {
                                    s.add(link.attr("abs:href"));
                                    //System.out.println(link.attr("abs:href"));
                                    processPage2(link.attr("abs:href"));
                                }
			}
		
                        writeinJson(Author, Quote, Source, Pages);
	}
    
        public static void processPage2(String URL) throws SQLException, IOException{
            Document doc = Jsoup.connect(URL).get();
            ArrayList <String> s = new ArrayList <String>();
            Elements questions = doc.select("a[href]");
			for(Element link: questions){
				if(link.attr("href").contains("quotepage.php"))
                                {
                                    s.add(link.attr("abs:href"));
                                    
                                    getText(link.attr("abs:href"));
                                    
                                }
			}
        }
        public static void getText(String URL) throws IOException{
            
            InputStream input = new URL(URL).openStream();
            Document doc = Jsoup.parse(input, "ISO-8859-1", URL);
            
            Elements p = doc.select("p");
            
            for(Element link: p){
               
                String s5=link.text();
                testProcess(s5);
               
            }
        }
        public static void testProcess(String txt){
           
            
            String quote =" ";
            String author=" ";
            String source=" ";
            String pages=" ";
            int begin=txt.lastIndexOf("(");
            boolean refstyle = (txt.charAt(txt.length()-1)==')');
            
            if(begin==(-1) || !refstyle){
                failNum++;
                return;
            }
            String sub=txt.substring(begin+1, txt.length()-1);
            //System.out.println(sub);
           
            
            String qq=txt.substring(0, begin-1);
            quote=qq;
            
            
            String [] ss= sub.split(",");
            //for(int i=0; i<ss.length;i++){
                //System.out.println(ss[i]);
            //}
           
            if(ss.length!=3)
            {
                
                System.out.println(sub + " :Not a well-organized style");
                failNum++;
            }
            else{
                 author=ss[0];
                 source=ss[1];
                 pages=ss[2];
                 Author.add(author);
                 Source.add(source);
                 Pages.add(pages);
                 Quote.add(quote);
                 sucNum++;
                 
            }
            
            
           
        }
        // it converts and writes all the arrays into JSON file Quote.JSON
        public static void writeinJson(ArrayList<String> a, ArrayList <String> q, ArrayList <String> s, ArrayList <String> pp){
            
      
           
             ArrayList <myQuote>m=new ArrayList();
            for(int i=0; i< q.size();i++)
            {
                //System.out.println(a.get(i));
               
                m.add(new myQuote(a.get(i),q.get(i),s.get(i).substring(1),pp.get(i).replace(" p. ", "")));
              
               
            }
           
             //Gson gson = new Gson();
             
             Gson gson = new GsonBuilder().setPrettyPrinting().create();
             String json = gson.toJson(m);
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(json);
            String prettyJsonString = gson.toJson(je);
                  
            try 
                {
 
                   
                    Writer out = new BufferedWriter(new OutputStreamWriter(
                             new FileOutputStream("Quote_Aria.json"), "Cp1252"));
                    
                    try {
                        out.write(prettyJsonString);
                    } finally {
                         out.close();
                        }
                    
 
                } 
                catch (IOException e)
                {
                    e.printStackTrace();
                }
               
        }
        
}
