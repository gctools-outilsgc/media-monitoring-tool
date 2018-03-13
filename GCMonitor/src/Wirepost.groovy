// Object representation of a wirepost in GCCollab/GCConnex
// While this is a forum it is different than other forums in a few way, might make it its own class in the future 

import java.util.ArrayList
import java.net.URL

public class Wirepost extends Forum{	
	public Wirepost(int g, Group o,URL u, String d, String t,long ts) {
		super(g,o,u,d,t,ts)
	}
}
