package at.meks.backupclientserver.backend.webservices.backup

enum class WebBackupAction() { INITIAL_BACKUP, UPLOAD, DELETE, UPDATE}
enum class WebMethod() { POST, PUT, GET, DELETE}
data class WebLink(val rel:WebBackupAction, val href:String, val type:WebMethod)

data class FileStatus(val fileId:String, val links:List<WebLink>)