import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="text-center">
      <h1 className="text-2xl font-semibold">Страница не найдена</h1>
      <Link to="/" className="mt-4 inline-block text-primary underline">
        На главную
      </Link>
    </section>
  )
}
