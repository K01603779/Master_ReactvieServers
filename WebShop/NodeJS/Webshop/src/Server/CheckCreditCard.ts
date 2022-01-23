import { User } from "../Entities/entities";

export async function checkCreditCard(user: User): Promise<Boolean> {
    return new Promise((resolve) => {
        setTimeout(() => {
            if (Math.random() % 100 < 95) {
                resolve(true);
            }
            resolve(false);
        }, 3000);
    });
}