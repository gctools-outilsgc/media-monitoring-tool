Skip to content
This repository
Search
Pull requests
Issues
Marketplace
Explore
 @gpatrick2
Sign out
10
0 0 gctools-outilsgc/media-monitoring-tool
 Code  Issues 0  Pull requests 0  Projects 0  Wiki  Insights
media-monitoring-tool/GCMonitor/src/ReportGenerator.groovy
e277b4e  3 hours ago
@HoussamBedja HoussamBedja AddGroup Script added
@gpatrick2 @HoussamBedja

116 lines (91 sloc)  2.77 KB
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
	private ArrayList<Forum> newForum
	private ArrayList<Forum> updatedForum

	public ReportGenerator(ArrayList<Forum> n, ArrayList<Forum> u) {
		newForum = n
		updatedForum = u
	}

	public generateReport() {
		def date = new Date()
		def type
		def fileName = "Monitoring_Report_" + date.getTime() + ".csv"
		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));

		bw.write("Forum(Score), Link")
		bw.newLine()
		bw.write("New Forums")
		bw.newLine()

		for(Forum f in newForum) {

			if(f.getClass().equals(Discussion.class)) {
				type = "Discussion"
			}

			if(f.getClass().equals(Blog.class)) {
				type = "Blog"
			}

			if(f.getClass().equals(Event.class)) {
				type = "Event"
			}

			bw.write(type + ":" + f.getTitle() + "(" + f.getScore() + "), " + f.getLink())
			bw.newLine()
		}

		bw.newLine()
		bw.write("Updated Forums")
		bw.newLine()

		for(Forum f in updatedForum) {
			bw.write(f.class.toString() + ":" + f.getTitle() + "(" + f.getScore() + "), " + f.getLink())
			bw.newLine()
		}

		bw.close()
		sendReport(fileName)
	}

	//Generate report from a new group
	public static generateGroupReport(Group g) {
		def date = new Date()
		def groupName = g.getName().replaceAll(" ", "_")
		def fileName = groupName + date.getTime() + ".csv"
		def type

		println("This is g.getName(): " + g.getName())

		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));

		bw.writeLine("Group, Link")
		bw.writeLine(g.getName() + "," + g.getLink())
		bw.newLine()
		bw.writeLine("Forum(Score), Type, Link")
		bw.newLine()

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
			bw.writeLine(f.getTitle() + "(" + f.getScore() + ")," + type + "," + f.getLink())
		}

		bw.close()
	}

	//TODO
	public void sendReport(String fileName) {

	}
}
© 2018 GitHub, Inc.
Terms
Privacy
Security
Status
Help
Contact GitHub
API
Training
Shop
Blog
About
