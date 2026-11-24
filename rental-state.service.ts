import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { Booking, BookingRequest, Equipment } from '../models/rental.models';

@Injectable({ providedIn: 'root' })
export class RentalStateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  // Centralized RxJS State Subjects
  private readonly equipmentSubject = new BehaviorSubject<Equipment[]>([]);
  private readonly bookingsSubject = new BehaviorSubject<Booking[]>([]);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);

  readonly equipment$ = this.equipmentSubject.asObservable();
  readonly bookings$ = this.bookingsSubject.asObservable();
  readonly error$ = this.errorSubject.asObservable();

  clearError(): void {
    this.errorSubject.next(null);
  }

  loadAll(): void {
    this.loadEquipment();
    this.loadBookings();
  }

  loadEquipment(): void {
    this.http.get<Equipment[]>(`${this.apiUrl}/equipment`).pipe(
      tap(list => this.equipmentSubject.next(list)),
      catchError(err => this.handleError(err))
    ).subscribe();
  }

  saveEquipment(item: Equipment): Observable<Equipment> {
    this.clearError();
    const request$ = item.id
      ? this.http.put<Equipment>(`${this.apiUrl}/equipment/${item.id}`, item)
      : this.http.post<Equipment>(`${this.apiUrl}/equipment`, item);

    return request$.pipe(
      tap(() => {
        this.loadEquipment();
        this.loadBookings();
      }),
      catchError(err => this.handleError(err))
    );
  }

  deleteEquipment(id: number): Observable<void> {
    this.clearError();
    return this.http.delete<void>(`${this.apiUrl}/equipment/${id}`).pipe(
      tap(() => this.loadEquipment()),
      catchError(err => this.handleError(err))
    );
  }

  loadBookings(): void {
    this.http.get<Booking[]>(`${this.apiUrl}/bookings`).pipe(
      tap(list => this.bookingsSubject.next(list)),
      catchError(err => this.handleError(err))
    ).subscribe();
  }

  saveBooking(id: number | undefined, payload: BookingRequest): Observable<Booking> {
    this.clearError();
    const request$ = id
      ? this.http.put<Booking>(`${this.apiUrl}/bookings/${id}`, payload)
      : this.http.post<Booking>(`${this.apiUrl}/bookings`, payload);

    return request$.pipe(
      tap(() => this.loadBookings()),
      catchError(err => this.handleError(err))
    );
  }

  deleteBooking(id: number): Observable<void> {
    this.clearError();
    return this.http.delete<void>(`${this.apiUrl}/bookings/${id}`).pipe(
      tap(() => this.loadBookings()),
      catchError(err => this.handleError(err))
    );
  }

  lookupPostalCode(postalCode: string): Observable<{ location: string }> {
    return this.http.get<{ location: string }>(`${this.apiUrl}/bookings/lookup-postal/${postalCode}`);
  }

  private handleError(error: HttpErrorResponse) {
    const detail = error.error?.detail || error.message || 'An unexpected error occurred.';
    this.errorSubject.next(detail);
    return throwError(() => new Error(detail));
  }
}