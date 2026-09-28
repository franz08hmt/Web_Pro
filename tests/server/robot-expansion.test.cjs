"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const root = path.join(__dirname, "..", "..");
const modelsPath = path.join(root, "assets", "js", "data.js");
const configPath = path.join(root, "assets", "js", "assembly-3d-config.js");
const partsPath = path.join(root, "assets", "js", "assembly-3d-parts.js");

const expectedParts = {
  "line-obstacle": {
    "chassis-2wd": 1,
    "arduino-uno": 1,
    "dc-motor": 2,
    wheel: 2,
    "caster-wheel": 1,
    "line-sensor": 2,
    "hc-sr04": 1,
    l298n: 1,
    "battery-holder": 1
  },
  "servo-scout": {
    "chassis-2wd": 1,
    "arduino-uno": 1,
    "dc-motor": 2,
    wheel: 2,
    "caster-wheel": 1,
    "hc-sr04": 1,
    sg90: 1,
    l298n: 1,
    "battery-holder": 1,
    "power-5v": 1
  }
};

function loadAssemblyData() {
  const context = { window: {} };
  vm.runInNewContext(fs.readFileSync(modelsPath, "utf8"), context);
  vm.runInNewContext(fs.readFileSync(configPath, "utf8"), context);
  vm.runInNewContext(fs.readFileSync(partsPath, "utf8"), context);
  return context.window;
}

function readSeed(name) {
  const file = path.join(root, "database", name);
  return fs.existsSync(file) ? fs.readFileSync(file, "utf8") : "";
}

function insertLines(sql, table) {
  return sql.split(/\r?\n/).filter(line => new RegExp(`^INSERT IGNORE INTO ${table}\\b`, "i").test(line));
}

test("the catalog contains five robots and both additions reuse existing component IDs", () => {
  const { ROBOT_MODELS, ASSEMBLY_3D_CONFIG, ASSEMBLY_3D_SUPPORTED_PARTS } = loadAssemblyData();
  assert.equal(ROBOT_MODELS.length, 5);
  assert.equal(new Set(ROBOT_MODELS.map(robot => robot.id)).size, 5, "ID mẫu robot phải duy nhất");

  for (const [robotId, parts] of Object.entries(expectedParts)) {
    const robot = ROBOT_MODELS.find(candidate => candidate.id === robotId);
    assert.ok(robot, `Thiếu ${robotId} trong window.ROBOT_MODELS`);
    assert.deepEqual(
      Object.fromEntries(robot.parts.map(part => [part.id, part.quantity])),
      parts,
      `${robotId}: component ID/số lượng phải khớp catalog`
    );
    assert.ok(robot.summary.length > 40, `${robotId} cần giải thích mục tiêu học tập`);
    assert.ok(robot.skills.length > 20, `${robotId} cần nêu kỹ năng học được`);
    assert.ok(ASSEMBLY_3D_CONFIG[robotId], `Thiếu config 3D ${robotId}`);

    for (const partId of Object.keys(parts)) {
      assert.ok(ASSEMBLY_3D_SUPPORTED_PARTS.includes(partId), `Thiếu geometry factory ${partId}`);
      const target = ASSEMBLY_3D_CONFIG[robotId][partId]?.target;
      assert.ok(target, `Thiếu target ${robotId}/${partId}`);
      assert.equal(Array.isArray(target) ? target.length : 1, parts[partId],
        `${robotId}/${partId} phải có một target cho mỗi part`);
    }
  }
});

test("each new robot has five ordered assembly steps and non-conflicting educational wiring", () => {
  const { ROBOT_MODELS, COMPONENTS_DATA } = loadAssemblyData();
  for (const robotId of Object.keys(expectedParts)) {
    const robot = ROBOT_MODELS.find(candidate => candidate.id === robotId);
    assert.ok(robot, `Thiếu ${robotId} trong window.ROBOT_MODELS`);
    assert.equal(robot.steps.length, 5, `${robotId} cần đủ năm bước lắp ráp ngoài 3D`);
    assert.ok(robot.wiring.length >= 10, `${robotId} cần sơ đồ nối dây đủ chi tiết`);
    const pins = robot.wiring.map(connection => connection.pin);
    assert.equal(new Set(pins).size, pins.length, `${robotId} không được gán trùng chân Arduino`);
    assert.ok(robot.wiring.every(connection => connection.target && connection.note),
      `${robotId} mỗi kết nối cần đích và ghi chú`);
  }

  const servoScout = ROBOT_MODELS.find(robot => robot.id === "servo-scout");
  const externalSource = servoScout.wiring.find(connection => connection.pin === "Nguồn bàn DC 9V");
  const servoRail = servoScout.wiring.find(connection => connection.pin === "5V XL4015");
  const motorSupply = servoScout.wiring.find(connection => connection.pin === "Hộp pin +");
  assert.ok(externalSource?.target.includes("Arduino — VIN/DC jack") && externalSource.target.includes("XL4015 — IN+"),
    "Servo scout phải khai báo nguồn bàn cho cả Arduino và XL4015");
  assert.ok(servoRail?.target.includes("SG90 — VCC") && !servoRail.target.includes("Arduino 5V"),
    "Không cấp đầu ra XL4015 vào chân 5V Arduino");
  assert.equal(motorSupply?.target, "L298N — Vs", "Hộp pin 4AA chỉ cấp nguồn động cơ");
  assert.match(servoScout.summary, /không nằm trong bộ linh kiện tối thiểu/,
    "Phải nói rõ nguồn bàn cần bổ sung ngoài BOM");
  assert.ok(COMPONENTS_DATA.some(component => component.id === "power-5v"),
    "Nguồn 5V phải có trong catalog dự phòng như database seed");
});

