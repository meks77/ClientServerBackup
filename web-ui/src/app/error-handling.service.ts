import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {LogService} from "./log.service";

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {

  constructor(private logger: LogService) { }

  whenErrorOnHttpRequest<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      this.logger.error(`${operation} failed: ${error.message}`);
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
