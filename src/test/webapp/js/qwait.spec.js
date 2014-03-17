"use strict";
describe('qwait core module', function () {
    beforeEach(module('qwait'));

    describe('duration filter', function () {
        it('filters durations about a day long correctly', inject(function (durationFilter) {
            expect(durationFilter(25 * 60 * 60 * 1000)).toEqual('a day');
        }));

        it('filters durations about two minutes long correctly', inject(function (durationFilter) {
            expect(durationFilter(2 * 60 * 1000)).toEqual('2 minutes');
        }));
    });
});
