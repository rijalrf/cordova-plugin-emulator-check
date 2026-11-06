package com.emulatorcheck;

import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

public class EmulatorCheck extends CordovaPlugin {

    // File khas emulator. Kalau salah satu ada, hampir pasti emulator.
    private static final String[] EMULATOR_FILES = {
        // LDPlayer
        "/system/bin/ldinit",
        "/system/bin/ldmountsf",
        "/system/lib/libldutils.so",
        // Nox
        "/system/bin/nox-prop",
        "/system/lib/libnoxspeedup.so",
        "/fstab.nox",
        "/init.nox.rc",
        "/ueventd.nox.rc",
        // MEmu / VirtualBox based
        "/system/bin/microvirt-prop",
        "/system/bin/androVM-prop",
        // BlueStacks
        "/data/.bluestacks.prop",
        // Andy / Droid4X / Windroye
        "/fstab.andy",
        "/ueventd.andy.rc",
        "/system/lib/libdroid4x.so",
        "/system/bin/windroyed",
        // Genymotion
        "/dev/socket/genyd",
        "/dev/socket/baseband_genyd",
        // QEMU (emulator Android Studio)
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/qemu_trace",
        "/system/bin/qemu-props"
    };

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (!"check".equals(action)) {
            return false;
        }
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callbackContext.success(runCheck());
                } catch (Exception e) {
                    callbackContext.error("EmulatorCheck error: " + e.getMessage());
                }
            }
        });
        return true;
    }

    private JSONObject runCheck() throws JSONException {
        JSONArray reasons = new JSONArray();

        String fingerprint = lower(Build.FINGERPRINT);
        String model = lower(Build.MODEL);
        String manufacturer = lower(Build.MANUFACTURER);
        String brand = lower(Build.BRAND);
        String device = lower(Build.DEVICE);
        String product = lower(Build.PRODUCT);
        String hardware = lower(Build.HARDWARE);
        String board = lower(Build.BOARD);
        String abi = (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0)
                ? lower(Build.SUPPORTED_ABIS[0]) : "";

        // 1. Properti Build
        if (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") || fingerprint.contains("vbox")) {
            reasons.put("fingerprint:" + fingerprint);
        }
        if (model.contains("google_sdk") || model.contains("emulator") || model.contains("android sdk built for")) {
            reasons.put("model:" + model);
        }
        if (manufacturer.contains("genymotion")) {
            reasons.put("manufacturer:" + manufacturer);
        }
        if (hardware.equals("goldfish") || hardware.equals("ranchu") || hardware.contains("vbox86")
                || hardware.contains("nox") || hardware.contains("ttvm")) {
            reasons.put("hardware:" + hardware);
        }
        if (product.contains("sdk") || product.contains("vbox86p") || product.contains("emulator")
                || product.contains("simulator") || product.contains("nox")) {
            reasons.put("product:" + product);
        }
        if (board.contains("nox")) {
            reasons.put("board:" + board);
        }
        if (brand.startsWith("generic") && device.startsWith("generic")) {
            reasons.put("brand/device:generic");
        }

        // 2. System property
        if ("1".equals(getProp("ro.kernel.qemu"))) {
            reasons.put("prop:ro.kernel.qemu=1");
        }
        if ("1".equals(getProp("ro.boot.qemu"))) {
            reasons.put("prop:ro.boot.qemu=1");
        }

        // 3. File khas emulator
        for (String path : EMULATOR_FILES) {
            try {
                if (new File(path).exists()) {
                    reasons.put("file:" + path);
                }
            } catch (Exception ignored) {
                // Akses ditolak = anggap tidak ada
            }
        }

        JSONObject info = new JSONObject();
        info.put("fingerprint", fingerprint);
        info.put("model", model);
        info.put("manufacturer", manufacturer);
        info.put("brand", brand);
        info.put("device", device);
        info.put("product", product);
        info.put("hardware", hardware);
        info.put("board", board);
        info.put("abi", abi);

        JSONObject result = new JSONObject();
        result.put("isEmulator", reasons.length() > 0);
        result.put("reasons", reasons);
        result.put("info", info);
        return result;
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.US);
    }

    private static String getProp(String key) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            Object v = get.invoke(null, key);
            return v == null ? "" : v.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
