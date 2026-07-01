import type {LatLngTuple} from "leaflet";

export const INITIAL_CENTER: LatLngTuple = [51.5142273, 7.4652789];
export const INITIAL_ZOOM = 15;

export const OSM = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
export const SATELLITE = '&copy; CNES, Airbus DS, PlanetObserver, OpenMapTiles | &copy; <a href="https://stadiamaps.com/">Stadia Maps</a>';

// Beide Kachel-Sätze sind immer sichtbar (große Map + Preview) → beide Credits dauerhaft zeigen
export const COMBINED_ATTRIBUTION = `${OSM} | ${SATELLITE}`;

export const skins = [
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