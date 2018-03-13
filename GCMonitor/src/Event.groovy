//Ojbect representation of an event in GCCollab/GCConnex

import java.util.ArrayList
import java.net.URL

class Event extends Forum{
	public Event(int g,Group o, URL u, String d, String t, long ts) {
		super(g,o,u,d,t,ts)
	}
}
