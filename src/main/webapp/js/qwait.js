(function () {
    var qwait = angular.module('qwait', []);

    qwait.config(function ($sceProvider) {
        // TODO: This is a security issue! Remove in final version and do sane interpolation
        $sceProvider.enabled(false);
    });

    qwait.filter('duration', function () {
        return function (milliseconds) {
            var res = moment.duration(milliseconds).humanize();

            res = res.replace(/a few seconds/g, "< 1 min");
            res = res.replace(/minute(s)?/g, "min");
            res = res.replace(/a(n)? /g, "1 ");

            return res;
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

    //This function returns the official color of the computer lab. 
    //In the cases where we return the hex color, it's because KTHs color doesn't match the CSS definition
    qwait.filter('getComputerColor', function () {
        return function (location) {

            if(/(blå|blue)/i.test(location)){
                return "blue";
            }

            else if(/(röd|red)/i.test(location)){
                return "red";
            }

            else if(/(orange)/i.test(location)){
                return "#FF7F00";
            }

            else if(/(gul|yellow)/i.test(location)){
                return "yellow";
            }

            else if(/(grön|green)/i.test(location)){
                return "green";
            }

            else if(/(brun|brown)/i.test(location)){
                return "#7F3F1F";
            }

            else if(/(grå|grey|gray)/i.test(location)){ 
                return "grey";
            }
            
            else if(/(karmosin|crimson)/i.test(location)){
                return "#D91536";
            }

            else if(/(vit|white)/i.test(location)){
                return "white";
            }

            else if(/(magenta|cerise)/i.test(location)){
                return "magenta";
            }

            else if(/(violett|violet)/i.test(location)){
                return "#AC00E6";
            }

            else if(/(turkos|turquoise)/i.test(location)){
                return "turquoise";
            }


            //These computer labs have official colours too
            else if(/(spel)/i.test(location)){
                return "#E6ADAD";
            }

            else if(/(sport)/i.test(location)){
                return "#ADADE6";
            }

            else if(/(musik)/i.test(location)){
                return "#ADE7AD";
            }

            else if(/(konst)/i.test(location)){
                return "#E8E7AF";
            }

            else if(/(mat)/i.test(location)){
                return "#E8C9AF";
            }

            //If the location doesn't match anything we return so that the element will be invisible
            else{
                return "transparent";
            }
        };
    });

})();
