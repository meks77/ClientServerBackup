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
Currently the idea is born. I will start with the architecture description and will continue with the first user stories.

The plan is to work iterative with an working artifact after each iteration to have a working backup using a Synology NAS as fast as possible. The first supported client OS will be Windows.