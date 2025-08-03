type Props = {
  name: string
  intro?: string
}

export default function CrewCard({ name, intro }: Props) {
  return (
    <div className="flex items-center gap-4 bg-yellow-100 px-4 py-3 rounded-xl shadow hover:shadow-lg transition">
      {/* 원형 이미지 자리 */}
      <div className="w-12 h-12 bg-yellow-300 rounded-full" />

      {/* 크루 소개 */}
      <div className="text-sm">
        <p className="font-semibold">{name}</p>
        {intro && <p className="text-gray-600">{intro}</p>}
      </div>
    </div>
  )
}
