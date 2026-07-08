import {Marker, Popup, Tooltip} from "react-leaflet";
import type {RestaurantFeature} from "../../types/restaurant.type.ts";
import {customIcon} from "../../assets/location-icon.ts";
import {LikeButton} from "./LikeButton.tsx";

interface RestaurantMarkerProps {
    restaurant: RestaurantFeature;
    favoriteId: number | null;
    onFavorited: (placeId: string, id: number) => void;
    onUnfavorited: (placeId: string) => void;
}

export function RestaurantMarker({restaurant, favoriteId, onFavorited, onUnfavorited}: RestaurantMarkerProps) {
    const {name, lat, lon, street, housenumber, opening_hours, catering, contact} = restaurant.properties;

    return (
        <Marker position={[lat, lon]} icon={customIcon}>
            <Tooltip permanent offset={[0, -20]} direction={"top"} interactive>
                {name}
            </Tooltip>
            <Popup>
                <div style={{minWidth: "220px"}}>
                    <h3 style={{margin: "0 0 6px 0", fontSize: "16px"}}>
                        {name}
                        <LikeButton
                            restaurant={restaurant.properties}
                            favoriteId={favoriteId}
                            onFavorited={onFavorited}
                            onUnfavorited={onUnfavorited}
                        />
                    </h3>

                    <p style={{margin: "0 0 6px 0", fontSize: "13px", color: "#555"}}>
                        {street} {housenumber}
                    </p>

                    <p style={{margin: "0 0 6px 0", fontSize: "13px", color: "#666"}}>
                        {opening_hours ?? "Öffnungszeiten nicht verfügbar"}
                    </p>

                    {catering?.cuisine && (
                        <p style={{margin: "0 0 6px 0", fontSize: "13px"}}>
                            🍽️ {catering.cuisine}
                        </p>
                    )}

                    {contact?.phone && (
                        <p style={{margin: "0", fontSize: "13px"}}>
                            📞{" "}
                            <a href={`tel:${contact.phone}`}>
                                {contact.phone}
                            </a>
                        </p>
                    )}
                </div>
            </Popup>
        </Marker>
    );
}
