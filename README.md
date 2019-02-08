# ClientServerBackup
A backup system where the admin can see the backup state of all backed clients. It should also run on low resources servers and on Synology NAS. It is similar to "Active Backup" from Synology, but can be run on any Java supported operating system and NAS models, which are currently not supported by Synology.

# Vision
The server is a very lightweight container. 

It provides an HTML5 GUI where the client(for windows, linux, mac) for the machine is provided from where the HTML5 is visitided.

Every user from every machine can visit the servers HTML site, download the client and begin a backup. But a regisration is necessary that only this user is able to start a recovery using the backupd data.

The backup admin can visit an admin area where he can see
* the backup clients
* the last time when each client contacted the server
* the timestamp when the last file was backed up
* the file structure of the backed up files, including the size

## further features
* encryption of backed up data
* optimized network usage
* performant backup
* keep history of files
* A Synology package even for ones with low resources
* recovery of all files
* reocvery of single files or directories
* reocvery to the defined timestamp

# Project state

I would describe the current state alpha. I need setup and run it on my machines to get an impression how stable and how efficient it runs.

When it is sufficient I'll would create the first beta release, which can be used already, but I would expect that problems occur.

Finnally, when those problems are solved, I will create the first stable release.

## What works
* Users who are familar with computers can allready run the server and the client. 
* Backup works already

## What is missing
* Installation/Configuration descpription
* Recovery
* Security

## Ideas for future releases
* Client state feedback to the user in the toolbar
* Automatic client installation
* comfortable client configuration within a GUI
* comfortable server configuration
* client configuration on the server which sends the update to the client
* automatic client update
* manage that some of reported errors are ignored
 
