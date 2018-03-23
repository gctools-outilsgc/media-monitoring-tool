import java.io.BufferedReader
import java.util.ArrayList
import java.util.TreeMap
import java.text.SimpleDateFormat

import groovy.json.JsonSlurper

/*--------------------------------------SCRIPT-----------------------------------------------*/

def groupTitle
userInfo = getUserInfo() //Global variable holding user info
dbStatic = new GCCollabDB("gc.db") //Database
heuristicValues = dbStatic.setScore()

BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
print("Please enter the group title: ")
groupTitle = reader.readLine()
//while(!groupTitle.isInteger()) {
//	print("Please enter a valid group ID: ")
//	groupTitle = reader.readLine()
//}


Group newGroup = addNewGroup(groupTitle)
getKeywords(newGroup)
def score = 0


if(newGroup) {
	if(!dbStatic.hasGroup(newGroup)) {
		addForums(newGroup)
		
		for(Forum f in newGroup.getForums()) {
		
			getKeywords(f)
				
			for(Reply r in f.getMessages()) {
				for(String s in getSentences(r.getMessage())) {
					score += scoreSentence(s, heuristicValues)
				}
		
				r.setScore(score)
		
				score = 0
			}
			
			for(String s in getSentences(f.getDescription())) {
				score += scoreSentence(s,heuristicValues)
			}
			
			for(String s in getSentences(f.getTitle())) {
				score += scoreSentence(s,heuristicValues)
			}
			
			f.setScore(score)
			
			println("This is f.getScore(): " + f.getScore())
		}
		
		dbStatic.insertGroup(newGroup)
		ReportGenerator.generateGroupReport(newGroup)
	} else {
		println("The group already exists in the database!")
	}
	
} else {
	println("The group is not found!")
}

//Closing the databases
dbStatic.close()


/*--------------------------------------FUNCTIONS-----------------------------------------------*/

public Group addNewGroup(String groupTitle) {
	
	def parser = new JsonSlurper()
	def post
	def postRC
	def responseString
	def response
	def url
	def query
	def value
	def g

	
	query = groupTitle.replaceAll(" ", "%20").replaceAll("/", "%2F").replaceAll("'", "%2C")	
	println("Your group title is: " + query)
		
	try {
	url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query)
	println("URL: " + url)
	post = url.openConnection()
	post.requestMethod = 'POST'
	post.setDoOutput(true)
	postRC = post.getResponseCode()
	} catch(java.net.ConnectException e) {
		println("Connection timeout detected, if this problem persist try again with a different connection")
		url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query)
		post = url.openConnection()
		post.requestMethod = 'POST'
		post.setDoOutput(true)
		postRC = post.getResponseCode()
	}
	
	if (postRC == 200) {
		responseString = post.getInputStream().getText()
		response = parser.parseText(responseString)
		println(response)
		for(def i = 0; i<response.result.size(); i++) {
			g = new Group(response.result.get(i).guid, response.result.get(i).name, new URL(response.result.get(i).url))
			println("API Title: "+ g.getName())
			println("Inserted title: " + groupTitle)
			if (g.getName().equals(groupTitle)) {
				break
			} else {
				g = null
			}
		}
	} else {
			println("This is error code: " + postRC)
	}
	return g
}



//Build user information from file
public UserInfo getUserInfo() {
	def br = new BufferedReader(new FileReader("userInfo.txt"))
	
	def line = br.readLine()

	String[] tokenize = line.split(",")
	def user = new UserInfo(tokenize[0],tokenize[1])
	
	br.close()

	return user
}



//get Forums from a specific Group
public void addForums(Group g) {
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

	id = g.getID()
	
	for(String s in cmd) {
				
		try {
		url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=" + s + "&group=" + id)
		post = url.openConnection()
		post.requestMethod = 'POST'
		post.setDoOutput(true)
		postRC = post.getResponseCode()
		} catch(java.net.ConnectException e) {
			post = url.openConnection()
			post.requestMethod = 'POST'
			post.setDoOutput(true)
			postRC = post.getResponseCode()
			continue
		}
		
		//FOR TESTING
		println("--------------------BREAKER LINE-----------------")
		println("This is id: " + id)
		println("This is cmd: " + s)
		
		if(postRC == 200) {

			responseString = post.getInputStream().getText()
			response = parser.parseText(responseString)

			if(s.equals("discussion")) {
				for(def i = 0; i<response.result.size();i++) {
					f = new Discussion(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
					getMessages(f,response.result.get(i).replies)
					for(Reply r in f.getMessages()) {
						dbStatic.insertMessage(r)
					}
					f.sanitize()
					g.addForum(f)
					dbStatic.insertForum(f,"Discussion")
				}
			}

			if(s.equals("blog")) {
				for(def i = 0; i<response.result.size();i++) {
					f = new Blog(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
					getMessages(f,response.result.get(i).replies)
					f.sanitize()
					g.addForum(f)
					dbStatic.insertForum(f,"Blog")
				}
			}

			if(s.equals("event")) {
				for(def i = 0; i<response.result.size();i++) {
					f = new Event(response.result.get(i).guid, g, new URL(response.result.get(i).url), response.result.get(i).description, response.result.get(i).title,getTimestamp(response.result.get(i).time_updated.toString()))
					getMessages(f,response.result.get(i).replies)
					f.sanitize()
					g.addForum(f)
					dbStatic.insertForum(f,"Event")
				}
			}
		} else {
			println("This is error code: " + postRC)
		}
		
		postRC = null
	}
}


//Returns long value representation of a date object parsed from a string parsed from the API results
public long getTimestamp(String s) {
	def date
	def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

	date = dateFormat.parse(s)

	return date.getTime()
}



public void getMessages(Forum f, ArrayList<String> replies) {
	def reply
	
	if(replies != null) {
		for(def i=0;i<replies.size();i++) {
			reply = new Reply(f,replies.get(i).guid, replies.get(i).description, new URL(replies.get(i).url),getTimestamp(replies.get(i).time_updated))
			f.addMessage(reply)
		}
	}
}

public void getKeywords(Forum f) {
	for(Map.Entry<String,Integer> entry : heuristicValues.entrySet()) {
		println("Looking at keyword: " + entry.getKey() + " inside forums")
		
		if(f.getDescription().contains(entry.getKey())) {
			f.addKeyword(entry.getKey())
		}
		
		if(f.getTitle().contains(entry.getKey())) {
			f.addKeyword(entry.getKey())
		}
		
		for(Reply r in f.getMessages()) {
			if(r.getMessage().contains(entry.getKey())) {
				f.addKeyword(entry.getKey())
			}
		}
	}
}
public void getKeywords(Group g) {
	for(Map.Entry<String,Integer> entry : heuristicValues.entrySet()) {
		println("Looking at keyword: " + entry.getKey() + " inside groups")
		
		if(g.getName().contains(entry.getKey())) {
			g.addKeyword(entry.getKey())
		}
	}
	
	for(Forum f in g.getForums()) {
		getKeywords(f)
	}
}

public ArrayList<String> getSentences(String message) {
	
		if(message == null) {//TEMPORARY
	
			message = ""
		}
	
		return message.split('[\\.\\?\\!]')//Add \\; later if needed.(Message needs to be sanitized first
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


