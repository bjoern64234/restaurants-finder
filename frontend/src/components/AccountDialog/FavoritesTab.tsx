import {useState} from "react";
import type {ReactElement} from "react";

type MockFavoriteRestaurant = {
    placeId: string;
    name: string;
    category: string;
    openingHours: string;
};

const MAX_FAVORITES = 5;

const INITIAL_FAVORITES: MockFavoriteRestaurant[] = [
    {placeId: "1", name: "Pizzeria Napoli", category: "Italienisch", openingHours: "Mo–So 11:00–22:00"},
    {placeId: "2", name: "Akropolis Grill", category: "Griechisch", openingHours: "Di–So 12:00–23:00"},
    {placeId: "3", name: "Sushi Sakura", category: "Japanisch", openingHours: "Mo–Sa 12:00–21:30"},
    {placeId: "4", name: "Curry Palace", category: "Indisch", openingHours: "Täglich 11:30–22:30"},
    {placeId: "5", name: "Burger Werk", category: "Amerikanisch", openingHours: "Mo–So 10:00–00:00"},
].slice(0, MAX_FAVORITES);

export function FavoritesTab(): ReactElement {
    const [favorites, setFavorites] = useState<MockFavoriteRestaurant[]>(INITIAL_FAVORITES);
    const [pendingRemovalId, setPendingRemovalId] = useState<string | null>(null);

    function handleRemove(placeId: string) {
        setFavorites((current) => current.filter((restaurant) => restaurant.placeId !== placeId));
        setPendingRemovalId(null);
    }

    if (favorites.length === 0) {
        return <p className="favorites-tab__empty">Noch keine Favoriten.</p>;
    }

    return (
        <ul className="favorites-tab__list">
            {favorites.map((restaurant) => (
                <li key={restaurant.placeId} className="favorite-card">
                    <div className="favorite-card__row">
                        <span className="favorite-card__name">{restaurant.name}</span>
                        <button
                            type="button"
                            className="favorite-card__heart"
                            aria-label={`${restaurant.name} aus Favoriten entfernen`}
                            onClick={() => setPendingRemovalId(restaurant.placeId)}
                        >
                            ❤️
                        </button>
                    </div>
                    <p className="favorite-card__category">{restaurant.category}</p>
                    <p className="favorite-card__hours">{restaurant.openingHours}</p>

                    {pendingRemovalId === restaurant.placeId && (
                        <div className="favorite-card__confirm">
                            <span>Aus Favoriten entfernen?</span>
                            <div className="favorite-card__confirm-actions">
                                <button
                                    type="button"
                                    className="favorite-card__confirm-cancel"
                                    onClick={() => setPendingRemovalId(null)}
                                >
                                    Abbrechen
                                </button>
                                <button
                                    type="button"
                                    className="favorite-card__confirm-remove"
                                    onClick={() => handleRemove(restaurant.placeId)}
                                >
                                    Entfernen
                                </button>
                            </div>
                        </div>
                    )}
                </li>
            ))}
        </ul>
    );
}
