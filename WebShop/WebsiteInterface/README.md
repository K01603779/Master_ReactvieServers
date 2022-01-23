# Webshop Website

## Prerequesites
- npm and node js have been installed
- run `npm update` in the console 

--- 
## Start the Webshop
To start the webshop first start the server (Node.js,  Java EE) and then run `npm run start`

## Change the Backend of the Website (JavaEE or Node.js)
To change the used backend from the webshop (JavaEE or Node.js) change the import in  [store.js](./src/stores/store.js#L3) additionaly if the webshop is deployed somewhere else change the address for [JavaEE](./src/stores/JavaEE/javaconnector.js#L2) or for [Node.js](./src/stores/NodeJS/nodeJSconnector.js#L4)