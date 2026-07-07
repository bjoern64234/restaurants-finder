import "./LikeButton.css"
import {useEffect, useState} from "react";
import {deleteFavoriteRestaurant, saveFavoriteRestaurant} from "../../api/favorites";
import type {RestaurantProperties} from "../../types/restaurant.type";

interface LikeButtonProps {
  restaurant: RestaurantProperties;
  favoriteId: number | null;
  onFavorited: (placeId: string, id: number) => void;
  onUnfavorited: (placeId: string) => void;
}

export function LikeButton({restaurant, favoriteId, onFavorited, onUnfavorited}: LikeButtonProps) {
  const [liked, setLiked] = useState(favoriteId !== null);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setLiked(favoriteId !== null);
  }, [favoriteId]);

  async function handleClick() {
    if (liked) {
      if (favoriteId === null) return;

      try {
        await deleteFavoriteRestaurant(restaurant.place_id);
        setLiked(false);
        onUnfavorited(restaurant.place_id);
      } catch (error) {
        console.error("Favorit konnte nicht entfernt werden", error);
      }
      return;
    }

    try {
      const response = await saveFavoriteRestaurant({
        name: restaurant.name,
        formatted: restaurant.formatted,
        website: restaurant.website,
        openingHours: restaurant.opening_hours,
        cuisine: restaurant.catering?.cuisine ?? null,
        phone: restaurant.contact?.phone ?? null,
        lat: restaurant.lat,
        lng: restaurant.lon,
        placeId: restaurant.place_id,
      });
      setLiked(true);
      onFavorited(restaurant.place_id, response.data.id);
    } catch (error) {
      console.error("Favorit konnte nicht gespeichert werden", error);
    }
  }

  return (
    <button
      id={restaurant.place_id}
      className="like-button"
      onClick={handleClick}
      aria-label={liked ? "Von Favoriten entfernen" : "Zu Favoriten hinzufügen"}
    >
      {liked ? "❤️" : "🤍"}
    </button>
  )
}
