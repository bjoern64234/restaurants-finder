import {useState} from "react";
import type {ReactElement} from "react";
import {AuthDialog} from "../AuthDialog/AuthDialog";
import {AccountDialog} from "../AccountDialog/AccountDialog";
import account from "../../assets/account.png"
import accountAuthenticated from "../../assets/account-authenticated.png"
import {useAuth} from "../../states/AuthContext.tsx"

export function AccountButton(): ReactElement {
    const [open, setOpen] = useState(false);
    const { isAuthenticated } = useAuth();

    return (
        <>
            <button
                type="button"
                onClick={() => setOpen(true)}
                className={`${isAuthenticated ? "colored" : ""} map-button`}
                aria-label="Konto öffnen"
            >
                {
                    isAuthenticated ?
                        <img src={accountAuthenticated} alt={"Account"}/>
                        : <img src={account} alt={"Account"}/>
                }
            </button>
            {isAuthenticated
                ? <AccountDialog open={open} onClose={() => setOpen(false)}/>
                : <AuthDialog open={open} onClose={() => setOpen(false)}/>}
        </>
    );
}
