import axios from 'axios'

/**
 * Pre-configured Axios instance.
 *
 * - baseURL points to the Vite proxy which forwards /api/* to the backend
 * - withCredentials ensures the session cookie is sent on every request
 * - Interceptor handles 401 responses globally by redirecting to /login
 */
const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Response interceptor — redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
