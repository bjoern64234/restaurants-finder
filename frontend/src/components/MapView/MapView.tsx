import {useEffect, useRef, useState, type RefObject} from "react";

import {divIcon, point, type Map as LeafletMap, type MarkerCluster, type LatLngTuple} from "leaflet";
import {
    AttributionControl,
    MapContainer,
    TileLayer,
    useMapEvents,
    ZoomControl,
} from "react-leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";

import "leaflet/dist/leaflet.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.Default.css";

import "./MapView.css";

import MyLocation from "../../assets/my-location.png";
import HeartIcon from "../../assets/heart.png";

import {MyLocationMarker} from "../MyLocationMarker/MyLocationMarker.tsx";
import {RestaurantMarker} from "../RestaurantMarker/RestaurantMarker.tsx";
import {SearchRestaurantsButton} from "../SearchRestaurantsButton/SearchRestaurantsButton.tsx";
import {AccountButton} from "../AccountButton/AccountButton.tsx";

import {
    COMBINED_ATTRIBUTION,
    INITIAL_CENTER,
    INITIAL_ZOOM,
    SEARCH_ZOOM,
    skins,
} from "../../constants/constants.ts";

import type {RestaurantFeature, RestaurantResponse} from "../../types/restaurant.type.ts";
import type {Position} from "../../types/position.type.ts";
import type {AxiosResponse} from "axios";
import {searchNearbyRestaurants} from "../../api/search-nearby-restaurants.ts";
import SearchLocationForm from "../SearchLocationForm/SearchLocationForm.tsx";

// Eine reine Verhaltenskomponente (return null)
function SyncPreview({previewRef}: {
    previewRef: RefObject<LeafletMap | null>
}) {
    const map = useMapEvents({
        move: () => previewRef.current?.setView(map.getCenter(), map.getZoom() - 4, {animate: false}),
        zoom: () => previewRef.current?.setView(map.getCenter(), map.getZoom() - 4, {animate: false}),
    });
    return null;
}

const createClusterIcon = (cluster: MarkerCluster) =>
    divIcon({
        html: `<span>${cluster.getChildCount()}</span>`,
        className: "custom-cluster",
        iconSize: point(40, 40, true),
    });


