import axios from "axios";

export function extractErrorMessage(error: unknown): string {
    if (axios.isAxiosError(error) && error.response) {
        const data: unknown = error.response.data;
        if (data && typeof data === "object") {
            const record = data as Record<string, unknown>;
            if (typeof record.message === "string") return record.message;
            const fieldMessages = Object.values(record).filter((value) => typeof value === "string");
            if (fieldMessages.length > 0) return fieldMessages.join(", ");
        }
    }
    return "Etwas ist schiefgelaufen. Bitte versuche es erneut.";
}
