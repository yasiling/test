import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Link {

	private File indexfile;
	
	private String baseurl,inputurl;
	private String imagepath;
	private int imagewidth,imageheight;
	
	private Analyzer analyzer;
	
	
	private Logger logger=Logger.getLogger(Link.class);
	
	private Link(){
		
		logger.debug("实例化对象"+Link.class.getName());
	}
	/**
	 * 返回当前类的一个实例
	 * @return
	 */
	public static Link getInstance(){
		
		Link link=new Link();
		loadConfig(link);
		return link;
	}
	
	/**
	 * 配置当前对象
	 * @param link
	 * @return
	 */
	private static Link loadConfig(Link link){
		
			link.imagepath= "E:/images4";
			new File(link.imagepath).mkdir();
			link.imagewidth=300;
			link.imageheight=300;
			link.indexfile=new  File("lucene");
			System.out.println(link.indexfile.getAbsolutePath());
			link.baseurl=link.getBaseURL(link.inputurl);
			System.out.println("inputurl :"+link.inputurl);
			System.out.println(link.baseurl);
			link.analyzer=new StandardAnalyzer(Version.LUCENE_35);
		return link;
	}
	/**
	 * 处理一些不合法的URL
	 * @param url
	 * @return
	 */
	public String processURL(String url,String from){
	 
		if(url!=null){
			if(url.startsWith("#")||url.startsWith("javascript")){
				return null;
			}
			else if(url.startsWith("..")){
			  url=url.substring(2);
			  
			  if(from.lastIndexOf("/")==from.length()-1){
				  from=from.substring(0, from.length()-1);
			  }
			  int indexof=-1;
			  if((indexof=from.lastIndexOf("/"))>0){
				  from=from.substring(0, indexof);
			  }
			   url=from+url;
			}
			else if(!url.startsWith("http")&&
					!url.startsWith("HTTP")){
				 return baseurl.concat(url);
			}
		}
		return url;
	}
	/**
	 * 得到baseurl
	 * @param url
	 * @return
	 */
	public String getBaseURL(String url){
  		
	     if(url!=null){
	    	  
	    	  if(!url.startsWith("http")&&!url.startsWith("HTTP")){
	    		   url="http://".concat(url);
	    	  }
	    	  String maphao="://";
	    	  int indexofmao=url.indexOf(maphao);
	    	  
	    	  int indexofgang=url.indexOf("/", indexofmao+maphao.length());
	    	  if(indexofgang>0){
	    		  url=url.substring(0,indexofgang);
	    	  }
	    	  }
		return url;
	}
	public void connect(String url){
		
		this.inputurl=url;
		this.baseurl=this.getBaseURL(url);
		
		try {
			indexUrl(url);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		processConnect(this.inputurl);
	} 
	/**
	 * 处理链接
	 * @param url
	 */
	 void processConnect(String url){
		
		logger.debug("进入"+url);
		Connection connection=
			HttpConnection.connect(url);
		connection.ignoreContentType(true);
		connection.ignoreHttpErrors(true);
		connection.timeout(10000);
		Document document=null;
		Response response=null;
		try {
			response=connection.execute();
			
		} catch (IOException e) {
			logger.debug("文档获取异常");
			e.printStackTrace();
		}
		if(response!=null){
			if(response.contentType()!=null&&response.contentType().indexOf("text/html")>=0){
				 try {
					document=response.parse();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(response.contentType()!=null&&response.contentType().indexOf("image")>=0){
				writeImage(url,url,url);
			}
			else{
				logger.debug("无法处理的文档类型");
			}
		}
		if(document!=null){
           processDoc(document,url);			
		}
	}
	 /**
	  * 处理文档
	  * @param document
	  */
	 void processDoc(Document document,String from){
	  logger.debug("处理文档");
	    Elements docimages=document.getElementsByTag("img");
	   if(docimages!=null){
		   
		   Iterator<Element> iterator=docimages.iterator();
		     while(iterator.hasNext()){
		    	 Element element=iterator.next();//a 链接
		    	 logger.debug(element.attr("alt"));
		    	 String imagename=element.attr("alt");
		    	 if(imagename==null||imagename.equals("")){
		    		 imagename=""+System.currentTimeMillis();
		    	 }
		    	writeImage(element.attr("src"),imagename,from);
		     }
		     
	   }
	   Elements elements=document.getElementsByTag("a");
	   if(elements!=null){
		   
		   Iterator<Element> iterator=elements.iterator();
		   while(iterator.hasNext()){
			   Element link=iterator.next();
			   try {
				fenxiUrl(link.attr("href"),from);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		   }
	   }
	    
	}
	/**
	 * 写图片
	 * @param imageurl
	 */
	 void writeImage(String imageurl,String imagename,String from){
		
		
		imageurl=processURL(imageurl,from);
		URL url=null;
		if(imageurl!=null){
			try {
				 url=new URL(imageurl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if(url!=null){
			InputStream inputStream=null;
			try {
				
					inputStream=url.openStream();
					BufferedImage  bufferedImage=ImageIO.read(inputStream);
					if(bufferedImage!=null&&
							bufferedImage.getWidth()>this.imagewidth
							&&bufferedImage.getHeight()>this.imageheight){
						 logger.debug("写图片"+imageurl);
						ImageIO.write(bufferedImage, "jpg", new FileOutputStream(this.imagepath+
								"/"+imagename+".jpg"));
					}
					else{
						logger.debug("图片尺寸太小");
					}
					
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	 /**
	  * 分析链接的URL地址
	  * @param url
	  * @throws ParseException
	  * @throws IOException
	  */
	 public void fenxiUrl(String url,String from) throws ParseException, IOException{
		 url=processURL(url,from);
		logger.debug("分析"+url);
		 if(url==null){
			 return;
		 }
		 if(hasOne(url)&&this.baseurl.equals(getBaseURL(url))){
			 indexUrl(url);
		    processConnect(url); 
		 }
	 }
	 /**
	  * 查询是否有url地址
	  * @param url
	  * @return
	  * @throws ParseException
	  * @throws IOException
	  */
	 public boolean hasOne(String url) throws ParseException, IOException{
		 logger.debug("查询是否有"+url);
		 IndexSearcher indexSearcher=null;
		 File file=indexfile;
		 IndexReader indexReader=null;
		 if(file==null||!file.exists()){
			 return true;
		 }
		 try {
			
			 indexSearcher=new IndexSearcher(indexReader=IndexReader.
					 open(FSDirectory.open(file)));
			 logger.debug((indexReader+"::"+indexSearcher.getIndexReader()));
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(indexSearcher!=null){
			QueryParser parser=new QueryParser(Version.LUCENE_35, "url", analyzer);
			Query query=parser.parse(Link.md5(url));
		   TopDocs docs=indexSearcher.search(query, 1);
			 indexReader.close();
		   if(docs.totalHits>0){
			   logger.debug("有"+url);
			   return false;
		   }
		}
		

		logger.debug("没有"+url);
		 return true;
	 }
	 /**
	  * 存储URL
	  * @param url
	  * @throws CorruptIndexException
	  * @throws LockObtainFailedException
	  * @throws IOException
	  */
	 public void indexUrl(String url) throws CorruptIndexException, LockObtainFailedException, IOException{
		 
		 
		 logger.debug("索引"+url);	
		 Directory directory=FSDirectory.open(indexfile);
		 IndexWriterConfig config=new IndexWriterConfig(Version.LUCENE_35, analyzer);
		 IndexWriter indexWriter=
			 new IndexWriter(directory,config);
		 
		 org.apache.lucene.document.Document document=new org.apache.lucene.document.Document();
		 Field field=new Field("url", Link.md5(url), Store.NO, Index.ANALYZED);
		 document.add(field);
         		
		 indexWriter.addDocument(document);
		 indexWriter.commit();
		 indexWriter.forceMerge(1);
		 indexWriter.close();
		 
	 }
	 /**
	  * MD5加密
	  * @param plainText
	  * @return
	  */
	 public static String md5(String plainText) {
		  try {
		   MessageDigest md = MessageDigest.getInstance("MD5");
		   md.update(plainText.getBytes());
		   byte b[] = md.digest();

		   int i;

		   StringBuffer buf = new StringBuffer("");
		   for (int offset = 0; offset < b.length; offset++) {
		    i = b[offset];
		    if (i < 0)
		     i += 256;
		    if (i < 16)
		     buf.append("0");
		    buf.append(Integer.toHexString(i));
		   }
		 
		    return buf.toString().substring(8, 24);
		  } catch (NoSuchAlgorithmException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();

		  }
		  return  null;
		 }
	public static void main(String[] args) {
		//String url="http://www.meinv86.com/";
		String url="http://www.siqijiaoyu.cn";
       Link link=Link.getInstance();
       link.connect(url);
	}
	public File getIndexfile() {
		return indexfile;
	}
	public void setIndexfile(File indexfile) {
		this.indexfile = indexfile;
	}
  
	
}
