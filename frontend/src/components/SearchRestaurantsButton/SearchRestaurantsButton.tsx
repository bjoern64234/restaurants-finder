import type {ReactElement} from "react";
import RestaurantImg from "../../assets/restaurant.png"

type Props = {
    onClick: () => Promise<void>;
};

export function SearchRestaurantsButton({onClick}: Readonly<Props>): ReactElement {


    return (
        <button
            onClick={onClick}
            className="map-button"
            aria-label="Restaurants in der Nähe suchen"
        >
            <img src={RestaurantImg} alt={"Restaurant"}/>
        </button>
    );
}