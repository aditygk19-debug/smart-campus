import { useState } from "react";
import {
  GraduationCap,
  BookOpen,
  ShieldCheck,
  LogIn,
  Loader2,
  Activity,
  CalendarClock,
  Route,
} from "lucide-react";

const ROLES = [
  { id: "student", label: "Student", icon: GraduationCap, placeholder: "e.g. 2403163" },
  { id: "faculty", label: "Faculty", icon: BookOpen, placeholder: "e.g. 10001" },
  { id: "admin", label: "Admin", icon: ShieldCheck, placeholder: "e.g. 99999" },
];

const FEATURES = [
  {
    icon: Activity,
    title: "Real-time availability",
    text: "See occupied vs free resources instantly.",
  },
  {
    icon: CalendarClock,
    title: "Smart scheduling",
    text: "FCFS + Priority handled by the OS engine.",
  },
  {
    icon: Route,
    title: "Shortest path routing",
    text: "DSA engine finds the nearest free lab.",
  },
];

// Pass an async `onLogin({ role, userId, password })` that throws on failure.
export default function LoginPage({ onLogin }) {
  const [role, setRole] = useState("student");
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const activeRole = ROLES.find((r) => r.id === role);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!userId.trim() || !password) {
      setError("Enter your User ID and password.");
      return;
    }

    setLoading(true);
    try {
      if (onLogin) await onLogin({ role, userId: userId.trim(), password });
    } catch (err) {
      setError(err?.message || "Invalid User ID or password for this role.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-slate-50 font-sans text-slate-800">
      {/* LEFT: branding */}
      <aside className="relative w-full md:w-1/2 lg:w-3/5 bg-gradient-to-br from-indigo-600 to-indigo-900 text-white flex flex-col justify-between px-8 py-8 md:px-12 md:py-12 lg:px-20 lg:py-16">
        <div className="my-auto py-4 md:py-0">
          <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold tracking-tight leading-tight">
            Smart Campus
          </h1>
          <p className="mt-2 text-lg md:text-xl lg:text-2xl font-medium text-indigo-200">
            Lab &amp; Resource Optimizer
          </p>

          {/* Feature rows: hidden on mobile to keep the header short */}
          <ul className="hidden md:block mt-12 space-y-6 max-w-md">
            {FEATURES.map(({ icon: Icon, title, text }) => (
              <li key={title} className="flex items-start gap-4">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white/10 ring-1 ring-white/20">
                  <Icon className="h-5 w-5 text-indigo-100" aria-hidden="true" />
                </span>
                <div>
                  <p className="font-semibold">{title}</p>
                  <p className="text-sm text-indigo-200">{text}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </aside>

      {/* RIGHT: login form */}
      <main className="w-full md:w-1/2 lg:w-2/5 flex items-center justify-center px-6 py-10 md:px-10 bg-white md:bg-slate-50">
        <div className="w-full max-w-md bg-white rounded-xl border border-slate-200 shadow-sm md:shadow-md p-6 sm:p-8">
          <h2 className="text-2xl font-semibold text-slate-800">Sign in</h2>
          <p className="mt-1 text-sm text-slate-500">Choose your role to continue</p>

          <form onSubmit={handleSubmit} className="mt-6" noValidate>
            {/* Role selector */}
            <div
              role="radiogroup"
              aria-label="Role"
              className="grid grid-cols-3 gap-3"
            >
              {ROLES.map(({ id, label, icon: Icon }) => {
                const selected = role === id;
                return (
                  <button
                    key={id}
                    type="button"
                    role="radio"
                    aria-checked={selected}
                    onClick={() => {
                      setRole(id);
                      setError("");
                    }}
                    className={`flex flex-col items-center gap-1.5 rounded-xl border px-2 py-3 text-sm font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 ${
                      selected
                        ? "border-indigo-600 bg-indigo-50 text-indigo-700"
                        : "border-slate-200 text-slate-600 hover:bg-indigo-50/60 hover:border-indigo-200"
                    }`}
                  >
                    <Icon className="h-5 w-5" aria-hidden="true" />
                    {label}
                  </button>
                );
              })}
            </div>

            {/* Fields */}
            <div className="mt-6 space-y-4">
              <div>
                <label
                  htmlFor="userId"
                  className="block text-sm font-medium text-slate-700 mb-1.5"
                >
                  User ID
                </label>
                <input
                  id="userId"
                  type="text"
                  inputMode="numeric"
                  autoComplete="username"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  placeholder={activeRole.placeholder}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:border-transparent"
                />
              </div>

              <div>
                <label
                  htmlFor="password"
                  className="block text-sm font-medium text-slate-700 mb-1.5"
                >
                  Password
                </label>
                <input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:border-transparent"
                />
              </div>
            </div>

            {/* Error area (reserved height so layout doesn't jump) */}
            <div className="min-h-[1.5rem] mt-3" aria-live="polite">
              {error && <p className="text-sm text-red-600">{error}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="mt-2 w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {loading ? (
                <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
              ) : (
                <LogIn className="h-4 w-4" aria-hidden="true" />
              )}
              Sign in
            </button>
          </form>

          <p className="mt-6 text-center text-xs text-slate-400">
            Credentials are validated against the campus database.
          </p>
        </div>
      </main>
    </div>
  );
}