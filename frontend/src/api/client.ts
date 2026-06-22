import createClient from 'openapi-fetch'
import type { paths } from './schema'

/**
 * Typesafe API client generated from the OpenAPI contract.
 * baseUrl is taken from VITE_API_BASE_URL:
 *  - dev   -> Prism mock (http://localhost:4010)
 *  - prod  -> backend (http://localhost:8080)
 */
export const api = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})
