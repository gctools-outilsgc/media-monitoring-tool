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
 * -	Sanitize the Forum's output to look better in the report
 * -	Change the format to something more readable without having to resize the columns in CSV format
 * -	Add additional information
 * -	Automatically send the report to interested parties
 * -	How to report changes in forums?
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
		def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss")
		def breaker = false
		DATE_FORMAT.format(date)
		 
		def fileName = "Monitoring_Report_" + date.getTime() + ".csv"
		def bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		bw.write("Forum(Score), Link")
		bw.newLine()		
		bw.write("New Forums")
		bw.newLine()
		
		for(Forum f in newForum) {
			
			if(f.getClass().equals(Wirepost.class) && breaker == false) {
				bw.newLine()
				bw.write("Wireposts")
				bw.newLine()
				bw.newLine()
				breaker = true
			}
			
			bw.write(f.class.toString() + ":" + f.getTitle() + "(" + f.getScore() + "), " + f.getLink())
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
	}
}
