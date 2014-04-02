(function () {
    var qwait = angular.module('qwait', ['ngRoute', 'request']);

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

    qwait.factory('users', ['$http', '$cacheFactory', 'messagebus', 'requestInfo', function ($http, $cacheFactory, messagebus, requestInfo) {
        var result = {},
            cache = $cacheFactory('users');

        messagebus.whenReady(function () {
            messagebus.subscribe('/topic/user/*', function (data) {
                switch (data.body['@type']) {
                    case 'UserAdminStatusChanged':
                        var user = cache.get(data.body.name);
                        if (user) {
                            user.admin = data.body.admin;
                        }
                        break;
                    default:
                        console.log('Unrecognized user message', data.body);
                }
            });
        });

        result.get = function (name) {
            var promise = $http.get('/api/user/' + name);
            promise.success(function (user) {
                cache.put(name, user);
            });
            return promise;
        };

        result.setAdmin = function (userName, admin) {
            return $http.put('/api/user/' + userName + '/role/admin', admin);
        };

        result.current = requestInfo.currentUser;

        if (result.current.name) {
            result.get(result.current.name).success(function (user) {
                result.current = user;
            });
        }

        return result;
    }]);

    qwait.factory('queues', ['$http', 'messagebus', function ($http, messagebus) {
        var result = {};

        result.all = {};

        messagebus.whenReady(function () {
            messagebus.subscribe('/topic/queue/*', function (data) {
                var queue, i;
                switch (data.body['@type']) {
                    case 'QueueActiveStatusChanged':
                        queue = result.all[data.body.name];
                        if (queue) {
                            queue.active = data.body.active;
                        }
                        break;
                    case 'QueueCleared':
                        queue = result.all[data.body.name];
                        if (queue) {
                            queue.positions = [];
                        }
                        break;
                    case 'QueueCreated':
                        result.get(data.body.name);
                        break;
                    case 'QueueLockedStatusChanged':
                        queue = result.all[data.body.name];
                        if (queue) {
                            queue.locked = data.body.locked;
                        }
                        break;
                    case 'QueueModeratorAdded':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            queue.moderators.push(data.body.userName);
                        }
                        break;
                    case 'QueueModeratorRemoved':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            i = queue.moderators.indexOf(data.body.userName);

                            if (i != -1) {
                                queue.moderators.splice(i, 1);
                            }
                        }
                        break;
                    case 'QueueOwnerAdded':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            queue.owners.push(data.body.userName);
                        }
                        break;
                    case 'QueueOwnerRemoved':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            i = queue.owners.indexOf(data.body.userName);

                            if (i != -1) {
                                queue.owners.splice(i, 1);
                            }
                        }
                        break;
                    case 'QueuePositionCommentChanged':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            for (i = 0; i < queue.positions.length; i++) {
                                if (queue.positions[i].userName = data.body.userName) {
                                    queue.positions[i].comment = data.body.comment;
                                }
                            }
                        }
                        break;
                    case 'QueuePositionCreated':
                        queue = result.all[data.body.queueName];
                        if (queue) {

                        }
                        break;
                    case 'QueuePositionLocationChanged':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            for (i = 0; i < queue.positions.length; i++) {
                                if (queue.positions[i].userName = data.body.userName) {
                                    queue.positions[i].location = data.body.location;
                                }
                            }
                        }
                    case 'QueuePositionRemoved':
                        break;
                    default:
                        console.log('Unrecognized user message', data.body);
                }
            });
        });

        $http.get('/api/queues').success(function (queues) {
            for (var i = 0; i < queues.length; i++) {
                result.all[queues[i].name] = queues[i];
            }
        });

        result.get = function (name) {
            var promise = $http.get('/api/queue/' + name);
            promise.success(function (queue) {
                result.all[queue.name] = queue;
            });
            return promise;
        };

        result.setLocked = function (name, locked) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + name + '/locked', '' + locked, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.setActive = function (name, active) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + name + '/active', '' + active, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

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

    qwait.factory('messagebus', ['$rootScope', '$timeout', '$interval', function ($rootScope, $timeout, $interval) {
        var result = {},
            client = Stomp.over(new SockJS('/bus/client')),
            readyContinuations = [],
            countdownJob = null,
            reconnectJob = null;

        client.debug = false;

        result.ready = false;
        result.protocol = null;
        result.reconnectAttempts = 0;
        result.reconnectInMillis = 0;

        result.describeProtocol = function (protocol) {
            switch (protocol) {
                case 'websocket':
                    return 'WebSocket';
                case 'xdr-streaming':
                    return 'XDR Streaming';
                case 'xhr-streaming':
                    return 'XHR Streaming';
                case 'iframe-eventsource':
                    return 'IFrame EventSource';
                case 'iframe-htmlfile':
                    return 'IFrame HTML File';
                case 'xdr-polling':
                    return 'XDR Polling';
                case 'xhr-polling':
                    return 'XHR Polling';
                case 'iframe-xhr-polling':
                    return 'IFrame XHR Polling';
                case 'jsonp-polling':
                    return 'JSONP Polling';
                default:
                    return 'Unknown';
            }
        };

        function doConnect() {
            client.connect({}, function (frame) {
                $rootScope.$apply(function () {
                    console.log('Connected to client bus', frame);

                    result.ready = true;
                    result.protocol = client.ws.protocol;
                    result.reconnectInMillis = 0;
                    result.reconnectAttempts = 0;

                    if (countdownJob) {
                        $interval.cancel(countdownJob);
                        countdownJob = null;
                    }

                    if (reconnectJob) {
                        $timeout.cancel(reconnectJob);
                        reconnectJob = null;
                    }

                    for (var i = 0; i < readyContinuations.length; i++) {
                        readyContinuations[i]();
                    }
                    readyContinuations = [];
                });
            }, function (frame) {
                $rootScope.$apply(function () {
                    console.log('Disconnected from client bus', frame);
                    result.ready = false;
                    result.protocol = null;

                    // Exponential back-off
                    result.reconnectInMillis = 100 * Math.random() * (Math.pow(2, result.reconnectAttempts) - 1);
                    result.reconnectAttempts++;
                    console.log('Scheduling reconnect in ' + moment.duration(result.reconnectInMillis).humanize());

                    if (reconnectJob) {
                        $timeout.cancel(reconnectJob);
                    }
                    reconnectJob = $timeout(doConnect, result.reconnectInMillis);

                    if (countdownJob) {
                        $interval.cancel(countdownJob);
                    }
                    countdownJob = $interval(function () {
                        result.reconnectInMillis -= 1000;

                        if (result.reconnectInMillis <= 0) {
                            $interval.cancel(countdownJob);
                            countdownJob = null;
                            result.reconnectInMillis = 0;
                        }
                    }, 1000);
                });
            });
        }

        doConnect();

        result.send = function (destination, headers, body) {
            return client.send(destination, headers, JSON.stringify(body));
        };

        result.subscribe = function (destination, callback, headers) {
            return client.subscribe(destination, function (frame) {
                frame.body = JSON.parse(frame.body);
                $rootScope.$apply(function () {
                    callback.apply(result, [frame]);
                });
            }, headers);
        };

        result.whenReady = function (continuation) {
            if (result.ready) {
                continuation();
            } else {
                readyContinuations.push(continuation);
            }
        };

        return result;
    }]);

    qwait.factory('broadcaster', ['messagebus', function (messagebus) {
        messagebus.whenReady(function () {
            messagebus.subscribe('/topic/broadcast', function (data) {
                console.log('Broadcast:', data.body.message);
            });
        });

        return {
            broadcast: function (message) {
                messagebus.whenReady(function () {
                    messagebus.send('/app/broadcast', {}, {message: message});
                });
            }
        }
    }]);

    qwait.controller('TopbarCtrl', ['$scope', '$location', 'users', 'system', 'messagebus', 'broadcaster', function ($scope, $location, users, system, messagebus, broadcaster) {
        $scope.location = $location;
        $scope.users = users;
        $scope.system = system;
        $scope.messagebus = messagebus;

        broadcaster.broadcast('User ' + users.current.name + ' visiting home page');
    }]);

    qwait.controller('TitleCtrl', ['$scope', 'system', 'page', function ($scope, system, page) {
        $scope.system = system;
        $scope.page = page;
    }]);

    qwait.controller('HomeCtrl', ['$scope', 'system', 'messagebus', 'page', 'contributors', function ($scope, system, messagebus, page, contributors) {
        page.title = 'Home';

        $scope.system = system;
        $scope.messagebus = messagebus;
        $scope.contributors = contributors;
    }]);

    qwait.controller('AboutCtrl', ['$scope', 'page', function ($scope, page) {
        page.title = 'About';
    }]);

    qwait.controller('QueueListCtrl', ['$scope', 'page', 'queues', function ($scope, page, queues) {
        page.title = 'Queue list';

        $scope.queues = queues;
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
