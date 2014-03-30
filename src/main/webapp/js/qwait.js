(function () {
    var qwait = angular.module('qwait', ['ngRoute', 'ngResource', 'request']);

    qwait.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'partial/home.html',
                controller: 'HomeCtrl'
            }).
            when('/about', {
                templateUrl: 'partial/about.html',
                controller: 'AboutCtrl'
            }).
            when('/queues', {
                templateUrl: 'partial/queue-list.html',
                controller: 'QueueListCtrl'
            }).
            when('/queue/:queueName', {
                templateUrl: 'partial/queue.html',
                controller: 'QueueCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);

    qwait.factory('user', ['$resource', '$cacheFactory', 'requestInfo', function ($resource, $cacheFactory, requestInfo) {
        var userCache = $cacheFactory('user');

        var result = $resource('/api/user/:name', {name: '@name'}, {
            setAdmin: {method: 'PUT', url: '/api/user/:name/role/admin'}
        });

        if (requestInfo.currentUser.name) {
            result.current = result.get({name: requestInfo.currentUser.name});
        } else {
            result.current = requestInfo.currentUser;
        }

        return result;
    }]);

    qwait.factory('system', ['requestInfo', function (requestInfo) {
        return {
            product: requestInfo.product
        }
    }]);

    qwait.factory('contributors', function () {
        return {
            all: [
                { name: 'Adrian Blanco' },
                { name: 'Casper Winsnes' },
                { name: 'Christoffer Pettersson' },
                { name: 'David Flemström', gravatar: '202ecb437d8bbd442d093a3a35c67a04', twitter: 'dflemstr' },
                { name: 'Eric Schmidt' },
                { name: 'Gustav Zander' },
                { name: 'Jacob Sievers' },
                { name: 'Michael Håkansson' },
                { name: 'Robin Engström' }
            ]
        }
    });

    qwait.factory('page', function () {
        return {
            title: ''
        };
    });

    qwait.controller('TopbarCtrl', ['$scope', '$location', 'user', 'system', function ($scope, $location, user, system) {
        $scope.location = $location;
        $scope.user = user;
        $scope.system = system;
    }]);

    qwait.controller('TitleCtrl', ['$scope', 'system', 'page', function ($scope, system, page) {
        $scope.system = system;
        $scope.page = page;
    }]);

    qwait.controller('HomeCtrl', ['$scope', 'system', 'page', 'contributors', function ($scope, system, page, contributors) {
        page.title = 'Home';

        $scope.system = system;
        $scope.contributors = contributors;
    }]);

    qwait.controller('AboutCtrl', ['$scope', 'page', function ($scope, page) {
        page.title = 'About';
    }]);

    qwait.controller('QueueListCtrl', ['$scope', 'page', function ($scope, page) {
        page.title = 'Queue list';
    }]);

    qwait.controller('QueueCtrl', ['$scope', 'page', function ($scope, page) {
        page.title = 'View queue';
    }]);

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

    qwait.factory('getQueuePos', function () {
        return function (name, positions) {
            for (var i = 0; i < positions.length; i++) {
                var pos = positions[i];

                if (pos.account.principalName == name)
                    return pos;
            }

            return null;
        }
    });

    qwait.factory('getQueuePosNr', function () {
        return function (name, positions) {
            var sortedPositions = positions.sort(function (a, b) {
                return(a.id - b.id);
            });

            for (var i = 0; i < sortedPositions.length; i++) {
                var pos = sortedPositions[i];

                if (pos.account.principalName == name)
                    return i + 1;
            }

            return null;
        }
    });

    //This function returns the official color of the computer lab. 
    //In the cases where we return the hex color, it's because KTHs color doesn't match the CSS definition
    qwait.filter('getComputerColor', function () {
        return function (location) {

            if (/(blå|blue)/i.test(location)) {
                return "blue";
            }

            else if (/(röd|red)/i.test(location)) {
                return "red";
            }

            else if (/(orange)/i.test(location)) {
                return "#FF7F00";
            }

            else if (/(gul|yellow)/i.test(location)) {
                return "yellow";
            }

            else if (/(grön|green)/i.test(location)) {
                return "green";
            }

            else if (/(brun|brown)/i.test(location)) {
                return "#7F3F1F";
            }

            else if (/(grå|grey|gray)/i.test(location)) {
                return "grey";
            }

            else if (/(karmosin|crimson)/i.test(location)) {
                return "#D91536";
            }

            else if (/(vit|white)/i.test(location)) {
                return "white";
            }

            else if (/(magenta|cerise)/i.test(location)) {
                return "magenta";
            }

            else if (/(violett|violet)/i.test(location)) {
                return "#AC00E6";
            }

            else if (/(turkos|turquoise)/i.test(location)) {
                return "turquoise";
            }


            //These computer labs have official colours too
            else if (/(spel)/i.test(location)) {
                return "#E6ADAD";
            }

            else if (/(sport)/i.test(location)) {
                return "#ADADE6";
            }

            else if (/(musik)/i.test(location)) {
                return "#ADE7AD";
            }

            else if (/(konst)/i.test(location)) {
                return "#E8E7AF";
            }

            else if (/(mat)/i.test(location)) {
                return "#E8C9AF";
            }

            //If the location doesn't match anything we return an empty string
            else {
                return "";
            }
        };
    });

})();
