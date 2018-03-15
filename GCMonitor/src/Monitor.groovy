/*
 * 
 * Main function of the script
 * 
 * How the script runs:
 * 	- Get a list of Groups and Forums, with their replies from the Database
 * 	- From the list of groups, query GCCollab for a live list of Forums for each group, with their replies
 * 	- Compare the list of forums from the database to a list compiled from the APIs
 * 	- Mark any new or changed forum, including replies to forums
 * 	- (Re)Calculate the score for each forum and message which was identified as new or having changed
 * 	- Update the database
 * 	- Generate a report
 * 
 * Things to add or change:
 * 	- Find a better way to filter results of live list by data and time
 * 	- Automate group detection for possible monitoring
 *  - Automate report delivery
 * 
 */

import java.util.ArrayList
import java.util.Set
import java.util.HashSet
import java.util.Map
import java.util.TreeMap
import java.util.Date
import java.io.BufferedReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import groovy.json.JsonSlurper
import java.nio.ByteBuffer
import java.nio.charset.Charset

//Returns an ArrayList of Strings which represent sentences from a message
//Message should never be null
public ArrayList<String> getSentences(String message) {
	
	if(message == null) {//TEMPORARY
		
		message = ""
	}
	
	return message.split('[\\.\\?\\!]')//Add \\; later if needed.(Message needs to be sanitized first
}

//Scores a sentence based on heuristic values
public int scoreSentence(String s, TreeMap<String,Integer> heuristicValues) {
	
	def score = 0
	def ArrayList<String> splitString = s.split()//Split the string into words
	def ArrayList<String> keywordCombinations = new ArrayList<String>()//Keep track of every instance of a keyword combination found
	def ArrayList<String> keywords = new ArrayList<String>()//Keep track of every instance of a keyword found
	def tmp//Used as temporary string to find keyword combinations
	def tmpScore// Used to calculate when two keywords combinations are found in the same sentence
	
	//Check single words
	for(def i=0;i<splitString.size();i++) {
		if(heuristicValues.containsKey(splitString.get(i))) {
			score += heuristicValues.get(splitString.get(i))
			keywords.add(splitString.get(i))
		}
	}
	
	//Check two words together
	for(def i=0;i<splitString.size()-1;i++) {
		tmp = splitString.get(i) + " " + splitString.get(i+1)
		
		if(heuristicValues.containsKey(tmp)) {
			score += heuristicValues.get(tmp)
			keywordCombinations.add(tmp)
		}
	}
	
	//Check for three words together
	for(def i=0;i<splitString.size()-2;i++) {
		tmp = splitString.get(i) + " " + splitString.get(i+1) + " " + splitString.get(i+2)
		
		if(heuristicValues.containsKey(tmp)) {
			score += heuristicValues.get(tmp)
			keywordCombinations.add(tmp)
		}
	}
	
	//Multiply keywords and keyword combination values
	for(k in keywords) {
		for(c in keywordCombinations) {
			score += heuristicValues.get(k) * heuristicValues.get(c)
		}
	}
	
	//Add multiplied values of combined values for keyword combination
	for(def i=0;i<keywordCombinations.size()-1;i++) {
		for(def j = i+1;j<keywordCombinations.size();j++) {
			tmpScore = (heuristicValues.get(keywordCombinations.get(i)) + heuristicValues.get(keywordCombinations.get(j)))
			score += tmpScore * tmpScore
		}
	}		
	
	return score
}

//Returns long value representation of a date object parsed from a string parsed from the API results 
public long getTimestamp(String s) {
	def date
	def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
	
	date = dateFormat.parse(s)
	
	return date.getTime()
}

