import "./AuthDialog.css";
import {useEffect, useRef, useState} from "react";
import type {MouseEvent, ReactElement} from "react";
import {useForm} from "react-hook-form";
import {login, register as registerUser} from "../../api/auth";
import {useAuth} from "../../states/AuthContext";
import {extractErrorMessage} from "../../utils/extractErrorMessage";

type Mode = "login" | "register";

type Props = {
    open: boolean;
    onClose: () => void;
};

type FormValues = {
    name: string;
    email: string;
    username: string;
    password: string;
};

export function AuthDialog({open, onClose}: Readonly<Props>): ReactElement {
    const dialogRef = useRef<HTMLDialogElement>(null);
    const {login: setAuthenticatedSession} = useAuth();
    const [mode, setMode] = useState<Mode>("login");
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);

    const {
        register: formField,
        handleSubmit,
        reset,
        formState: {errors, isSubmitting},
    } = useForm<FormValues>({
        defaultValues: {name: "", email: "", username: "", password: ""},
    });

    useEffect(() => {
        const dialog = dialogRef.current;
        if (!dialog) return;
        if (open && !dialog.open) {
            dialog.showModal();
        } else if (!open && dialog.open) {
            dialog.close();
        }
    }, [open]);

    function switchMode(nextMode: Mode) {
        setMode(nextMode);
        setError(null);
        setInfo(null);
        reset();
    }

    async function onSubmit(values: FormValues) {
        setError(null);
        try {
            if (mode === "login") {
                const response = await login({username: values.username, password: values.password});
                setAuthenticatedSession(response.data.sessionToken);
                reset();
                onClose();
            } else {
                await registerUser(values);
                reset({...values, password: ""});
                setInfo("Registrierung erfolgreich. Bitte melde dich an.");
                setMode("login");
            }
        } catch (err) {
            setError(extractErrorMessage(err));
        }
    }

    function handleDialogClick(event: MouseEvent<HTMLDialogElement>) {
        if (event.target === dialogRef.current) onClose();
    }

    const handleDialogKeyDown = (e: React.KeyboardEvent<HTMLDialogElement>) => {
        if (e.key === "Escape") {
            onClose();
        }
    };

    return (
        <dialog
            ref={dialogRef}
            className="auth-dialog"
            onClick={handleDialogClick}
            onKeyDown={handleDialogKeyDown}
            onClose={onClose}
        >
            <div className="auth-dialog__content">
                <div className="auth-dialog__header">
                    <h2>{mode === "login" ? "Anmelden" : "Registrieren"}</h2>
                    <button type="button" className="auth-dialog__close" aria-label="Schließen" onClick={onClose}>
                        &times;
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    {mode === "register" && (
                        <label>
                            Name
                            <input type="text" {...formField("name", {required: "Name wird benötigt"})}/>
                            {errors.name && <span className="auth-dialog__field-error">{errors.name.message}</span>}
                        </label>
                    )}
                    {mode === "register" && (
                        <label>
                            E-Mail
                            <input
                                type="email"
                                {...formField("email", {
                                    required: "E-Mail wird benötigt",
                                    pattern: {
                                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                                        message: "E-Mail muss gültig sein",
                                    },
                                })}
                            />
                            {errors.email && <span className="auth-dialog__field-error">{errors.email.message}</span>}
                        </label>
                    )}
                    <label>
                        Benutzername
                        <input
                            type="text"
                            autoComplete="username"
                            {...formField("username", {required: "Benutzername wird benötigt"})}
                        />
                        {errors.username &&
                            <span className="auth-dialog__field-error">{errors.username.message}</span>}
                    </label>
                    <label>
                        Passwort
                        <input
                            type="password"
                            autoComplete={mode === "login" ? "current-password" : "new-password"}
                            {...formField("password", {
                                required: "Passwort wird benötigt",
                                minLength: mode === "register"
                                    ? {value: 8, message: "Passwort muss mindestens 8 Zeichen haben"}
                                    : undefined,
                            })}
                        />
                        {errors.password &&
                            <span className="auth-dialog__field-error">{errors.password.message}</span>}
                    </label>

                    {error && <p className="auth-dialog__error" role="alert">{error}</p>}
                    {info && !error && <p className="auth-dialog__info">{info}</p>}

                    <button type="submit" className="auth-dialog__submit" disabled={isSubmitting}>
                        {mode === "login" ? "Anmelden" : "Registrieren"}
                    </button>
                </form>

                <p className="auth-dialog__switch-row">
                    {mode === "login" ? "Noch kein Konto?" : "Schon ein Konto?"}
                    {" "}
                    <button
                        type="button"
                        className="auth-dialog__switch"
                        onClick={() => switchMode(mode === "login" ? "register" : "login")}
                    >
                        {mode === "login" ? "Registrieren" : "Anmelden"}
                    </button>
                </p>
            </div>
        </dialog>
    );
}
