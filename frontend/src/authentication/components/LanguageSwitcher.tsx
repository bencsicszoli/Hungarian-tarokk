import { useTranslation } from "react-i18next";

function LanguageSwitcher() {
  const { i18n } = useTranslation();

  function changeLanguage(lang: "en" | "hu") {
    i18n.changeLanguage(lang);
    localStorage.setItem("lang", lang);
  }

  return (
    <div className="absolute top-4 right-4 flex gap-2">
      <button
        type="button"
        onClick={() => changeLanguage("en")}
        aria-label="English"
        className={`text-2xl cursor-pointer rounded-md p-1 transition ${
          i18n.language === "en"
            ? "opacity-100 ring-2 ring-green-300"
            : "opacity-50 hover:opacity-80"
        }`}
      >
        🇬🇧
      </button>
      <button
        type="button"
        onClick={() => changeLanguage("hu")}
        aria-label="Magyar"
        className={`text-2xl cursor-pointer rounded-md p-1 transition ${
          i18n.language === "hu"
            ? "opacity-100 ring-2 ring-green-300"
            : "opacity-50 hover:opacity-80"
        }`}
      >
        🇭🇺
      </button>
    </div>
  );
}

export default LanguageSwitcher;
