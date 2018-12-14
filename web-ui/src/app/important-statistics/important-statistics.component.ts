import {Component, OnInit} from '@angular/core';
import {FileStatistics} from "../file-statistics";
import {StatisticsService} from "../statistics.service";
import {formatNumber} from "@angular/common";

@Component({
  selector: 'app-important-statistics',
  templateUrl: './important-statistics.component.html',
  styleUrls: ['./important-statistics.component.css'],
})
export class ImportantStatisticsComponent implements OnInit {

  fileStatistics: FileStatistics;

  clientCount: number;

  constructor(private statisticsService: StatisticsService) { }

  ngOnInit() {
    this.fileStatistics = new FileStatistics();
    this.refresh();
  }

  getClientCount(): void {
    this.statisticsService.getClientCount().subscribe(count => this.clientCount = count);
  }

  getFileStatistics(): void {
    this.statisticsService.getFileStatistics().subscribe(stats => this.fileStatistics = stats);
  }

  getUsedSpaceHumanReadable(): string {
    return this.formatToHumanReadable(this.fileStatistics.sizeInMb);
  }

  private formatToHumanReadable(sizeInMb:number) {
    var unit = 'MB';
    var size = sizeInMb;
    if (sizeInMb > 1200) {
      unit = 'GB';
      size = sizeInMb / 1024;
    }
    if (size > 1200) {
      unit = 'TB';
      size = size / 1024;
    }
    if (size > 1200) {
      unit = 'PB';
      size = size / 1024;
    }
    if (size > 1200) {
      unit = 'EB';
      size = size / 1024;
    }
    return formatNumber(size, 'en', '0.1-1') + ' ' + unit;
  }

  getFreeSpaceHumanReadable() {
    return this.formatToHumanReadable(this.fileStatistics.freeSpaceInMb);
  }

  refresh() {
    this.getClientCount();
    this.getFileStatistics();
  }
}
