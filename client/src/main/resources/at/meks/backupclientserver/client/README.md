Edit the file .config to configure 

* the server host and port
* the directories which should be backed up
* the files and directories which should be excluded from the backup

# Configure the server host and port

To configure the host add/modify the key server.host. Eg. the server, where the backup server process is running, is 10.0.0.101 then the following should appear in the config:
```
server.host=10.0.0.101
```

As default the port 8080 is used. If the server runs on a different port you have to configure that. Lets expect the port is 80, then the entry look like this:
```
server.portt=80
```

# Configure the directories which should be backed up
Each directory which should be backed up has to be added to the configuration. The has the following name pattern: 
backupset.dir${index}.

The sign \ must be escaped with \.

Lets expect you want to backup the directories
* C:\Users\LaraCroft
* C:\Users\SteveRogers
* C:\Users\ToniStark

then the entries look like this:
```
backupset.dir0=C:\\Users\\LaraCroft
backupset.dir1=C:\\Users\\SteveRogers
backupset.dir2=C:\\Users\\ToniStark
```

# Configure files and directories which should be excluded from the backup
There are different cases you can configure:
* exclude a file extension (e.g. *.log or *.tmp)
* exclude an absolute path of a file or directory (e.g. C:\Users\LaraCroft\Downloads)
* exclude a directory/file which matches to a name (e.g. Downloads)

By default some excludes are added automatically. Those are visible in the config file.

## Exclude files by file extension/type
There are already some file extension ignored by default. The extensions are configured with the key excludes.extensions.
The value is a list of file extensions separated with a comma.
Lets expect you want to exclude all files of type lock, dmp and tmp the entry look like this:
```
excludes.fileextensions = lock,dmp,tmp
```

## Exclude files/directores by name
### Exclude by absolute path
Use case: The directories of all users are backed up, but you want to exclude only the Download directory of LaraCroft.

Then the excludes should look like this:
``` 
excludes.exclude000=C:/Users/LaraCroft/Downloads
```
### Exclude by name
Use case: The directories of all users are backed up. The Download directory of all users should be excluded.

Then the excludes should look like this:
``` 
excludes.exclude000=Downloads
```

### using placeholders

| Placeholder | Usage                                                                  | Examples                      |
| ----------- | ---------------------------------------------------------------------- | ----------------------------- |
| *           | Placeholder for one directory or part of a directory name or file name | C:/Users/*/Downloads, *tmp___ |         |
| **          | Placeholder for a complete directory hierarchy                         | C:/Users/**/*.tmp             |

