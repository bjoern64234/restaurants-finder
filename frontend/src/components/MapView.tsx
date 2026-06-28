import {MapContainer, Marker, Popup, TileLayer, AttributionControl, useMapEvents, Tooltip} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import 'react-leaflet-cluster/dist/assets/MarkerCluster.css'
import 'react-leaflet-cluster/dist/assets/MarkerCluster.Default.css'
import "./MapView.css";
import MarkerPin from "../assets/location-pin.png";
import MyLocation from "../assets/my-location.png";
import {Icon, divIcon, point, type LatLngTuple} from "leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import type {MarkerCluster, Map as LeafletMap} from "leaflet";
import {useState, useRef, useEffect, type RefObject, type KeyboardEvent} from "react";


const markers: { id: number; position: LatLngTuple; title: string; text: string }[] = [
    {
        id: 1,
        position: [51.5113, 7.4636],
        title: "Hövels Hausbrauerei",
        text: "Traditionelle Brauereigaststätte mit eigenem Bier am Hoher Wall.",
    },
    {
        id: 2,
        position: [51.5136, 7.4659],
        title: "Pfefferkorn am Alten Markt",
        text: "Rustikale deutsche Küche direkt am historischen Marktplatz.",
    },
    {
        id: 4,
        position: [51.5130, 7.4675],
        title: "Mongo's",
        text: "Mongolisches Barbecue-Buffet nahe der Kleppingstraße.",
    },
    {
        id: 5,
        position: [51.5145, 7.4625],
        title: "Vapiano",
        text: "Italienische Pasta und Pizza nahe dem Westenhellweg.",
    },
];

const INITIAL_CENTER: LatLngTuple = [51.5142273, 7.4652789];
const INITIAL_ZOOM = 15;

// Possible to use custom Markers:
const customIcon = new Icon({
    iconUrl: MarkerPin,
    iconSize: [38, 38]
})
const OSM = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
const SATELLITE = '&copy; CNES, Airbus DS, PlanetObserver, OpenMapTiles | &copy; <a href="https://stadiamaps.com/">Stadia Maps</a>';

// Beide Kachel-Sätze sind immer sichtbar (große Map + Preview) → beide Credits dauerhaft zeigen
const COMBINED_ATTRIBUTION = `${OSM} | ${SATELLITE}`;

const skins = [
    {
        id: 1,
        name: "Standard",
        url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    },
    {
        id: 2,
        name: "Satellit",
        url: "https://tiles.stadiamaps.com/tiles/alidade_satellite/{z}/{x}/{y}{r}.jpg",
    }
]

// Eine reine Verhaltenskomponente (return null)
function SyncPreview({previewRef}: { previewRef: RefObject<LeafletMap | null> }) {
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
    const previewMapRef = useRef<LeafletMap | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    // Fehlermeldung nach 5s automatisch ausblenden; Timer wird bei neuer Meldung/Unmount aufgeräumt
    useEffect(() => {
        if (!errorMessage) return;
        const timer = setTimeout(() => setErrorMessage(null), 5000);
        return () => clearTimeout(timer);
    }, [errorMessage]);

    function goToMyLocation() {
        navigator.geolocation.getCurrentPosition(
            position => mapRef.current?.flyTo([position.coords.latitude, position.coords.longitude], 15),
            () => setErrorMessage("Standort nicht verfügbar. Bitte die Freigabe im Browser prüfen.")
        );
    }

    function toggleSkin() {
        setMainSkin(prev => prev === 0 ? 1 : 0)
    }

    function handleSkinButtonKeyDown(event: KeyboardEvent) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault()
            toggleSkin()
        }
    }


    return (
        <div className={"map-wrapper"}>
            <>
                <button onClick={goToMyLocation} className={"actual-button"} aria-label={"Zu meinem Standort springen"}>
                    <img src={MyLocation} alt={""}/>
                </button>
            </>
            {errorMessage && <p className={"error-message"} role={"alert"}>{errorMessage}</p>}
            <MapContainer id={"map"} ref={mapRef} center={INITIAL_CENTER} zoom={INITIAL_ZOOM}
                          attributionControl={false}>
                <AttributionControl prefix={"ⓘ"}/>
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


                <MarkerClusterGroup chunkedLoading
                                    iconCreateFunction={createClusterIcon}>
                    {markers.map(marker => <Marker
                        key={marker.id}
                        position={marker.position}
                        icon={customIcon}
                    >
                        <Tooltip permanent offset={[0, -20]} direction={"top"} interactive>
                            {marker.title}
                        </Tooltip>
                        <Popup>
                            <h2>{marker.title}</h2>
                            <p>{marker.text}</p>
                        </Popup>
                    </Marker>)
                    }

                </MarkerClusterGroup>

            </MapContainer>
            <div className={"skin-button"}
                 role={"button"}
                 tabIndex={0}
                 aria-label={"Zwischen Karten- und Satellitenansicht wechseln"}
                 onClick={toggleSkin}
                 onKeyDown={handleSkinButtonKeyDown}>
                <MapContainer id={"preview-map"}
                              ref={previewMapRef}
                              center={INITIAL_CENTER}
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