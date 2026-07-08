import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import InputField from "./components/InputField";
import { translateMessage } from "../i18n/translateMessage";

function RegistrationPage() {
  const { t } = useTranslation();
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
        throw new Error(translateMessage(await response.json()));
      }
      navigate(`/`);
    } catch (e) {
      setRegError(e instanceof Error ? e.message : t("auth.errors.unknown"));
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

      <h2 className="text-4xl font-extrabold mb-11 drop-shadow-lg text-center text-green-50">
        {t("auth.welcome")}
      </h2>

      <div className="z-0 w-full bg-[#2f4b3a] border border-green-300 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
        <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
          <h1 className="text-xl font-bold leading-tight tracking-tight text-green-50 md:text-2xl">
            {t("auth.register.title")}
          </h1>
          <form
            className="space-y-4 md:space-y-6"
            onSubmit={handleRegistration}
          >
            <InputField
              htmlFor="usrnm"
              labelText={t("auth.register.usernameLabel")}
              inputType="text"
              inputName="username"
              inputId="usrnm"
              placeholderText={t("auth.register.usernamePlaceholder")}
              inputValue={username}
              onInputValue={setUsername}
              autoComplete="off"
            />
            <InputField
              htmlFor="email"
              labelText={t("auth.register.emailLabel")}
              inputType="email"
              inputName="email"
              inputId="email"
              placeholderText={t("auth.register.emailPlaceholder")}
              inputValue={email}
              onInputValue={setEmail}
              autoComplete="off"
            />
            <InputField
              htmlFor="password"
              labelText={t("auth.register.passwordLabel")}
              inputType="password"
              inputName="password"
              inputId="password"
              placeholderText={t("auth.register.passwordPlaceholder")}
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
              className="w-full text-green-50 bg-[#23392c] hover:bg-[#3b5f4a] focus:ring-4 focus:outline-none focus:ring-green-300 cursor-pointer rounded-lg text-lg px-5 py-2.5 text-center"
            >
              {t("auth.register.submit")}
            </button>
            <div className="flex items-center justify-center gap-4 text-lg">
              <p className="text-green-50">{t("auth.register.haveAccount")}</p>
              <button
                onClick={switchToLogin}
                className="font-medium text-green-50 hover:underline"
              >
                {t("auth.register.loginHere")}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default RegistrationPage;
