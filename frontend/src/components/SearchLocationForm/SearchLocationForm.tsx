import "./SearchLocationForm.css"
import React, {useEffect, useRef, useState} from "react";
import axios from "axios";
import {autocompleteLocations} from "../../api/autocomplete";
import type {AutocompleteSuggestion} from "../../types/autocomplete.type";
import type {LatLngTuple} from "leaflet";
import submitIcon from "../../assets/submit-icon.png";

type Props = {
    onSubmit: (data: LatLngTuple) => void
}
export default function SearchLocationForm({onSubmit}: Readonly<Props>) {
    const [query, setQuery] = useState("");
    const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([]);
    const [activeIndex, setActiveIndex] = useState(-1);
    const [position, setPosition] = useState<LatLngTuple | null>(null);
    const inputRef = useRef<HTMLInputElement>(null);
    const containerRef = useRef<HTMLFormElement>(null);

    // Debounce: erst 300ms nach dem letzten Tastendruck fetchen.
    // Der Cleanup bricht bei jedem neuen Tastendruck den offenen Timer
    // UND eine ggf. schon laufende Anfrage ab (verhindert veraltete Vorschläge).
    useEffect(() => {
        if (query.length < 3) return;

        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            autocompleteLocations(query, controller.signal)
                .then((response) => {
                    setSuggestions(response.data.results);
                    setActiveIndex(-1);
                })
                .catch((error) => {
                    if (!axios.isCancel(error)) throw error;
                });
        }, 300);

        return () => {
            controller.abort();
            clearTimeout(timeoutId);
        };
    }, [query]);

    // Liste schließen, wenn außerhalb des Formulars geklickt wird
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setSuggestions([]);
                setActiveIndex(-1);
            }
        }

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    function selectSuggestion(suggestion: AutocompleteSuggestion) {
        setPosition([suggestion.lat, suggestion.lon]);
        setQuery(suggestion.formatted);
        setSuggestions([]);
        setActiveIndex(-1);
        // Fokus zurück ins Input – nach Tastatur-Auswahl lag er auf dem (nun
        // entfernten) Button. So kann direkt mit Enter abgeschickt werden.
        inputRef.current?.focus();
    }

    function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
        const value = event.target.value;
        setQuery(value);
        setActiveIndex(-1);
        setPosition(null); // getippter Text ⇒ alte Auswahl ungültig
        if (value.length < 3) setSuggestions([]);
    }

    function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
        if (suggestions.length === 0) return;

        if (event.key === "ArrowDown") {
            event.preventDefault();
            setActiveIndex((index) => (index + 1) % suggestions.length);
        } else if (event.key === "ArrowUp") {
            event.preventDefault();
            setActiveIndex((index) => (index - 1 + suggestions.length) % suggestions.length);
        } else if ((event.key === "Enter" || event.key === "Tab") && activeIndex >= 0) {
            event.preventDefault();
            selectSuggestion(suggestions[activeIndex]);
        } else if (event.key === "Escape") {
            setSuggestions([]);
            setActiveIndex(-1);
        }
    }

    function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!position) return;
        onSubmit(position);
        setQuery("");
        setSuggestions([]);
        setActiveIndex(-1);
        setPosition(null);
        inputRef.current?.blur();
    }

    return (
        <form ref={containerRef} className="search-location-form" onSubmit={handleSubmit}>
            <div className="input-wrapper">
                <input
                    ref={inputRef}
                    type="text"
                    name="searchquery"
                    placeholder="e.g. Hamburg"
                    autoComplete="off"
                    value={query}
                    onChange={handleChange}
                    onKeyDown={handleKeyDown}
                />
                {position && (
                    <button type="submit" className="submit-button" aria-label="Suchen">
                        <img src={submitIcon} alt=""/>
                    </button>
                )}
            </div>
            {suggestions.length > 0 && (
                <ul className="suggestions">
                    {suggestions.map((suggestion, index) => (
                        <li key={suggestion.place_id}>
                            <button
                                type="button"
                                className={index === activeIndex ? "active" : undefined}
                                // Maus: preventDefault verhindert den Fokus-Wechsel weg vom Input.
                                // Die Selektion läuft über onClick – der feuert bei Maus-Klick
                                // UND bei Enter/Space, wenn per TAB auf den Button fokussiert wurde.
                                onMouseDown={(event) => event.preventDefault()}
                                onClick={() => selectSuggestion(suggestion)}
                            >
                                {suggestion.formatted}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </form>
    )
}
