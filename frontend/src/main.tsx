import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./i18n";
import LoginPage from "./authentication/LoginPage.tsx";
import RegisterPage from "./authentication/RegisterPage.tsx";
import { UserProvider } from "./context/UserContext.tsx";
import { WebSocketProvider } from "./context/WebSocketContext.tsx";
import MenuPage from "./MenuPage.tsx";
import Game from "./Game.tsx";
import EditPage from "./EditPage.tsx";

export default function App() {
  const router = createBrowserRouter([
    { path: "/", element: <LoginPage /> },
    { path: "/register", element: <RegisterPage /> },
    { path: "/menu", element: <MenuPage /> },
    { path: "/game", element: <Game /> },
    { path: "/edit", element: <EditPage />}
    
  ]);

  return <RouterProvider router={router} />;
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <UserProvider>
      <WebSocketProvider>
        <App />
      </WebSocketProvider>
    </UserProvider>
  </StrictMode>,
);
