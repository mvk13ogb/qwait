var qwait = angular.module('qwait', []);

qwait.config(function ($sceProvider) {
    // TODO: This is a security issue! Remove in final version and do sane interpolation
    $sceProvider.enabled(false);
});

qwait.filter('timestamp', function() {
    return function (milliseconds) {
        return new Date(milliseconds);
    }
});
