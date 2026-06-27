import {MapContainer, Marker, Popup, TileLayer, AttributionControl, useMapEvents} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import 'react-leaflet-cluster/dist/assets/MarkerCluster.css'
import 'react-leaflet-cluster/dist/assets/MarkerCluster.Default.css'
import "./MapView.css";
import MarkerPin from "../assets/location-pin.png";
import {Icon, divIcon, point} from "leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import type {MarkerCluster, Map as LeafletMap} from "leaflet";
import {useState, useRef, type RefObject, type KeyboardEvent} from "react";


const markers = [
    {
        id: 1,
        position: [51.5181, 7.4561] as [number, number],
        title: "Dortmunder U",
        text: "Kultur- und Kreativzentrum im alten Brauereiturm.",
    },
    {
        id: 2,
        position: [51.4926, 7.4518] as [number, number],
        title: "Signal Iduna Park",
        text: "Heimstadion von Borussia Dortmund, größtes Stadion Deutschlands.",
    },
    {
        id: 3,
        position: [51.5145, 7.4660] as [number, number],
        title: "Reinoldikirche",
        text: "Älteste Kirche der Stadt mitten im Zentrum.",
    },
];

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
    const previewMapRef = useRef<LeafletMap | null>(null);


    function handleSkinButtonClick() {
        setMainSkin(prev => prev === 0 ? 1 : 0)
    }

    function handleSkinButtonKeyDown(event: KeyboardEvent) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault()
            handleSkinButtonClick()
        }
    }


    return (
        <>
            <MapContainer id={"map"} center={[51.5142273, 7.4652789]} zoom={13}
                          attributionControl={false}>
                <AttributionControl prefix={"ⓘ"}/>
                <SyncPreview previewRef={previewMapRef}/>
                <TileLayer
                    url={skins[mainSkin].url}
                    attribution={COMBINED_ATTRIBUTION}
                />


                <MarkerClusterGroup chunkedLoading
                                    iconCreateFunction={createClusterIcon}>
                    {markers.map(marker => <Marker
                        key={marker.id}
                        position={marker.position}
                        icon={customIcon}
                        title={marker.title}
                    >
                        <Popup>
                            <h2>{marker.title}</h2>
                            <p>{marker.text}</p>
                        </Popup>
                    </Marker>)
                    }

                </MarkerClusterGroup>
                <div className={"skin-button"}
                     role={"button"}
                     tabIndex={0}
                     aria-label={"Zwischen Karten- und Satellitenansicht wechseln"}
                     onClick={handleSkinButtonClick}
                     onKeyDown={handleSkinButtonKeyDown}>
                    <MapContainer id={"preview-map"}
                                  ref={previewMapRef}
                                  center={[51.5142273, 7.4652789]}
                                  zoom={9}
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
            </MapContainer>
        </>)
}