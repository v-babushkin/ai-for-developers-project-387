import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type { BookingCreate, EventTypeCreate } from './types'

/** Извлечь сообщение об ошибке из тела ответа API. */
export function apiErrorMessage(error: unknown, fallback = 'Произошла ошибка'): string {
  if (error && typeof error === 'object' && 'message' in error) {
    const msg = (error as { message?: unknown }).message
    if (typeof msg === 'string') return msg
  }
  return fallback
}

// ---------------------------------------------------------------------------
// Guest: event types
// ---------------------------------------------------------------------------

export function useEventTypes() {
  return useQuery({
    queryKey: ['event-types'],
    queryFn: async () => {
      const { data, error } = await api.GET('/api/event-types')
      if (error) throw error
      return data
    },
  })
}

export function useEventType(id: number) {
  return useQuery({
    queryKey: ['event-types', id],
    enabled: Number.isFinite(id),
    queryFn: async () => {
      const { data, error } = await api.GET('/api/event-types/{id}', {
        params: { path: { id } },
      })
      if (error) throw error
      return data
    },
  })
}

export function useSlots(id: number) {
  return useQuery({
    queryKey: ['event-types', id, 'slots'],
    enabled: Number.isFinite(id),
    queryFn: async () => {
      const { data, error } = await api.GET('/api/event-types/{id}/slots', {
        params: { path: { id } },
      })
      if (error) throw error
      return data
    },
  })
}

// ---------------------------------------------------------------------------
// Guest: bookings
// ---------------------------------------------------------------------------

export function useBooking(id: number) {
  return useQuery({
    queryKey: ['bookings', id],
    enabled: Number.isFinite(id),
    queryFn: async () => {
      const { data, error } = await api.GET('/api/bookings/{id}', {
        params: { path: { id } },
      })
      if (error) throw error
      return data
    },
  })
}

export function useCreateBooking() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (body: BookingCreate) => {
      const { data, error } = await api.POST('/api/bookings', { body })
      if (error) throw error
      return data
    },
    onSuccess: (_data, variables) => {
      qc.invalidateQueries({ queryKey: ['event-types', variables.eventTypeId, 'slots'] })
      qc.invalidateQueries({ queryKey: ['admin', 'bookings'] })
    },
  })
}

// ---------------------------------------------------------------------------
// Admin
// ---------------------------------------------------------------------------

export function useAdminEventTypes() {
  return useQuery({
    queryKey: ['admin', 'event-types'],
    queryFn: async () => {
      const { data, error } = await api.GET('/api/admin/event-types')
      if (error) throw error
      return data
    },
  })
}

export function useCreateEventType() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (body: EventTypeCreate) => {
      const { data, error } = await api.POST('/api/admin/event-types', { body })
      if (error) throw error
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin', 'event-types'] })
      qc.invalidateQueries({ queryKey: ['event-types'] })
    },
  })
}

export function useCancelBooking() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      const { data, error } = await api.POST('/api/bookings/{id}/cancel', {
        params: { path: { id } },
      })
      if (error) throw error
      return data
    },
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['bookings', data.id] })
      qc.invalidateQueries({ queryKey: ['admin', 'bookings'] })
      qc.invalidateQueries({ queryKey: ['event-types', data.eventTypeId, 'slots'] })
    },
  })
}

export function useAdminBookings() {
  return useQuery({
    queryKey: ['admin', 'bookings'],
    queryFn: async () => {
      const { data, error } = await api.GET('/api/admin/bookings')
      if (error) throw error
      return data
    },
  })
}
