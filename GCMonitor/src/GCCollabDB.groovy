/*
 * 
 * Database class for the script
 * Ensure that dbLocation points to the correct database
 * 
 */

import groovy.sql.Sql;

import java.net.URL
import java.sql.ResultSet
import java.util.ArrayList

public class GCCollabDB {
	
	private Sql dbInstance;
	
	private String dbLocation = "jdbc:sqlite:/C:/Users/trinet.tbs-rl-005182/eclipse-workspace/gcCollabMonitor/"
	private static String dbDriver = "org.sqlite.JDBC"
	
	public GCCollabDB(String dbName) {
		dbLocation += dbName
		dbInstance = Sql.newInstance(dbLocation, dbDriver);
	}		

	public void insertGroup(Group g){
		if(!hasGroup(g)) {
			dbInstance.execute("insert into Groups(groupID, link, name) values(?, ?, ?)", [g.getID(), g.getLink().toString(), g.getName()])
		}
	}
	
	public void insertForum(Forum f, String type){
		dbInstance.execute("insert into Forum(forumID, ownerID, link, score, description, title, type, timestamp) values(?, ?, ?, ?, ?, ?, ?, ?)", [f.getID(), f.getOwner().getID(), f.getLink().toString(), f.getScore(), f.getDescription(), f.getTitle(), type, f.getTimestamp()])
				
		if(!f.getClass().equals(Wirepost.class)) {
			insertGroup(f.getOwner())		
		}
	}
	
	public void insertMessage(Reply r){			
		if(!hasMessage(r)) {
			dbInstance.execute("insert into Messages(messageID, message, link, forumID, score, title, timestamp) values(?, ?, ?, ?, ?, ?, ?)", [r.getID(), r.getMessage(), r.getLink().toString(), r.getForum().getID(), r.getScore(), r.getTitle(), r.getTimestamp()])
		}
	}
		
	public void printTable(String table){
		dbInstance.rows("select * from " + table).each{
			println(it)
		}
	}	
	
	public void close() {
		dbInstance.close();
	}
		
	public boolean hasGroup(Group g) {
		int groupID = g.getID();
		return dbInstance.rows("select groupID from groups where groupID='"+ groupID +"'").size() > 0;
	}
	
	public boolean hasGroup(int groupID) {
		return dbInstance.rows("select groupID from groups where groupID='"+ groupID +"'").size() > 0;
	}
		
	public boolean hasForum(Forum f) {
		int forumID = f.getID();
		return dbInstance.rows("select forumID from forum where forumID='"+ forumID +"'").size() > 0;
	}
	
	public boolean hasMessage(Reply r) {
		int messageID = r.getID();
		return dbInstance.rows("select messageID from messages where messageID='"+ messageID +"'").size() > 0;
	}	
	
	public void deleteMessage(Reply r) {		
		if(hasMessage(r)) {
			dbInstance.execute("DELETE FROM messages WHERE messageID='" + r.getID() + "'")
		} 
	}
	
	public void updateForum(Forum f) {
		def description = f.getDescription();
		def timestamp = f.getTimestamp();
		def title = f.getTitle();
		def score = f.getScore();
		def forumID = f.getID();	
		
		dbInstance.execute("update forum set description ='"+ description +"', timestamp='"+ timestamp +"', score='"+ score +"', title='"+ title  +"' where forumID='"+ forumID +"'");
	}
		
	public void updateMessage(Reply r) {
		def timestamp = r.getTimestamp();
		def title = r.getTitle();
		def messageID = r.getID();
		def score = r.getScore();
		def message = r.getMessage();
			
		dbInstance.execute("update messages set timestamp='"+ timestamp +"', title='"+ title  +"', score='"+ score +"', message='"+ message +"' where MessageID='"+ messageID +"'");
	}
	
