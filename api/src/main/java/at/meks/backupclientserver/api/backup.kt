package at.meks.backupclientserver.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.nio.file.Path

data class FileId(val id: String)

data class BackupFile(@TargetAggregateIdentifier val fileProperties: FileProperties, val uploadedFile:String)

abstract class FileEvent(open val fileId: FileId)
data class FileBackedUp(override val fileId: FileId, val pathToUploadedFile:Path) : FileEvent(fileId)
data class FileDeleted(override val fileId: FileId) : FileEvent(fileId)
data class FileReAdded(override val fileId: FileId) : FileEvent(fileId)