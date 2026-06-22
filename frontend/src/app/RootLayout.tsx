import { Link, NavLink, Outlet } from 'react-router-dom'
import { cn } from '@/lib/utils'

export function RootLayout() {
  return (
    <div className="min-h-svh bg-background text-foreground">
      <header className="border-b">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <Link to="/" className="text-lg font-semibold">
            CalBooking
          </Link>
          <nav className="flex gap-4 text-sm">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                cn('text-muted-foreground hover:text-foreground', isActive && 'text-foreground font-medium')
              }
            >
              Типы встреч
            </NavLink>
            <NavLink
              to="/admin"
              className={({ isActive }) =>
                cn('text-muted-foreground hover:text-foreground', isActive && 'text-foreground font-medium')
              }
            >
              Админка
            </NavLink>
            <NavLink
              to="/admin/bookings"
              className={({ isActive }) =>
                cn('text-muted-foreground hover:text-foreground', isActive && 'text-foreground font-medium')
              }
            >
              Встречи
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
