import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import enCommon from "./locales/en/common.json";
import huCommon from "./locales/hu/common.json";
import enMessages from "./locales/en/messages.json";
import huMessages from "./locales/hu/messages.json";

function detectInitialLanguage(): "en" | "hu" {
  const stored = localStorage.getItem("lang");
  if (stored === "en" || stored === "hu") {
    return stored;
  }
  return navigator.language.toLowerCase().startsWith("hu") ? "hu" : "en";
}

i18n.use(initReactI18next).init({
  lng: detectInitialLanguage(),
  fallbackLng: "en",
  defaultNS: "common",
  ns: ["common", "messages"],
  resources: {
    en: { common: enCommon, messages: enMessages },
    hu: { common: huCommon, messages: huMessages },
  },
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