	//Gets all forums from the database and reconstructs them into Java objects
	public ArrayList<Forum> getAllForums() {
		// this function excludes wire posts
				
		ArrayList<Forum> forums = new ArrayList<Forum>();
		dbInstance.rows("select * from Forum where not type='Wirepost'").each { row ->
			int forumID = row.getProperty("forumID");
			int ownerID = row.getProperty("ownerID");
			URL link = new URL(row.getProperty("link"));
			int score = row.getProperty("score");
			String title = row.getProperty("title");
			String description = row.getProperty("description");
			String type = row.getProperty("type");
			long timestamp = row.getProperty("timestamp");
			Group owner = getOwner(ownerID);
			
			def f
			
			if(type == "Discussion") {
				f = new Discussion(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "Blog") {
				f = new Blog(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "File") {
				f = new Files(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "Document") {
				f = new Document(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "Event") {
				f = new Event(forumID, owner, link, description, title, timestamp)
			}		
			
			forums.add(f);
			
			for(Reply r in getForumMessages(f)) {
				f.addMessage(r)
			}
		}
		return forums;
	}
		
	public Forum getForum(int forumID) {
		def f
		
		dbInstance.rows("SELECT * FROM forum WHERE forumID='" + forumID + "'").each {
			def ownerID = it.getProperty("ownerID");
			def link = new URL(it.getProperty("link"));
			def score = it.getProperty("score");
			def title = it.getProperty("title");
			def description = it.getProperty("description");
			def type = it.getProperty("type");
			def timestamp = it.getProperty("timestamp");
			def owner = getOwner(ownerID);
			
			if(type == "Discussion") {
				f = new Discussion(forumID,owner,link,description,title,timestamp)
			}
			
			if(type == "Blog") {
				f = new Blog(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "File") {
				f = new Files(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "Document") {
				f = new Document(forumID, owner, link, description, title, timestamp)
			}
			
			if(type == "Event") {
				f = new Event(forumID, owner, link, description, title, timestamp)
			}
			
		}
		
		return f
	}
		
	//Used to recreated the Group class
	public Group getOwner(int ownerID) {
		def g
		
		dbInstance.rows("select * from groups where groupID='"+ ownerID +"'").each {
			URL link = new URL(it.getProperty("link"));
			String name = it.getProperty("name");
			g = new Group(ownerID, name, link);		
		}
		
		return g;
	}
	
	public ArrayList<Group> getGroups() {
		def g
		def list = new ArrayList<Group>()
		
		dbInstance.rows("SELECT * FROM groups").each {
			URL link = new URL(it.getProperty("link"))
			def name = it.getProperty("name")
			def GUID = it.getProperty("groupID")
			g = new Group(GUID,name,link)
			list.add(g)			
		}
		
		return list
	}
		
	//Used to recreated the replies of a forum into a Java class
	public ArrayList<Reply> getForumMessages(Forum f) {
		ArrayList<Reply> forumMessages = new ArrayList<Reply>();
		def forumID = f.getID();
		
		dbInstance.rows("select * from messages where forumID='"+ forumID +"'").each { row ->
			
			//TEMPORARY
			def link = new URL("https://www.google.com")	
			
			if(row.getProperty("link") == null) {
				println("REAL LINK")
				link = new URL(row.getProperty("link"));
			} 
			
			String message = row.getProperty("message");
			long timestamp = row.getProperty("timestamp");
			int messageID = row.getProperty("messageID");
			int score = row.getProperty("score")
			Reply r = new Reply(f, messageID, message, link, timestamp);
			r.setScore(score)
			forumMessages.add(r);
		}
		
		return forumMessages;
	}	
	
	//Used to get the heuristic values used for scoring
	public TreeMap<String,Integer> setScore() {
		def map = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
		dbInstance.rows("select * from HeuristicValues").each{ row ->
			map.put(row.getProperty("Name"), row.getProperty("Value"));
		}
		
		return map
	}
	
	public Integer getLargestWirepostGUID() {
		def result
		
		dbInstance.rows("SELECT MAX(forumID) FROM Forum WHERE Type='Wirepost'").each{ row ->
			result = row.getProperty("MAX(forumID)")
		}
		
		return result
	}
	
	public void updateHeusticScore(String key, int value) {
		dbInstance.execute("UPDATE HeuristicValues set Value ='" + value + "' WHERE name ='" + key +"'")
	}
	
	//Add cases for strings with apostrophies
	public void addHeusticKey(String key, int value) {
		println("Getting in here")
		
		if(!hasKey(key)) {
			dbInstance.execute("INSERT INTO HeuristicValues(name,value) values(?,?)", [key,value])
		}
	}
	
	public boolean hasKey(String key) {		
		return dbInstance.rows("SELECT name FROM HeuristicValues WHERE name='" + key + "'").size() > 0
	}
	
	public void deleteKey(String key) {
		dbInstance.execute("DELETE FROM HeuristicValues WHERE name='" + key +"'")
	}
}