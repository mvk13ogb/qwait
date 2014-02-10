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
        var p = Math.floor((new Date() - milliseconds)/1000);
        var x = Math.floor(p/60);
        var m = x % 60;
        x = Math.floor(x/60);
        var h = x % 24;

        return p < 86400 ? h + 'h ' + m + 'm' : 'Over a day';
    }
});

qwait.factory('inQueueFunc', function() {
    return {
        inQueue: function(name, positions) {
            for(var i=0; i<positions.length; i++) {
                var pos = positions[i];

                if(pos.account.principalName == name)
                    return true;
            }

            return false;
        }
    }
});

qwait.factory('getQueuePosFunc', function() {
    return {
        getQueuePos: function(name, positions) {
            for(var i=0; i<positions.length; i++) {
                var pos = positions[i];

                if(pos.account.principalName == name)
                    return pos;
            }

            return null;
        }
    }
});
