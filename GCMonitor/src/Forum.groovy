/*
 * 
 * Forum is the super type of all discussions, events, files, documents, blogs in GCCollab/GCConnex
 * GUID is a unique identifier used by GCCollab/GCConnex to identify a forum
 * Messages is a list of all replies to a forum
 * Link is the URL to the forum 
 * Score is a value which represents how likely a forum is to mention topics related to the OneGC vision
 * Tags are a list of tags which the forum has (NOT BEING USED CURRENTLY, for future consideration)
 * Description is a description of the forum from the forum's creator
 * Title is the title of the forum
 * Timestamp is the a representation of the last time the forum was updated(NOT THE LAST TIME IT WAS ACTIVE)
 * hasChanged and isNew are booleans used to notify the script that a forum has changed or is new(NOT SAVED IN THE DB)
 * DeletedMessages is a list of all messages that have been deleted from time the script ran(NOT SAVED IN THE DB)
 * 
 */

import java.util.ArrayList
import org.jsoup.Jsoup;
import groovy.json.JsonSlurper
import java.net.URL

/*
 * Represents any type of discussion/blog/file/event owned by a Group where users can post a reply to
 * Does not include wireposts
 */
public class Forum {
	private Group owner
	private int GUID
	private ArrayList<Reply> messages//Replies
	private URL link
	private int score = 0
	private ArrayList<String> tags//Keep track or not?
	private String description//Message about the forum
	private String title
	private long timestamp//Temporary until permanent fix in place
	private boolean hasChanged
	private boolean isNew
	private ArrayList<Reply> deletedMessages
		
	public Forum(int g, Group group, URL u, String d, String t, long ts) {
		GUID = g
		owner = group
		link = u
		description = d
		title = t//TO BE FIXED
		timestamp = ts
		messages = new ArrayList<Reply>()
		tags = new ArrayList<String>()
		deletedMessages = new ArrayList<Reply>()
		hasChanged = false
		isNew = false			
	}
	
	public Forum(int g, URL u, String d, String t, long ts) {
		GUID = g
		link = u
		description = d
		title = t
		timestamp = ts
		messages = new ArrayList<Reply>()
		tags = new ArrayList<String>()
		deletedMessages = new ArrayList<Reply>()
		hasChanged = false
		isNew = false
	}
	
	public int getID() {
		return GUID
	}
	
	public ArrayList<Reply> getMessages() {
		return messages
	}
	
	public ArrayList<Reply> getDeletedMessages() {
		return deletedMessages
	}	
	
	public URL getLink() {
		return link
	}
	
	public void setLink(URL l) {
		link = l
	}
	
	public void setTags(ArrayList<String> t) {
		tags = t
	}
	
	public void addTags(String t) {
		tags.add(t)
	}
	
	public int getScore() {
		return score
	}
	
	//To be called once all messages for a given discussion have been score
	public void setScore(int s) {
		score = s
		
		for(m in messages) {
			score += m.getScore()
		}
	}
	
	public ArrayList<String> getTags() {
		return tags
	}
	
	public void setDescription(String d) {
		description = d
	}
	
	public String getDescription() {
		return description
	}
	
	public void addMessage(Reply r) {
		messages.add(r)
	}
	
	public void addDeletedMessage(Reply r) {
		deletedMessages.add(r)
	}
	
	public void setOwner(Group p) {
		owner = p
	}
	
	public Group getOwner() {
		return owner
	}
	
	//Titles need to be fixed/standardized
	public String getTitle() {
		return title
	}
	
	public void setTitle(String t) {
		title = t
	}
	
	//To be changed once timestamp is decided
	public long getTimestamp() {
		return timestamp
	}
	
	public void setTimestamp(long l) {
		timestamp = l
	}
	
	//Cleans up the output from the API into a human readable format
	public void sanitize() {
		sanitizeTitle()
		sanitizeMessages()
		sanitizeDescription()
	}
	
	public void sanitizeTitle() {
		def parser = new JsonSlurper()
		if(title == null) {
			return
		}
				
		if (title) {
			if(title.contains("\"en\":\"")) {
				def titleJson = parser.parseText(title);
				title = titleJson.en;
				
			}
		}
		def dom = Jsoup.parse(title)	
		
		//Add list of special characters to sanitize
		title = dom.text().replaceAll("\\\\u2013", "-");
		title = title.replaceAll("\\\\u2018","‘")
		title = title.replaceAll("\\\\u201",'”')
		title = title.replaceAll("\\\\u2019","’")
		title = title.replaceAll("\\\\u00b2","²")	
	}
	
	public void sanitizeMessages() {
		for(Reply r in messages) {
			r.sanitizeMessage()
		}
	}
	
	public void sanitizeDescription() {
		def parser = new JsonSlurper()
		if(description == null) {
			return
		}
		
		if (description && description.indexOf("\"en\":\"") == 0) {
			if (description.contains("\"en\":\"")) {						
				def descriptionJson = parser.parseText(description);
				
				description = descriptionJson.en;
			}
			def dom = Jsoup.parse(description);
			description = dom.text();		

			//Add list of special characters to sanitize
			description = description.replaceAll("<\\\\/p>","")
			description = description.replaceAll("<\\\\/em>","")
			description = description.replaceAll("<\\\\/span>", "")
			description = description.replaceAll("<\\\\/div>","")
			description = description.replaceAll("<\\\\/li>","")
			description = description.replaceAll("<\\\\/h1>","")
			description = description.replaceAll("<\\\\/ul>","")
			description = description.replaceAll("<\\\\/blockquote>","")
			description = description.replaceAll("<\\\\/ins>","")
			description = description.replaceAll("<\\\\/a>","")
			description = description.replaceAll("<\\\\/b>","")
			description = description.replaceAll("<\\\\/strong>","")
			description = description.replaceAll("<\\\\/sup>", "")
			description = description.replaceAll("\\\\r","")
			description = description.replaceAll("\\\\n","")
			description = description.replaceAll("\\\\t","")				
		}
	}
	
//	public String sanitizeForDB(String s) {
//		if(s) {
//			return s.replaceAll("'","''")
//		}
//	}
	
	//hasChanged and isNew values are flags used to determine if a forum has changed or is new
	public void notifyOfChange() {
		if(isNew == false) {
			hasChanged = true
		}	
	}
	
	public boolean hasChanged() {		
		return hasChanged
	}
	
	public void notifyNew() {
		isNew = true	
		hasChanged = false	
	}
	
	public boolean isNew() {
		return isNew
	}
	
	//Returns a message with the GUID 'id'
	public Reply getMessage(int id) {
		for(def i=0;i<messages.size();i++) {
			if(messages.get(i).getID() == id) {
				return messages.get(i)
			}
		}
		
		return null
	}
		
	public void findDeletedMessages(Forum f) {		
		for(Reply r in messages) {
			if(f.getMessage(r.getID()) == null) {
				deletedMessages.add(r)
			}
		}
	}
}