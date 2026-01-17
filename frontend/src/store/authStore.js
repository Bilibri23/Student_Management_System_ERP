import { create } from 'zustand'
import Cookies from 'js-cookie'
import { authApi } from '../api/auth'

// Load from localStorage on init
const loadFromStorage = () => {
  try {
    const stored = localStorage.getItem('auth-storage')
    if (stored) {
      const parsed = JSON.parse(stored)
      // Also check if token exists in cookies
      const token = Cookies.get('accessToken')
      if (token && parsed.user) {
        return { ...parsed, accessToken: token, isAuthenticated: true }
      }
    }
  } catch {
    // Ignore errors
  }
  return null
}

const initialState = loadFromStorage() || {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  loading: false,
  error: null,
}

const useAuthStore = create((set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      loading: false,
      error: null,

      login: async (credentials) => {
        set({ loading: true, error: null })
        try {
          const response = await authApi.login(credentials)
          const { data } = response.data
          
          // Backend returns 'token' not 'accessToken'
          const accessToken = data.token || data.accessToken
          const refreshToken = data.refreshToken
          
          // Store tokens in cookies
          Cookies.set('accessToken', accessToken, { expires: 1 })
          Cookies.set('refreshToken', refreshToken, { expires: 7 })

          // Use user data directly from response instead of decoding token
          set({
            user: {
              id: data.userId,
              username: data.username,
              role: data.role,
              email: data.email,
              fullName: data.fullName,
            },
            accessToken: accessToken,
            refreshToken: refreshToken,
            isAuthenticated: true,
            loading: false,
            error: null,
          })

          return { success: true }
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Login failed'
          set({ loading: false, error: errorMessage })
          return { success: false, error: errorMessage }
        }
      },

      register: async (userData) => {
        set({ loading: true, error: null })
        try {
          await authApi.register(userData)
          set({ loading: false, error: null })
          return { success: true }
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Registration failed'
          set({ loading: false, error: errorMessage })
          return { success: false, error: errorMessage }
        }
      },

      logout: async () => {
        try {
          await authApi.logout()
        } catch (error) {
          console.error('Logout error:', error)
        } finally {
          Cookies.remove('accessToken')
          Cookies.remove('refreshToken')
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            error: null,
          })
        }
      },

      setUser: (user) => set({ user, isAuthenticated: !!user }),

      clearError: () => set({ error: null }),
  })
)

// Initialize from storage
if (initialState.user) {
  useAuthStore.setState(initialState)
}

// Subscribe to changes and save to localStorage
useAuthStore.subscribe((state) => {
  try {
    localStorage.setItem('auth-storage', JSON.stringify({
      user: state.user,
      isAuthenticated: state.isAuthenticated,
    }))
  } catch (error) {
    console.error('Failed to save auth state:', error)
  }
})

export { useAuthStore }

