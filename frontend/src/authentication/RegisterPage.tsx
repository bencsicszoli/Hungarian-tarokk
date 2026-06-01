import { useState } from "react";
import { useNavigate } from "react-router-dom";
import CardTableDecoration from "../pageComponents/CardTableDecoration";
import InputField from "../pageComponents/InputField";

function RegistrationPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [regError, setRegError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function postRegistration() {
    try {
      const response = await fetch(`/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password, email }),
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      navigate(`/`);
    } catch (e) {
      setRegError(e instanceof Error ? e.message : "An unknown error occurred");
    }
  }

  function handleRegistration(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    postRegistration();
  }

  function switchToLogin() {
    navigate(`/`);
  }

  return (
        <div className="w-full h-screen bg-[#2f4b3a] flex flex-col items-center justify-center text-white px-6 sm:px-8">
          <CardTableDecoration />

          <h2 className="text-4xl font-extrabold mb-11 drop-shadow-lg text-center text-green-100">
            Welcome to Tarokk game!
          </h2>

          <div className="z-0 w-full bg-[#2f4b3a] border border-green-300 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-green-100 md:text-2xl">
                Create an account
              </h1>
              <form
                className="space-y-4 md:space-y-6"
                onSubmit={handleRegistration}
              >
                <InputField
                  htmlFor="usrnm"
                  labelText="Your username"
                  inputType="text"
                  inputName="username"
                  inputId="usrnm"
                  placeholderText="username"
                  inputValue={username}
                  onInputValue={setUsername}
                  autoComplete="off"
                />
                <InputField
                  htmlFor="email"
                  labelText="Your email"
                  inputType="email"
                  inputName="email"
                  inputId="email"
                  placeholderText="name@company.com"
                  inputValue={email}
                  onInputValue={setEmail}
                  autoComplete="off"
                />
                <InputField
                  htmlFor="password"
                  labelText="Password"
                  inputType="password"
                  inputName="password"
                  inputId="password"
                  placeholderText="••••••••"
                  inputValue={password}
                  onInputValue={setPassword}
                  autoComplete="off"
                />

                {regError && (
                  <p className="text-lg font-semibold text-red-200 dark:text-red-400">
                    {regError}
                  </p>
                )}
                <button
                  type="submit"
                  className="w-full text-[#2f4b3a] bg-green-300 hover:bg-green-500 focus:ring-4 focus:outline-none focus:ring-green-400 font-medium rounded-lg text-md px-5 py-2.5 text-center"
                >
                  Create an account
                </button>
                <div className="flex items-center justify-center gap-4">
                  <p className=" text-green-100">
                    Already have an account?
                  </p>
                  <button
                    onClick={switchToLogin}
                    className="font-medium text-green-100 hover:underline"
                  >
                    Login here
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
  );
}

export default RegistrationPage;
