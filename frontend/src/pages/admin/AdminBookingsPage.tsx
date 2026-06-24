import { useMemo } from 'react'
import { toast } from 'sonner'
import { useAdminBookings, useCancelBooking, apiErrorMessage } from '@/api/hooks'
import { formatDateTime } from '@/lib/datetime'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

export function AdminBookingsPage() {
  const { data, isLoading, isError, error } = useAdminBookings()
  const cancelBooking = useCancelBooking()

  const sorted = useMemo(
    () => [...(data ?? [])].sort((a, b) => +new Date(a.start) - +new Date(b.start)),
    [data],
  )

  const handleCancel = (id: number) => {
    if (!window.confirm('Вы уверены, что хотите отменить эту встречу?')) return
    cancelBooking.mutate(id, {
      onSuccess: () => toast.success('Встреча отменена'),
      onError: (err) => toast.error(apiErrorMessage(err, 'Не удалось отменить встречу')),
    })
  }

  return (
    <section>
      <Card>
        <CardHeader>
          <CardTitle>Предстоящие встречи</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && <Skeleton className="h-40 w-full" />}
          {isError && (
            <p className="text-destructive">{apiErrorMessage(error, 'Не удалось загрузить встречи')}</p>
          )}
          {data && sorted.length === 0 && (
            <p className="text-muted-foreground">Встреч пока нет.</p>
          )}
          {sorted.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Начало</TableHead>
                  <TableHead>Окончание</TableHead>
                  <TableHead>Гость</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Статус</TableHead>
                  <TableHead>Действия</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sorted.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell>{formatDateTime(b.start)}</TableCell>
                    <TableCell>{formatDateTime(b.end)}</TableCell>
                    <TableCell className="font-medium">{b.guestName}</TableCell>
                    <TableCell>{b.guestEmail}</TableCell>
                    <TableCell>
                      <Badge variant={b.status === 'CONFIRMED' ? 'default' : 'secondary'}>
                        {b.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {b.status === 'CONFIRMED' && (
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleCancel(b.id)}
                          disabled={cancelBooking.isPending}
                        >
                          Отменить
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </section>
  )
}
