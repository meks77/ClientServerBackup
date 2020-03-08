package at.meks.backupclientserver.api

data class FileId(val id: String)

abstract class FileEvent(open val fileId: FileId)

data class FileBackedUp(override val fileId: FileId) : FileEvent(fileId)
data class FileDeleted(override val fileId: FileId) : FileEvent(fileId)
data class FileReAdded(override val fileId: FileId) : FileEvent(fileId)