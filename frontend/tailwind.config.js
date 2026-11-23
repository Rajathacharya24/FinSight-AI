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
        // Primary brand: refined violet / indigo
        brand: {
          50:  '#f2f0ff',
          100: '#e8e3ff',
          200: '#d2c8ff',
          300: '#b3a1ff',
          400: '#9270ff',
          500: '#7c4dff',
          600: '#6a30f5',
          700: '#5a21d8',
          800: '#4a1cae',
          900: '#3b1a86',
          950: '#220f4d',
        },
        // Secondary accent: electric cyan for data highlights
        accent: {
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
        },
        // Cool navy background tones
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
        // Dark surface tones (used for panels, cards, borders)
        dark: {
          50:  '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#1e2d3d',
          800: '#131d2a',
          850: '#0e1722',
          900: '#0a1019',
          950: '#060a11',
        },
        // Semantic states
        success: {
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
        },
        warning: {
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
        },
        danger: {
          300: '#fca5a5',
          400: '#f87171',
          500: '#ef4444',
          600: '#dc2626',
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-mesh': 'linear-gradient(135deg, #7c4dff 0%, #3d5af8 55%, #0ea5e9 100%)',
        'gradient-brand': 'linear-gradient(135deg, #7c4dff 0%, #5a21d8 50%, #3d5af8 100%)',
        'gradient-brand-soft': 'linear-gradient(135deg, rgba(124,77,255,0.18) 0%, rgba(14,165,233,0.12) 100%)',
        'gradient-dark': 'linear-gradient(180deg, #0a1019 0%, #060a11 100%)',
        'glass-sheen': 'linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0) 45%)',
      },
      boxShadow: {
        'glow-brand':   '0 0 30px rgba(124, 77, 255, 0.38)',
        'glow-success': '0 0 20px rgba(34, 197, 94, 0.32)',
        'glow-danger':  '0 0 20px rgba(239, 68, 68, 0.32)',
        'glass':        '0 8px 32px rgba(0, 0, 0, 0.35)',
        'card':         '0 1px 0 0 rgba(255,255,255,0.04) inset, 0 12px 32px -12px rgba(0, 0, 0, 0.65)',
        'card-hover':   '0 1px 0 0 rgba(255,255,255,0.06) inset, 0 24px 48px -16px rgba(0, 0, 0, 0.75)',
        'inset-line':   'inset 0 1px 0 0 rgba(255,255,255,0.06)',
      },
      animation: {
        'fade-in':       'fadeIn 0.4s ease-out forwards',
        'slide-up':      'slideUp 0.4s ease-out forwards',
        'slide-in-left': 'slideInLeft 0.3s ease-out forwards',
        'pulse-brand':   'pulseBrand 2.4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow':     'spin 3s linear infinite',
        'shimmer':       'shimmer 2s linear infinite',
        'float':         'float 7s ease-in-out infinite',
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
          '0%, 100%': { boxShadow: '0 0 20px rgba(124,77,255,0.30)' },
          '50%':      { boxShadow: '0 0 40px rgba(124,77,255,0.60)' },
        },
        shimmer: {
          '0%':   { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%':      { transform: 'translateY(-14px)' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
};
