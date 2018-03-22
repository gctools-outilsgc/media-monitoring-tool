//Object representation of a discussion in GCCollab/GCConnex

import java.net.URL
import java.util.ArrayList

public class Discussion extends Forum{
	public Discussion(int g, Group o, URL u, String d, String t, long ts) {
		super(g,o,u,d,t,ts)
	}
}
