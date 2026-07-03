import {protectedApi} from "./protectedApi.ts";
import type {AxiosResponse} from "axios";
import type {AuthUser} from "../types/auth.type";

export const getCurrentUser = (): Promise<AxiosResponse<AuthUser>> =>
    protectedApi.get("/session");
