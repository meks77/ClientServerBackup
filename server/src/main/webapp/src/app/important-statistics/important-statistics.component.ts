import {Component, OnInit} from '@angular/core';
import {FileStatistics} from "../services/file-statistics";
import {StatisticsService} from "../services/statistics.service";
import {ByteFormatterService} from "../commons/byte-formatter.service";

@Component({
  selector: 'app-important-statistics',
  templateUrl: './important-statistics.component.html',
  styleUrls: ['./important-statistics.component.css'],
})
export class ImportantStatisticsComponent implements OnInit {

  fileStatistics: FileStatistics;

  clientCount: number;

  constructor(private statisticsService: StatisticsService, private byteFormatter: ByteFormatterService) { }

  ngOnInit() {
    this.refresh();
  }

  getClientCount(): void {
    this.clientCount = undefined;
    this.statisticsService.getClientCount().subscribe(count => this.clientCount = count);
  }

  getFileStatistics(): void {
    this.fileStatistics = new FileStatistics();
    this.statisticsService.getFileStatistics().subscribe(stats => this.fileStatistics = stats);
  }

  getUsedSpaceHumanReadable(): string {
    return this.byteFormatter.formatToHumanReadable(this.fileStatistics.sizeInMb);
  }

  getFreeSpaceHumanReadable() {
    return this.byteFormatter.formatToHumanReadable(this.fileStatistics.freeSpaceInMb);
  }

  refresh() {
    this.getClientCount();
    this.getFileStatistics();
  }
}
