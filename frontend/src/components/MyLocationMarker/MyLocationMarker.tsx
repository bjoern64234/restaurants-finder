import {Marker, Popup} from "react-leaflet";
import {locationIcon as myLocationIcon} from "../../assets/location-icon.ts";

interface MyLocationMarkerProps {
    lat: number;
    lng: number;
}

export function MyLocationMarker({lat, lng}: MyLocationMarkerProps) {
    return (
        <Marker position={[lat, lng]} icon={myLocationIcon}>
            <Popup>
                <div>
                    <strong>My Location</strong>
                    <br/>
                    Lat: {lat.toFixed(5)}
                    <br/>
                    Lng: {lng.toFixed(5)}
                </div>
            </Popup>
        </Marker>
    );
}
