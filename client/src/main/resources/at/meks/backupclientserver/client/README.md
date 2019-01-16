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

The sign \ must be escaped with \. For Windows Paths alternativly you can use forward slash(/).

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
or this
```
backupset.dir0=C:/Users/LaraCroft
backupset.dir1=C:/Users/SteveRogers
backupset.dir2=C:/Users/ToniStark
```
# Configure files and directories which should be excluded from the backup
There are different cases you can configure:
1. exclude a file extension (e.g. *.log or *.tmp)
2. exclude files or directories matching to an path

By default some excludes are added automatically. Those are visible in the config file.

## Exclude files by file extension/type
There are already some file extension ignored by default. The extensions are configured with the key excludes.extensions.
The value is a list of file extensions separated with a comma.
Lets expect you want to exclude all files of type lock, dmp and tmp the entry look like this:
```
excludes.fileextensions = lock,dmp,tmp
```

## Exclude files/directores
### Simple cases
#### The directories of all users are backed up. The Download directory of LaraCroft should be excluded.
``` 
excludes.exclude000=C:/Users/LaraCroft/Downloads/**
```
#### The directories of all users are backed up. The Download directory of all users should be excluded.
``` 
excludes.exclude000=C:/Users/*/Downloads/**
```
#### Exclude all directories with the name Downloads
```
excludes.exclude000=**/Downloads/**
```
#### Exclude all files with the name PleaseExclude.me
```
excludes.exclude000=**/PleaseExclude.me
```

### More complex definitions
For verifying if the path should be excluded the glob pattern is used. If you need more enhanced excludes feel free
to use all features of the glob pattern.

This is an excerpt of the oracle java documentation, which explains the use of the glob pattern. 

A glob pattern is specified as a string and is matched against other strings, such as directory or file names. Glob syntax follows several simple rules:

An asterisk, *, matches any number of characters (including none).
Two asterisks, **, works like * but crosses directory boundaries. This syntax is generally used for matching complete paths.
A question mark, ?, matches exactly one character.
Braces specify a collection of subpatterns. For example:
* {sun,moon,stars} matches "sun", "moon", or "stars".
* {temp*,tmp*} matches all strings beginning with "temp" or "tmp".

Square brackets convey a set of single characters or, when the hyphen character (-) is used, a range of characters. For example:
* [aeiou] matches any lowercase vowel.
* [0-9] matches any digit.
* [A-Z] matches any uppercase letter.
* [a-z,A-Z] matches any uppercase or lowercase letter.
Within the square brackets, *, ?, and \ match themselves.
* All other characters match themselves.
* To match *, ?, or the other special characters, you can escape them by using the backslash character, \\. For example: \\\\ matches a single backslash, and \\? matches the question mark.

Here are some examples of glob syntax:

* *.html – Matches all strings that end in .html
* ??? – Matches all strings with exactly three letters or digits
* *[0-9]* – Matches all strings containing a numeric value
* *.{htm,html,pdf} – Matches any string ending with .htm, .html or .pdf
* a?*.java – Matches any string beginning with a, followed by at least one letter or digit, and ending with .java
* {foo*,*[0-9]*} – Matches any string beginning with foo or any string containing a numeric value

