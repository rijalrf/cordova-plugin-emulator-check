# cordova-plugin-emulator-check

Plugin Cordova untuk mendeteksi emulator Android (LDPlayer, Nox, MEmu, BlueStacks, Genymotion, dan Android Studio AVD).

> Platform: Android saja. Tidak memerlukan permission tambahan atau akses root.

---

## Instalasi

### Cordova
```bash
cordova plugin add https://github.com/rijalrf/cordova-plugin-emulator-check.git
```

### OutSystems 11 (Extensibility Configurations)
```json
{
  "plugin": {
    "url": "https://github.com/rijalrf/cordova-plugin-emulator-check.git"
  }
}
```

---

## Penggunaan

### Cordova / JavaScript
```javascript
document.addEventListener("deviceready", function () {
  EmulatorCheck.check(
    function (result) {
      console.log("Is Emulator:", result.isEmulator); // true / false
      console.log("Reasons:", result.reasons);         // alasan deteksi
      console.log("Device Info:", result.info);       // detail hardware/build
    },
    function (err) {
      console.error("Gagal mendeteksi:", err);
    }
  );
});
```

### OutSystems (Node JavaScript)
```javascript
if (typeof EmulatorCheck === "undefined") {
  $parameters.IsEmulator = false;
  $parameters.Reasons = "Plugin tidak tersedia";
  $resolve();
} else {
  EmulatorCheck.check(
    function (r) {
      $parameters.IsEmulator = r.isEmulator;
      $parameters.Reasons = r.reasons.join(", ");
      $parameters.Info = JSON.stringify(r.info);
      $resolve();
    },
    function (err) {
      $parameters.IsEmulator = false;
      $parameters.Reasons = "Error: " + err;
      $resolve();
    }
  );
}
```

---

## Format Output

```json
{
  "isEmulator": true,
  "reasons": ["file:/system/bin/ldinit", "hardware:ttvm_x86"],
  "info": {
    "fingerprint": "...",
    "model": "...",
    "manufacturer": "...",
    "brand": "...",
    "device": "...",
    "product": "...",
    "hardware": "...",
    "board": "...",
    "abi": "x86_64"
  }
}
```

---

## Catatan & Batasan

- Deteksi mencakup berkas khas emulator, properti build/hardware, dan system property QEMU.
- Khusus platform Android (tidak mendukung iOS).
- Deteksi di sisi client dapat dimanipulasi dengan hooking (Frida/Xposed). Gunakan validasi server tambahan untuk alur transaksi penting.

---

## Lisensi

MIT
