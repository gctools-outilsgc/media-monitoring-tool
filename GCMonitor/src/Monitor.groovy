/*
 *
 * Main function of the script
 *
 * How the script runs:
 * 	- Get the heuristic values of keywords from the database
 *  - Update the list of groups to monitor and update the database
 *  - Get the updated list of groups from the database and request Discussion, Events and Blogs from each group
 * 	- For each discussion, event or blog, look if either is new, has been updated or has a new/updated reply and mark them
 * 	- (Re)Calculate the score for each forum and message which was identified as new or having changed
 * 	- Update the database
 * 	- Generate a report
 *  - Additionally the script will look for wireposts, based on the largest wirepost ID in the database
 *  	- If this is the first time you run the script, the script will look for all wireposts within the last week
 *
 * Things to add or change:
 * 	- Manually add a group to be monitored and produce a report with all information from that group
 *  - Manually add one or multiple keywords with their heuristic values and generate a report based on new groups discovered based on those keywords
 */

import java.util.ArrayList
import java.util.HashSet
import java.util.TreeMap
import java.util.Date
import java.util.regex.Pattern
import java.io.BufferedReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import groovy.json.JsonSlurper

//Returns an ArrayList of Strings which represent sentences from a message
//Message should never be null
public ArrayList<String> getSentences(String message) {

	if(message == null) {//TEMPORARY

		message = ""
	}

	return message.split('[ //,//.//?//!//://;??//&]')//Add \\; later if needed.(Message needs to be sanitized first
}

public void getKeywords(Group g) {
	def ArrayList<String> s = g.getDescription().split('[ //,//.//?//!//://;??//&]')
	def tmp

	for(def i=0;i<s.size();i++) {
		if(heuristicValues.containsKey(s.get(i))) {
			g.addKeyword(s.get(i).toLowerCase())
		}
	}

	for(def i=0;i<s.size()-1;i++) {
		tmp = s.get(i) + " " + s.get(i+1)

		if(heuristicValues.containsKey(tmp)) {
			g.addKeyword(tmp.toLowerCase())
		}
	}

	for(def i=0;i<s.size()-2;i++) {
		tmp = s.get(i) + " " + s.get(i+1) + " " + s.get(i+2)

		if(heuristicValues.containsKey(tmp)) {
			g.addKeyword(tmp.toLowerCase())
		}
	}

	s = g.getName().split('[ //,//.//?//!//://;??//&]')

	for(def i=0;i<s.size();i++) {
		if(heuristicValues.containsKey(s.get(i))) {
			g.addKeyword(s.get(i).toLowerCase())
		}
	}

	for(def i=0;i<s.size()-1;i++) {
		tmp = s.get(i) + " " + s.get(i+1)

		if(heuristicValues.containsKey(tmp)) {
			g.addKeyword(tmp.toLowerCase())
		}
	}

	for(def i=0;i<s.size()-2;i++) {
		tmp = s.get(i) + " " + s.get(i+1) + " " + s.get(i+2)

		if(heuristicValues.containsKey(tmp)) {
			g.addKeyword(tmp.toLowerCase())
		}
	}
}

public void getKeywords(Forum f) {
	def ArrayList<String> s = f.getDescription().split('[ //,//.//?//!//://;??//&]')
	def tmp

	for(def i=0;i<s.size();i++) {
		if(heuristicValues.containsKey(s.get(i))) {
			f.addKeyword(s.get(i).toLowerCase())
		}
	}

	for(def i=0;i<s.size()-1;i++) {
		tmp = s.get(i) + " " + s.get(i+1)

		if(heuristicValues.containsKey(tmp)) {
			f.addKeyword(tmp.toLowerCase())
		}
	}

	for(def i=0;i<s.size()-2;i++) {
		tmp = s.get(i) + " " + s.get(i+1) + " " + s.get(i+2)

		if(heuristicValues.containsKey(tmp)) {
			f.addKeyword(tmp.toLowerCase())
		}
	}

	s = f.getTitle().split('[ //,//.//?//!//://;??//&]')

	for(def i=0;i<s.size();i++) {
		if(heuristicValues.containsKey(s.get(i))) {
			f.addKeyword(s.get(i).toLowerCase())
		}
	}

	for(def i=0;i<s.size()-1;i++) {
		tmp = s.get(i) + " " + s.get(i+1)

		if(heuristicValues.containsKey(tmp)) {
			f.addKeyword(tmp.toLowerCase())
		}
	}

	for(def i=0;i<s.size()-2;i++) {
		tmp = s.get(i) + " " + s.get(i+1) + " " + s.get(i+2)

		if(heuristicValues.containsKey(tmp)) {
			f.addKeyword(tmp.toLowerCase())
		}
	}
}

