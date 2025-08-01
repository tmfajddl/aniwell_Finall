'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'

export default function EditPage() {
  const router = useRouter()

  // 사용자 정보 상태
  const [user] = useState({
    id: 'example_user',
    nickname: '정민123',
    email: 'jm@example.com',
    phone: '010-1234-5678',
    address: '대전광역시 서구 정림동',
  })

  const [form, setForm] = useState({
    nickname: user.nickname,
    email: user.email,
    phone: user.phone,
    address: user.address,
    password: '',
    confirmPassword: '',
  })

  const handleChange = (field: string, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }))
  }

  const handleSubmit = () => {
    if (form.password !== form.confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }
    alert('수정 완료되었습니다!')
    router.push('/my-page')
  }

  return (
    <div className="flex flex-col min-h-screen p-6">
      {/* 🔹 상단 뒤로가기 */}
      <div className="mb-4">
        <button onClick={() => router.back()} className="text-blue-600 hover:underline">
          &lt; 뒤로가기
        </button>
      </div>

      {/* 🔸 3등분 레이아웃 */}
      <div className="flex flex-1 justify-between">

        {/* 🟠 1/3: 아이디, 비밀번호 */}
        <div className="flex gap-4 max-w-4xl mx-auto w-full">
          <div className="flex gap-4 flex-col">
            <div className="flex-1">
              <label className="block text-sm font-semibold">아이디</label>
              <div className="mt-1 p-2 bg-gray-100 rounded">{user.id}</div>
            </div>
            <div className="flex-1">
              <label className="block text-sm font-semibold">비밀번호</label>
              <input
                type="password"
                value={form.password}
                onChange={(e) => handleChange('password', e.target.value)}
                className="mt-1 w-full border rounded px-3 py-1"
              />
            </div>
            <div className="flex-1">
              <label className="block text-sm font-semibold">비밀번호 확인</label>
              <input
                type="password"
                value={form.confirmPassword}
                onChange={(e) => handleChange('confirmPassword', e.target.value)}
                className="mt-1 w-full border rounded px-3 py-1"
              />
            </div>
          </div>
        </div>

        {/* 🟡 2/3: 닉네임, 이메일, 전화번호 */}
        <div className="flex gap-4 max-w-4xl mx-auto w-full">
          <div className="flex gap-4 flex-col">
            <div className="flex-1">
              <label className="block text-sm font-semibold">닉네임</label>
              <input
                type="text"
                value={form.nickname}
                onChange={(e) => handleChange('nickname', e.target.value)}
                className="mt-1 w-full border rounded px-3 py-1"
              />
            </div>
            <div className="flex-1">
              <label className="block text-sm font-semibold">이메일</label>
              <input
                type="email"
                value={form.email}
                onChange={(e) => handleChange('email', e.target.value)}
                className="mt-1 w-full border rounded px-3 py-1"
              />
            </div>
            <div className="flex-1">
              <label className="block text-sm font-semibold">전화번호</label>
              <input
                type="tel"
                value={form.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                className="mt-1 w-full border rounded px-3 py-1"
              />
            </div>
          </div>
        </div>

        {/* 🟢 3/3: 주소 + 수정 완료 */}
        <div className="flex gap-3 max-w-4xl mx-auto w-full">
          <label className="block text-sm font-semibold">주소</label>
          <input
            type="text"
            value={form.address}
            onChange={(e) => handleChange('address', e.target.value)}
            className="mt-1 w-full border rounded px-3 py-1"
          />

          <div className="flex justify-end mt-3">
            <button
              onClick={handleSubmit}
              className="bg-yellow-300 hover:bg-yellow-400 px-6 py-2 rounded-md shadow text-black"
            >
              수정 완료
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
