// src/app/crew/walkCrew/page.tsx
'use client'
import { useEffect, useState } from 'react'
import { FiSearch } from 'react-icons/fi'
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/crew/Sidebar'


type WalkCrew = {
  id: number
  title: string
  description: string
  districtId: number
  leaderId: number
  createdAt: string // Java의 LocalDateTime은 문자열로 받음 (ISO 8601)

  // optional fields (join된 경우에만 올 수도 있음)
  nickname?: string
  city?: string
  district?: string
  dong?: string
}



export default function WalkCrew() {
  const [crewList, setCrewList] = useState<WalkCrew[]>([])
  const router = useRouter();
  const [open, setOpen] = useState(false)

  const [query, setQuery] = useState('')
  const handleSearch = () => {
    console.log('🔍 검색어:', query)
    // 여기에 fetch나 필터링 함수 연결 일단 콘솔 디버깅으로 ㄱㄱ
  }

  useEffect(() => {
    fetch('http://localhost:8080/usr/walkCrew/api/list', {
      credentials: 'include', // 세션 로그인 유지 시 필요
    })
      .then(res => {
        if (!res.ok) throw new Error('서버 오류')
        return res.json()
      })
      .then((resData) => {
        console.log('✅ 전체 응답:', resData)
        const crewArray: WalkCrew[] = resData.data1
        setCrewList(crewArray)
      })
      .catch(err => console.error('❌ 크루 리스트 에러:', err))
  }, [])

  return (

    <div className="h-[97%]">
      <div className="stickypb">
        <div className="w-full max-w-xl mx-auto">
          <div className="flex items-center bg-white border border-gray-200 shadow-sm rounded-full px-4 py-2">
            <input
              type="text"
              placeholder="검색어를 입력하세요"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="flex-1 text-sm text-gray-800 bg-transparent outline-none placeholder:text-gray-400"
            />
            <button onClick={handleSearch} className="ml-2 text-gray-500 hover:text-black">
              <FiSearch className="w-5 h-5" />
            </button>
          </div>
        </div>
        <div className="text-center">
          <button onClick={() => setOpen(true)} className="my-6 mb-3 px-7 py-2 rounded-xl text-center font-semibold shadow bg-[#e4f0b9] hover:bg-[#FEEEA4]">등록하기</button>
        </div>
      </div>

      {/* 모달 */}
      {open && (
        <div className="fixed inset-0 z-50 shadow flex items-center justify-center">
          <div className="relative mx-[-50px] bg-white w-[90%] h-[60%] rounded-xl shadow-lg overflow-hidden">
            {/* 닫기 버튼 */}
            <button
              onClick={() => setOpen(false)}
              className="absolute top-3 right-4 text-2xl text-gray-600 hover:text-black"
            >
              &times;
            </button>

            {/* iframe으로 외부 페이지 삽입 */}
            <iframe
              src="http://localhost:8080/usr/walkCrew/create"
              className="w-full h-full border-none"
            ></iframe>
          </div>
        </div>
      )}

      <div className="overflow-y-auto w-full h-[90%] pb-1">
        <div className="p-4 space-y-4">
          {crewList.length === 0 ? (
            <p className="text-center text-gray-400">등록된 크루가 없습니다.</p>
          ) : (
            crewList.map((crew) => (
              <div
                key={crew.id}
                className="bg-white rounded-lg shadow p-4 flex gap-4 items-start"
                onClick={() => window.location.href= (`http://localhost:8080/usr/walkCrew/detail/${crew.id}`)}
              >
                {/* 썸네일 자리 (필요 시 이미지 추가 가능) */}
                <div className="w-20 h-20 bg-gray-300 rounded-md shrink-0" />

                {/* 본문 */}
                <div className="flex-1 space-y-2">
                  <h3 className="text-lg font-bold">{crew.title}</h3>
                  <p className="text-sm text-gray-700 line-clamp-2">
                    {crew.description || '설명이 없습니다.'}
                  </p>
                  <div className="flex items-center gap-1 text-sm text-gray-600">
                    <span>📍</span>
                    <span>
                      {crew.city ?? ''} {crew.district ?? ''} {crew.dong ?? ''}
                    </span>
                  </div>
                  <div className="text-xs text-gray-400">
                    작성자: {crew.nickname || '익명'} ·{' '}
                    {new Date(crew.createdAt).toLocaleDateString('ko-KR')}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
