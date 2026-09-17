"use strict";

/*
 * ============================================
 * ROBOT ASSEMBLY 3D
 * ============================================
 *
 * Luồng:
 * URL ?model=line-follower
 *        ↓
 * ROBOT_MODELS
 *        ↓
 * model.parts
 *        ↓
 * ASSEMBLY_3D_CONFIG
 *        ↓
 * Three.js
 */

const params = new URLSearchParams(window.location.search);

const modelId = params.get("model") || "line-follower";

const robotModels = window.ROBOT_MODELS || [];
const assemblyConfig = window.ASSEMBLY_3D_CONFIG || {};

const model =
    robotModels.find(item => item.id === modelId) ||
    robotModels[0];

const container = document.querySelector("#assembly-3d-canvas");

if (!container) {
    throw new Error("Không tìm thấy #assembly-3d-canvas");
}

if (!model) {
    throw new Error("Không tìm thấy dữ liệu robot.");
}


/* ============================================
 * 1. DOM
 * ============================================ */

const modelName = document.querySelector("#model-name");
const partsContainer = document.querySelector("#assembly-3d-parts");
const statusElement = document.querySelector("#assembly-3d-status");
const progressElement = document.querySelector("#assembly-3d-progress");
const progressValue = document.querySelector("#assembly-3d-progress-value");
const resetButton = document.querySelector("#assembly-3d-reset");


/* ============================================
 * 2. HIỂN THỊ TÊN ROBOT
 * ============================================ */

if (modelName) {
    modelName.textContent = model.name;
}


/* ============================================
 * 3. THREE.JS SCENE
 * ============================================ */

const scene = new THREE.Scene();

scene.background = new THREE.Color(0x11151a);


/* ============================================
 * 4. CAMERA
 * ============================================ */

const camera = new THREE.PerspectiveCamera(
    45,
    container.clientWidth / container.clientHeight,
    0.1,
    100
);

camera.position.set(5, 4, 6);


/* ============================================
 * 5. RENDERER
 * ============================================ */

const renderer = new THREE.WebGLRenderer({
    antialias: true
});

renderer.setSize(
    container.clientWidth,
    container.clientHeight
);

renderer.setPixelRatio(
    Math.min(window.devicePixelRatio, 2)
);

renderer.shadowMap.enabled = true;

container.appendChild(renderer.domElement);


/* ============================================
 * 6. ÁNH SÁNG
 * ============================================ */

const ambientLight = new THREE.AmbientLight(
    0xffffff,
    1.8
);

scene.add(ambientLight);


const directionalLight = new THREE.DirectionalLight(
    0xffffff,
    2.5
);

directionalLight.position.set(5, 8, 5);

directionalLight.castShadow = true;

scene.add(directionalLight);


/* ============================================
 * 7. GRID
 * ============================================ */

const grid = new THREE.GridHelper(
    10,
    20,
    0x444444,
    0x222222
);

grid.position.y = 0;

scene.add(grid);


/* ============================================
 * 8. GROUP CHỨA ROBOT
 * ============================================ */

const robotGroup = new THREE.Group();

scene.add(robotGroup);


/* ============================================
 * 9. LƯU LINH KIỆN ĐÃ LẮP
 * ============================================ */

const assembledParts = new Map();


/* ============================================
 * 10. HELPER LẤY VỊ TRÍ CONFIG
 * ============================================ */

function getTargetPosition(partId, index = 0) {

    const modelConfig =
        assemblyConfig[model.id] || {};

    const config =
        modelConfig[partId];

    if (!config || !config.target) {
        return {
            x: 0,
            y: 0,
            z: 0
        };
    }

    const target = Array.isArray(config.target)
        ? config.target[index] || config.target[0]
        : config.target;

    return {
        x: target.x || 0,
        y: target.y || 0,
        z: target.z || 0
    };
}


/* ============================================
 * 11. TẠO KHUNG XE
 * ============================================ */

