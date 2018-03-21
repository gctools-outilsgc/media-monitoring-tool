import java.io.BufferedReader;
import java.util.ArrayList

import groovy.json.JsonSlurper

/*--------------------------------------SCRIPT-----------------------------------------------*/

def groupTitle;
userInfo = getUserInfo() //Global variable holding user info
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
print("Please enter the group title: ")
groupTitle = reader.readLine();
//while(!groupTitle.isInteger()) {
//	print("Please enter a valid group ID: ");
//	groupTitle = reader.readLine();
//}


Group newGroup = addNewGroup(groupTitle);
if(newGroup) {
	ReportGenerator.generateGroupReport(newGroup);
} else {
	println("The group is not found!")
}




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


	query = groupTitle.replaceAll(" ", "%20").replaceAll("/", "%2F").replaceAll("'", "%27");
	println("Your group title is: " + query);

	try {
	url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query);
	println("URL: " + url)
	post = url.openConnection()
	post.requestMethod = 'POST'
	post.setDoOutput(true)
	postRC = post.getResponseCode()
	} catch(java.net.ConnectException e) {
		url = new URL("https://gccollab.ca/services/api/rest/json/?method=query.posts&user=" + userInfo.getUser() + "&password=" + userInfo.getPassword() + "&object=group&query=" + query);
		post = url.openConnection()
		post.requestMethod = 'POST'
		post.setDoOutput(true)
		postRC = post.getResponseCode()
	}

	if (postRC == 200) {
		responseString = post.getInputStream().getText()
		response = parser.parseText(responseString)
		println(response);
		for(def i = 0; i<response.result.size(); i++) {
			g = new Group(response.result.get(i).guid, response.result.get(i).name, new URL(response.result.get(i).url));
			println("API Title: "+ g.getName());
			println("Inserted title: " + groupTitle);
			if (g.getName().equals(groupTitle)) {
				break;
			} else {
				g = null;
			}
		}
	} else {
			println("This is error code: " + postRC)
	}
	return g;
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
public ArrayList<Forum> getForums(Group g) {
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
	return list
}
