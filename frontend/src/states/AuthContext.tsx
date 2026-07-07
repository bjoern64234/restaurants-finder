import { createContext, useContext, useEffect, useState } from "react";
import { getCurrentUser } from "../api/session";

type AuthContextType = {
    isAuthenticated: boolean;
    token: string | null;
    login: (token: string) => void;
    logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [token, setToken] = useState<string | null>(null);

    const login = (newToken: string) => {
        localStorage.setItem("sessionToken", newToken);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem("sessionToken");
        setToken(null);
    };

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const oauthToken = params.get("token");
        if (oauthToken) {
            login(oauthToken);
            params.delete("token");
            const nextSearch = params.toString();
            window.history.replaceState(null, "", window.location.pathname + (nextSearch ? `?${nextSearch}` : ""));
            return;
        }

        const storedToken = localStorage.getItem("sessionToken");
        if (storedToken) setToken(storedToken);
    }, []);

    // Stored/OAuth token might be expired or revoked server-side; confirm it still resolves to a user.
    useEffect(() => {
        if (!token) return;
        getCurrentUser().catch(() => logout());
    }, [token]);

    useEffect(() => {
        window.addEventListener("auth:sessionExpired", logout);
        return () => window.removeEventListener("auth:sessionExpired", logout);
    }, []);

    return (
        <AuthContext.Provider
            value={{
                isAuthenticated: !!token,
                token,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}