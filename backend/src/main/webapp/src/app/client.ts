import {FileStatistics} from "./file-statistics";

export class Client {

  hostName: string;
  lastBackupTimestamp: Date;
  heartbeatTimestamp: Date;
  fileStatistics: FileStatistics;

}
