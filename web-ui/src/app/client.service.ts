import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {LogService} from "./log.service";
import {ErrorHandlingService} from "./error-handling.service";
import {Observable} from "rxjs";
import {catchError} from "rxjs/operators";
import {Client} from "./client";
import {FileStatistics} from "./file-statistics";

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private clientsUrl = "http://localhost:8080/api/v1.0/statistics/clients";

  constructor( private http: HttpClient,
               private logger: LogService,
               private errorHandler: ErrorHandlingService) { }

  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this.clientsUrl)
      .pipe(
        catchError(this.errorHandler.whenErrorOnHttpRequest<Client[]>(`fetching client count failed`))
      )
  }

  getClientFileStats(client: Client): Observable<FileStatistics> {
    var clientFileStatsUrl = `http://localhost:8080/api/v1.0/statistics/client/${client.hostName}`;
    return this.http.get<FileStatistics>(clientFileStatsUrl).pipe(
      catchError(this.errorHandler.whenErrorOnHttpRequest<FileStatistics>(`fetching stats for client ${client.hostName} failed`))
    );
  }

}
