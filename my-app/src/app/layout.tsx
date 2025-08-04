// src/app/layout.tsx
'use client'

import './globals.css'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <div className="flex h-full">
          <main className="h-full p-1 overflow-hidden w-full mx-auto">
            {children}
          </main>
        </div>
      </body>
    </html>
  )
}
