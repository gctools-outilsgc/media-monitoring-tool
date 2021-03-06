/*
 *
 * Database class for the script
 * Ensure that dbLocation points to the correct database
 *
 */

import groovy.sql.Sql

import java.net.URL
import java.sql.ResultSet
import java.util.ArrayList

public class GCCollabDB {

	private Sql dbInstance

	private String dbLocation = "jdbc:sqlite:/"+ new File("").absolutePath.replace("\\", "/") + "/"
	private static String dbDriver = "org.sqlite.JDBC"

	public GCCollabDB(String dbName) {
		dbLocation += dbName
		dbInstance = Sql.newInstance(dbLocation, dbDriver)
	}

	public void insertGroup(Group g){
		if(!hasGroup(g)) {
			dbInstance.execute("insert into Groups(groupID, link, name, description, keywords) values(?, ?, ?, ?, ?)", [g.getID(), g.getLink().toString(), sanitize(g.getName()), g.getDescription(), g.getKeywords()])
		}
	}

	public void insertForum(Forum f, String type){
		if(!hasForum(f)) {
			dbInstance.execute("insert into Forum(forumID, ownerID, link, score, description, title, type, timestamp, keywords) values(?, ?, ?, ?, ?, ?, ?, ?, ?)", [f.getID(), f.getOwner().getID(), f.getLink().toString(), f.getScore(), sanitize(f.getDescription()), sanitize(f.getTitle()), type, f.getTimestamp(), f.getKeywords()])
		}

		if(!f.getClass().equals(Wirepost.class)) {
			insertGroup(f.getOwner())
		}

		for(Reply r in f.getMessages()) {
			insertMessage(r)
		}
	}

	public void insertMessage(Reply r){
		if(!hasMessage(r)) {
			dbInstance.execute("insert into Messages(messageID, message, link, forumID, score, title, timestamp) values(?, ?, ?, ?, ?, ?, ?)", [r.getID(), sanitize(r.getMessage()), r.getLink().toString(), r.getForum().getID(), r.getScore(), sanitize(r.getTitle()), r.getTimestamp()])
		}
	}

	public void printTable(String table){
		dbInstance.rows("select * from " + table).each{
			println(it)
		}
	}

	public void close() {
		dbInstance.close()
	}

	public boolean hasGroup(Group g) {
		int groupID = g.getID()
		return dbInstance.rows("select groupID from groups where groupID='"+ groupID +"'").size() > 0
	}

	public boolean hasGroup(int groupID) {
		return dbInstance.rows("select groupID from groups where groupID='"+ groupID +"'").size() > 0
	}

	public boolean hasForum(Forum f) {
		int forumID = f.getID()
		return dbInstance.rows("select forumID from forum where forumID='"+ forumID +"'").size() > 0
	}

	public boolean hasMessage(Reply r) {
		int messageID = r.getID()
		return dbInstance.rows("select messageID from messages where messageID='"+ messageID +"'").size() > 0
	}

	public void deleteMessage(Reply r) {
		if(hasMessage(r)) {
			dbInstance.execute("DELETE FROM messages WHERE messageID='" + r.getID() + "'")
		}
	}

	public void updateForum(Forum f) {
		def description = sanitize(f.getDescription())
		def timestamp = f.getTimestamp()
		def title = sanitize(f.getTitle())
		def score = f.getScore()
		def forumID = f.getID()
		def keywords = f.getKeywords()
		
		dbInstance.execute("update forum set description ='"+ description +"', timestamp='"+ timestamp +"', score='" + score + "', title='" + title  + " keywords=" + keywords + "' where forumID='" + forumID + "'")

		for(Reply r in f.getMessages()) {
			if(r.hasChanged()) {
				updateMessage(r)
			}

			if(r.isNew()) {
				insertMessage(r)
			}
		}
	}

	public void updateMessage(Reply r) {
		def timestamp = r.getTimestamp()
		def title = sanitize(r.getTitle())
		def messageID = r.getID()
		def score = r.getScore()
		def message = sanitize(r.getMessage())

		dbInstance.execute("update messages set timestamp='"+ timestamp +"', title='"+ title  +"', score='"+ score +"', message='"+ message +"' where MessageID='"+ messageID +"'")
	}

	//Gets all forums from the database and reconstructs them into Java objects
	//This fucntion excludes wireposts
	public ArrayList<Forum> getAllForums() {

		ArrayList<Forum> forums = new ArrayList<Forum>()
		dbInstance.rows("select * from Forum where not type='Wirepost'").each { row ->
			def forumID = row.getProperty("forumID")
			def ownerID = row.getProperty("ownerID")
			def link = new URL(row.getProperty("link"))
			def score = row.getProperty("score")
			def title = row.getProperty("title")
			def description = row.getProperty("description")
			def type = row.getProperty("type")
			def timestamp = row.getProperty("timestamp")
			def owner = getOwner(ownerID)
			def keywords = row.getProperty("keywords")

			def f

			if(type == "Discussion") {
				f = new Discussion(forumID, owner, link, description, title, timestamp, keywords)
			}

			if(type == "Blog") {
				f = new Blog(forumID, owner, link, description, title, timestamp, keywords)
			}

			if(type == "File") {
				f = new Files(forumID, owner, link, description, title, timestamp, keywords)
			}

			if(type == "Document") {
				f = new Document(forumID, owner, link, description, title, timestamp, keywords)
			}

			if(type == "Event") {
				f = new Event(forumID, owner, link, description, title, timestamp)
			}

			forums.add(f)

			for(Reply r in getForumMessages(f)) {
				f.addMessage(r)
			}
		}
		return forums
	}

