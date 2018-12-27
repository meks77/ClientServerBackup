import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ErrorHandlingService} from "./error-handling.service";
import {Observable} from "rxjs";
import {catchError} from "rxjs/operators";
import {ErrorLog} from "./error-log";

@Injectable({
  providedIn: 'root'
})
export class HealthService {

  private healthUrl = "/api/v1.0/health/errors/maxSize/20";

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlingService) { }

  getErrors(): Observable<ErrorLog[]> {
    return this.http.get<ErrorLog[]>(this.healthUrl)
      .pipe(
        catchError(this.errorHandler.whenErrorOnHttpRequest<ErrorLog[]>(`fetching errors failed`))
      )
  }
}
