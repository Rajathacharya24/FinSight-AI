/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        // Primary brand: deep violet/indigo
        brand: {
          50:  '#f0f0ff',
          100: '#e0e0ff',
          200: '#c4b5fd',
          300: '#a78bfa',
          400: '#8b5cf6',
          500: '#7c3aed',
          600: '#6d28d9',
          700: '#5b21b6',
          800: '#4c1d95',
          900: '#2e1065',
          950: '#1a0840',
        },
        // Dark navy background tones
        navy: {
          50:  '#f0f4ff',
          100: '#dce6ff',
          200: '#b9ccff',
          300: '#8faeff',
          400: '#6488fd',
          500: '#3d5af8',
          600: '#2541e0',
          700: '#1b30c4',
          800: '#1627a1',
          900: '#0f1a6e',
          950: '#080e3d',
        },
        // Dark surface tones
        dark: {
          50:  '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          700: '#1e2d3d',
          800: '#141d29',
          850: '#0f1720',
          900: '#090e16',
          950: '#050810',
        },
        // Success/warning/error
        success: {
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
        },
        warning: {
          400: '#fb923c',
          500: '#f97316',
        },
        danger: {
          400: '#f87171',
          500: '#ef4444',
          600: '#dc2626',
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-mesh': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'gradient-brand': 'linear-gradient(135deg, #7c3aed 0%, #3d5af8 100%)',
        'gradient-dark': 'linear-gradient(180deg, #090e16 0%, #0f1720 100%)',
      },
      boxShadow: {
        'glow-brand':   '0 0 30px rgba(124, 58, 237, 0.4)',
        'glow-success': '0 0 20px rgba(34, 197, 94, 0.35)',
        'glow-danger':  '0 0 20px rgba(239, 68, 68, 0.35)',
        'glass':        '0 8px 32px rgba(0, 0, 0, 0.3)',
        'card':         '0 4px 24px rgba(0, 0, 0, 0.2)',
        'card-hover':   '0 8px 40px rgba(0, 0, 0, 0.35)',
      },
      animation: {
        'fade-in':       'fadeIn 0.4s ease-out forwards',
        'slide-up':      'slideUp 0.4s ease-out forwards',
        'slide-in-left': 'slideInLeft 0.3s ease-out forwards',
        'pulse-brand':   'pulseBrand 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow':     'spin 3s linear infinite',
        'shimmer':       'shimmer 2s linear infinite',
      },
      keyframes: {
        fadeIn: {
          from: { opacity: 0 },
          to:   { opacity: 1 },
        },
        slideUp: {
          from: { opacity: 0, transform: 'translateY(16px)' },
          to:   { opacity: 1, transform: 'translateY(0)' },
        },
        slideInLeft: {
          from: { opacity: 0, transform: 'translateX(-16px)' },
          to:   { opacity: 1, transform: 'translateX(0)' },
        },
        pulseBrand: {
          '0%, 100%': { boxShadow: '0 0 20px rgba(124,58,237,0.3)' },
          '50%':      { boxShadow: '0 0 40px rgba(124,58,237,0.6)' },
        },
        shimmer: {
          '0%':   { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
};
