"use strict";
describe('qwait core module', function () {
    beforeEach(module('qwait'));

    describe('duration filter', function () {
        it('filters durations about a day long correctly', inject(function (durationFilter) {
            expect(durationFilter(25 * 60 * 60 * 1000)).toEqual('1 day');
        }));

        it('filters durations about an hour long correctly', inject(function (durationFilter) {
            expect(durationFilter(60 * 60 * 1000)).toEqual('1 hour');
        }));

        it('filters durations about two minutes long correctly', inject(function (durationFilter) {
            expect(durationFilter(2 * 60 * 1000)).toEqual('2 min');
        }));

        it('filters durations about one minute long correctly', inject(function (durationFilter) {
            expect(durationFilter(1 * 60 * 1000)).toEqual('1 min');
        }));

        it('filters durations new queuer correctly', inject(function (durationFilter) {
            expect(durationFilter(1 * 1000)).toEqual('< 1 min');
        }));
    });

    describe('getComputerName filter', function () {
        it('filters getComputerName red correctly', inject(function (getComputerNameFilter) {
            expect(getComputerNameFilter("red5.csc.kth.se")).toEqual('Red 5');
        }));

        it('filters getComputerName magenta correctly', inject(function (getComputerNameFilter) {
            expect(getComputerNameFilter("magenta-5.ug.kth.se")).toEqual('Magenta 5');
        }));

        it('filters getComputerName junk correctly', inject(function (getComputerNameFilter) {
            expect(getComputerNameFilter("red.comhem.se")).toEqual('');
        }));
    });

    describe('getComputerColor filter', function () {
        it('filters getComputerColor red correctly', inject(function (getComputerColorFilter) {
            expect(getComputerColorFilter("Red 5")).toEqual('red');
        }));

        it('filters getComputerColor violett correctly', inject(function (getComputerColorFilter) {
            expect(getComputerColorFilter("Violett 5")).toEqual('#AC00E6');
        }));

        it('filters getComputerColor spel correctly', inject(function (getComputerColorFilter) {
            expect(getComputerColorFilter("spel")).toEqual('#E6ADAD');
        }));

        it('filters getComputerColor junk correctly', inject(function (getComputerColorFilter) {
            expect(getComputerColorFilter("Test")).toEqual('transparent');
        }));
    });
});