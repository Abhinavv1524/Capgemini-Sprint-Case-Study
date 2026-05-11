import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ReviewPage } from './review-page';

describe('ReviewPage', () => {
  let component: ReviewPage;
  let fixture: ComponentFixture<ReviewPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewPage],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
