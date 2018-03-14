# Media-Monitoring-Tool

Media monitoring application for use with GCconnex and GCcollab

**Work In Progress - This is not a final product**

Project is written in groovy using Eclipse

In file UserInfo.txt [EMAIL] needs to be replaced with the email used to connect to GCCollab and [PASSWORD] Needs to be replaced with your GCCollab password, without the brackets.

In GCCollabDB.groovy on line 18, the path to the database needs to be changed to the correct location on your host machine(TO BE FIXED TO RELATIVE PATH LATER)

In order to run the script you will need to import a couple jar files to the project

- Jsoup version 1.10.3 or older at [JSoup](https://jsoup.org/download)

- sqlite-jdbc version 3.21.0 or older [SQLite-JDBC](https://bitbucket.org/xerial/sqlite-jdbc/downloads/)

## How to run the script for GCcollab

- The database given is has four empty tables

  - The groups table holds a list of all groups that are being monitored. The list is determined by querying GCCollab/GCConnex for groups which match key words with high likelihood of mentioning topics to the related subject which is being monitored

    - Groups can be entered manually **(Not implemented yet)**

  - The forums table (Discussions, Blogs, Events...) and the message table holds data on items from groups which have already been monitored

  - The heuristicValues table holds all the keyword and values which are used in scoring how relevant a forum is and finding new groups to monitor. User's have to input each keyword(name) and value(value) in the table for the script to run **(Not yet implemented can be done using any SQLite client)**

Once an initial set of heuristic values is entered in the database in the database and the changes to UserInfo.txt and line 18 of GCCollabDB.groovy have been made the script can run.

## How to run the script for GCconnex

- Follow the instructions in the "How to run the script for GCCollab" section

- In the UserInfo.txt file needs the user account information for GCConnex and not GCcollab

- Additionally the script must run on a host machine connected to an internal GC network

- The script hasn't been tested on GCConnex only on GCCollab. Both systems are nearly identical and share the same APIs. The script should work without anything modifications.

## TODO List

- Improvements on how the script determines the likelihood a group is should be monitored is a work in progress

- Currently the entire content of the database is rebuilt during the script and compared with a list gathered from the APIs. In the future the script will look at each individual forum and message and look if they've been created or updated since the last check and only recreate forums and messages which have been found since the last time the script ran. Current plan is to have the script run once every day at the same time.

- Improve how a forum/group/message is determined to be relevant. Currently a number is given based on keywords and how many times they are seen in a sentence and if they are seen with other keywords in the same sentence (See scoreSentence() function in Monitor.groovy). The higher the number the more likely the item in question is relevant to the topic being monitored.
