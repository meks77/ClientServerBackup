import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LogService {

  constructor() { }

  error(message: string) {
    console.error(`ERROR ${message}`);
  }

  debug(message: string) {
    console.debug(`DEBUG ${message}`);

  }
}
