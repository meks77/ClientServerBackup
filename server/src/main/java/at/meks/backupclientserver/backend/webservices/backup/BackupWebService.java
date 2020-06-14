package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.api.AvailableActionsQuery;
import at.meks.backupclientserver.api.BackupAction;
import at.meks.backupclientserver.api.BackupFile;
import at.meks.backupclientserver.api.ClientId;
import at.meks.backupclientserver.api.FileId;
import at.meks.backupclientserver.api.FileProperties;
import at.meks.backupclientserver.api.ManagedPath;
import at.meks.backupclientserver.api.ManagedRootDir;
import at.meks.backupclientserver.backend.services.backup.BackupService;
import at.meks.backupclientserver.common.service.BackupCommandArgs;
import at.meks.backupclientserver.common.service.backup.FileStatus;
import at.meks.backupclientserver.common.service.backup.WebBackupAction;
import at.meks.backupclientserver.common.service.backup.WebLink;
import at.meks.backupclientserver.common.service.backup.WebMethod;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.apache.commons.lang3.ArrayUtils;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Path("/api/v1.0/backup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BackupWebService {

    @ConfigProperty(name = "application.root.dir")
    private String rootDir;

    @ConfigProperty(name = "application.upload.dir")
    private String uploadDir;

    @Inject
    private BackupService backupService;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Inject
    private QueryGateway queryGateway;

    @Inject
    private CommandGateway commandGateway;

    @POST
    public void backupFile(BackupCommandArgs backupCommandArgs) {
        final BackupFile backupCmd = new BackupFile(
                new FileProperties(
                        new ManagedPath(
                                new ManagedRootDir(backupCommandArgs.getBackupedPath()),
                                String.join("/", backupCommandArgs.getRelativePath()),
                                new ClientId(backupCommandArgs.getClientId())),
                        backupCommandArgs.getFileName()),
                Paths.get(rootDir, uploadDir, backupCommandArgs.getRelativePathUplodadedFile()).toString());
        commandGateway.sendAndWait(GenericCommandMessage.asCommandMessage(backupCmd));
    }

    @Path("/isFileUpToDate")
    @POST
    public FileUp2dateResult isFileUp2date(FileUp2dateInput fileUp2DateInput) {
        return exceptionHandler.runReportingException(() -> "isFileUp2date", () -> {
            boolean upToDate = backupService.isFileUpToDate(fileUp2DateInput, fileUp2DateInput.getMd5Checksum());
            return new FileUp2dateResult(upToDate);
        });
    }

    @Path("/fileStatus")
    @POST
    public FileStatus getAvailableActions(@Context UriInfo uriInfo, FileUp2dateInput fileInputArgs) {
        FileProperties fileProperties = getFileProperties(fileInputArgs);
        BackupAction[] backupActions = queryGateway.query(
                new AvailableActionsQuery(fileProperties), BackupAction[].class)
                .join();
        FileId fileId = fileProperties.getId();
        List<WebLink> links = createWebLinks(uriInfo, backupActions);
        return new FileStatus(fileId.getId(), links);
    }

    private FileProperties getFileProperties(FileUp2dateInput fileInputArgs) {
        StringJoiner pathJoiner = new StringJoiner("/");
        Arrays.stream(fileInputArgs.getRelativePath()).forEach(pathJoiner::add);
        return new FileProperties(new ManagedPath(
                new ManagedRootDir(fileInputArgs.getBackupedPath()), pathJoiner.toString(),
                fileInputArgs.getClientId()),
                fileInputArgs.getFileName());
    }

    private List<WebLink> createWebLinks(UriInfo uriInfo, BackupAction[] backupActions) {
        List<WebLink> links = new ArrayList<>();
        links.add(new WebLink(WebBackupAction.UPLOAD, getPath(uriInfo, FileUploadService.class), WebMethod.POST));
        if (ArrayUtils.contains(backupActions, BackupAction.INITIAL_BACKUP)) {
            links.add(new WebLink(WebBackupAction.INITIAL_BACKUP, getPath(uriInfo, BackupWebService.class),
                    WebMethod.POST));
        } else if (ArrayUtils.contains(backupActions, BackupAction.UPDATE)) {
            links.add(new WebLink(WebBackupAction.UPDATE, getPath(uriInfo, BackupWebService.class), WebMethod.PUT));
        }
        if (ArrayUtils.contains(backupActions, BackupAction.DELETE)) {
            links.add(new WebLink(WebBackupAction.DELETE, getPath(uriInfo, BackupWebService.class), WebMethod.DELETE));
        }
        return links;
    }

    private String getPath(UriInfo uriInfo, Class<?> resource) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(resource)).build().getUri().getPath();
    }

    @DELETE
    public void deletePath(FileInputArgs deletePathArgs) {
        exceptionHandler.runReportingException(() -> "deletePath", () -> backupService.delete(deletePathArgs));

    }

}
