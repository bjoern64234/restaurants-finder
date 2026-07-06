export type LoginRequest = {
    username: string;
    password: string;
};

export type RegisterRequest = {
    name: string;
    email: string;
    username: string;
    password: string;
};

export type AuthResponse = {
    sessionToken: string;
};

export type AuthUser = {
    id: number;
    name: string;
    email: string;
    username: string;
};
