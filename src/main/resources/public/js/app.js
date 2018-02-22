'use strict';

var App = angular.module('myApp', []);

var showAlert = function (errResponse) {
    console.log(JSON.stringify(errResponse));
    if (!errResponse.statusText) {
        errResponse.statusText = "No connection to server."
    }
    $.smkAlert({
        text: errResponse.statusText,
        type: 'danger',
        position: 'top-center',
        time: 10
    });
}
