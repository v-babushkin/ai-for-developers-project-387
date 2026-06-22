import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { useEventType, useSlots, useCreateBooking, apiErrorMessage } from '@/api/hooks'
import type { Slot } from '@/api/types'
import { dayKey, formatDate, formatTime, formatWeekday } from '@/lib/datetime'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'

interface DayGroup {
  key: string
  date: string
  slots: Slot[]
}

function groupByDay(slots: Slot[]): DayGroup[] {
  const map = new Map<string, DayGroup>()
  for (const slot of slots) {
    const key = dayKey(slot.start)
    let group = map.get(key)
    if (!group) {
      group = { key, date: slot.start, slots: [] }
      map.set(key, group)
    }
    group.slots.push(slot)
  }
  const groups = [...map.values()]
  groups.sort((a, b) => +new Date(a.date) - +new Date(b.date))
  for (const g of groups) g.slots.sort((a, b) => +new Date(a.start) - +new Date(b.start))
  return groups
}

export function BookingPage() {
  const { eventTypeId } = useParams()
  const id = Number(eventTypeId)
  const navigate = useNavigate()

  const eventType = useEventType(id)
  const slots = useSlots(id)
  const createBooking = useCreateBooking()

  const days = useMemo(() => groupByDay(slots.data ?? []), [slots.data])
  const [selectedDay, setSelectedDay] = useState<string | null>(null)
  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null)
  const [guestName, setGuestName] = useState('')
  const [guestEmail, setGuestEmail] = useState('')

  const activeDay = selectedDay ?? days[0]?.key ?? null
  const activeGroup = days.find((d) => d.key === activeDay) ?? null

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!selectedSlot) {
      toast.error('Выберите слот')
      return
    }
    createBooking.mutate(
      {
        eventTypeId: id,
        guestName,
        guestEmail,
        start: selectedSlot.start,
      },
      {
        onSuccess: (booking) => {
          toast.success('Бронь создана')
          if (booking?.id != null) navigate(`/booking/${booking.id}`)
        },
        onError: (error) => {
          const status = (error as { status?: number } | undefined)?.status
          if (status === 409) {
            toast.error('Этот слот уже занят. Выберите другое время.')
            setSelectedSlot(null)
            slots.refetch()
          } else {
            toast.error(apiErrorMessage(error, 'Не удалось создать бронь'))
          }
        },
      },
    )
  }

  if (!Number.isFinite(id)) {
    return <p className="text-destructive">Некорректный идентификатор типа события.</p>
  }

  return (
    <section className="space-y-6">
      <div>
        <Link to="/" className="text-sm text-muted-foreground hover:text-foreground">
          ← К списку типов
        </Link>
        <div className="mt-2 flex items-center gap-3">
          <h1 className="text-2xl font-semibold">
            {eventType.data?.title ?? (eventType.isLoading ? 'Загрузка…' : 'Бронирование')}
          </h1>
          {eventType.data && <Badge variant="secondary">{eventType.data.durationMinutes} мин</Badge>}
        </div>
        {eventType.data?.description && (
          <p className="mt-1 text-muted-foreground">{eventType.data.description}</p>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <Card>
          <CardHeader>
            <CardTitle>Свободные слоты (14 дней)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {slots.isLoading && <Skeleton className="h-40 w-full" />}
            {slots.isError && (
              <p className="text-destructive">{apiErrorMessage(slots.error, 'Не удалось загрузить слоты')}</p>
            )}
            {slots.data && days.length === 0 && (
              <p className="text-muted-foreground">Свободных слотов нет.</p>
            )}

            {days.length > 0 && (
              <>
                <div className="flex flex-wrap gap-2">
                  {days.map((d) => (
                    <Button
                      key={d.key}
                      type="button"
                      size="sm"
                      variant={d.key === activeDay ? 'default' : 'outline'}
                      onClick={() => {
                        setSelectedDay(d.key)
                        setSelectedSlot(null)
                      }}
                    >
                      <span className="capitalize">{formatWeekday(d.date)}</span>
                      <span className="ml-1">{formatDate(d.date)}</span>
                    </Button>
                  ))}
                </div>

                <div className="flex flex-wrap gap-2">
                  {activeGroup?.slots.map((slot) => (
                    <Button
                      key={slot.start}
                      type="button"
                      size="sm"
                      variant={selectedSlot?.start === slot.start ? 'default' : 'outline'}
                      onClick={() => setSelectedSlot(slot)}
                    >
                      {formatTime(slot.start)}
                    </Button>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ваши данные</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label>Выбранный слот</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedSlot
                    ? `${formatDate(selectedSlot.start)}, ${formatTime(selectedSlot.start)}`
                    : 'Слот не выбран'}
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="guestName">Имя</Label>
                <Input
                  id="guestName"
                  value={guestName}
                  onChange={(e) => setGuestName(e.target.value)}
                  required
                  maxLength={120}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="guestEmail">Email</Label>
                <Input
                  id="guestEmail"
                  type="email"
                  value={guestEmail}
                  onChange={(e) => setGuestEmail(e.target.value)}
                  required
                  maxLength={254}
                />
              </div>
              <Button type="submit" className="w-full" disabled={!selectedSlot || createBooking.isPending}>
                {createBooking.isPending ? 'Создание…' : 'Забронировать'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </section>
  )
}
