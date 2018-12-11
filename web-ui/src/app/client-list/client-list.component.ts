import { Component, OnInit } from '@angular/core';
import {ClientService} from "../client.service";
import {Client} from "../client";
import {FileStatistics} from "../file-statistics";

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  clients: Client[];

  constructor(private clientService: ClientService) { }

  ngOnInit() {
    this.clientService.getClients().subscribe(clients => this.setClientsAndUpdateStats(clients));
  }

  private setClientsAndUpdateStats(clients: Client[]) {
    clients.forEach(client => client.fileStatistics = new FileStatistics());
    this.setFileStatistics(clients);
    return this.clients = clients;

  }

  private setFileStatistics(clients: Client[]) {
    clients.forEach(client => this.clientService.getClientFileStats(client).subscribe(stats => client.fileStatistics = stats));
  }
}
