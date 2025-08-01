'use client'

import Sidebar from '../components/side/Sidebar'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'


type Member = {
  id: number
  regDate: string  // ISO 형식 날짜 문자열로 받음
  updateDate: string
  loginId: string
  loginPw: string
  name: string
  nickname: string
  cellphone: string
  email: string
  delStatus: boolean
  delDate: string | null
  authLevel: number
  authName: string
  photo: string

  vetCertUrl: string
  vetCertApproved: number | null
}


export default function MyPage() {
  const router = useRouter()
  const [member, setMember] = useState<Member | null>(null)

  const handleEditClick = () => {
    router.push('/my-page/edit') // 원하는 경로로 이동
  }

  useEffect(() => {
    fetch('http://localhost:8080/api/member/myPage', {
      credentials: 'include',
    })
      .then((res) => {
        if (!res.ok) throw new Error('로그인 필요')
        return res.json()
      })
      .then((data) => {
        console.log("✅ 불러온 회원정보", data)
        setMember(data)
      })
      .catch((err) => {
        alert('로그인이 필요합니다.')
        console.error(err)
      })
  }, [])

  if (!member) return <p>로딩 중...</p>

  return (

    <div className="flex mx-auto jutify-center">
      <div className="p-6">
        <Sidebar />
      </div>
      <div className="p-6 mt-[-10px] bg-white rounded-lg shadow w-full min-w-3xl mx-auto">
        <div className="mb-6">
          <h2 className="text-xl w-full font-bold border-b pb-">회원정보</h2>
        </div>
        <div className="flex w-full gap-10 items-center justify-around mt-5">
          {/* 프로필 이미지 */}
          <div className="relative">
            <div className="w-40 h-40 rounded-full bg-yellow-200"></div>
            <div className="absolute bottom-2 right-2 bg-green-100 p-2 rounded-full shadow">
              <svg xmlns="http://www.w3.org/2000/svg" className="w-6 h-6 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </div>
          </div>

          {/* 회원 정보 텍스트 */}
          <div className="text-sm space-y-2 w-full">
            <p><strong>이름:</strong> {member.name}</p>
            <p><strong>ID:</strong> {member.loginId}</p>
            {member.email && <p><strong>이메일:</strong> {member.email}</p>}
            {member.authName && <p><strong>등급:</strong> {member.authName}</p>}
          </div>
        </div>

        <div className="flex justify-end mt-6">
          <button onClick={handleEditClick} className="bg-yellow-200 hover:bg-yellow-300 text-black px-4 py-2 rounded-md shadow">
            회원정보 수정
          </button>
        </div>
      </div>
    </div>

  )
}
