const promise = new Promise((resolve, reject) => {
    console.log("Creating the promise");
    setTimeout(() => {
        reject("timeOut");
    }, 1000); // rejected after 1sec
    console.log("Exiting Promise executor");
});


console.log("I am sync");
promise.then(result => {
    console.log(`promise was a success ${result}`);
}).catch(failure => {
    console.log(`promise was a failure ${failure}`);
}).finally(() => {
    console.log("finnally called");
});
console.log("I am sync too");