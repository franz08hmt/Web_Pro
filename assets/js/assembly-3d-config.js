"use strict";

/*
 * Mỗi target đại diện cho đúng một linh kiện vật lý. Các phần tử có quantity
 * lớn hơn một phải có số target tương ứng để không bị chồng lên cùng vị trí.
 */
window.ASSEMBLY_3D_CONFIG = {
  "line-follower": {
    "chassis-2wd": { target: { x: 0, y: 0.42, z: 0 } },
    "arduino-uno": { target: { x: -0.55, y: 0.78, z: 0.15 } },
    "dc-motor": {
      target: [
        { x: -1.35, y: 0.34, z: 0.1, rz: Math.PI / 2 },
        { x: 1.35, y: 0.34, z: 0.1, rz: Math.PI / 2 }
      ]
    },
    wheel: {
      target: [
        { x: -1.72, y: 0.43, z: 0.1, rz: Math.PI / 2 },
        { x: 1.72, y: 0.43, z: 0.1, rz: Math.PI / 2 }
      ]
    },
    "caster-wheel": { target: { x: 0, y: 0.18, z: 0.88 } },
    "line-sensor": {
      target: [
        { x: -0.48, y: 0.25, z: -1.18 },
        { x: 0.48, y: 0.25, z: -1.18 }
      ]
    },
    l298n: { target: { x: 0.58, y: 0.8, z: 0.22 } },
    "battery-holder": { target: { x: 0, y: 0.72, z: 0.78, ry: Math.PI / 2 } }
  },

  "obstacle-avoider": {
    "chassis-2wd": { target: { x: 0, y: 0.42, z: 0 } },
    "arduino-uno": { target: { x: -0.58, y: 0.78, z: 0.22 } },
    "dc-motor": {
      target: [
        { x: -1.35, y: 0.34, z: 0.1, rz: Math.PI / 2 },
        { x: 1.35, y: 0.34, z: 0.1, rz: Math.PI / 2 }
      ]
    },
    wheel: {
      target: [
        { x: -1.72, y: 0.43, z: 0.1, rz: Math.PI / 2 },
        { x: 1.72, y: 0.43, z: 0.1, rz: Math.PI / 2 }
      ]
    },
    "caster-wheel": { target: { x: 0, y: 0.18, z: 0.88 } },
    "hc-sr04": { target: { x: 0, y: 1.02, z: -0.92, ry: Math.PI } },
    l298n: { target: { x: 0.6, y: 0.8, z: 0.28 } },
    "battery-holder": { target: { x: 0, y: 0.72, z: 0.82, ry: Math.PI / 2 } }
  },

  "mini-arm": {
    "arm-frame": { target: { x: 0, y: 0.12, z: 0 } },
    "arduino-uno": { target: { x: -1.65, y: 0.24, z: 0.55, ry: Math.PI / 2 } },
    sg90: {
      target: [
        { x: 0, y: 0.34, z: 0 },
        { x: 0, y: 1.02, z: 0 },
        { x: 0.72, y: 1.88, z: 0, rz: -0.62 },
        { x: 1.45, y: 2.4, z: 0, rz: -0.92 }
      ]
    },
    "mini-gripper": { target: { x: 2.04, y: 2.62, z: 0, rz: -0.25 } },
    "power-5v": { target: { x: 1.45, y: 0.28, z: 0.72 } }
  }
};