export default function MapView() {
    const [mainSkin, setMainSkin] = useState<0 | 1>(0);
    const secondSkin = mainSkin === 0 ? 1 : 0;
    const mapRef = useRef<LeafletMap | null>(null);
    const [currentPosition, setCurrentPosition] = useState<Position>({
        lat: INITIAL_CENTER[0],
        lng: INITIAL_CENTER[1],
    });
    const [restaurants, setRestaurants] = useState<RestaurantFeature[]>([]);
    const previewMapRef = useRef<LeafletMap | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    // Fehlermeldung nach 5s automatisch ausblenden; Timer wird bei neuer Meldung/Unmount aufgeräumt
    useEffect(() => {
        if (!errorMessage) return;
        const timer = setTimeout(() => setErrorMessage(null), 5000);
        return () => clearTimeout(timer);
    }, [errorMessage]);

    useEffect(() => {
        goToMyLocation();
    }, []);


    function goToMyLocation() {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const location = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude,
                };
                setCurrentPosition(location);
                mapRef.current?.flyTo([location.lat, location.lng], 15);
            },
            () => {
                setErrorMessage(
                    "Standort nicht verfügbar. Bitte die Freigabe im Browser prüfen."
                );
            }
        );
    }

    function toggleSkin() {
        setMainSkin(prev => prev === 0 ? 1 : 0)
    }

    function flyToLocation(latLng: LatLngTuple) {
        mapRef.current?.flyTo(latLng, SEARCH_ZOOM)
    }

    function findFavorites()

    async function handleFindRestaurants() {
        const map = mapRef.current;
        if (!map) {
            setErrorMessage("Karte noch nicht bereit.");
            return;
        }
        const center = map.getCenter();   // exakte aktuelle Mitte, kein Zwischenstand
        const bounds = map.getBounds();
        const ne = bounds.getNorthEast();

        const MIN_RADIUS = 500;    // m – verzeiht kleinen Zentrums-Versatz beim Reinzoomen
        const MAX_RADIUS = 50000;  // m – Geoapify-Circle-Grenze (verifizieren)
        const halfHeight = center.distanceTo([ne.lat, center.lng]);
        const halfWidth = center.distanceTo([center.lat, ne.lng]);
        const viewportRadius = Math.max(halfHeight, halfWidth);
        const radius = Math.round(Math.min(Math.max(viewportRadius, MIN_RADIUS), MAX_RADIUS));

        try {
            const res: AxiosResponse<RestaurantResponse> = await searchNearbyRestaurants({
                lat: center.lat.toString(),
                lng: center.lng.toString(),
                radius: radius.toString(),
            });
            setRestaurants(res.data.features);
        } catch (error) {
            setErrorMessage((error as Error).message);
        }
    }

    return (
        <div className={"map-wrapper"}>
            <div className="top-bar">
                <SearchLocationForm onSubmit={flyToLocation}/>
                <AccountButton/>
            </div>
            <div className="map-button-group">
                <button
                    title={"Show Favorites"}
                    onClick={findFavorites}
                    className={"map-button"}
                    aria-label={"Favoriten anzeigen"}
                >
                    <img src={HeartIcon} alt={""}/>
                </button>
                <button
                    title={"Fly home"}
                    onClick={goToMyLocation}
                    className={"map-button"}
                    aria-label={"Zu meinem Standort springen"}
                >
                    <img src={MyLocation} alt={""}/>
                </button>
                <SearchRestaurantsButton
                    onClick={handleFindRestaurants}
                />
            </div>

            {errorMessage && <p className={"error-message"} role={"alert"}>{errorMessage}</p>}

            <MapContainer id={"map"} ref={mapRef} center={INITIAL_CENTER} zoom={INITIAL_ZOOM}
                          attributionControl={false} zoomControl={false}>
                <AttributionControl prefix={"ⓘ"}/>
                <ZoomControl position={"bottomright"}/>
                <SyncPreview previewRef={previewMapRef}/>

                <TileLayer
                    url={skins[mainSkin].url}
                    attribution={COMBINED_ATTRIBUTION}
                    eventHandlers={{
                        tileerror: () => {
                            if (mainSkin === 1) {
                                setMainSkin(0)
                                setErrorMessage("Satellitenansicht gerade nicht verfügbar – zeige Standardkarte.")
                            }
                        },
                    }}
                />

                {restaurants.length > 0 ? <MarkerClusterGroup chunkedLoading iconCreateFunction={createClusterIcon}>
                    {restaurants.map(restaurant => (
                        <RestaurantMarker
                            key={restaurant.properties.place_id}
                            restaurant={restaurant}
                        />
                    ))}
                </MarkerClusterGroup> : null}

                {currentPosition && (
                    <MyLocationMarker lat={currentPosition.lat} lng={currentPosition.lng}/>
                )}
            </MapContainer>

            <div className={"skin-button"}>
                <button
                    type={"button"}
                    className={"skin-button__toggle"}
                    aria-label={"Zwischen Karten- und Satellitenansicht wechseln"}
                    onClick={toggleSkin}
                />
                <MapContainer id={"preview-map"}
                              ref={previewMapRef}
                              center={currentPosition}
                              zoom={INITIAL_ZOOM - 4}
                              attributionControl={false}
                              zoomControl={false}
                              dragging={false}
                              scrollWheelZoom={false}
                              doubleClickZoom={false}
                              boxZoom={false}
                              keyboard={false}
                              touchZoom={false}>
                    <TileLayer
                        url={skins[secondSkin].url}
                    />
                </MapContainer>
            </div>
        </div>)
}