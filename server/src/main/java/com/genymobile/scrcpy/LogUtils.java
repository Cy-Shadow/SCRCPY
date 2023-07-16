package com.genymobile.scrcpy;

import com.genymobile.scrcpy.wrappers.DisplayManager;
import com.genymobile.scrcpy.wrappers.ServiceManager;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.os.Looper;

import java.util.Arrays;
import java.util.List;

public final class LogUtils {

    private LogUtils() {
        // not instantiable
    }

    public static String buildVideoEncoderListMessage() {
        StringBuilder builder = new StringBuilder("List of video encoders:");
        List<CodecUtils.DeviceEncoder> videoEncoders = CodecUtils.listVideoEncoders();
        if (videoEncoders.isEmpty()) {
            builder.append("\n    (none)");
        } else {
            for (CodecUtils.DeviceEncoder encoder : videoEncoders) {
                builder.append("\n    --video-codec=").append(encoder.getCodec().getName());
                builder.append(" --video-encoder='").append(encoder.getInfo().getName()).append("'");
            }
        }
        return builder.toString();
    }

    public static String buildAudioEncoderListMessage() {
        StringBuilder builder = new StringBuilder("List of audio encoders:");
        List<CodecUtils.DeviceEncoder> audioEncoders = CodecUtils.listAudioEncoders();
        if (audioEncoders.isEmpty()) {
            builder.append("\n    (none)");
        } else {
            for (CodecUtils.DeviceEncoder encoder : audioEncoders) {
                builder.append("\n    --audio-codec=").append(encoder.getCodec().getName());
                builder.append(" --audio-encoder='").append(encoder.getInfo().getName()).append("'");
            }
        }
        return builder.toString();
    }

    public static String buildDisplayListMessage() {
        StringBuilder builder = new StringBuilder("List of displays:");
        DisplayManager displayManager = ServiceManager.getDisplayManager();
        int[] displayIds = displayManager.getDisplayIds();
        if (displayIds == null || displayIds.length == 0) {
            builder.append("\n    (none)");
        } else {
            for (int id : displayIds) {
                builder.append("\n    --display=").append(id).append("    (");
                DisplayInfo displayInfo = displayManager.getDisplayInfo(id);
                if (displayInfo != null) {
                    Size size = displayInfo.getSize();
                    builder.append(size.getWidth()).append("x").append(size.getHeight());
                } else {
                    builder.append("size unknown");
                }
                builder.append(")");
            }
        }
        return builder.toString();
    }

    public static String buildCameraListMessage() {
        try {
            Looper.prepare();

            StringBuilder builder = new StringBuilder("List of cameras:");
            CameraManager cameraManager = (CameraManager) Workarounds.getSystemContext()
                    .getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                builder.append("\n    (none)");
            } else {
                for (String id : cameraIds) {
                    builder.append("\n    --video-source=camera --camera=").append(id).append("    (");
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    switch (facing) {
                        case CameraCharacteristics.LENS_FACING_BACK:
                            builder.append("Back");
                            break;
                        case CameraCharacteristics.LENS_FACING_FRONT:
                            builder.append("Front");
                            break;
                        case CameraCharacteristics.LENS_FACING_EXTERNAL:
                            builder.append("External");
                            break;
                        default:
                            builder.append("Unknown");
                            break;
                    }
                    builder.append(", ");
                    StreamConfigurationMap map = characteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    android.util.Size[] sizes = Arrays.stream(map.getOutputSizes(MediaCodec.class))
                            .sorted((a, b) -> b.getWidth() - a.getWidth()).toArray(android.util.Size[]::new);
                    if (sizes.length == 0) {
                        builder.append("size unknown");
                    } else {
                        builder.append(sizes[0].getWidth()).append("x").append(sizes[0].getHeight());
                    }
                    builder.append(")");
                }
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Can't access camera", e);
        }
    }
}