function createChassis() {

    const group = new THREE.Group();

    const geometry = new THREE.BoxGeometry(
        3.2,
        0.25,
        2
    );

    const material =
        new THREE.MeshStandardMaterial({
            color: 0x333333,
            metalness: 0.2,
            roughness: 0.7
        });

    const mesh =
        new THREE.Mesh(
            geometry,
            material
        );

    mesh.castShadow = true;
    mesh.receiveShadow = true;

    group.add(mesh);

    return group;
}


/* ============================================
 * 12. TẠO ĐỘNG CƠ DC
 * ============================================ */

function createMotor() {

    const group = new THREE.Group();

    const bodyGeometry =
        new THREE.CylinderGeometry(
            0.3,
            0.3,
            0.8,
            32
        );

    const bodyMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x777777,
            metalness: 0.7,
            roughness: 0.35
        });

    const body =
        new THREE.Mesh(
            bodyGeometry,
            bodyMaterial
        );

    body.rotation.z =
        Math.PI / 2;

    body.castShadow = true;

    group.add(body);


    // Đầu motor
    const headGeometry =
        new THREE.CylinderGeometry(
            0.22,
            0.22,
            0.18,
            32
        );

    const headMaterial =
        new THREE.MeshStandardMaterial({
            color: 0xaaaaaa,
            metalness: 0.8
        });

    const head =
        new THREE.Mesh(
            headGeometry,
            headMaterial
        );

    head.rotation.z =
        Math.PI / 2;

    head.position.x = 0.45;

    group.add(head);

    return group;
}


/* ============================================
 * 13. TẠO BÁNH XE
 * ============================================ */

function createWheel() {

    const geometry =
        new THREE.CylinderGeometry(
            0.55,
            0.55,
            0.25,
            32
        );

    const material =
        new THREE.MeshStandardMaterial({
            color: 0x111111,
            roughness: 0.8
        });

    const wheel =
        new THREE.Mesh(
            geometry,
            material
        );

    wheel.rotation.x =
        Math.PI / 2;

    wheel.castShadow = true;

    return wheel;
}


/* ============================================
 * 14. TẠO ARDUINO UNO
 * ============================================ */

function createArduino() {

    const group = new THREE.Group();

    const boardGeometry =
        new THREE.BoxGeometry(
            1.4,
            0.12,
            0.8
        );

    const boardMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x146b55,
            roughness: 0.6
        });

    const board =
        new THREE.Mesh(
            boardGeometry,
            boardMaterial
        );

    board.castShadow = true;

    group.add(board);


    // USB connector
    const usbGeometry =
        new THREE.BoxGeometry(
            0.35,
            0.18,
            0.3
        );

    const usbMaterial =
        new THREE.MeshStandardMaterial({
            color: 0xaaaaaa,
            metalness: 0.7
        });

    const usb =
        new THREE.Mesh(
            usbGeometry,
            usbMaterial
        );

    usb.position.x = 0.55;

    group.add(usb);


    // Chip
    const chipGeometry =
        new THREE.BoxGeometry(
            0.35,
            0.1,
            0.3
        );

    const chipMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x111111
        });

    const chip =
        new THREE.Mesh(
            chipGeometry,
            chipMaterial
        );

    chip.position.set(
        -0.1,
        0.1,
        0
    );

    group.add(chip);

    return group;
}


/* ============================================
 * 15. TẠO L298N
 * ============================================ */

function createL298N() {

    const group = new THREE.Group();

    const boardGeometry =
        new THREE.BoxGeometry(
            1.2,
            0.15,
            0.8
        );

    const boardMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x263d31,
            roughness: 0.7
        });

    const board =
        new THREE.Mesh(
            boardGeometry,
            boardMaterial
        );

    group.add(board);


    // heatsink
    const heatGeometry =
        new THREE.BoxGeometry(
            0.45,
            0.35,
            0.35
        );

    const heatMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x555555,
            metalness: 0.8
        });

    const heatsink =
        new THREE.Mesh(
            heatGeometry,
            heatMaterial
        );

    heatsink.position.y = 0.22;

    group.add(heatsink);

    return group;
}