test("robot and assembly-step seed is UTF-8, additive-only, complete, and repeat-safe", () => {
  const seed = readSeed("seed-robots-phase3.sql");
  assert.ok(seed, "Thiếu database/seed-robots-phase3.sql");
  assert.match(seed, /SET NAMES utf8mb4/i);
  assert.doesNotMatch(seed, /\b(DROP|TRUNCATE|DELETE FROM|UPDATE\s+(?:robots|robot_components|assembly_steps))\b/i);

  const robotRows = insertLines(seed, "robots");
  const relationRows = insertLines(seed, "robot_components");
  const stepRows = insertLines(seed, "assembly_steps");
  assert.equal(robotRows.length, 2);

  for (const [robotId, parts] of Object.entries(expectedParts)) {
    assert.ok(robotRows.some(line => line.includes(`'${robotId}'`)), `Thiếu robot ${robotId} trong DB seed`);
    for (const [partId, quantity] of Object.entries(parts)) {
      assert.ok(relationRows.some(line => line.includes(`'${robotId}', '${partId}', ${quantity}`)),
        `DB seed lệch parts ${robotId}/${partId}`);
    }

    const steps = stepRows.filter(line => line.includes(`'${robotId}-step-`));
    assert.equal(steps.length, 5, `${robotId} cần năm dòng assembly_steps`);
    for (let order = 1; order <= 5; order += 1) {
      assert.ok(steps.some(line => line.includes(`'${robotId}-step-${order}', '${robotId}', ${order},`)),
        `Thiếu hoặc sai thứ tự bước ${robotId}-${order}`);
    }
  }
});

test("each new robot has six quiz questions with four options and exactly one correct answer", () => {
  const seed = readSeed("seed-quiz-phase3.sql");
  assert.ok(seed, "Thiếu database/seed-quiz-phase3.sql");
  assert.match(seed, /SET NAMES utf8mb4/i);
  assert.doesNotMatch(seed, /\b(DROP|TRUNCATE|DELETE FROM|UPDATE\s+users|UPDATE\s+robots)\b/i);

  const questions = insertLines(seed, "quiz_questions");
  const options = insertLines(seed, "quiz_options");
  for (const robotId of Object.keys(expectedParts)) {
    const robotQuestions = questions.filter(line => line.includes(`'${robotId}-q`) && line.includes(`'${robotId}'`));
    assert.equal(robotQuestions.length, 6, `${robotId} cần sáu câu hỏi`);

    for (let questionOrder = 1; questionOrder <= 6; questionOrder += 1) {
      const questionId = `${robotId}-q${questionOrder}`;
      assert.ok(robotQuestions.some(line => line.includes(`'${questionId}', '${robotId}'`) &&
        line.endsWith(`, ${questionOrder});`)), `Thiếu câu ${questionId} hoặc sai thứ tự`);
      const questionOptions = options.filter(line => line.includes(`'${questionId}-`) && line.includes(`'${questionId}'`));
      assert.equal(questionOptions.length, 4, `${questionId} cần bốn phương án`);
      assert.equal(questionOptions.filter(line => /, 1, [1-4]\);$/.test(line)).length, 1,
        `${questionId} phải có đúng một đáp án đúng`);
    }
  }
});

