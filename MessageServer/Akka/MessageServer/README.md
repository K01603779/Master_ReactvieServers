# Akka MessageServer

## Structure of the project
 - TODO 
---
## Prerequesites

- Intelij with gradle is installed
- run `gradle build`
- setup the port of the server
- setup the location of the mysql server
---
## Setup MySQL Location
- The location of the server has to be set up in [DBMessageConnector.kt](./src/main/kotlin/connectionpool/DBMessageConnector.kt#L10)
---
## Akka Configuration
- The akka server can be configured in [config](./src/main/resources/config)
- In [main.kt](./src/main/kotlin/main.kt#L43) one can configure the port of the server 
---

Just run the main.kt main function