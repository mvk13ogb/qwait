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

qwait.filter('queuetime', function() {
    return function (milliseconds) {
        p = Math.floor((new Date() - milliseconds)/1000);
        x = p;
        s = x % 60;
        x = Math.floor(x/60)
        m = x % 60;
        x = Math.floor(x/60)
        h = x % 24;

        return p < 86400 ? h + 'h ' + m + 'm ' + s + 's' : 'Over a day';
    }
});