/* ============================================
 * 16. TẠO BATTERY HOLDER
 * ============================================ */

function createBatteryHolder() {

    const group = new THREE.Group();

    const holderGeometry =
        new THREE.BoxGeometry(
            1.4,
            0.35,
            0.7
        );

    const holderMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x222222,
            roughness: 0.8
        });

    const holder =
        new THREE.Mesh(
            holderGeometry,
            holderMaterial
        );

    group.add(holder);


    // 4 viên pin
    for (let i = 0; i < 4; i++) {

        const batteryGeometry =
            new THREE.CylinderGeometry(
                0.12,
                0.12,
                0.55,
                20
            );

        const batteryMaterial =
            new THREE.MeshStandardMaterial({
                color: 0xaaaaaa,
                metalness: 0.6
            });

        const battery =
            new THREE.Mesh(
                batteryGeometry,
                batteryMaterial
            );

        battery.rotation.z =
            Math.PI / 2;

        battery.position.set(
            -0.45 + i * 0.3,
            0.22,
            0
        );

        group.add(battery);
    }

    return group;
}


/* ============================================
 * 17. TẠO LINE SENSOR
 * ============================================ */

function createLineSensor() {

    const group = new THREE.Group();

    const boardGeometry =
        new THREE.BoxGeometry(
            0.7,
            0.12,
            0.3
        );

    const boardMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x222222
        });

    const board =
        new THREE.Mesh(
            boardGeometry,
            boardMaterial
        );

    group.add(board);


    // Hai mắt sensor
    for (let i = 0; i < 2; i++) {

        const sensorGeometry =
            new THREE.CylinderGeometry(
                0.09,
                0.09,
                0.15,
                20
            );

        const sensorMaterial =
            new THREE.MeshStandardMaterial({
                color: 0xdddddd,
                metalness: 0.5
            });

        const sensor =
            new THREE.Mesh(
                sensorGeometry,
                sensorMaterial
            );

        sensor.rotation.x =
            Math.PI / 2;

        sensor.position.set(
            -0.2 + i * 0.4,
            -0.1,
            0
        );

        group.add(sensor);
    }

    return group;
}


/* ============================================
 * 18. TẠO CASTER WHEEL
 * ============================================ */

function createCasterWheel() {

    const group = new THREE.Group();

    const wheelGeometry =
        new THREE.SphereGeometry(
            0.25,
            24,
            24
        );

    const wheelMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x111111
        });

    const wheel =
        new THREE.Mesh(
            wheelGeometry,
            wheelMaterial
        );

    group.add(wheel);


    const supportGeometry =
        new THREE.CylinderGeometry(
            0.08,
            0.08,
            0.35,
            16
        );

    const supportMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x777777,
            metalness: 0.6
        });

    const support =
        new THREE.Mesh(
            supportGeometry,
            supportMaterial
        );

    support.position.y = 0.25;

    group.add(support);

    return group;
}


/* ============================================
 * 19. FACTORY LINH KIỆN
 * ============================================ */

function createPart(partId) {

    switch (partId) {

        case "chassis":
        case "chassis-2wd":
            return createChassis();

        case "arduino-uno":
            return createArduino();

        case "dc-motor":
            return createMotor();

        case "wheel":
            return createWheel();

        case "caster-wheel":
            return createCasterWheel();

        case "line-sensor":
            return createLineSensor();

        case "l298n":
            return createL298N();

        case "battery-holder":
            return createBatteryHolder();

        default:
            return null;
    }
}


/* ============================================
 * 20. LẮP LINH KIỆN
 * ============================================ */

function assemblePart(part, partIndex = 0) {

    const partId = part.id;

    const object = createPart(partId);

    if (!object) {
        console.warn(
            `Chưa có model 3D cho linh kiện: ${partId}`
        );

        return null;
    }


    const position =
        getTargetPosition(
            partId,
            partIndex
        );


    object.position.set(
        position.x,
        position.y,
        position.z
    );


    robotGroup.add(object);

    return object;
}


