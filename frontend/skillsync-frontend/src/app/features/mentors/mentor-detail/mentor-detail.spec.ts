import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { MentorDetail } from './mentor-detail';

describe('MentorDetail', () => {
  let component: MentorDetail;
  let fixture: ComponentFixture<MentorDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MentorDetail],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(MentorDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
