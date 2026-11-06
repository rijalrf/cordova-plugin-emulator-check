var exec = require('cordova/exec');

module.exports = {
    /**
     * Hasil di onSuccess berupa object:
     * {
     *   isEmulator: true/false,
     *   reasons: ["file:/system/bin/ldinit", "hardware:ttvm_x86", ...],
     *   info: { fingerprint, model, manufacturer, brand, device, product, hardware, board, abi }
     * }
     */
    check: function (onSuccess, onError) {
        exec(onSuccess, onError, 'EmulatorCheck', 'check', []);
    }
};
