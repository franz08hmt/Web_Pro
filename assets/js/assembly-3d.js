"use strict";

// ================================
// 1. LẤY MODEL TỪ URL
// ================================

const params = new URLSearchParams(window.location.search);

const modelId = params.get("model") || "line-follower";

const model =
    window.ROBOT_MODELS.find(item => item.id === modelId) ||
    window.ROBOT_MODELS[0];


// ================================
// 2. LẤY KHU VỰC HIỂN THỊ 3D
// ================================

const container =
    document.querySelector("#assembly-3d-canvas");


// Nếu không tìm thấy vùng 3D thì dừng
if (!container) {
    throw new Error("Không tìm thấy #assembly-3d-canvas");
}


// ================================
// 3. TẠO SCENE
// ================================

const scene = new THREE.Scene();


// ================================
// 4. TẠO CAMERA
// ================================

const camera = new THREE.PerspectiveCamera(
    45,
    container.clientWidth / container.clientHeight,
    0.1,
    100
);

camera.position.set(4, 3, 5);


// ================================
// 5. TẠO RENDERER
// ================================

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

container.appendChild(renderer.domElement);


// ================================
// 6. ÁNH SÁNG
// ================================

const ambientLight = new THREE.AmbientLight(
    0xffffff,
    1.5
);

scene.add(ambientLight);


const directionalLight = new THREE.DirectionalLight(
    0xffffff,
    2
);

directionalLight.position.set(5, 5, 5);

scene.add(directionalLight);

// ================================
// TẠO ĐỘNG CƠ DC
// ================================

function createMotor() {
    const group = new THREE.Group();

    // Thân động cơ
    const bodyGeometry = new THREE.CylinderGeometry(
        0.3,
        0.3,
        0.8,
        32
    );

    const bodyMaterial = new THREE.MeshStandardMaterial({
        color: 0x555555
    });

    const body = new THREE.Mesh(
        bodyGeometry,
        bodyMaterial
    );

    // Cylinder mặc định nằm theo trục Y
    // Xoay để động cơ nằm ngang
    body.rotation.z = Math.PI / 2;

    group.add(body);

    return group;
}

// ================================
// TẠO 2 ĐỘNG CƠ
// ================================

const motorLeft = createMotor();
const motorRight = createMotor();

motorLeft.position.set(
    -1.3,
    0.3,
    0
);

motorRight.position.set(
    1.3,
    0.3,
    0
);

scene.add(motorLeft);
scene.add(motorRight);


// ================================
// TẠO BÁNH XE
// ================================

function createWheel() {

    const geometry = new THREE.CylinderGeometry(
        0.55,
        0.55,
        0.25,
        32
    );

    const material = new THREE.MeshStandardMaterial({
        color: 0x111111
    });

    const wheel = new THREE.Mesh(
        geometry,
        material
    );

    // Xoay bánh để trục bánh nằm theo hướng Z
    wheel.rotation.x = Math.PI / 2;

    return wheel;
}


// ================================
// TẠO 2 BÁNH XE
// ================================

const wheelLeft = createWheel();
const wheelRight = createWheel();

wheelLeft.position.set(
    -1.6,
    0.3,
    0
);

wheelRight.position.set(
    1.6,
    0.3,
    0
);

scene.add(wheelLeft);
scene.add(wheelRight);


// ================================
// 7. TẠO KHUNG XE 3D
// ================================

const chassisGeometry = new THREE.BoxGeometry(
    3.2,    // chiều dài X
    0.25,   // chiều cao Y
    2.0     // chiều rộng Z
);

const chassisMaterial = new THREE.MeshStandardMaterial({
    color: 0x333333
});

const chassis = new THREE.Mesh(
    chassisGeometry,
    chassisMaterial
);

chassis.position.set(
    0,
    0.3,
    0
);

scene.add(chassis);


const grid = new THREE.GridHelper(
    10,
    10
);

scene.add(grid);


// ================================
// 8. HIỂN THỊ TÊN ROBOT
// ================================

const modelName =
    document.querySelector("#model-name");

if (modelName) {
    modelName.textContent = model.name;
}


// ================================
// 9. VÒNG LẶP RENDER
// ================================

function animate() {

    requestAnimationFrame(animate);

    renderer.render(
        scene,
        camera
    );
}

animate();


// ================================
// 10. RESPONSIVE
// ================================

window.addEventListener("resize", () => {

    const width = container.clientWidth;
    const height = container.clientHeight;

    camera.aspect = width / height;

    camera.updateProjectionMatrix();

    renderer.setSize(
        width,
        height
    );
});