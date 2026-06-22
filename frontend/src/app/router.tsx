import { createBrowserRouter } from 'react-router-dom'
import { RootLayout } from './RootLayout'
import { EventTypesPage } from '@/pages/EventTypesPage'
import { BookingPage } from '@/pages/BookingPage'
import { BookingConfirmPage } from '@/pages/BookingConfirmPage'
import { AdminEventTypesPage } from '@/pages/admin/AdminEventTypesPage'
import { AdminBookingsPage } from '@/pages/admin/AdminBookingsPage'
import { NotFoundPage } from '@/pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <EventTypesPage /> },
      { path: 'book/:eventTypeId', element: <BookingPage /> },
      { path: 'booking/:id', element: <BookingConfirmPage /> },
      { path: 'admin', element: <AdminEventTypesPage /> },
      { path: 'admin/bookings', element: <AdminBookingsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
