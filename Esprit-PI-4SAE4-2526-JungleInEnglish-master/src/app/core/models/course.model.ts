export interface Course {
  id?: number;
  title: string;
  level: string;
  description?: string | null;
  durationHours: number;
  startDate: string;
  endDate?: string | null;
  price?: number | null;
  maxStudents: number;
  active: boolean;
}

