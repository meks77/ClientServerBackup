import { Component, OnInit } from '@angular/core';
import {ClientService} from "../services/client.service";
import {Client} from "../services/client";
import {FileStatistics} from "../services/file-statistics";
import {isNullOrUndefined} from "util";
import {formatDate} from "@angular/common";
import {ByteFormatterService} from "../commons/byte-formatter.service";

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  clients: Client[];

  constructor(private clientService: ClientService, public byteFormatter: ByteFormatterService) { }

  ngOnInit() {
    this.refresh();
  }

  private setClientsAndUpdateStats(clients: Client[]) {
    clients.forEach(client => client.fileStatistics = new FileStatistics());
    this.setFileStatistics(clients);
    return this.clients = clients;

  }

  private setFileStatistics(clients: Client[]) {
    clients.forEach(client => this.clientService.getClientFileStats(client).subscribe(stats => client.fileStatistics = stats));
  }

  refresh() {
    this.clientService.getClients().subscribe(clients => this.setClientsAndUpdateStats(clients));
  }

  getFormatedDate(date: Date): string {
    if (isNullOrUndefined(date)) {
      return "N/A";
    }
    return formatDate(date, 'short', 'en');
  }

}
