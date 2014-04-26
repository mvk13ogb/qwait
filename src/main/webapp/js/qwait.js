(function () {
    var qwait = angular.module('qwait', ['ngRoute', 'ngAnimate', 'request']);

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
            when('/admin', {
                templateUrl: 'partial/admin.html',
                controller: 'AdminCtrl'
            }).
            otherwise({
                redirectTo: '/'
            });
    }]);

    qwait.factory('users', ['$q', '$http', '$cacheFactory', 'messagebus', 'requestInfo', function ($q, $http, $cacheFactory, messagebus, requestInfo) {
        var result = {},
            cache = $cacheFactory('users');

        messagebus.whenReady(function () {
            messagebus.subscribe('/topic/user/*', function (data) {
                switch (data.body['@type']) {
                    case 'UserAdminStatusChanged':
                        var user = cache.get(data.body.name);
                        if (user) {
                            user.admin = data.body.admin;

                            var index = result.admins.indexOf(user);

                            if (data.body.admin && index == -1) {
                                result.admins.push(user);
                            }

                            if (!data.body.admin && index >= 0) {
                                result.admins.splice(index, 1);
                            }
                        }
                        break;
                    case 'QueueOwnerAdded':
                        break;
                    case 'QueuePositionCreatedInAccount':
                        var user = cache.get(data.body.userName);
                        if (user) {
                            user.queuePositions.push(data.body.queuePosition);
                        }
                        break;
                    case 'QueuePositionRemoved':
                        var user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].userName == data.body.userName) {
                                    user.queuePositions.splice(i, 1);
                                }
                            }
                        }
                        break;
                    case 'QueuePositionCommentChanged':
                        var user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].userName == data.body.userName) {
                                    user.queuePositions[i].comment = data.body.comment;
                                }
                            }
                        }
                        break;
                    case 'QueuePositionLocationChanged':
                        var user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].userName == data.body.userName) {
                                    user.queuePositions[i].location = data.body.location;
                                }
                            }
                        }
                        break;
                    default:
                        console.log('Unrecognized user message', data.body);
                }
            });
        });

        result.find = function (query) {
            var promise = $http.get('/api/users?query=' + encodeURIComponent(query));
            promise.success(function (users) {
                for (var i = 0; i < users.length; i++) {
                    var user = cache.get(users[i].name);
                    if (user) {
                        users[i] = user;
                    } else {
                        cache.put(users[i].name, users[i]);
                    }
                }
            });
            return promise;
        };

        result.doGet = function (name) {
            return $http.get('/api/user/' + encodeURIComponent(name));
        };

        result.get = function (name) {
            var cached = cache.get(name);

            if (!cached) {
                cached = {};
                cache.put(name, cached);

                result.doGet(name).success(function (user) {
                    angular.extend(cached, user);
                });
            }

            return cached;
        };

        result.setAdmin = function (userName, admin) {
            return $http.put('/api/user/' + encodeURIComponent(userName) + '/role/admin', '' + admin);
        };

        result.current = requestInfo.currentUser;

        if (result.current.name) {
            cache.put(result.current.name, result.current);
            result.doGet(result.current.name).success(function (user) {
                angular.extend(result.current, user);
            });
        }

        result.admins = [];

        $http.get('/api/users?role=admin').success(function (admins) {
            for (var i = 0; i < admins.length; i++) {
                var user = cache.get(admins[i].name);
                if (user) {
                    admins[i] = user;
                } else {
                    cache.put(admins[i].name, admins[i]);
                }
            }

            result.admins = admins;
        });

        return result;
    }]);

    qwait.factory('queues', ['$http', '$timeout', 'messagebus', function ($http, $timeout, messagebus) {
        var result = {};

        result.all = {};

        messagebus.whenReady(function () {
            messagebus.subscribe('/topic/queue', function (data) {
                switch (data.body['@type']) {
                    case 'QueueCreated':
                        // HACK: the message arrives so fast that the database transaction on the server might not have
                        // ended yet. This delay "ensures" that the transaction has time to end.
                        $timeout(function () {

                            // This call will store the queue in result.all
                            result.get(data.body.name);
                        }, 500);
                        break;
                    default:
                        console.log('Unrecognized queue message', data.body);
                }
            });
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
                                if (queue.positions[i].userName == data.body.userName) {
                                    queue.positions[i].comment = data.body.comment;
                                }
                            }
                        }
                        break;
                    case 'QueuePositionCreatedInQueue':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            queue.positions.push(data.body.queuePosition);
                        }
                        break;
                    case 'QueuePositionLocationChanged':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            for (i = 0; i < queue.positions.length; i++) {
                                if (queue.positions[i].userName == data.body.userName) {
                                    queue.positions[i].location = data.body.location;
                                }
                            }
                        }
                        break;
                    case 'QueuePositionRemoved':
                        queue = result.all[data.body.queueName];
                        if (queue) {
                            for (i = 0; i < queue.positions.length; i++) {
                                if (queue.positions[i].userName == data.body.userName) {
                                    queue.positions.splice(i, 1);
                                }
                            }
                        }
                        break;
                    default:
                        console.log('Unrecognized queue message', data.body);
                }
            });
        });

        $http.get('/api/queues').success(function (queues) {
            for (var i = 0; i < queues.length; i++) {
                var name = queues[i].name;
                if (result.all[name]) {
                    angular.extend(result.all[name], queues[i]);
                } else {
                    result.all[name] = queues[i];
                }
            }
        });

        result.doGet = function (name) {
            return $http.get('/api/queue/' + encodeURIComponent(name));
        };

        result.get = function (name) {
            var cached = result.all[name];

            if (!cached) {
                cached = {};
                result.all[name] = cached;
                result.doGet(name).success(function (queue) {
                    angular.extend(cached, queue);
                });
            }
            return cached;
        };

        result.contains = function (queueName, queues) {
            if (queueName === undefined) {
                return false;
            }
            var title = queueName.replace(/[\s\/]+/g, '-').toLowerCase();

            for (var o in queues.all) {
                if (o == title) {
                    return true;
                }
            }
            return false;
        }

        result.validateForm = function (queueName, queues) {
            if (!result.contains(queueName, queues)) {
                result.putQueue(queueName, queues);
            }
        }

        result.setLocked = function (name, locked) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + encodeURIComponent(name) + '/locked', '' + locked, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.setActive = function (name, active) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + encodeURIComponent(name) + '/active', '' + active, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.putQueue = function (title, queues) {
            var name = title.replace(/[\s\/]+/g, '-').toLowerCase();
            return $http.put('/api/queue/' + encodeURIComponent(name), {
                'title': title
            });
        };

        result.clearQueue = function (name) {
            return $http.post('/api/queue/' + name + '/clear', {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.joinQueue = function (name, user) {
            return $http.put('/api/queue/' + name + '/position/' + user, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.leaveQueue = function (name, user) {
            return $http.delete('/api/queue/' + name + '/position/' + user, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.changeComment = function (name, user, comment) {
            return $http.put('/api/queue/' + name + '/position/' + user + '/comment', '' + comment, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.changeLocation = function (name, user, location) {
            return $http.put('/api/queue/' + name + '/position/' + user + '/location', '' + location, {
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
                { name: 'Adrian Blanco', gravatar: '5e24f37bda5a846cdaa822e72627fe63', github: 'adrianblp'},
                { name: 'Casper Winsnes' },
                { name: 'Christoffer Pettersson', gravatar: '5ba6cca11f93ea6d22f458700ac8a506' },
                { name: 'David Flemström', gravatar: '202ecb437d8bbd442d093a3a35c67a04', twitter: 'dflemstr' },
                { name: 'Eric Schmidt', gravatar: '62c78ae979bece6aeb0a153641a46fbd' },
                { name: 'Gustav Zander', gravatar: '354a77646cf4a560ea5d5357a5a4aa84' },
                { name: 'Hampus Liljekvist', gravatar: '9f977d80508af50fe1fcc53f6db7b1a1', twitter: 'hlilje' },
                { name: 'Jacob Sievers', gravatar: '00c2d95911a8ccf7e5200257f03ffb34' },
                { name: 'Michael Håkansson', gravatar: 'e12d2965870d5054f901b088ab692d3d', twitter: 'michaelhak' },
                { name: 'Robin Engström', gravatar: 'd3389ec4c8f9a0d7d0500ec982a35099' }
            ]
        }
    });

    qwait.factory('page', function () {
        return {
            title: ''
        };
    });

    qwait.factory('clock', ['$interval', function ($interval) {
        var result = {
            now: moment()
        };

        $interval(function () {
            result.now = moment();
        }, 1000);

        return result;
    }]);

    qwait.factory('security', function () {
        var result = {
            isQueueOwner: function (user, queue) {
                if (!queue.owners) {
                    return undefined;
                }
                return queue.owners.indexOf(user.name) != -1;
            },
            isQueueModerator: function (user, queue) {
                if (!queue.owners) {
                    return undefined;
                }
                return queue.moderators.indexOf(user.name) != -1;
            },
            canModerateQueue: function (user, queue) {
                return result.isQueueOwner(user, queue) || result.isQueueModerator(user, queue) || user.admin;
            }
        };

        return result;
    });

    qwait.factory('queuePositions', function () {
        var result = {
            getUserQueuePos: function (user, positions) {
                if (!(user && positions)) {
                    return undefined;
                }

                for (var i = 0; i < positions.length; i++) {
                    if (positions[i].userName == user.name) {
                        return positions[i];
                    }
                }

                return null;
            }
        };

        return result;
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

    qwait.controller('HomeCtrl', ['$scope', 'system', 'messagebus', 'page', function ($scope, system, messagebus, page) {
        page.title = 'Home';

        $scope.system = system;
        $scope.messagebus = messagebus;
    }]);

    qwait.controller('AboutCtrl', ['$scope', 'system', 'page', 'contributors', function ($scope, system, page, contributors) {
        page.title = 'About';

        $scope.system = system;
        $scope.contributors = contributors;
    }]);

    qwait.controller('QueueListCtrl', ['$scope', 'page', 'clock', 'queues', 'users', 'security', 'queuePositions', function ($scope, page, clock, queues, users, security, queuePositions) {
        page.title = 'Queue list';

        $scope.users = users;
        $scope.queues = queues;

        $scope.canModerateQueue = security.canModerateQueue;
        $scope.userQueuePos = queuePositions.getUserQueuePos;
        $scope.timeDiff = function (time) {
            return moment(time).from(clock.now, true);
        };
    }]);

    qwait.controller('QueueCtrl', ['$scope', '$route', 'clock', 'queues', 'users', 'page', 'queuePositions', function ($scope, $route, clock, queues, users, page, queuePositions) {
        page.title = 'View queue';

        $scope.queues = queues;
        $scope.users = users;
        $scope.queue = queues.get($route.current.params.queueName);
        $scope.getUser = function (userName) {
            return users.get(userName);
        };
        $scope.userQueuePos = queuePositions.getUserQueuePos;
        $scope.timeDiff = function (time) {
            return moment(time).from(clock.now, true);
        };
    }]);

    qwait.controller('AdminCtrl', ['$scope', 'page', 'users', function ($scope, page, users) {
        page.title = 'Admin tools';

        $scope.users = users;
    }]);

    qwait.filter('duration', function () {
        return function (milliseconds) {
            return moment.duration(milliseconds).humanize();
        }
    });

    qwait.filter('arrayify', function () {
        return function (object) {
            var result = [];

            for (var key in object) {
                if (object.hasOwnProperty(key)) {
                    result.push(object[key]);
                }
            }

            return result;
        };
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