//Scores a sentence based on heuristic values
public int scoreSentence(String s, TreeMap<String,Integer> heuristicValues) {
	def score = 0
	def ArrayList<String> splitString = s.split(" +")//Split the string into words
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

		if(b) {
			println("This is tmp X 2: " + tmp)
		}
		if(heuristicValues.containsKey(tmp)) {
			score += heuristicValues.get(tmp)
			keywordCombinations.add(tmp)
		}
	}

	//Check for three words together
	for(def i=0;i<splitString.size()-2;i++) {
		tmp = splitString.get(i) + " " + splitString.get(i+1) + " " + splitString.get(i+2)

		if(b) {
			println("This is tmp X3: " + tmp)
		}
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

//Queries GCCollab/GCConnex for new groups matching high value keywords
public void updateGroupList() {

	def parser = new JsonSlurper()
	def post
	def postRC
	def responseString
	def response
	def url
	def query
	def value
	def g
	def score = 0

	for(Map.Entry<String,Integer> entry : heuristicValues.entrySet()) {
		query = entry.getKey().replaceAll(" ", "%20")
		value = entry.getValue()

		if (value >= 10) {

			try {
			url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query);
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
			} catch(java.net.ConnectException e) {
				println("A connection timeout error has been detected. If this error persist, please try with a better connection")
				url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query);
				post = url.openConnection()
				post.requestMethod = 'POST'
				post.setDoOutput(true)
				postRC = post.getResponseCode()
			}

			if (postRC == 200) {
				responseString = post.getInputStream().getText()
				response = parser.parseText(responseString)

				for(def i = 0; i<response.result.size(); i++) {
					g = new Group(response.result.get(i).guid, response.result.get(i).name, response.result.get(i).description, new URL(response.result.get(i).url));
					getKeywords(g)
					dbStatic.insertGroup(g);
				}
			} else {
					println("This is error code: " + postRC)
			}
		}
	}
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

			try {
			url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=" + s + "&group=" + id)
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
			} catch(java.net.ConnectException e) {
				println("A connection timeout error has been detected. If this error persist, please try with a better connection")
				url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=" + s + "&group=" + id)
				post = url.openConnection()
				post.requestMethod = 'POST'
				post.setDoOutput(true)
				postRC = post.getResponseCode()
			}

			//FOR TESTING
			println("--------------------BREAKER LINE-----------------")
			println("This is id: " + id)
			println("This is cmd: " + s)
			println("")

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

			postRC = null
		}
	}

	return list
}

public void getMessages(Forum f, ArrayList<String> replies) {
	def reply

	if(replies != null) {
		for(def i=0;i<replies.size();i++) {
			reply = new Reply(f,replies.get(i).guid, replies.get(i).description, new URL(replies.get(i).url),getTimestamp(replies.get(i).time_updated))

			if(getTimestamp(replies.get(i).time_created) > timestamp) {
				f.notifyOfChange()
				reply.notifyNew()
			} else if(getTimestamp(replies.get(i).time_updated) > timestamp) {
				f.notifyOfChange()
				reply.notifyOfChange()
			}

			f.addMessage(reply)
		}
	}
}

//Returns a list of all wireposts
//Gathers 25 wireposts at a time
public HashSet<Wirepost> getWireposts() {
	def HashSet<Wirepost> wireposts = new HashSet<Wirepost>()
	def wire
	def post
	def postRC
	def responseString
	def response
	def url
	def offset = 0
	def group = new Group(1,"wirepost","wirepost group",new URL("https://gccollab.ca/newsfeed/"))//Fake group for wireposts
	def parser = new JsonSlurper()
	def time = 0

	while(time < timestampWire) {
		try {
			url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=wire&limit=25&offset=" + offset)
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
		} catch (java.net.ConnectException e) {
			println("A connection timeout error has been detected. If this error persist, please try with a better connection")
			url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=wire&limit=25&offset=" + offset)
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
		}

		if(postRC == 200) {
			responseString = post.getInputStream().getText()
			response = parser.parseText(responseString)

			for(def i=0;i<response.result.size();i++) {
				if(response.result.get(i).guid <= largestWirepostID) {
					return wireposts
				}

				println("This is the GUID of the current wirepost: " + response.result.get(i).guid)

				wire = new Wirepost(response.result.get(i).guid, group, new URL(response.result.get(i).url),response.result.get(i).description,response.result.get(i).title,getTimestamp(response.result.get(i).time_created))
				wire.notifyNew()
				wireposts.add(wire)

				time = getTimestamp(response.result.get(i).time_updated)

				if(timestampWire > time) {
					return wireposts
				}
			}
		}

		offset = offset + 25
	}

	return wireposts
}

public ArrayList<Reply> findDeletedMessages(Forum f) {
	def deletedMessages = new ArrayList<Reply>()
	def forum = dbStatic.getForum(f.getID())

	if(forum != null) {
		for(Reply r in forum.getMessages()) {
			if(!f.hasMessage(r.getID())) {
				deletedMessages.add(r)
			}
		}
	}

	return deletedMessages
}

public UserInfo getUserInfo() {
	def br = new BufferedReader(new FileReader("userInfo.txt"))
	def line = br.readLine()

	String[] tokenize = line.split(",")
	def user = new UserInfo(tokenize[0],tokenize[1])

	br.close()

	return user
}

