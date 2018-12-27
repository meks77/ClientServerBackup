import { TestBed } from '@angular/core/testing';

import { ByteFormatterService } from './byte-formatter.service';

describe('ByteFormatterService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ByteFormatterService = TestBed.get(ByteFormatterService);
    expect(service).toBeTruthy();
  });
});
