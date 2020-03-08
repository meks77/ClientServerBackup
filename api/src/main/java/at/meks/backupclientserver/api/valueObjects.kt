package at.meks.backupclientserver.api

data class ClientId(val id: String)
data class ManagedRootDir(val path: String)
data class ManagedPath(val rootDir: ManagedRootDir, val relativePath: String, val clientId: ClientId)
data class FileProperties constructor( val managedPath: ManagedPath, val fileName: String) {
    val id: FileId = FileId(Integer.toHexString((managedPath.toString() + fileName).hashCode()))
}