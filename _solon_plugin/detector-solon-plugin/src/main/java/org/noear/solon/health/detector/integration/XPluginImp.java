package org.noear.solon.health.detector.integration;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.health.HealthChecker;
import org.noear.solon.health.detector.Detector;
import org.noear.solon.health.detector.DetectorManager;
import org.noear.solon.health.detector.impl.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author noear
 * @since 1.5
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        String detectorNamesStr = Solon.cfg().get("solon.health.detector");
        if (Utils.isEmpty(detectorNamesStr)) {
            return;
        }

        Set<String> detectorNames = new HashSet<>(Arrays.asList(detectorNamesStr.split(",")));

        if (detectorNames.size() == 0) {
            return;
        }


        DetectorManager.add(new CpuDetector());
        DetectorManager.add(new JvmMemoryDetector());
        DetectorManager.add(new OsDetector());
        DetectorManager.add(new QpsDetector());
        DetectorManager.add(new MemoryDetector());
        DetectorManager.add(new DiskDetector());

        context.subBeansOfType(Detector.class, detector -> {
            DetectorManager.add(detector);
        });

        context.beanOnloaded((x) -> {
            onLoaded(detectorNames);
        });
    }

    private void onLoaded(Set<String> detectorNames) {
        for (String name : detectorNames) {
            if ("*".equals(name)) {
                for (Detector detector : DetectorManager.all()) {
                    startDo(detector);
                }
            } else {
                Detector detector = DetectorManager.get(name);
                startDo(detector);
            }
        }
    }

    private void startDo(Detector detector) {
        if (detector != null) {
            detector.start();
            HealthChecker.addIndicator(detector.getName(), detector);
        }
    }
}
