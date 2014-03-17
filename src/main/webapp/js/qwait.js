(function () {
    var qwait = angular.module('qwait', []);

    qwait.config(function ($sceProvider) {
        // TODO: This is a security issue! Remove in final version and do sane interpolation
        $sceProvider.enabled(false);
    });

    qwait.filter('duration', function () {
        return function (milliseconds) {
            return moment.duration(milliseconds).humanize();
        }
    });

    //Returns the computer name if we recognize it, otherwise returns empty string
    qwait.filter('getComputerName', function () {
        return function (hostname) {

            function capitaliseFirstLetter(string) {
                return string.charAt(0).toUpperCase() + string.slice(1);
            }

            function insertSpace(string) {

                //Removes every hyphen
                string = string.replace(/-/g, "");

                //Splits the string before every digit
                string = string.split(/(?=\d)/);

                //Returns the first element from the split, adds a space and returns the rest
                return string[0] + " " + string.splice(1).join().replace(",", "");
            }

            //Returns everything before the first dot
            function splitAtDot(string) {
                return string.split(".", 1)[0];
            }

            var exclude = /share|kthopen|eduroam/;
            var include = /(csc|ug)\.kth\.se/;

            //Filters out the most common non-school computers
            if (exclude.test(hostname)) {
                return '';
            }
            //If it's a school computer, we get the name
            else if (include.test(hostname)) {
                return capitaliseFirstLetter(insertSpace(splitAtDot(hostname)));
            }

            return '';
        }
    });

    qwait.filter('ownedBy', function () {
        return function (positions, name) {
            var result = [];

            for (var i = 0; i < positions.length; i++) {
                var position = positions[i];
                if (position && position.account &&
                    position.account.principalName === name) {
                    result.push(position);
                }
            }

            return result;
        };
    });

    qwait.factory('getQueuePos', function() {
        return function(name, positions) {
            for(var i=0; i<positions.length; i++) {
                var pos = positions[i];

                if(pos.account.principalName == name)
                    return pos;
            }

            return null;
        }
    });

    qwait.factory('getQueuePosNr', function() {
        return function(name, positions) {
            var sortedPositions = positions.sort(function(a,b) {
                return(a.id - b.id);
            });

            for(var i=0; i<sortedPositions.length; i++) {
                var pos = sortedPositions[i];

                if(pos.account.principalName == name)
                    return i+1;
            }

            return null;
        }
    });

    qwait.factory('page', function () {
        return {
            title: ''
        };
    });

    qwait.controller('TitleCtrl', ['$scope', 'page', function ($scope, page) {
        $scope.page = page;
    }]);
})();
