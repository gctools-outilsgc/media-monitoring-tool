# Media-Monitoring-Tool

Media monitoring application for use with GCconnex and GCcollab

**Work In Progress - This is not a final product**

## How to run the script for GCcollab

- Follow the installation instruction [here](INSTALL.md)

- Modify the userinfo.txt file. Since permission to the platform is based on your account, your account information will need to be supplied in this file. Replace [EMAIL] with the email you used to register for GCCollab. Replace [PASSWORD] with the password to your account. If a group is closed, you will not be able to monitor it without first being accepted in the group.

- If you know specific groups you wish to monitor, groups can be added manually by running the addGroupScript.groovy and give each groups URL when requested. An initial report will be generated containing all information about the group.

- If you wish to find groups to monitor, input keywords into the heuristicValues table by running the addKeywordScript.groovy and supply a keyword and a value. Any value greater than 10 will be used to look for new groups. **(NOT YET IMPLEMENTED)** An initial report will be generated for each group which was found using the new keyword similar to the report generated when adding groups manually

  - For now keywords and values can be added with any SQLite client of your choice

- Once an initial set of heuristic values is entered in the database and the changes to userinfo.txt have been made the monitor script can run. The script is designed to look for any new information within the last 24h (Last week if the Groups table is empty). It is recommended that the script be scheduled to run every 24h

## How to run the script for GCConnex

- Follow the instructions in the "How to run the script for GCCollab" section

- In the UserInfo.txt file needs the user account information for GCConnex and not GCcollab

- Additionally the script must run on a host machine connected to an internal GC network

- The script hasn't been tested on GCConnex, only on GCCollab. Both systems are nearly identical and share the same APIs. The script should work without any other modifications.

## Configuration

  - The heuristic values given to keyword are unique for each script and users will need to supply values that make sense for topics they wish to monitor.

  -  Keywords can contain as many words as possible, but unsafe SQL or URL characters should be avoided

  - The current threshold for keywords that are used to find new groups is 10 which should given to keywords which have a reasonably high likelihood of being used in discussions about the topic in question

## TODO List

- Add script to add keywords to the database

- Find more unsafe characters to the list of characters that need to be sanitized for the database or the URL used in the POST request.

- For technical reasons, the script isn't guaranteed to work properly with groups which have discussions, events or blogs which are only in French. The script currently on supports English content.  
