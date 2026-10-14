package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DesignLayoutTest {
    static DesignLayout fixture() {
        DesignLayout layout = new DesignLayout();
        String[] components = {"chassis-2wd", "dc-motor", "wheel", "hc-sr04", "arduino-uno", "arm-frame"};
        String[] roles = {"BASE", "DRIVE_MOTOR", "WHEEL", "DISTANCE_SENSOR", "CONTROLLER"};
        for (int n = 0; n < components.length; n++) {
            DesignInstance item = new DesignInstance();
            item.setId(n + 1);
            item.setComponentId(components[n]);
            item.setName("Vật thể " + n);
            layout.getInstances().add(item);
            if (n < roles.length) {
                DesignComponentRole role = new DesignComponentRole();
                role.setComponentId(components[n]);
                role.setRole(roles[n]);
                layout.getRules().getRoles().add(role);
            }
        }
        mount(layout, "chassis-2wd", "motor-left", "MOTOR_MOUNT_L", -1.35, -.08, .1);
        mount(layout, "chassis-2wd", "front-top", "FRONT_TOP", 0, .6, -.92);
        mount(layout, "chassis-2wd", "deck-left", "BOARD_DECK", -.58, .36, .22);
        mount(layout, "dc-motor", "axle", "AXLE", 0, .09, -.37);
        fit(layout, "chassis-2wd", "ROOT", "Y0");
        fit(layout, "dc-motor", "MOTOR_MOUNT_L", "Y90");
        fit(layout, "wheel", "AXLE", "Y0");
        fit(layout, "wheel", "AXLE", "Y180");
        fit(layout, "hc-sr04", "FRONT_TOP", "Y180");
        fit(layout, "arduino-uno", "BOARD_DECK", "Y0");
        return layout;
    }

    static void mount(DesignLayout layout, String component, String code, String type, double x, double y, double z) {
        DesignMount mount = new DesignMount();
        mount.setComponentId(component);
        mount.setMountCode(code);
        mount.setMountType(type);
        mount.setLabel("Điểm " + code);
        mount.setX(x);
        mount.setY(y);
        mount.setZ(z);
        layout.getRules().getMounts().add(mount);
    }

    static void fit(DesignLayout layout, String component, String type, String orientation) {
        DesignFit fit = new DesignFit();
        fit.setComponentId(component);
        fit.setMountType(type);
        fit.setOrientationCode(orientation);
        layout.getRules().getFits().add(fit);
    }

    static void placed(DesignLayout layout, long item, long host, String mount, String orientation) {
        DesignPlacement placed = new DesignPlacement();
        placed.setInstanceId(item);
        placed.setHostInstanceId(host);
        placed.setMountCode(mount);
        placed.setOrientationCode(orientation);
        layout.getPlacements().add(placed);
    }

    @Test
    void validationKeepsOwnerFirstThenStateVersionTypeOrientationAndParent() {
        DesignLayout layout = fixture();
        assertEquals("NOT_FOUND", layout.validateAttach(999, "0:base", "Y0", 1));
        assertEquals("LAYOUT_CONFLICT", layout.validateAttach(1, "0:base", "Y0", 2));
        assertEquals("NOT_SUPPORTED", layout.validateAttach(6, "0:base", "Y0", 1));
        assertEquals("MOUNT_INCOMPATIBLE", layout.validateAttach(3, "1:deck-left", "Y0", 1));
        assertEquals("ORIENTATION_NOT_ALLOWED", layout.validateAttach(2, "1:motor-left", "Y0", 1));
        assertEquals("HOST_MISSING", layout.validateAttach(2, "1:motor-left", "Y90", 1));
        placed(layout, 1, 0, "base", "Y0");
        assertEquals("UNCHANGED", layout.validateAttach(1, "0:base", "Y0", 99));
        placed(layout, 2, 1, "motor-left", "Y90");
        assertEquals("ALREADY_PLACED", layout.validateAttach(2, "1:deck-left", "Y90", 1));
        assertEquals("HAS_DEPENDENTS", layout.validateDetach(1, 1));
        layout.getDesign().setStatus("LOCKED");
        assertEquals("LOCKED", layout.validateDetach(2, 1));
    }

    @Test
    void childOffsetRotatesByHostOrientationAndUndoUsesTheSameRules() {
        DesignLayout layout = fixture();
        placed(layout, 1, 0, "base", "Y0");
        placed(layout, 2, 1, "motor-left", "Y90");
        placed(layout, 3, 2, "axle", "Y0");
        DesignSlot pose = layout.poseOf(3);
        assertEquals(-1.72, pose.getX(), .00001);
        assertEquals(.43, pose.getY(), .00001);
        assertEquals(.1, pose.getZ(), .00001);
        DesignEvent event = new DesignEvent();
        event.setAction("ATTACH");
        event.setOutcome("ACCEPTED");
        event.setInstanceId(2);
        assertEquals("HAS_DEPENDENTS", layout.validateUndo(event, 1));
        assertEquals("NOTHING_TO_UNDO", layout.validateUndo(null, 1));
    }
}
