"use strict";

(() => {
  const SUPPORTED_PARTS = [
    "arduino-uno", "arm-frame", "battery-holder", "caster-wheel",
    "chassis-2wd", "dc-motor", "hc-sr04", "l298n", "line-sensor",
    "mini-gripper", "power-5v", "sg90", "wheel"
  ];

  const COLORS = {
    accent: 0xff8748,
    blue: 0x1688b8,
    copper: 0xc77a36,
    dark: 0x171d21,
    green: 0x218c67,
    metal: 0xa9b2b7,
    red: 0xc9343f,
    rubber: 0x111416,
    white: 0xe9eef0,
    yellow: 0xf2c230
  };

  function material(THREE, color, options = {}) {
    return new THREE.MeshStandardMaterial({
      color,
      metalness: options.metalness ?? 0.15,
      roughness: options.roughness ?? 0.58
    });
  }

  function box(THREE, size, color, position = [0, 0, 0], options = {}) {
    const mesh = new THREE.Mesh(
      new THREE.BoxGeometry(size[0], size[1], size[2]),
      material(THREE, color, options)
    );
    mesh.position.set(...position);
    return mesh;
  }

  function cylinder(THREE, radius, length, color, position = [0, 0, 0], rotation = [0, 0, 0], options = {}) {
    const mesh = new THREE.Mesh(
      new THREE.CylinderGeometry(radius, radius, length, 32),
      material(THREE, color, options)
    );
    mesh.position.set(...position);
    mesh.rotation.set(...rotation);
    return mesh;
  }

  function finish(group) {
    group.traverse((object) => {
      if (!object.isMesh) return;
      object.castShadow = true;
      object.receiveShadow = true;
    });
    return group;
  }

  function createChassis(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [3.25, 0.18, 2.05], COLORS.dark, [0, 0, 0], { metalness: 0.45 }));
    group.add(box(THREE, [2.5, 0.08, 0.12], COLORS.accent, [0, 0.14, -0.76]));
    group.add(box(THREE, [2.5, 0.08, 0.12], COLORS.accent, [0, 0.14, 0.76]));
    [[-1.25, -0.7], [1.25, -0.7], [-1.25, 0.7], [1.25, 0.7]].forEach(([x, z]) => {
      group.add(cylinder(THREE, 0.07, 0.16, COLORS.metal, [x, 0.18, z], [0, 0, 0], { metalness: 0.8 }));
    });
    return finish(group);
  }

  function createMotor(THREE) {
    const group = new THREE.Group();
    group.add(cylinder(THREE, 0.28, 0.72, COLORS.metal, [0, 0, 0], [0, 0, 0], { metalness: 0.75 }));
    group.add(cylinder(THREE, 0.19, 0.16, COLORS.white, [0, 0.43, 0]));
    group.add(cylinder(THREE, 0.07, 0.28, COLORS.copper, [0, 0.63, 0], [0, 0, 0], { metalness: 0.85 }));
    group.add(box(THREE, [0.58, 0.12, 0.5], COLORS.yellow, [0, -0.36, 0]));
    return finish(group);
  }

  function createWheel(THREE) {
    const group = new THREE.Group();
    const tire = new THREE.Mesh(
      new THREE.TorusGeometry(0.48, 0.14, 14, 32),
      material(THREE, COLORS.rubber, { roughness: 0.92 })
    );
    tire.rotation.x = Math.PI / 2;
    group.add(tire);
    group.add(cylinder(THREE, 0.28, 0.18, COLORS.yellow, [0, 0, 0], [Math.PI / 2, 0, 0]));
    group.add(cylinder(THREE, 0.08, 0.22, COLORS.dark, [0, 0, 0], [Math.PI / 2, 0, 0]));
    return finish(group);
  }

  function createArduino(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [1.45, 0.1, 0.92], COLORS.blue));
    group.add(box(THREE, [0.38, 0.24, 0.34], COLORS.metal, [-0.55, 0.16, 0.18], { metalness: 0.7 }));
    group.add(box(THREE, [0.28, 0.2, 0.3], COLORS.dark, [0.52, 0.15, 0.12]));
    group.add(box(THREE, [0.42, 0.12, 0.24], COLORS.dark, [0.05, 0.12, -0.08]));
    [-0.35, 0.35].forEach((z) => group.add(box(THREE, [1.05, 0.14, 0.09], COLORS.dark, [0.08, 0.12, z])));
    for (let index = -4; index <= 4; index += 1) {
      group.add(box(THREE, [0.025, 0.13, 0.025], COLORS.metal, [index * 0.11, 0.22, -0.35], { metalness: 0.8 }));
    }
    return finish(group);
  }

  function createL298N(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [1.25, 0.1, 0.92], COLORS.red));
    for (let index = -2; index <= 2; index += 1) {
      group.add(box(THREE, [0.08, 0.42, 0.42], COLORS.dark, [index * 0.13, 0.26, 0]));
    }
    [-0.45, 0.45].forEach((x) => group.add(box(THREE, [0.28, 0.2, 0.3], COLORS.blue, [x, 0.15, -0.32])));
    [-0.42, 0.42].forEach((x) => group.add(cylinder(THREE, 0.11, 0.24, COLORS.dark, [x, 0.17, 0.27])));
    return finish(group);
  }

  function createBatteryHolder(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [1.55, 0.16, 1.05], COLORS.dark));
    [-0.55, -0.18, 0.18, 0.55].forEach((x, index) => {
      group.add(cylinder(THREE, 0.16, 0.82, index % 2 ? COLORS.copper : COLORS.green, [x, 0.22, 0], [Math.PI / 2, 0, 0]));
    });
    group.add(box(THREE, [0.08, 0.08, 0.55], COLORS.red, [0.68, 0.08, -0.72]));
    group.add(box(THREE, [0.08, 0.08, 0.55], COLORS.rubber, [0.52, 0.08, -0.72]));
    return finish(group);
  }

  function createLineSensor(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [0.72, 0.08, 0.42], COLORS.dark));
    [-0.2, 0.2].forEach((x) => {
      group.add(cylinder(THREE, 0.07, 0.13, COLORS.rubber, [x, -0.08, -0.08]));
      group.add(cylinder(THREE, 0.035, 0.14, COLORS.white, [x, 0.1, 0.1]));
    });
    group.add(box(THREE, [0.24, 0.1, 0.12], COLORS.blue, [0, 0.08, 0.1]));
    return finish(group);
  }

  function createCasterWheel(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [0.72, 0.08, 0.48], COLORS.metal, [0, 0.24, 0], { metalness: 0.75 }));
    group.add(cylinder(THREE, 0.09, 0.34, COLORS.metal, [0, 0.06, 0], [0, 0, 0], { metalness: 0.8 }));
    const ball = new THREE.Mesh(new THREE.SphereGeometry(0.22, 24, 16), material(THREE, COLORS.rubber, { roughness: 0.85 }));
    ball.position.y = -0.14;
    group.add(ball);
    return finish(group);
  }

  function createUltrasonic(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [1.12, 0.62, 0.08], COLORS.blue));
    [-0.3, 0.3].forEach((x) => {
      group.add(cylinder(THREE, 0.22, 0.22, COLORS.metal, [x, 0, -0.12], [Math.PI / 2, 0, 0], { metalness: 0.8 }));
      group.add(cylinder(THREE, 0.14, 0.23, COLORS.dark, [x, 0, -0.24], [Math.PI / 2, 0, 0]));
    });
    for (let index = 0; index < 4; index += 1) {
      group.add(box(THREE, [0.025, 0.26, 0.025], COLORS.metal, [-0.18 + index * 0.12, -0.43, 0], { metalness: 0.8 }));
    }
    return finish(group);
  }

  function createServo(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [0.62, 0.72, 0.32], COLORS.blue));
    group.add(box(THREE, [0.88, 0.1, 0.36], COLORS.blue, [0, 0.22, 0]));
    group.add(cylinder(THREE, 0.16, 0.15, COLORS.dark, [0, 0.43, 0]));
    group.add(cylinder(THREE, 0.07, 0.2, COLORS.white, [0, 0.58, 0]));
    group.add(box(THREE, [0.72, 0.06, 0.1], COLORS.white, [0, 0.7, 0]));
    return finish(group);
  }

  function createArmFrame(THREE) {
    const group = new THREE.Group();
    group.add(cylinder(THREE, 0.78, 0.16, COLORS.dark, [0, 0, 0], [0, 0, 0], { metalness: 0.35 }));
    group.add(cylinder(THREE, 0.54, 0.08, COLORS.accent, [0, 0.12, 0]));
    group.add(box(THREE, [0.22, 1.35, 0.22], COLORS.metal, [-0.28, 0.86, 0], { metalness: 0.65 }));
    group.add(box(THREE, [0.22, 1.35, 0.22], COLORS.metal, [0.28, 0.86, 0], { metalness: 0.65 }));
    const lowerArm = box(THREE, [1.35, 0.2, 0.24], COLORS.accent, [0.48, 1.48, 0]);
    lowerArm.rotation.z = -0.52;
    group.add(lowerArm);
    const upperArm = box(THREE, [1.28, 0.18, 0.22], COLORS.metal, [1.28, 2.05, 0], { metalness: 0.65 });
    upperArm.rotation.z = -0.24;
    group.add(upperArm);
    [[0, 1.45], [0.94, 2.0], [1.75, 2.32]].forEach(([x, y]) => {
      group.add(cylinder(THREE, 0.16, 0.4, COLORS.dark, [x, y, 0], [Math.PI / 2, 0, 0]));
    });
    return finish(group);
  }

  function createGripper(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [0.5, 0.28, 0.38], COLORS.dark));
    [-1, 1].forEach((direction) => {
      const finger = box(THREE, [0.68, 0.12, 0.14], COLORS.metal, [0.42, direction * 0.21, 0], { metalness: 0.72 });
      finger.rotation.z = direction * 0.18;
      group.add(finger);
      group.add(box(THREE, [0.18, 0.34, 0.14], COLORS.metal, [0.76, direction * 0.32, 0], { metalness: 0.72 }));
    });
    return finish(group);
  }

  function createPowerSupply(THREE) {
    const group = new THREE.Group();
    group.add(box(THREE, [1.28, 0.1, 0.76], COLORS.blue));
    [-0.5, 0.5].forEach((x) => group.add(box(THREE, [0.24, 0.22, 0.34], COLORS.green, [x, 0.16, 0])));
    group.add(cylinder(THREE, 0.19, 0.22, COLORS.copper, [0, 0.18, 0], [0, 0, 0], { metalness: 0.75 }));
    group.add(cylinder(THREE, 0.11, 0.25, COLORS.dark, [0.28, 0.18, 0.18]));
    return finish(group);
  }

  const factories = {
    "arduino-uno": createArduino,
    "arm-frame": createArmFrame,
    "battery-holder": createBatteryHolder,
    "caster-wheel": createCasterWheel,
    "chassis-2wd": createChassis,
    "dc-motor": createMotor,
    "hc-sr04": createUltrasonic,
    l298n: createL298N,
    "line-sensor": createLineSensor,
    "mini-gripper": createGripper,
    "power-5v": createPowerSupply,
    sg90: createServo,
    wheel: createWheel
  };

  window.ASSEMBLY_3D_SUPPORTED_PARTS = Object.freeze([...SUPPORTED_PARTS]);
  window.createAssemblyPart = (THREE, partId) => {
    const factory = factories[partId];
    return factory ? factory(THREE) : null;
  };
})();
