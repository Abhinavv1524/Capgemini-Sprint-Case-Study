import { TestBed } from '@angular/core/testing';

import { Mentor } from './mentor';

describe('Mentor', () => {
  let service: Mentor;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Mentor);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
