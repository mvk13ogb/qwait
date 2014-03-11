"use strict";
describe('qwait core module', function () {
    beforeEach(module('qwait'));

    describe('duration filter', function () {
        it('filters durations about a day long correctly', inject(function (durationFilter) {
            var d = new Date("March 11, 2014 15:15:00");
            Date = function(){return d;};
            var d2 = new Date("March 10, 2014 15:10:00");
            expect(durationFilter(d2)).toEqual('a day');
        }));

        it('filters durations about two minutes long correctly', inject(function (durationFilter) {
            var d = new Date("March 11, 2014 15:15:00");
            Date = function(){return d;};
            var d2 = new Date("March 11, 2014 15:13:00");
            expect(durationFilter(d2)).toEqual('2 minutes');
        }));
    });
});