test("each new robot has three focused troubleshooting scenarios in a separate additive seed", () => {
  const seed = readSeed("seed-troubleshooting-phase3.sql");
  assert.ok(seed, "Thiếu database/seed-troubleshooting-phase3.sql");
  assert.match(seed, /SET NAMES utf8mb4/i);
  assert.doesNotMatch(seed, /\b(DROP|TRUNCATE|DELETE FROM|UPDATE\s+users|UPDATE\s+robots)\b/i);

  const guides = insertLines(seed, "troubleshooting_guides");
  assert.equal(guides.length, 6);
  for (const robotId of Object.keys(expectedParts)) {
    const robotGuides = guides.filter(line => line.includes(`'${robotId}-`) && line.includes(`'${robotId}'`));
    assert.equal(robotGuides.length, 3, `${robotId} cần ba tình huống tra cứu lỗi`);
    assert.ok(robotGuides.every(line => /'[^']{20,}'/.test(line)), "Mỗi tình huống cần mô tả và cách xử lý cụ thể");
  }
});

test("new robot image references resolve to non-empty project assets", () => {
  const { ROBOT_MODELS } = loadAssemblyData();
  for (const robotId of Object.keys(expectedParts)) {
    const robot = ROBOT_MODELS.find(candidate => candidate.id === robotId);
    assert.ok(robot, `Thiếu ${robotId} trong window.ROBOT_MODELS`);
    assert.match(robot.image, /^\.\.\/assets\/images\/robots\//);
    const imagePath = path.resolve(root, "pages", robot.image);
    assert.ok(fs.existsSync(imagePath), `Ảnh ${robotId} không tồn tại: ${imagePath}`);
    assert.ok(fs.statSync(imagePath).size > 20_000, `Ảnh ${robotId} có vẻ là placeholder rỗng`);
  }
});

test("static pages and fallback component labels agree with the five-model catalog", () => {
  const home = fs.readFileSync(path.join(root, "index.html"), "utf8");
  const catalog = fs.readFileSync(path.join(root, "pages", "mau-robot.html"), "utf8");
  const components = fs.readFileSync(path.join(root, "pages", "linh-kien.html"), "utf8");
  const mainScript = fs.readFileSync(path.join(root, "assets", "js", "main.js"), "utf8");
  const personalization = fs.readFileSync(path.join(root, "assets", "js", "personalization.js"), "utf8");
  const componentApi = fs.readFileSync(path.join(root, "assets", "js", "component-catalog-api.js"), "utf8");
  const { COMPONENTS_DATA } = loadAssemblyData();
  const staticComponentIds = [...components.matchAll(/data-component-id="([^"]+)"/g)]
    .map((match) => match[1])
    .sort();

  assert.match(home, /<dd class="stat-value">05<\/dd>/);
  assert.match(home, /<dd class="stat-value">14<\/dd>/);
  assert.match(home, /một trong năm mẫu thực hành/);
  assert.doesNotMatch(home, /một trong ba mẫu thực hành/);
  assert.match(catalog, /so sánh năm mô hình/);
  assert.match(catalog, /So sánh năm mẫu robot/);
  assert.doesNotMatch(catalog, /ba mô hình|ba mẫu robot/);
  assert.match(mainScript, /Bảng so sánh năm mẫu robot/);
  assert.match(personalization, /Khám phá 5 mô hình robot/);
  assert.match(components, /Dùng cho:<\/strong> cả năm mẫu robot\./);
  assert.match(components, /Dùng cho:<\/strong> bốn mẫu xe 2WD\./);
  assert.match(components, /Tất cả <span>14<\/span>/);
  assert.match(components, /data-chart-total>14<\/strong>/);
  assert.equal(COMPONENTS_DATA.length, 14, "Dữ liệu thông số dự phòng phải bao phủ toàn bộ thư viện DB");
  assert.deepEqual(staticComponentIds, [...COMPONENTS_DATA].map((component) => component.id).sort(),
    "Thẻ HTML dự phòng và dữ liệu thông số phải có cùng 14 component ID");
  assert.match(components, /Robot dò line kết hợp tránh vật cản/);
  assert.match(components, /Robot quét hướng tránh vật cản/);
  assert.doesNotMatch(home, /Chọn robot dò đường, tránh vật cản hoặc cánh tay robot mini\./);
  assert.match(componentApi, /usages\.set\("jumper-wire", robots\.map/,
    "Dây jumper là vật tư đấu nối dùng cho mọi robot, không phải quan hệ BOM/tiến độ riêng lẻ");
  assert.match(componentApi, /component\.id === "power-5v"[\s\S]*nguồn bàn DC 9V[\s\S]*không nối đầu ra XL4015 vào chân 5V Arduino/i,
    "Thư viện cần nhắc đúng nguồn an toàn cho servo-scout dù mô tả dùng chung trong DB");
});
