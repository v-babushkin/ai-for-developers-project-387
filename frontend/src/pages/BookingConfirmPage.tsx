import { Link, useParams } from 'react-router-dom'
import { useBooking, apiErrorMessage } from '@/api/hooks'
import { formatDate, formatTime } from '@/lib/datetime'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'

export function BookingConfirmPage() {
  const { id } = useParams()
  const bookingId = Number(id)
  const { data, isLoading, isError, error } = useBooking(bookingId)

  return (
    <section className="mx-auto max-w-lg">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Бронь подтверждена
            {data && (
              <Badge variant={data.status === 'CONFIRMED' ? 'default' : 'secondary'}>{data.status}</Badge>
            )}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading && <Skeleton className="h-32 w-full" />}
          {isError && (
            <p className="text-destructive">{apiErrorMessage(error, 'Не удалось загрузить бронь')}</p>
          )}

          {data && (
            <dl className="grid grid-cols-3 gap-y-3 text-sm">
              <dt className="text-muted-foreground">Дата</dt>
              <dd className="col-span-2">{formatDate(data.start)}</dd>

              <dt className="text-muted-foreground">Время</dt>
              <dd className="col-span-2">
                {formatTime(data.start)} – {formatTime(data.end)}
              </dd>

              <dt className="text-muted-foreground">Гость</dt>
              <dd className="col-span-2">{data.guestName}</dd>

              <dt className="text-muted-foreground">Email</dt>
              <dd className="col-span-2">{data.guestEmail}</dd>
            </dl>
          )}

          <Button asChild variant="outline" className="w-full">
            <Link to="/">На главную</Link>
          </Button>
        </CardContent>
      </Card>
    </section>
  )
}
