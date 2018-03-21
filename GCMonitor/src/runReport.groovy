def db = new GCCollabDB("gc.db")
def from = new Date().minus(30)
def to = new Date()
def n = new ArrayList<Forum>()
def u = n

def rg = new ReportGenerator(n,u)

rg.reportFromTo(to, from, db)
