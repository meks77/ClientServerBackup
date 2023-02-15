create table BACKUPED_FILE_VERSION (
    ID VARCHAR PRIMARY KEY,
    BACKUPED_FILE_ID VARCHAR,
    BACKUP_TIME TIMESTAMP WITH TIME ZONE
);

create table BACKUPED_FILE_VERSION_CONTENT (
    ID VARCHAR PRIMARY KEY,
    VERSION_ID VARCHAR,
    CONTENT BLOB
);