public ArrayList<Forum> getForums() {
	def list = new ArrayList<Forum>()
	def cmd = new ArrayList<String>()
	
	def parser = new JsonSlurper()
	def id 
	Forum f
	
	def post
	def postRC
	def responseString
	def response
	def url
	
	cmd.add("discussion")
	cmd.add("blog")
	cmd.add("event")
	
	for(Group g in listGroups) {
		id = g.getID()
		
		for(String s in cmd) {
			
			url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=" + s + "&group=" + id)
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
			
			if(postRC == 200) {
				
				responseString = post.getInputStream().getText()
				response = parser.parseText(responseString)							
								
				if(s.equals("discussion")) {
					for(def i = 0; i<response.result.size();i++) {
						f = new Discussion(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
						
						if(getTimestamp(response.result.get(i).time_created) > timestamp) {
							f.notifyNew()
						} else if(getTimestamp(response.result.get(i).time_updated) > timestamp) {
							f.notifyOfChange()
						}
						
						getMessages(f,response.result.get(i).replies)
						
						list.add(f)
					}
				}
								
				if(s.equals("blog")) {
					for(def i = 0; i<response.result.size();i++) {
						f = new Blog(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
					
						if(getTimestamp(response.result.get(i).time_created) > timestamp) {
							f.notifyNew()
						} else if(getTimestamp(response.result.get(i).time_updated) > timestamp) {
							f.notifyOfChange()
						}
						
						getMessages(f,response.result.get(i).replies)
						
						list.add(f)
					}
				}
				
				if(s.equals("event")) {
					for(def i = 0; i<response.result.size();i++) {
						f = new Event(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
															
						if(getTimestamp(response.result.get(i).time_created) > timestamp) {
							f.notifyNew()
						} else if(getTimestamp(response.result.get(i).time_updated) > timestamp) {
							f.notifyOfChange()
						}
						
						getMessages(f,response.result.get(i).replies)
						
						list.add(f)
					}
				}
			} else {
				println("This is error code: " + postRC)
			}				
		} 
	}	
	
	return list
}

public void getMessages(Forum f, ArrayList<String> replies) {
	def reply
	
	if(replies != null) {
		for(def i=0;i<replies.size();i++) {
			reply = new Reply(f,replies.get(i).guid, replies.get(i).description, new URL(replies.get(i).url),getTimestamp(replies.get(i).time_updated))	
			f.addMessage(reply)
								
			if(getTimestamp(replies.get(i).time_created) > timestamp) {
				f.notifyOfChange()
				reply.notifyNew()
			} else if(getTimestamp(replies.get(i).time_updated) > timestamp) {
				f.notifyOfChange()
				reply.notifyOfChange()
			}
		}
	}
}

//TODO After API access has been granted
//Get all wireposts
public Wirepost findWirepost(HashSet<Wirepost> l, int i) {
	for(Wirepost w in l) {
		if(w != null && w.getID() == i) {
			return w
		}
	}
	
	return null
}

//Returns a list of all wireposts
public HashSet<Wirepost> getWireposts() {
	def HashSet<Wirepost> wireposts = new HashSet<Wirepost>()
	def wire
	def post
	def postRC
	def responseString
	def response
	def url
	def offset = 0
	def group = new Group(1,"wirepost",new URL("https://gccollab.ca/newsfeed/"))//Fake group for wireposts
	def parser = new JsonSlurper()
	
	while(1) {
		url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=wire&limit=25&offset=" + offset)
		post = url.openConnection()
		post.requestMethod = 'POST'
		post.setDoOutput(true)
		postRC = post.getResponseCode()
		
		if(postRC == 200) {
			responseString = post.getInputStream().getText()
			response = parser.parseText(responseString)
						
			for(def i=0;i<response.result.size();i++) {				
				if(response.result.get(i).guid <= largestWirepostID) {
					return wireposts
				}
				
				wire = new Wirepost(response.result.get(i).guid, group, new URL(response.result.get(i).url),response.result.get(i).description,response.result.get(i).title,getTimestamp(response.result.get(i).time_created))
				wireposts.add(wire)	
			}
		}
		
		offset = offset + 25
	}
	
	return wireposts
}

//Checks if a list contains a forum with a specific ID
public boolean listContains(ArrayList<Forum> l, Forum forum) {
	for(Forum f in l) {
		if(f.getID()== forum.getID()) {
			return true
		}
	}
	
	return false
}

//Return the index of a forum in the list
public int getIndex(Forum f, ArrayList<Forum> list) {
	
	for(def i=0;i<list.size();i++){
		if(list.get(i).getID() == f.getID()) {
			return i
		}
	}
	
	return -1
}

public UserInfo getUserInfo() {
	def br = new BufferedReader(new FileReader("userInfo.txt"))
	def line = br.readLine()
	
	String[] tokenize = line.split(",")
	def user = new UserInfo(tokenize[0],tokenize[1])
	
	return user
}


// -------------------------------------------------- BEGINING OF THE SCRIPT --------------------------------------------

def dbStatic = new GCCollabDB("gc.db") //Database
user = getUserInfo() //Global variable holding user info
heuristicValues = dbStatic.setScore() //Heuristic values for keywords from the database
listGroups = dbStatic.getGroups() //List of all groups from the database
timestamp = new Date().minus(1).getTime()//Returns yesterday
largestWirepostID = dbStatic.getLargestWirepostGUID()

println("Getting all forum info from DB")

def staticList = dbStatic.getAllForums() //List of all forums from the database

println("Getting all forums from API")

def liveList = getForums() //dbLive.getAllForums()//List of all forums from the API requests
def wireposts = getWireposts()
liveList.addAll(wireposts)

println("Found " + liveList.size() + " elements to compare")
println(wireposts.size() + " elements are wireposts")

//Call this loop once all objects are created from the API call

println("Sanitizing forums and messages")
 
//Clean up the description/title and replies
for(Forum f in liveList) {
	f.sanitize()
}


println("(Re)calculation scores for forums")

//Check every forum/message that has been changed and recalculate the score
//Calculate the score of every new forum found
//Maybe change description into a message(First message in the list?)
for(Forum f in liveList) {
	
	def score = 0
	
	if(f.isNew()) {
		for(Reply r in f.getMessages()) {			
			for(String s in getSentences(r.getMessage())) {
				score += scoreSentence(s, heuristicValues)
			}
			
			r.setScore(score)
			score = 0
		}		
		
		//Null checks
		if(f.getDescription() == null) {
			f.setDescription("")
		}
		
		if(f.getTitle() == null) {
			f.setTitle("")
		}
		
		for(String s in getSentences(f.getDescription())) {
			score += scoreSentence(s, heuristicValues)
		}
		
		f.setScore(score)
		score = 0
	}
	
	if(f.hasChanged()) {
		for(Reply r in f.getMessages()) {
			if(r.hasChanged() || r.isNew()) {
				for(String s in getSentences(r.getMessage())) {
					score += scoreSentence(s, heuristicValues)
				}
				
				r.setScore(score)
				score = 0
			}			
		}
		
		//Temporary
		for(String s in getSentences(f.getDescription())) {
			score += scoreSentence(s, heuristicValues)
		}
		
		f.setScore(score)
		score = 0
	}
}

println("Creating list for report")

//For report only
def n = new ArrayList<Forum>()//List of new forums
def u = new ArrayList<Forum>()//List of updated forums

for(Forum f in liveList) {
	if(f.isNew()) {
		n.add(f)
	}
	
	if(f.hasChanged()) {
		u.add(f)
	}
}

println("Found " + n.size() + " new forums")
println("Found " + u.size() + " changes to existing forums")

//Sanitizing for DB
for(Forum f in liveList) {
	f.setTitle(f.sanitizeForDB(f.getTitle()))
	f.setDescription(f.sanitizeForDB(f.getDescription()))
	
	for(Reply r in f.getMessages()) {
		r.setMessage(f.sanitizeForDB(r.getMessage()))
	}
}

println("Updating DB")

//Perform updates on DB
for (Forum f in liveList) {
	
	//Replies to wirepost are currently saved as their own wirepost and not replies to the original wirepost
	if (f.getClass().equals(Wirepost.class)) {
		dbStatic.insertForum(f, "Wirepost")
	}
	
	if(f.isNew()) {		
		
		if (f.getClass().equals(Discussion.class)) {
			dbStatic.insertForum(f , "Discussion")
		}
		
		if (f.getClass().equals(Blog.class)) {
			dbStatic.insertForum(f, "Blog")
		}
		
		if (f.getClass().equals(Event.class)) {
			dbStatic.insertForum(f, "Event")
		}
		
		//TODO Test Files and Documents from API later
		if (f.getClass().equals(Files.class)) {
			dbStatic.insertForum(f, "Files")
		}
		
		if (f.getClass().equals(Document.class)) {
			dbStatic.insertForum(f, "Document")
		}				
	}

	if (f.hasChanged()) {
		dbStatic.updateForum(f)	
		
		if(!f.getDeletedMessages().isEmpty()) {			
			for(Reply r in f.getDeletedMessages()) {
				dbStatic.deleteMessage(r)
			}
		}
	}	
}

//Close the databases
dbStatic.close()

//Produce report
//def reporter = new ReportGenerator(n,u)
//reporter.generateReport()