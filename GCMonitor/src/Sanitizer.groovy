import groovy.json.JsonSlurper
import org.jsoup.Jsoup;

public final class Sanitizer {
	public String sanitize(String s) {
		def parser = new JsonSlurper()				
		
		if (s && s.contains("\"en\":\"")) {
			def sJson = parser.parseText(s)
			s = sJson.en
		}
		
		if(s == null) {
			return ""
		}
		
		def dom = Jsoup.parse(s)

		//Add list of special characters to sanitize
		s = dom.text().replaceAll("\u2013", "-")
		s = s.replaceAll("<\\\\/p>","")
		s = s.replaceAll("<\\\\/em>","")
		s = s.replaceAll("<\\\\/span>", "")
		s = s.replaceAll("<\\\\/div>","")
		s = s.replaceAll("<\\\\/li>","")
		s = s.replaceAll("<\\\\/h1>","")
		s = s.replaceAll("<\\\\/ul>","")
		s = s.replaceAll("<\\\\/blockquote>","")
		s = s.replaceAll("<\\\\/ins>","")
		s = s.replaceAll("<\\\\/a>","")
		s = s.replaceAll("<\\\\/b>","")
		s = s.replaceAll("<\\\\/strong>","")
		s = s.replaceAll("\\\\r","")
		s = s.replaceAll("\\\\n","")
		s = s.replaceAll("\\\\t","")
		s = s.replaceAll("<\\\\/sup>", "")
		s = s.replaceAll("&nbsp;", " ")
		s = s.replaceAll("\\\\u2018","�")
		s = s.replaceAll("\\\\u201",'�')
		s = s.replaceAll("\\\\u2019","�")
		s = s.replaceAll("\\\\u00b2","�")
			
		return s
	}
}
