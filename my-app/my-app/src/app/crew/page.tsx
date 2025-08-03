'use client'

import Sidebar from '../components/side/Sidebar'
import CrewCard from '../components/CrewCard'
import Image from 'next/image'
import { useEffect, useState } from 'react'



type crews = [{
  id: number;
  title: string;
  description: string;
  // 필요한 필드 추가
}];

type Article = {
  id: number;
  title: string;
  body: string;
  createdAt: string;
};
//테스트데이터
const myCrew = {
  id: 10,
  title: "내가 만든 크루",
  description: "크루 설명 예시",
  imageUrl: "https://via.placeholder.com/300"
}

const myCrews = [
  { id: 1, title: "궁동 아침크루", description: "매일 아침 7시에 모입니다." },
  { id: 2, title: "반려견 산책", description: "주말마다 유성천에서 모여요." }
];



export default function CrewPage() {
  const [crews, setCrews] = useState<crews | null>(null);
  const [articles, setArticles] = useState<Article[]>([]);
  const [msg, setMsg] = useState<string | null>(null);;


  useEffect(() => {
    fetch("http://localhost:8080/usr/crewCafe/apiMyCrewCafe", {
      method: "GET",
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error(`서버 오류: ${res.status}`);
        }
        return res.json(); // 여기서 안전하게 파싱
      })
      .then((data) => {
        if (data.resultCode === "S-1") {
          setCrews(data.data.myCrew);
          setArticles(data.data.articles);
        } else {
          setMsg(data.msg);
        }
      })
      .catch((err) => {
        console.error("❌ fetch 실패:", err);
        setMsg("서버와 통신 중 오류가 발생했습니다.");
      });

  }, []);

  return (
    <div className="flex mx-auto jutify-center h-full ">
      <div className="p-6">
        <Sidebar />
      </div>
      <div className="p-6 mt-[-20px] bg-white rounded-lg w-full shadow min-w-3xl mx-auto flex justify-around">
        {/* 좌측: 참가한 크루 목록 */}
        <div className="flex-1 pr-6">
          <div className="">
            <h2 className="text-xl font-bold border-b pb-3">참가한 크루</h2>
          </div>

          <div className="space-y-2 overflow-y-auto h-[80%] mt-4">
            {crews ? (
              crews.map(crew => (
                <CrewCard key={crew.id} name={crew.title} intro={crew.description} />
              ))
            ) : (
              <p className="text-sm text-gray-400">참가한 크루가 없습니다.</p>
            )}
          </div>
        </div>

        {/* 우측: 내가 만든 크루 */}
        <div className="w-[350px] h-[350px] rounded-xl overflow-hidden shadow ml-6 p-4 flex flex-col justify-between bg-gray-50">
          <h2 className="text-lg font-bold text-gray-800 mb-2">나의 크루</h2>
          <div className="space-y-3 overflow-y-auto">
            {myCrews && myCrews.length > 0 ? (
              myCrews.map((crew) => (
                <div key={crew.id} className="bg-white rounded-lg p-3 shadow hover:shadow-md transition cursor-pointer">
                  <p className="font-semibold text-sm">{crew.title}</p>
                  <p className="text-xs text-gray-500 truncate">{crew.description}</p>
                </div>
              ))
            ) : (
              <p className="text-sm text-gray-400">내가 만든 크루가 없습니다.</p>
            )}
          </div>
        </div>
      </div>

    </div>
  )
}
