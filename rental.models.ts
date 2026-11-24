export interface Equipment {
  id?: number;
  name: string;
  category: string;
  serialNumber: string;
  dailyRate: number;
  conditionStatus: string;
}

export interface Booking {
  id?: number;
  equipment: Equipment;
  customerName: string;
  customerEmail: string;
  postalCode: string;
  resolvedLocation?: string;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  totalCost?: number;
}

export interface BookingRequest {
  equipmentId: number;
  customerName: string;
  customerEmail: string;
  postalCode: string;
  startDate: string;
  endDate: string;
}