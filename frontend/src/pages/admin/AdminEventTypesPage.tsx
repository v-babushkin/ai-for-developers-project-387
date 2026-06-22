import { useState } from 'react'
import { toast } from 'sonner'
import { useAdminEventTypes, useCreateEventType, apiErrorMessage } from '@/api/hooks'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
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

export function AdminEventTypesPage() {
  const list = useAdminEventTypes()
  const createEventType = useCreateEventType()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [durationMinutes, setDurationMinutes] = useState(30)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    createEventType.mutate(
      { title, description, durationMinutes, active: true },
      {
        onSuccess: () => {
          toast.success('Тип события создан')
          setTitle('')
          setDescription('')
          setDurationMinutes(30)
        },
        onError: (error) => toast.error(apiErrorMessage(error, 'Не удалось создать тип события')),
      },
    )
  }

  return (
    <section className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <Card>
        <CardHeader>
          <CardTitle>Типы событий</CardTitle>
        </CardHeader>
        <CardContent>
          {list.isLoading && <Skeleton className="h-40 w-full" />}
          {list.isError && (
            <p className="text-destructive">{apiErrorMessage(list.error, 'Не удалось загрузить типы')}</p>
          )}
          {list.data && list.data.length === 0 && (
            <p className="text-muted-foreground">Типов событий пока нет.</p>
          )}
          {list.data && list.data.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Название</TableHead>
                  <TableHead>Длительность</TableHead>
                  <TableHead>Статус</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map((et) => (
                  <TableRow key={et.id}>
                    <TableCell className="font-medium">{et.title}</TableCell>
                    <TableCell>{et.durationMinutes} мин</TableCell>
                    <TableCell>
                      <Badge variant={et.active ? 'default' : 'secondary'}>
                        {et.active ? 'Активен' : 'Неактивен'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Новый тип события</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <Label htmlFor="title">Название</Label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
                maxLength={120}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Описание</Label>
              <Textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                maxLength={2000}
                rows={4}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="duration">Длительность (мин)</Label>
              <Input
                id="duration"
                type="number"
                min={1}
                max={1440}
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(Number(e.target.value))}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={createEventType.isPending}>
              {createEventType.isPending ? 'Создание…' : 'Создать'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </section>
  )
}
