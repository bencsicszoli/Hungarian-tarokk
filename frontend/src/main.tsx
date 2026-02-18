import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import LoginPage from "./authentication/LoginPage.tsx";
import RegisterPage from "./authentication/RegisterPage.tsx";
import { UserProvider } from "./context/UserContext.tsx";

export default function App() {
  const router = createBrowserRouter([
    { path: "/", element: <LoginPage /> },
    { path: "/register", element: <RegisterPage /> },
    
  ]);

  return <RouterProvider router={router} />;
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <UserProvider>
      <App />
    </UserProvider>
  </StrictMode>,
);
