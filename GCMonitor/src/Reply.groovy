/*
 *
 * Reply is a representation of replies to forums in GCCollab/GCConnex
 * Forum is the forum the replies are written to
 * GUID is a unique identifier used by GCCollab/GCConnex
 * Message is the reply itself
 * Link is a URL to the reply
 * Title is the title of the reply if any is given(NOT ALWAYS THE CASE)
 * Score is a value which represents the likelyhood a reply mentions topics related to the OneGC vision
 * Tags are a list of tags which belong to the reply(NOT CURRENTLY BEING USED, for future consideration)
 * Timestamp is the a representation of the last time the forum was updated(NOT THE LAST TIME IT WAS ACTIVE)
 * hasChagned and isNew are booleans used to notify the script that a reply has changed or is new since the last time the script ran
 */

import java.net.URL
import org.jsoup.Jsoup;
import java.util.ArrayList

public class Reply {
	private Forum forum
	private int GUID
	private String message
	private URL link
	private String title
	private int score
	private ArrayList<String> tags//Debate use of tags
	private long timestamp
	private boolean hasChanged
	private boolean isNew

	public Reply(Forum f, String m, long ts) {
		forum = f
		message = m
		timestamp = ts
		hasChanged = false
		isNew = false
	}

	public Reply(Forum f, int g, String m, URL l, long ts) {
		forum = f
		GUID = g
		message = m
		link = l
		tags = null
		title = null
		timestamp = ts
		hasChanged = false
		isNew = false
	}

	public void setScore(int s) {
		score = s
	}

	public int getScore() {
		return score
	}

	public Forum getForum() {
		return forum
	}

	public int getID() {
		return GUID
	}

	public String getMessage() {
		return message
	}

	public URL getLink() {
		return link
	}

	public String getTitle() {
		return title
	}

	public ArrayList<String> getTags() {
		return tags
	}

	public long getTimestamp() {
		return timestamp
	}

	public void setTimestamp(long l) {
		timestamp = l
	}

	//Functions related to a message being new or having changed only used for monitoring purposes, not saved in the db
	public void notifyOfChange() {
		hasChanged = true
	}

	public void notifyNew() {
		isNew = true
	}

	public boolean hasChanged() {
		return hasChanged
	}

	public boolean isNew() {
		return isNew
	}

	//Clean up the message, make it readable by the script/person
	public void sanitizeMessage() {
		if (message && message.contains("\"en\":\"")) {
			def messageJson = parser.parseText(message);
			message = messageJson.en;
		}
		def dom = Jsoup.parse(message);

		//Add list of special characters to sanitize
		message = dom.text().replaceAll("\u2013", "-");
		message = message.replaceAll("<\\\\/p>","")
		message = message.replaceAll("<\\\\/em>","")
		message = message.replaceAll("<\\\\/span>", "")
		message = message.replaceAll("<\\\\/div>","")
		message = message.replaceAll("<\\\\/li>","")
		message = message.replaceAll("<\\\\/h1>","")
		message = message.replaceAll("<\\\\/ul>","")
		message = message.replaceAll("<\\\\/blockquote>","")
		message = message.replaceAll("<\\\\/ins>","")
		message = message.replaceAll("<\\\\/a>","")
		message = message.replaceAll("<\\\\/b>","")
		message = message.replaceAll("<\\\\/strong>","")
		message = message.replaceAll("\\\\r","")
		message = message.replaceAll("\\\\n","")
		message = message.replaceAll("\\\\t","")
		message = message.replaceAll("<\\\\/sup>", "")
		message = message.replaceAll("&nbsp;", " ")
	}

	public void setMessage(String m) {
		message = m
	}

	//Functions to determine if two messages are identical
	//This function should only be called if "this" reply and reply "m" have the same GUID
	public boolean equal(Reply r) {
		if(r.getMessage() != message) {

			r.getForum().notifyOfChange()
			r.notifyOfChange()
			return false
		}

		return true
	}
}
