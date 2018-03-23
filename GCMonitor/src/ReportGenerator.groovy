/*
 * 
 * Class that generates reports at the end of the script
 * NOT FINAL
 * newForum is a list of all new forums discovered since the last time the script ran
 * updatedForum is a list of all forums which have changed in some way since the last time the script ran
 * 
 */

import java.util.ArrayList
import java.util.Map
import java.util.Date
import java.io.BufferedReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter  

/*		TODO LIST
 * -	Change the format to something more readable without having to resize the columns in CSV format
 * -	Automatically send the report to interested parties
 * 
 */
 
 public class ReportGenerator {
		
	public generateReport(ArrayList<Forum> list) {
		def date = new Date()
		def type,state
		def fileName = "Monitoring_Report_" + date.getTime() + ".csv"
		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		bw.write("Forum, Type, Score, Link, Keyword Mathcs, New/Updated")
		bw.newLine()
		
		for(Forum f in list) {
			if(!f.isNew() && !f.hasChanged()) { 
				continue
			}
			
			if(f.isNew()) {
				state = "New"
			} else {
				state = "Changed"
			}
			
			if(f.getClass().equals(Discussion.class)) {
				type = "Discussion"
			}
			
			if(f.getClass().equals(Blog.class)) {
				type = "Blog"
			}
			
			if(f.getClass().equals(Event.class)) {
				type = "Event"
			}	
			
			if(f.getClass().equals(Wirepost.class)) {
				type = "Wirepost"
			}		
					
			bw.write("\"" + f.getTitle() + "\",\"" + type + "\",\"" + f.getScore() + "\",\"" + f.getLink() + "\",\"" + f.getKeywords() + "\"," + state)
			bw.newLine()
		}
				
		bw.close()
		sendReport(fileName)
	}
	
	//Generate report from a new group
	public static generateGroupReport(Group g) {
		def date = new Date()
		def groupName = g.getName().replaceAll(" ", "_")
		groupName = groupName.replaceAll(":","")
		groupName = groupName.replaceAll(",","")
		def fileName = groupName + date.getTime() + ".csv"
		def type
		
		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		bw.writeLine("Group, Group Link, Group Keyword Matchs,Forum, Type, Score, Forum Link, Forum Keyword Matchs")
	
		println("This is the size of g.getForums(): " + g.getForums().size())
		
		for(Forum f in g.getForums()) {
			if(f.getClass().equals(Discussion.class)) {
				type = "Discussion"
			}
			
			if(f.getClass().equals(Blog.class)) {
				type = "Blog"
			}
			
			if(f.getClass().equals(Event.class)) {
				type = "Event"
			}
			
			bw.write("\"" + g.getName() + "\",\"" + g.getLink() + "\",\"" + g.getKeywords() + "\",\"" + f.getTitle() + "\",\"" + type + "\",\"" + f.getScore() + "\",\"" + f.getLink() + "\",\"" + f.getKeywords() + "\"")
			bw.newLine()
		}
		 
		bw.close()	
	}

  public static generateKeywordReport(ArrayList<Group> groups, String k) {
		for(Group g in groups) {
			generateGroupReport(g)
		}
	}
	
	public static reportFromTo(Date t, Date f, GCCollabDB db) {
		def to = t.getTime()
		def from = f.getTime()
		def fileName = "MonitorReport_From_" + f.getTime() + "_to_" + t.getTime() + ".csv"
		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		def forums = db.getAllForums()
		def type

		bw.writeLine("Forum, Type, Score, Link, Keyword Matchs")
		bw.newLine()

		for(Forum fs in forums) {
			if(fs.getTimestamp() <= to && fs.getTimestamp() >= from) {
				if(fs.class.equals(Discussion.class)) {
					type = "Discussion"
				}

				if(fs.getClass().equals(Blog.class)) {
					type = "Blog"
				}

				if(fs.getClass().equals(Event.class)) {
					type = "Event"
				}
				
				if(fs.getClass().equals(Wirepost.class)) {
					type = "Wirepost"
				}

				bw.newLine()
				bw.writeLine(fs.getTitle() + "(" + fs.getScore() + ")," + type + "," + fs.getLink())
			}
		}
		bw.close()
	}
	
	//TODO 
	public void sendReport(String fileName) {
		
	}
}
