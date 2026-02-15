import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import LoginPage from "./authentication/LoginPage.tsx";

import { PlayerProvider } from "./context/PlayerContext.tsx";

export default function App() {
  const router = createBrowserRouter([
    { path: "/", element: <LoginPage /> },
    
  ]);

  return <RouterProvider router={router} />;
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <PlayerProvider>
      <App />
    </PlayerProvider>
  </StrictMode>,
);