// -------------------------------------------------- BEGINING OF THE SCRIPT --------------------------------------------

dbStatic = new GCCollabDB("gc.db") //Database
user = getUserInfo() //Global variable holding user info
heuristicValues = dbStatic.setScore() //Heuristic values for keywords from the database
largestWirepostID = dbStatic.getLargestWirepostGUID()

if(!dbStatic.isEmpty()) {
	timestamp = new Date().minus(1).getTime()//Returns this time yesterday
	timestampWire = new Date().minus(1).getTime()
	println("24h run")
} else {
	timestamp = new Date(100,0,0).getTime()//Returns a time before GCCollab/GCConnex
	timestampWire = new Date().minus(7).getTime()
	println("First run")
}

//Temporarily removed during testing, will be added later
println("Updating list of groups")
updateGroupList();
listGroups = dbStatic.getGroups() //List of all groups from the database

println("Getting all forums from GCCollab/GCConnex")

def liveList = getForums() //List of all forums from the API requests
def wireposts = getWireposts()
liveList.addAll(wireposts)

println("This is the number of  forums: " + (liveList.size() + wireposts.size()))
println(wireposts.size() + " of them are wireposts")

def count = 0

for(Forum f in liveList) {
	if(f.isNew() || f.hasChanged()) {
		count++
	}
}

println("This is the number of new or updated forums: " + count)

//Call this loop once all objects are created from the API call

println("Sanitizing forums and messages")

//Clean up the description/title and replies
for(Forum f in liveList) {
	f.sanitize()
}


println("(Re)calculation scores for forums")

//Check every forum/message that has been changed and recalculate the score
//Calculate the score of every new forum found
for(Forum f in liveList) {
	def score = 0

	getKeywords(f)

	if(f.getClass().equals(Wirepost.class)) {
		for(String s in getSentences(f.getDescription())) {
			score += scoreSentence(s,heuristicValues)
		}

		getKeywords(f)
		f.setScore(score)
		score = 0
	}

	if(f.isNew() && !f.getClass().equals(Wirepost.class)) {
		for(Reply r in f.getMessages()) {
			for(String s in getSentences(r.getMessage())) {
				score += scoreSentence(s, heuristicValues)
			}

			r.setScore(score)

			score = 0
		}

		if(f.getDescription() == null) {
			f.setDescription("")
		}

		for(String s in getSentences(f.getDescription())) {
			score += scoreSentence(s, heuristicValues)
		}

		for(String s in getSentences(f.getTitle())) {
			score += scoreSentence(s,heuristicValues)
		}

		f.setScore(score)
		score = 0
	}

	if(f.hasChanged()) {
		for(Reply r in f.getMessages()) {
			for(String s in getSentences(r.getMessage())) {
				score += scoreSentence(s, heuristicValues)
			}

			r.setScore(score)
			score = 0
		}

		for(String s in getSentences(f.getDescription())) {
			score += scoreSentence(s, heuristicValues)
		}

		for(String s in getSentences(f.getTitle())) {
			score += scoreSentence(s,heuristicValues)
		}

		f.setScore(score)
		score = 0
	}
}

println("Updating DB")

//Perform updates on DB
for (Forum f in liveList) {
	if (f.getClass().equals(Wirepost.class)) {
		dbStatic.insertForum(f, "Wirepost")
	}

	if(f.isNew()) {
		if (f.getClass().equals(Discussion.class)) {
			dbStatic.insertForum(f , "Discussion")

			for(Reply r in f.getMessages()) {
				dbStatic.insertMessage(r)
			}
		}

		if (f.getClass().equals(Blog.class)) {
			dbStatic.insertForum(f, "Blog")

			for(Reply r in f.getMessages()) {
				dbStatic.insertMessage(r)
			}
		}

		if (f.getClass().equals(Event.class)) {
			dbStatic.insertForum(f, "Event")

			for(Reply r in f.getMessages()) {
				dbStatic.insertMessage(r)
			}
		}

		if (f.getClass().equals(Files.class)) {
			dbStatic.insertForum(f, "Files")

			for(Reply r in f.getMessages()) {
				dbStatic.insertMessage(r)
			}
		}

		if (f.getClass().equals(Document.class)) {
			dbStatic.insertForum(f, "Document")

			for(Reply r in f.getMessages()) {
				dbStatic.insertMessage(r)
			}
		}
	}

	if (f.hasChanged()) {
		dbStatic.updateForum(f)

		for(Reply r in f.getMessages()) {
			if(r.hasChanged()) {
				dbStatic.updateMessage(r)
			}

			if(r.isNew()) {
				dbStatic.insertMessage(r)
			}
		}

		if(!f.getDeletedMessages().isEmpty()) {
			for(Reply r in f.getDeletedMessages()) {
				dbStatic.deleteMessage(r)
			}
		}
	}
}

//Close the databases
dbStatic.close()

println("Producing report")

//Produce report
def reporter = new ReportGenerator()
reporter.generateReport(liveList)
