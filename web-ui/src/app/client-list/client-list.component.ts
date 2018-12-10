import { Component, OnInit } from '@angular/core';
import {ClientService} from "../client.service";
import {Client} from "../client";

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  clients: Client[];

  constructor(private clientService: ClientService) { }

  ngOnInit() {
    this.clientService.getClients().subscribe(clients => this.clients = clients);

  }

}
