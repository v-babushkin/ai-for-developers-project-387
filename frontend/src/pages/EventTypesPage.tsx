import { Link } from 'react-router-dom'
import { useEventTypes, apiErrorMessage } from '@/api/hooks'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'

export function EventTypesPage() {
  const { data, isLoading, isError, error } = useEventTypes()

  return (
    <section>
      <header className="mb-6">
        <h1 className="text-2xl font-semibold">Выберите тип встречи</h1>
        <p className="mt-1 text-muted-foreground">
          Запишитесь на свободный слот в ближайшие 14 дней.
        </p>
      </header>

      {isLoading && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-44 w-full rounded-xl" />
          ))}
        </div>
      )}

      {isError && (
        <p className="text-destructive">{apiErrorMessage(error, 'Не удалось загрузить типы встреч')}</p>
      )}

      {data && data.length === 0 && (
        <p className="text-muted-foreground">Пока нет доступных типов встреч.</p>
      )}

      {data && data.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((et) => (
            <Card key={et.id} className="flex flex-col">
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <CardTitle>{et.title}</CardTitle>
                  <Badge variant="secondary">{et.durationMinutes} мин</Badge>
                </div>
                <CardDescription className="line-clamp-3">{et.description}</CardDescription>
              </CardHeader>
              <CardContent className="flex-1" />
              <CardFooter>
                <Button asChild className="w-full">
                  <Link to={`/book/${et.id}`}>Выбрать время</Link>
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </section>
  )
}
