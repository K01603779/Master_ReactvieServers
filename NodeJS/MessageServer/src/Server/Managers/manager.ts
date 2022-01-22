import { Message } from "../../Messages/messages";

export interface Manager {
    handleMessage(message: Message);
    receiveMessage(msg: Message);
}