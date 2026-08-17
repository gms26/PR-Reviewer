/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#fdf8f6',
          100: '#f2e8e5',
          200: '#eaddd7',
          300: '#e0cec7',
          400: '#d2bab0',
          500: '#a37c68', // Warm Ochre / Leather
          600: '#8c6b59',
          700: '#75594a',
          800: '#5e483b',
          900: '#47362d',
          950: '#2f241e',
        },
        paper: {
          900: '#f9f8f6', // Main background (Oatmeal)
          800: '#f0efe9', // Panel background
          700: '#e6e5db', // Borders / subtle hover
          600: '#d6d5c9',
        },
        ink: {
          900: '#1a1a1a', // Main text (Charcoal)
          800: '#2d2d2d',
          700: '#4a4a4a', // Secondary text
          600: '#71717a',
        },
        sage: {
          50: '#f2f6f5',
          400: '#81b29a',
          500: '#6b9080', // Accents for additions
          600: '#52796f',
        },
        terracotta: {
          50: '#fdf3f0',
          400: '#e07a5f',
          500: '#d95d39', // Accents for deletions/errors
          600: '#b84a2b',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Playfair Display', 'Merriweather', 'serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
        'card-hover': '0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.04)',
        'btn': '0 2px 4px rgba(0, 0, 0, 0.05)',
        'btn-hover': '0 4px 6px rgba(0, 0, 0, 0.08)',
      },
      backgroundImage: {
        'paper-texture': 'url("data:image/svg+xml,%3Csvg width=\'100\' height=\'100\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cfilter id=\'noise\'%3E%3CfeTurbulence type=\'fractalNoise\' baseFrequency=\'0.8\' numOctaves=\'4\' stitchTiles=\'stitch\'/%3E%3C/filter%3E%3Crect width=\'100\' height=\'100\' filter=\'url(%23noise)\' opacity=\'0.05\'/%3E%3C/svg%3E")',
      },
      animation: {
        'fade-in':      'fadeIn 0.8s ease-out forwards',
        'slide-up':     'slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%':   { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}
