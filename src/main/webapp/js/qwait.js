(function () {
    var qwait = angular.module('qwait', ['mm.foundation', 'ngRoute', 'ngAnimate', 'request']);

    qwait.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
        $locationProvider.html5Mode(true);

        // Don't forget to update the white list for index() in HomeController with the entries in this routing table
        $routeProvider.
            when('/about', {
                templateUrl: 'partial/about.html',
                controller: 'AboutCtrl'
            }).
            when('/help', {
                templateUrl: 'partial/help.html',
                controller: 'HelpCtrl'
            }).
            when('/', {
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
                var i;
                var user;
                switch (data.body['@type']) {
                    case 'UserAdminStatusChanged':
                        user = cache.get(data.body.name);
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
                        user = cache.get(data.body.userName);
                        i = user.ownedQueues.indexOf(data.body.queueName);
                        if (user) {
                            if (i == -1) {
                                user.ownedQueues.push(data.body.queueName);
                            }
                        }
                        break;
                    case 'QueueOwnerRemoved':
                        user = cache.get(data.body.userName);
                        i = user.ownedQueues.indexOf(data.body.queueName);
                        if (user) {
                            if (i != -1) {
                                user.queuePositions.splice(i, 1);
                            }
                        }
                        break;
                    case 'QueuePositionCreatedInAccount':
                        user = cache.get(data.body.userName);
                        if (user) {
                            user.queuePositions.push(data.body.queuePosition);
                        }
                        break;
                    case 'QueuePositionRemoved':
                        user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].queueName == data.body.queueName) {
                                    user.queuePositions.splice(i, 1);
                                }
                            }
                        }
                        break;
                    case 'QueuePositionCommentChanged':
                        user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].queueName == data.body.queueName) {
                                    user.queuePositions[i].comment = data.body.comment;
                                }
                            }
                        }
                        break;
                    case 'QueuePositionLocationChanged':
                        user = cache.get(data.body.userName);
                        if (user) {
                            for (i = 0; i < user.queuePositions.length; i++) {
                                if (user.queuePositions[i].queueName == data.body.queueName) {
                                    user.queuePositions[i].location = data.body.location;
                                }
                            }
                        }
                        break;
                    case 'QueueModeratorAdded':
                        user = cache.get(data.body.userName);
                        if (user) {
                            i = user.moderatedQueues.indexOf(data.body.queueName);
                            if (i == -1) {
                                user.moderatedQueues.push(data.body.queueName);
                            }
                        }
                        break;
                    case 'QueueModeratorRemoved':
                        user = cache.get(data.body.userName);
                        if (user) {
                            i = user.moderatedQueues.indexOf(data.body.queueName);
                            if (i != -1) {
                                user.moderatedQueues.splice(i, 1);
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
                    case 'QueueRemoved':
                        delete result.all[data.body.name];
                        break;
                    default:
                        console.log('Unrecognized queue message', data.body);
                }
            });
            messagebus.subscribe('/topic/queue/*', function (data) {
                var queue, i;
                switch (data.body['@type']) {
                    case 'QueueHiddenStatusChanged':
                        queue = result.all[data.body.name];
                        if (queue) {
                            queue.hidden = data.body.hidden;
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
                            i = queue.moderators.indexOf(data.body.userName);

                            if (i == -1) {
                                queue.moderators.push(data.body.userName);
                            }
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
        };

        result.validateForm = function (queueName, queues) {
            if (!result.contains(queueName, queues)) {
                result.putQueue(queueName, queues);
            }
        };

        result.setLocked = function (name, locked) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + encodeURIComponent(name) + '/locked', '' + locked, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.setHidden = function (name, hidden) {
            // The "'' + " bit is needed because apparently you can't send "false" as JSON here
            return $http.put('/api/queue/' + encodeURIComponent(name) + '/hidden', '' + hidden, {
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

        result.deleteQueue = function (name) {
            return $http.delete('/api/queue/' + encodeURIComponent(name));
        };

        result.clearQueue = function (name) {
            return $http.post('/api/queue/' + name + '/clear', {});
        };

        result.joinQueue = function (name, user) {
            return $http.put('/api/queue/' + name + '/position/' + user, {});
        };

        result.leaveQueue = function (name, user) {
            return $http.delete('/api/queue/' + name + '/position/' + user);
        };

        result.changeComment = function (name, user, comment) {
            return $http.put('/api/queue/' + name + '/position/' + user + '/comment', {comment: comment}, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.changeLocation = function (name, user, location) {
            return $http.put('/api/queue/' + name + '/position/' + user + '/location', {location: location}, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        };

        result.addModerator = function (name, user) {
            return $http.put('/api/queue/' + name + '/moderator/' + user, {});
        };

        result.removeModerator = function (name, user) {
            return $http.delete('/api/queue/' + name + '/moderator/' + user);
        };

        result.addOwner = function (name, user) {
            return $http.put('/api/queue/' + name + '/owner/' + user, {});
        };

        result.removeOwner = function (name, user) {
            return $http.delete('/api/queue/' + name + '/owner/' + user);
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
                { name: 'Casper Winsnes', gravatar: '0cb03d273d7ab05bcdd39b317a3bb401' },
                { name: 'Christoffer Pettersson', gravatar: '5ba6cca11f93ea6d22f458700ac8a506', github: 'krillmeister', twitter: 'csieuwerts' },
                { name: 'David Flemström', gravatar: '202ecb437d8bbd442d093a3a35c67a04', github: 'dflemstr', twitter: 'dflemstr' },
                { name: 'Eric Schmidt', gravatar: '62c78ae979bece6aeb0a153641a46fbd' },
                { name: 'Gustav Zander', gravatar: '354a77646cf4a560ea5d5357a5a4aa84' },
                { name: 'Hampus Liljekvist', gravatar: '9f977d80508af50fe1fcc53f6db7b1a1', github: 'hlilje', twitter: 'hlilje' },
                { name: 'Jacob Sievers', gravatar: '00c2d95911a8ccf7e5200257f03ffb34' },
                { name: 'Michael Håkansson', gravatar: 'e12d2965870d5054f901b088ab692d3d', github: 'michaelhakansson', twitter: 'michaelhak' },
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
        return {
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

    qwait.factory('debounce', function ($timeout, $q) {
        return function (func, wait, immediate) {
            var timeout;
            var deferred = $q.defer();
            return function () {
                var context = this, args = arguments;
                var later = function () {
                    timeout = null;
                    if (!immediate) {
                        deferred.resolve(func.apply(context, args));
                        deferred = $q.defer();
                    }
                };
                var callNow = immediate && !timeout;
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(later, wait);
                if (callNow) {
                    deferred.resolve(func.apply(context, args));
                    deferred = $q.defer();
                }
                return deferred.promise;
            };
        };
    });

    qwait.controller('TopbarCtrl', ['$scope', '$location', 'users', 'system', 'messagebus', function ($scope, $location, users, system, messagebus) {
        $scope.location = $location;
        $scope.users = users;
        $scope.system = system;
        $scope.messagebus = messagebus;

    }]);

    qwait.controller('TitleCtrl', ['$scope', 'system', 'page', function ($scope, system, page) {
        $scope.system = system;
        $scope.page = page;
    }]);

    qwait.controller('HelpCtrl', ['$scope', '$location', 'page', 'users', function ($scope, $location, page, users) {
        page.title = 'Help';
        $scope.users = users;
        $scope.location = $location;
    }]);

    qwait.controller('AboutCtrl', ['$scope', 'system', 'page', 'contributors', function ($scope, system, page, contributors) {
        page.title = 'About';

        $scope.system = system;
        $scope.contributors = contributors;
    }]);

    qwait.controller('QueueListCtrl', ['$scope', '$location', 'page', 'clock', 'queues', 'users', 'security', 'queuePositions', 'getQueuePosNr',
        function ($scope, $location, page, clock, queues, users, security, queuePositions, getQueuePosNr) {
            page.title = 'Queues';

            $scope.users = users;
            $scope.queues = queues;
            $scope.queuePos = getQueuePosNr;

            $scope.canModerateQueue = security.canModerateQueue;
            $scope.userQueuePos = queuePositions.getUserQueuePos;
            $scope.joinQueue = function (queueName, userName) {
                queues.joinQueue(queueName, userName);
                $location.path('/queue/' + queueName)
            };
            $scope.timeDiff = function (time) {
                return moment(time).from(clock.now, true);
            };
        }]);

    qwait.controller('QueueCtrl', ['$scope', '$location', '$route', '$timeout', '$filter', '$modal', 'clock', 'queues', 'users', 'security', 'page', 'queuePositions', 'debounce', 'getQueuePosNr', 'requestInfo',
        function ($scope, $location, $route, $timeout, $filter, $modal, clock, queues, users, security, page, queuePositions, debounce, getQueuePosNr, requestInfo) {

            $scope.queue = queues.get($route.current.params.queueName);
            $scope.queues = queues;
            $scope.users = users;
            $scope.location = $location;

            $scope.isQueueOwner = security.isQueueOwner;
            $scope.canModerateQueue = security.canModerateQueue;
            $scope.locationplaceholder = $filter('getComputerName')(requestInfo.hostname);

            var temp = getQueuePosNr;
            $timeout(function () {
                $scope.queuePosNr = function () {
                    var i = temp(users.current.name, $scope.queue.positions);
                    page.title = i ? (' [' + i + '] ' + $scope.queue.title || 'Queue') :
                        ($scope.queue.title || 'Queue');
                    return i;
                }
            }, 500);

            $scope.userQueuePos = queuePositions.getUserQueuePos;

            $scope.removeQueue = function (queueName) {
                queues.deleteQueue(queueName);
                $location.path('/queues');
            };

            $scope.timeDiff = function (time) {
                return moment(time).from(clock.now, true);
            };

            var wait = 2000;

            $scope.changeLocationDebounced = debounce(function (form, queueName, userName, location) {
                if (form.$valid) {
                    queues.changeLocation(queueName, userName, location);
                }
            }, wait, false);

            $scope.changeCommentDebounced = debounce(function (form, queueName, userName, comment) {
                if (form.$valid) {
                    queues.changeComment(queueName, userName, comment);
                }
            }, wait, false);

            $scope.joinQueueFull = debounce(function (name, user, location, locationform, comment, commentform) {
                if (locationform.$valid) {
                    if (users.get(user).queuePositions.length != 0) {
                        $scope.open = function () {
                            var modalInstance = $modal.open({
                                templateUrl: 'confirmationModal.html',
                                controller: function ($scope, $modalInstance, queuePositions, queueName, userName, location, locationform, comment, commentform) {
                                    $scope.queuePositions = queuePositions;

                                    $scope.getQueue = function (queueName) {
                                        return queues.get(queueName);
                                    };

                                    $scope.ok = function () {
                                        queues.joinQueue(queueName, userName);

                                        // HACK: Places a timeout so there is time to join the queue
                                        setTimeout(function () {
                                            if (locationform.$valid) {
                                                queues.changeLocation(queueName, userName, location);
                                            }

                                            if (commentform.$valid) {
                                                setTimeout(function () {
                                                    queues.changeComment(queueName, userName, comment);
                                                }, 500);
                                            }
                                        }, 500);

                                        $modalInstance.close();
                                    };
                                    $scope.cancel = function () {
                                        $modalInstance.dismiss('cancel');
                                    };
                                },
                                resolve: {
                                    queuePositions: function () {
                                        return users.current.queuePositions;
                                    },
                                    queueName: function () {
                                        return name;
                                    },
                                    userName: function () {
                                        return user;
                                    },
                                    location: function () {
                                        return location;
                                    },
                                    locationform: function () {
                                        return locationform;
                                    },
                                    comment: function () {
                                        return comment;
                                    },
                                    commentform: function () {
                                        return commentform;
                                    }
                                }
                            });
                        };

                        $scope.open();
                    } else {
                        queues.joinQueue(name, user);

                        //HACK, places a timeout so we have time to join the queue
                        setTimeout(function () {
                            if (locationform.$valid) {
                                queues.changeLocation(name, user, location);
                            }

                            if (commentform.$valid) {
                                queues.changeComment(name, user, comment);
                            }
                        }, 500);
                    }
                }
            }, 200, true);
        }]);

    qwait.controller('AdminCtrl', ['$scope', '$timeout', 'page', 'users', 'queues', function ($scope, $timeout, page, users, queues) {
        page.title = 'Admin tools';

        $scope.users = users;
        $scope.queues = queues;

        $scope.selectedQueue = undefined;
        $scope.dropdown = undefined;

        $scope.find = function (user) {
            return users.find(user).then(function (res) {
                return res.data;
            });
        };

        $scope.getUser = function (userName) {
            return users.get(userName);
        };

        $scope.selectQueue = function (queue) {
            $scope.selectedQueue = queue;
        };

        var ownedQueues = users.current.ownedQueues;

        $scope.ownedQueues = [];
        if (ownedQueues) {
            for (var i = 0; i < ownedQueues.length; i++) {
                // Fetch the queues of the current user
                $scope.ownedQueues.push(queues.get(ownedQueues[i]));
            }
        } else {
            console.log("Current user was not loaded");
        }
    }]);

    qwait.controller('LockQueueModalCtrl', ['$scope', '$modal', function ($scope, $modal) {

        $scope.open = function () {

            var modalInstance = $modal.open({
                templateUrl: 'lock-queue-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues) {

                    $scope.queue = queue;
                    $scope.queues = queues;

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return $scope.queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    }
                }
            });
        };
    }]);

    qwait.controller('HideQueueModalCtrl', ['$scope', '$modal', function ($scope, $modal) {

        $scope.open = function () {

            var modalInstance = $modal.open({
                templateUrl: 'hide-queue-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues) {

                    $scope.queue = queue;
                    $scope.queues = queues;

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return $scope.queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    }
                }
            });
        };
    }]);

    qwait.controller('ClearQueueModalCtrl', ['$scope', '$modal', function ($scope, $modal) {

        $scope.open = function () {

            var modalInstance = $modal.open({
                templateUrl: 'clear-queue-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues) {

                    $scope.queue = queue;
                    $scope.queues = queues;

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return $scope.queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    }
                }
            });
        };
    }]);

    qwait.controller('DeleteQueueModalCtrl', ['$scope', '$modal', '$location', function ($scope, $modal, $location) {

        $scope.open = function () {

            var modalInstance = $modal.open({
                templateUrl: 'delete-queue-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues, location) {

                    $scope.queue = queue;
                    $scope.queues = queues;
                    $location = location;

                    $scope.deleteQueue = function (queueName) {
                        $scope.queues.deleteQueue(queueName);
                        $location.path('/queues');
                    };

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return $scope.queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    },
                    location: function () {
                        return $location;
                    }
                }
            });
        };
    }]);

    qwait.controller('RemoveUserModalCtrl', ['$scope', '$modal', function ($scope, $modal) {

        $scope.open = function (position) {

            var modalInstance = $modal.open({
                templateUrl: 'remove-user-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues, position, users) {
                    $scope.queue = queue;
                    $scope.queues = queues;
                    $scope.position = position;
                    $scope.users = users;

                    $scope.getUser = function (userName) {
                        return users.get(userName);
                    };

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return $scope.queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    },
                    position: function () {
                        return position;
                    },
                    users: function () {
                        return $scope.users;
                    }
                }
            });
        };
    }]);

    qwait.controller('RemoveModeratorModalCtrl', ['$scope', '$modal', function($scope, $modal) {

        $scope.open = function (moderator, queue) {

            var modalInstance = $modal.open({
                templateUrl: 'remove-moderator-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues, position, users) {
                    $scope.queue = queue;
                    $scope.queues = queues;
                    $scope.position = position;

                    $scope.getUser = function (userName) {
                        return users.get(userName);
                    }

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    },
                    position: function () {
                        return moderator;
                    }
                }
            });
        };
    }]);

    qwait.controller('RemoveOwnerModalCtrl', ['$scope', '$modal', function($scope, $modal) {

        $scope.open = function (owner, queue) {

            var modalInstance = $modal.open({
                templateUrl: 'remove-owner-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues, position, users) {
                    $scope.queue = queue;
                    $scope.queues = queues;
                    $scope.position = position;

                    $scope.getUser = function (userName) {
                        return users.get(userName);
                    }

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    },
                    position: function () {
                        return owner;
                    }
                }
            });
        };
    }]);

    qwait.controller('RemoveAdminModalCtrl', ['$scope', '$modal', function($scope, $modal) {

        $scope.open = function (admin, queue) {

            var modalInstance = $modal.open({
                templateUrl: 'remove-admin-modal-content.html',
                controller: function ($scope, $modalInstance, queue, queues, position, users) {
                    $scope.queue = queue;
                    $scope.queues = queues;
                    $scope.position = position;
                    $scope.users = users;

                    $scope.ok = function () {
                        $modalInstance.close();
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                },
                resolve: {
                    queue: function () {
                        return queue;
                    },
                    queues: function () {
                        return $scope.queues;
                    },
                    position: function () {
                        return admin;
                    },
                    users: function () {
                        return $scope.users;
                    }
                }
            });
        };
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

            var exclude = /share|kthopen|eduroam|dyna/;
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

    qwait.filter('queuesSeenBy', function () {
        return function (queues, user) {
            var result = [];

            for (var i = 0; i < queues.length; i++) {
                var queue = queues[i];
                if (queue && !queue.hidden) {
                    result.push(queue);
                } else if (queue && queue.hidden && (user.admin || queue.owners.indexOf(user.name) != -1)) {
                    result.push(queue);
                }
            }
            return result;
        };
    });

    // Returns the queues that the user are moderator for
    qwait.filter('queuesModeratedBy', function () {
        return function (queues, user) {
            var result = [];

            for (var i = 0; i < queues.length; i++) {
                var queue = queues[i];
                if (user.moderatedQueues.indexOf(queue.name) != -1) {
                    result.push(queue);
                }
            }
            return result;
        };
    });

    // Returns the queues that the user are owner for
    qwait.filter('queuesOwnedBy', function () {
        return function (queues, user) {
            var result = [];

            for (var i = 0; i < queues.length; i++) {
                var queue = queues[i];
                if (user.ownedQueues.indexOf(queue.name) != -1) {
                    result.push(queue);
                }
            }
            return result;
        };
    });

    qwait.factory('getQueuePos', function () {
        return function (name, positions) {
            for (var i = 0; i < positions.length; i++) {
                var pos = positions[i];

                if (pos.account.principalName == name) {
                    return pos;
                }
            }

            return null;
        }
    });

    qwait.factory('getQueuePosNr', function () {
        return function (name, positions) {
            var sortedPositions = positions.sort(function (a, b) {
                return(a.startTime - b.startTime);
            });

            for (var i = 0; i < sortedPositions.length; i++) {
                var pos = sortedPositions[i];

                if (pos.userName == name)
                    return i + 1;
            }

            return null;
        }
    });

    //This function returns the official color of the computer lab.
    //In the cases where we return the hex color, it's because KTHs color doesn't match the CSS definition
    qwait.filter('getComputerColor', function () {
        return function (location) {

            if (/(cerise)/i.test(location)) {
                return "pink";
            }

            else if (/(rosa|pink)/i.test(location)) {
                return "#E2007F";
            }

            else if (/(blå|blue)/i.test(location)) {
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

            else if (/(magenta)/i.test(location)) {
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

            //If the location doesn't match anything we return transparent
            else {
                return "transparent";
            }
        };
    });
})();
