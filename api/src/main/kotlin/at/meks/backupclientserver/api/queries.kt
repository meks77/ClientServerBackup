package at.meks.backupclientserver.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class FileQuery(open val fileProperties: FileProperties,
                         @TargetAggregateIdentifier val managedFileId: FileId = fileProperties.id)

data class AvailableActionsQuery(override val fileProperties: FileProperties) : FileQuery(fileProperties)
enum class BackupAction { INITIAL_BACKUP, UPDATE, DELETE}