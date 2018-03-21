/*
 *
 * Object representation of a group in GCCollab/GCConnex
 * GUID is a unique identifier to identify the group
 * Link is a URL to the page on GCCollab/GCConnex
 * Name is the name of the group
 * Forum holds a list of all discussion,blogs,files,documents,events... the group has
 * Tags is a list of all tags which the group has(NOT CURRENTLY BEING USED, for future consideration)
 *
 */

import groovy.json.JsonSlurper
import java.net.URL
import java.util.ArrayList

public class Group{
	private int GUID
	private URL link
	private String name
	private ArrayList<Forum> forums
	private ArrayList<String> tags

	public Group(int g, String n, URL u) {
		GUID = g
		link = u
		name = n
		forums = new ArrayList<Forum>()
		tags = new ArrayList<String>()

		sanitize()
	}

	public int getID() {
		return GUID
	}

	public URL getLink() {
		return link
	}

	public String getName() {
		return name
	}

	public void addForum(Forum f) {
		forums.add(f)
	}

	//Checks if a group has a particular forum that was already monitored
	public boolean containsForum(Forum f) {
		for(Forum forum in forums) {
			if(f.getID() == forum.getID())
				return true
		}

		return false
	}

	public ArrayList<Forum> getForums() {
		return forums
	}

	public ArrayList<String> getTags() {
		return tags
	}

	public void setTag(String t) {
		tags.add(t)
	}

	public void setTags(ArrayList<String> t) {
		tags = t
	}

	//Get methods for individual types of forums
	public ArrayList<Discussion> getDiscussions() {
		def list = new ArrayList<Discussion>()

		for(Forum f in forums) {
			if(f.getClass().equals(Discussion.class)) {
				list.add(f)
			}
		}

		return list
	}

	public ArrayList<Blog> getBlogs() {
		def tmp = new ArrayList<Blog>()

		for(Forum f in forums) {
			if(f.getClass().equals(Blog.class)) {
				tmp.add(f)
			}
		}

		return tmp
	}

	public ArrayList<Event> getEvents() {
		def tmp = new ArrayList<Event>()

		for(Forum f in forums) {
			if(f.getClass().equals(Event.class)) {
				tmp.add(f)
			}
		}

		return tmp
	}

	public ArrayList<File> getFiles() {
		def tmp = new ArrayList<File>()

		for(Forum f in forums) {
			if(f.getClass().equals(File.class)) {
				tmp.add(f)
			}
		}

		return tmp
	}

	public ArrayList<Document> getDocuments() {
		def tmp = new ArrayList<Document>()

		for(Forum f in forums) {
			if(f.getClass().equals(Document.class)) {
				tmp.add(f)
			}
		}

		return tmp
	}

	//Returns a list of forums which have changed since the last check
	public ArrayList<Forum> getUpdatedForums(){
		def list = new ArrayList<Forum>()

		for(Forum f in forums) {
			if(f.hasChanged()) {
				list.add(f)
			}
		}

		return list
	}

	//Returns a list of forums which are new since the last check
	public ArrayList<Forum> getNewForums(){
		def list = new ArrayList<Forum>()

		for(Forum f in forums) {
			if(f.isNew()) {
				list.add(f)
			}
		}

		return list
	}

	public void sanitize() {
		def parser = new JsonSlurper()

		if(name == null) {
			return
		}

		if (name) {
			if(name.contains("\"en\":\"")) {
				def titleJson = parser.parseText(name);
				name = titleJson.en;

			}
		}
	}
}
