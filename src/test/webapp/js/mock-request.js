angular.module('request', []).factory('requestInfo', [function () {
    return {
        currentUser: {
            name: null,
            readableName: null,
            admin: false,
            roles: [],
            anonymous: true
        },
        product: {
            name: 'QWait',
            version: '(snapshot)'
        }
    };
}]);
