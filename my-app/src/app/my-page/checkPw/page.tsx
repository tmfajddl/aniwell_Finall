"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function CheckPwPage() {
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const res = await fetch("http://localhost:8080/usr/member/doCheckPw", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ loginPw: password }),
        credentials: "include",
      });

      const text = await res.text();
      if (text === "OK" || text === "SOCIAL_OK") {
        router.push("/my-page/edit");
      } else {
        setErrorMsg(text);
      }
    } catch (err) {
      console.error(err);
      setErrorMsg("요청 중 오류 발생");
    }
  };

  return (
    <section className="mt-12 text-lg px-4">
      <div className="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md">
        <h1 className="text-2xl font-bold mb-6 text-center">비밀번호 확인</h1>

        <form onSubmit={handleSubmit}>
          <table className="w-full table-auto text-sm">
            <tbody>
              <tr className="border-t">
                <th className="text-left px-4 py-2 w-1/3">비밀번호</th>
                <td className="px-4 py-2">
                  <input
                    name="loginPw"
                    type="password"
                    placeholder="비밀번호 입력"
                    className="input input-sm w-full"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="off"
                  />
                </td>
              </tr>
              <tr className="border-t">
                <td colSpan={2} className="text-center py-4">
                  <button type="submit" className="btn btn-primary">
                    확인
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </form>

        {errorMsg && (
          <div className="text-center mt-4 text-red-500">{errorMsg}</div>
        )}

        <div className="text-center mt-4">
          <button className="btn" type="button" onClick={() => router.back()}>
            ← 뒤로가기
          </button>
        </div>
      </div>
    </section>
  );
}
