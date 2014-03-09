"use strict";
describe('qwait core module', function () {
    beforeEach(module('qwait'));

    describe('queuetime filter', function () {
        it('filters times over a day correctly', inject(function (queuetimeFilter) {
            expect(queuetimeFilter(25 * 60 * 60 * 1000)).toEqual('Over a day');
        }));
        it('filters times that are exactly one day correctly', inject(function (queuetimeFilter) {
            expect(queuetimeFilter(24 * 60 * 60 * 1000)).toEqual('24h 0m');
        }));
    });
});
