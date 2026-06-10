import { useState, useEffect, type SubmitEventHandler } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "../context/UserContext.tsx";
import InputField from "./components/InputField.tsx";

function LoginPage() {
  const userContext = useUser();
  if (!userContext) {
    throw new Error("useUser must be used within UserProvider");
  }
  const { setUser, token, setToken } = userContext;
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (token) localStorage.setItem("jwtToken", token);
  }, [token]);

  async function postLogin() {
    try {
      const response = await fetch(`/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      const { jwt } = await response.json();
      localStorage.setItem("jwtToken", jwt);
      setToken(jwt);

      const playerRes = await fetch(`/api/auth/me`, {
        headers: { Authorization: "Bearer " + jwt },
      });
      if (!playerRes.ok) throw new Error("Could not fetch user info");
      const playerData = await playerRes.json();
      setUser(playerData);

      setIsLoggedIn(true);
      setError(null);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "An unknown error occurred",
      );
    }
  }

  function switchToRegister() {
    navigate(`/register`);
  }

  const handleLogin: SubmitEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    postLogin();
  };

  useEffect(() => {
    if (isLoggedIn) {
      navigate("/menu");
    }
  }, [isLoggedIn, navigate]);

  return (
    <div className="w-full h-screen bg-[#2f4b3a] flex flex-col items-center justify-center text-white px-6 sm:px-8">
      {/*<CardTableDecoration />*/}

      <h2 className="text-4xl font-extrabold mb-11 drop-shadow-lg text-center text-green-100">
        Welcome to Tarokk game!
      </h2>
      <div className="w-full border border-green-300 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
        <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
          <h1 className="text-xl font-bold leading-tight tracking-tight text-green-100 md:text-2xl">
            Sign in to your account
          </h1>
          <form className="space-y-4 md:space-y-6" onSubmit={handleLogin}>
            <InputField
              htmlFor="username"
              labelText="Your username"
              inputType="text"
              inputName="username"
              inputId="username"
              placeholderText="username"
              inputValue={username}
              onInputValue={setUsername}
              autoComplete="off"
            />
            <InputField
              htmlFor="pwd"
              labelText="Password"
              inputType="password"
              inputName="pwd"
              inputId="pwd"
              placeholderText="••••••••"
              inputValue={password}
              onInputValue={setPassword}
              autoComplete="off"
            />

            {error && (
              <p className="text-sm font-light text-red-500 dark:text-red-400">
                {error}
              </p>
            )}
            <div className="flex items-center justify-between">
              <div className="flex items-start">
                <div className="flex items-center h-5">
                  <input
                    id="remember"
                    aria-describedby="remember"
                    type="checkbox"
                    className="w-4 h-4 border border-green-300 rounded bg-green-300 focus:ring-3 focus:ring-primary-300"
                  />
                </div>
                <div className="ml-3 text-md">
                  <label htmlFor="remember" className="text-green-50">
                    Remember me
                  </label>
                </div>
              </div>
              <a href="#" className="text-md text-green-50 hover:underline">
                Forgot password?
              </a>
            </div>
            <button
              type="submit"
              className="w-full text-green-50 bg-[#23392c] hover:bg-[#3b5f4a] focus:ring-4 focus:outline-none focus:ring-green-300 cursor-pointer rounded-lg text-lg px-5 py-2.5 text-center"
            >
              Sign in
            </button>
          </form>
          <div className="flex flex-col items-center justify-center gap-2 mt-4">
            <p className="text-md text-green-50">Don’t have an account yet? </p>
            <button
              onClick={switchToRegister}
              className="w-full text-green-50 bg-[#49745a] hover:bg-[#294132] focus:ring-4 focus:outline-none focus:ring-blue-300 cursor-pointer rounded-lg text-lg px-5 py-2.5 text-center"
            >
              Sign up
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage; // [#2f4b3a]
