# RxKotlin MessageServer

## Structure of the project
 - This project uses ktor for the server deployment https://ktor.io/ for a detailed documentation 
 - TODO 
---
## Prerequisites

- Intelij with gradle is installed
- run `gradle build`
- setup the port of the server
- setup the location of the mysql server
---
## Setup MySQL Location
- The location of the server has to be set up in [DBConnR2DBC.kt](./src/main/kotlin/at/mh/kotlin/message/server/db/DBConnR2DBC.kt#L19)
---
## Setup Port

In [application.conf](./src/main/resources/application.conf#L3) one can set the deployment location of the ktor server

---

Just run the main.kt main function

