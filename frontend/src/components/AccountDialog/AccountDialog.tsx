import "./AccountDialog.css";
import {useEffect, useRef, useState} from "react";
import type {MouseEvent, ReactElement} from "react";
import {getCurrentUser} from "../../api/session";
import {useAuth} from "../../states/AuthContext";
import {extractErrorMessage} from "../../utils/extractErrorMessage";
import type {AuthUser} from "../../types/auth.type";
import {logout as logoutUser} from "../../api/auth.ts";

type Props = {
    open: boolean;
    onClose: () => void;
};

export function AccountDialog({open, onClose}: Readonly<Props>): ReactElement {
    const dialogRef = useRef<HTMLDialogElement>(null);
    const {logout} = useAuth();
    const [user, setUser] = useState<AuthUser | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const dialog = dialogRef.current;
        if (!dialog) return;
        if (open && !dialog.open) {
            dialog.showModal();
        } else if (!open && dialog.open) {
            dialog.close();
        }
    }, [open]);

    useEffect(() => {
        if (!open) return;
        setLoading(true);
        setError(null);
        getCurrentUser()
            .then((response) => setUser(response.data))
            .catch((err) => setError(extractErrorMessage(err)))
            .finally(() => setLoading(false));
    }, [open]);

    function handleDialogClick(event: MouseEvent<HTMLDialogElement>) {
        if (event.target === dialogRef.current) onClose();
    }

    async function handleLogout() {
        try {
            await logoutUser();
        } catch (err) {
            console.error(extractErrorMessage(err));
        } finally {
            logout();
            setUser(null);
            onClose();
        }
    }

    return (
        <dialog
            ref={dialogRef}
            className="account-dialog"
            onClick={handleDialogClick}
            onClose={onClose}
        >
            <div className="account-dialog__content">
                <div className="account-dialog__header">
                    <h2>Mein Konto</h2>
                    <button type="button" className="account-dialog__close" aria-label="Schließen" onClick={onClose}>
                        &times;
                    </button>
                </div>

                {loading && <p>Lade...</p>}
                {error && <p className="account-dialog__error" role="alert">{error}</p>}

                {user && !loading && !error && (
                    <dl className="account-dialog__details">
                        <dt>Name</dt>
                        <dd>{user.name}</dd>
                        <dt>Benutzername</dt>
                        <dd>{user.username}</dd>
                        <dt>E-Mail</dt>
                        <dd>{user.email}</dd>
                    </dl>
                )}

                <button type="button" className="account-dialog__logout" onClick={handleLogout}>
                    Abmelden
                </button>
            </div>
        </dialog>
    );
}
