export class AppComponent implements OnInit {
  readonly state = inject(RentalStateService);
  private readonly fb = inject(FormBuilder);

  editingEquipmentId?: number;
  editingBookingId?: number;
  previewLocation = 'Enter ZIP for city lookup';

  equipmentForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    category: ['Guitars & Basses', Validators.required],
    serialNumber: ['', Validators.required],
    dailyRate: [25, [Validators.required, Validators.min(0.01)]],
    conditionStatus: ['Excellent', Validators.required]
  });

  bookingForm = this.fb.nonNullable.group({
    equipmentId: [null as number | null, Validators.required],
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    postalCode: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  });

  ngOnInit(): void {
    this.state.loadAll();

    // Reactive RxJS pipeline for live postal code lookup
    this.bookingForm.controls.postalCode.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      filter(zip => zip.trim().length >= 5),
      switchMap(zip => this.state.lookupPostalCode(zip.trim()))
    ).subscribe({
      next: res => this.previewLocation = res.location,
      error: () => this.previewLocation = 'Lookup unavailable'
    });
  }

  submitEquipment(): void {
    if (this.equipmentForm.invalid) return;
    const payload: Equipment = { id: this.editingEquipmentId, ...this.equipmentForm.getRawValue() };
    this.state.saveEquipment(payload).subscribe(() => this.resetEquipmentForm());
  }

  editEquipment(item: Equipment): void {
    this.editingEquipmentId = item.id;
    this.equipmentForm.patchValue(item);
  }

  removeEquipment(id: number): void {
    this.state.deleteEquipment(id).subscribe();
  }

  resetEquipmentForm(): void {
    this.editingEquipmentId = undefined;
    this.equipmentForm.reset({ category: 'Guitars & Basses', dailyRate: 25, conditionStatus: 'Excellent' });
  }

  submitBooking(): void {
    if (this.bookingForm.invalid) return;
    const raw = this.bookingForm.getRawValue();
    this.state.saveBooking(this.editingBookingId, {
      equipmentId: raw.equipmentId!,
      customerName: raw.customerName,
      customerEmail: raw.customerEmail,
      postalCode: raw.postalCode,
      startDate: raw.startDate,
      endDate: raw.endDate
    }).subscribe(() => this.resetBookingForm());
  }

  editBooking(booking: Booking): void {
    this.editingBookingId = booking.id;
    this.bookingForm.patchValue({
      equipmentId: booking.equipment.id!,
      customerName: booking.customerName,
      customerEmail: booking.customerEmail,
      postalCode: booking.postalCode,
      startDate: booking.startDate,
      endDate: booking.endDate
    });
    this.previewLocation = booking.resolvedLocation || booking.postalCode;
  }

  removeBooking(id: number): void {
    this.state.deleteBooking(id).subscribe();
  }

  resetBookingForm(): void {
    this.editingBookingId = undefined;
    this.previewLocation = 'Enter ZIP for city lookup';
    this.bookingForm.reset();
  }
}