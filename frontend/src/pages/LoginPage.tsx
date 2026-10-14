import { Link } from "react-router-dom";
export function LoginPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-slate-950 text-white">
      <div>
        <h1 className="text-3xl font-bold">Log in</h1>
        <p>Authentication is not implemented yet.</p>
        <Link className="text-cyan-300" to="/">
          Open dashboard
        </Link>
      </div>
    </main>
  );
}