	//TODO
	public ArrayList<Wirepost> getWireposts() {
		def wireposts = new ArrayList<Wirepost>()

		return wireposts
	}

	//TODO
	public Wirepost getWirepost(int guid) {
		def wirepost

		return wirepost
	}

	//Get forum by ID
	public Forum getForum(int forumID) {
		def f
		def type

		dbInstance.rows("SELECT * FROM forum WHERE forumID='" + forumID + "'").each {
			def ownerID = it.getProperty("ownerID")
			def link = new URL(it.getProperty("link"))
			def score = it.getProperty("score")
			def title = it.getProperty("title")
			def description = it.getProperty("description")
			type = it.getProperty("type")
			def timestamp = it.getProperty("timestamp")
			def owner = getOwner(ownerID)

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

		if(type == "Discussion") {
			for(Reply r in getForumMessages(f)) {
				f.addMessage(r)
			}
		}

		return f
	}

	//Get message by ID
	public Reply getMessage(int messageID) {
		def r

		dbInstance.rows("SELECT * FROM message WHERE messageID='" + messageID + "'").each {
			def forum = getForum(it.getProperty("forumID"))
			def message = it.getProperty("Message")
			def link = new URL(it.getProperty("Link"))
			def score = it.getProperty("Score")
			def timestamp = it.getProperty("timestamp")

			r = new Reply(forum,messageID,message,link,timestamp)
			r.setScore(score)
		}

		return r
	}

	//Used to recreated the Group class
	public Group getOwner(int ownerID) {
		def g

		dbInstance.rows("select * from groups where groupID='"+ ownerID +"'").each {
			def link = new URL(it.getProperty("link"))
			def name = it.getProperty("name")
			def description = it.getProperty("description")
			g = new Group(ownerID, name, description, link)
		}

		return g
	}

	public ArrayList<Group> getGroups() {
		def g
		def list = new ArrayList<Group>()

		dbInstance.rows("SELECT * FROM groups").each {
			URL link = new URL(it.getProperty("link"))
			def name = it.getProperty("name")
			def GUID = it.getProperty("groupID")
			def description = it.getProperty("description")
			g = new Group(GUID,name,description,link)
			list.add(g)
		}

		return list
	}

	//Used to recreated the replies of a forum into a Java class
	public ArrayList<Reply> getForumMessages(Forum f) {
		ArrayList<Reply> forumMessages = new ArrayList<Reply>()
		def forumID = f.getID()

		dbInstance.rows("select * from messages where forumID='"+ forumID +"'").each { row ->

			//TEMPORARY
			def link = new URL("https://www.google.com")

			if(row.getProperty("link") == null) {
				println("REAL LINK")
				link = new URL(row.getProperty("link"))
			}

			def message = row.getProperty("message")
			long timestamp = row.getProperty("timestamp")
			def messageID = row.getProperty("messageID")
			def score = row.getProperty("score")
			Reply r = new Reply(f, messageID, message, link, timestamp)
			r.setScore(score)
			forumMessages.add(r)
		}

		return forumMessages
	}

	//Used to get the heuristic values used for scoring
	public TreeMap<String,Integer> setScore() {
		def map = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER)
		dbInstance.rows("select * from HeuristicValues").each{ row ->
			map.put(row.getProperty("Name"), row.getProperty("Value"))
		}

		return map
	}

	public Integer getLargestWirepostGUID() {
		def result = 207//First wirepost

		dbInstance.rows("SELECT MAX(forumID) FROM Forum WHERE Type='Wirepost'").each{ row ->
			result = row.getProperty("MAX(forumID)")
		}

		return result
	}

	public void updateHeusticScore(String key, int value) {
		dbInstance.execute("UPDATE HeuristicValues set Value ='" + value + "' WHERE name ='" + sanitize(key) +"'")
	}

	public void addHeusticKey(String key, int value) {
		if(!hasKey(key)) {
			dbInstance.execute("INSERT INTO HeuristicValues(name,value) values(?,?)", sanitize(key),value)
		}
	}

	public boolean hasKey(String key) {
		return dbInstance.rows("SELECT name FROM HeuristicValues WHERE name='" + sanitize(key) + "'").size() > 0
	}

	public void deleteKey(String key) {
		dbInstance.execute("DELETE FROM HeuristicValues WHERE name='" + sanitize(key) +"'")
	}

	public boolean isEmpty() {
		if(dbInstance.rows("SELECT * FROM Groups").size() == 0) {
			return true
		}

		return false
	}

	//Removes reserved characters from strings
	//TODO add other reserved characters to the list of characters to be removed
	public String sanitize(String s) {
		if(s) {
			return s.replaceAll("'","\'").replaceAll('"', '\\"')
		}
	}
}