/* ============================================
 * 21. RENDER PANEL LINH KIỆN
 * ============================================ */

function renderPartsPanel() {

    if (!partsContainer) {
        return;
    }

    partsContainer.innerHTML = "";


    model.parts.forEach((part, index) => {

        const wrapper =
            document.createElement("label");

        wrapper.className =
            "assembly-3d-part-item";


        const checkbox =
            document.createElement("input");

        checkbox.type = "checkbox";

        checkbox.dataset.partId =
            part.id;

        checkbox.dataset.index =
            index;


        const text =
            document.createElement("span");

        text.textContent =
            `${part.name} × ${part.quantity}`;


        wrapper.appendChild(checkbox);
        wrapper.appendChild(text);

        partsContainer.appendChild(wrapper);


        checkbox.addEventListener(
            "change",
            () => {

                if (checkbox.checked) {

                    const object =
                        assemblePart(
                            part,
                            index
                        );

                    if (object) {

                        assembledParts.set(
                            `${part.id}-${index}`,
                            object
                        );
                    }

                } else {

                    const key =
                        `${part.id}-${index}`;

                    const object =
                        assembledParts.get(key);

                    if (object) {

                        robotGroup.remove(object);

                        assembledParts.delete(key);
                    }
                }

                updateProgress();
            }
        );
    });
}


/* ============================================
 * 22. TIẾN ĐỘ
 * ============================================ */

function updateProgress() {

    const total =
        model.parts.length;

    const completed =
        assembledParts.size;

    const percent =
        total === 0
            ? 0
            : Math.round(
                (completed / total) * 100
            );


    if (progressElement) {
        progressElement.value =
            percent;
    }


    if (progressValue) {
        progressValue.textContent =
            `${percent}%`;
    }


    if (statusElement) {

        if (percent === 100) {

            statusElement.textContent =
                "Đã lắp ráp đầy đủ robot.";

        } else if (percent === 0) {

            statusElement.textContent =
                "Hãy chọn linh kiện để bắt đầu lắp ráp.";

        } else {

            statusElement.textContent =
                `Đã lắp ${completed}/${total} nhóm linh kiện.`;
        }
    }
}


/* ============================================
 * 23. RESET
 * ============================================ */

function resetAssembly() {

    assembledParts.forEach(object => {

        robotGroup.remove(object);

    });

    assembledParts.clear();


    if (partsContainer) {

        const checkboxes =
            partsContainer.querySelectorAll(
                "input[type='checkbox']"
            );

        checkboxes.forEach(
            checkbox => {
                checkbox.checked = false;
            }
        );
    }


    updateProgress();
}


if (resetButton) {

    resetButton.addEventListener(
        "click",
        resetAssembly
    );
}


/* ============================================
 * 24. CAMERA
 * ============================================ */

function focusRobot() {

    const box =
        new THREE.Box3().setFromObject(
            robotGroup
        );

    if (box.isEmpty()) {

        camera.position.set(
            5,
            4,
            6
        );

        return;
    }


    const center =
        box.getCenter(
            new THREE.Vector3()
        );

    camera.lookAt(center);
}


/* ============================================
 * 25. KHỞI TẠO
 * ============================================ */

renderPartsPanel();

updateProgress();

focusRobot();


/* ============================================
 * 26. ANIMATION
 * ============================================ */

function animate() {

    requestAnimationFrame(
        animate
    );

    renderer.render(
        scene,
        camera
    );
}

animate();


/* ============================================
 * 27. RESPONSIVE
 * ============================================ */

window.addEventListener(
    "resize",
    () => {

        const width =
            container.clientWidth;

        const height =
            container.clientHeight;


        if (height === 0) {
            return;
        }


        camera.aspect =
            width / height;

        camera.updateProjectionMatrix();


        renderer.setSize(
            width,
            height
        );
    }
);