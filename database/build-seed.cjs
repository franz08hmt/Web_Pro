'use strict';

const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const root = path.resolve(__dirname, '..');
const dataPath = path.join(root, 'assets/js/data.js');

const context = { window: {} };

vm.runInNewContext(
  fs.readFileSync(dataPath, 'utf8'),
  context
);

const data = structuredClone(context.window);

const normalizeImage = image => image.replace(/^\.\.\//, '/');

const components = data.COMPONENTS_DATA.map(component => ({
  ...component,
  image: normalizeImage(component.image)
}));

const extraComponents = [
  {
    id: 'caster-wheel',
    name: 'Bánh tự do',
    category: 'Cơ khí',
    image: '/assets/images/components/caster-wheel.jpg',
    description: 'Bánh bi tự do đỡ đầu còn lại của khung xe và giúp robot đổi hướng linh hoạt.',
    specs: {
      'Cơ cấu': 'bi cầu xoay tự do',
      'Vai trò': 'đỡ tải và giữ cân bằng cho khung xe 2 bánh',
      'Lắp đặt': 'bắt vít vào mặt đáy khung',
      'Lưu ý': 'chọn chiều cao tương thích với bánh chủ động'
    }
  },
  {
    id: 'arm-frame',
    name: 'Bộ khung cánh tay',
    category: 'Cơ khí',
    image: '/assets/images/components/arm-frame.jpg',
    description: 'Cụm khung tay robot nhiều khớp dùng để gá cơ cấu truyền động và bộ kẹp.',
    specs: {
      'Cấu trúc': 'đế xoay và các liên kết nhiều khớp',
      'Vai trò': 'đỡ động cơ, truyền lực và định vị đầu kẹp',
      'Lắp đặt': 'siết vít tại từng khớp theo thứ tự lắp ráp',
      'Lưu ý': 'kiểm tra hành trình khớp trước khi cấp nguồn'
    }
  },
  {
    id: 'mini-gripper',
    name: 'Bộ kẹp mini',
    category: 'Cơ khí',
    image: '/assets/images/components/mini-gripper.jpg',
    description: 'Bộ kẹp hai ngón dùng ở đầu cánh tay để giữ các vật thể nhỏ.',
    specs: {
      'Cơ cấu': 'hai ngón kẹp truyền động bằng bánh răng',
      'Vật liệu tham khảo': 'nhựa in 3D',
      'Điều khiển': 'kết hợp động cơ servo phù hợp',
      'Lưu ý': 'giới hạn lực kẹp để tránh kẹt cơ cấu'
    }
  },
  {
    id: 'power-5v',
    name: 'Nguồn 5V phù hợp',
    category: 'Nguồn điện',
    image: '/assets/images/components/power-5v.jpg',
    description: 'Module hạ áp DC-DC XL4015 dùng tạo nguồn 5V ổn định cho mạch logic hoặc servo.',
    specs: {
      'Loại': 'bộ chuyển đổi hạ áp DC-DC XL4015',
      'Đầu ra': 'điều chỉnh về 5V trước khi nối tải',
      'Ứng dụng': 'cấp nguồn logic hoặc servo từ nguồn DC cao hơn',
      'Lưu ý': 'đo lại điện áp đầu ra và nối chung GND'
    }
  }
];

components.push(...extraComponents);

const componentAliases = {
  'Khung xe 2 bánh': 'chassis-2wd',
  'Arduino Uno': 'arduino-uno',
  'Động cơ DC': 'dc-motor',
  'Bánh xe': 'wheel',
  'Bánh tự do': 'caster-wheel',
  'Cảm biến dò line': 'line-sensor',
  'Module L298N': 'l298n',
  'Hộp pin': 'battery-holder',
  'Cảm biến siêu âm HC-SR04': 'hc-sr04',
  'Bộ khung cánh tay': 'arm-frame',
  'Động cơ servo': 'sg90',
  'Bộ kẹp mini': 'mini-gripper',
  'Nguồn 5V phù hợp': 'power-5v'
};

const robots = [];
const steps = [];
const relations = [];
const library = [];

for (const model of data.ROBOT_MODELS) {
  const {
    parts,
    steps: instructions,
    ...robot
  } = model;

  robot.image = normalizeImage(robot.image);
  robots.push(robot);

  parts.forEach(part => {
    const componentId = componentAliases[part.name];

    const componentExists = components.some(
      component => component.id === componentId
    );

    if (!componentExists) {
      throw new Error(`Thiếu mapping: ${part.name}`);
    }

    relations.push({
      robotId: robot.id,
      componentId,
      quantity: part.quantity
    });
  });

  instructions.forEach((instruction, index) => {
    const stepOrder = index + 1;

    steps.push({
      id: `${robot.id}-step-${stepOrder}`,
      robotId: robot.id,
      stepOrder,
      title: `Bước ${stepOrder}`,
      instruction,
      illustration: {}
    });
  });

  library.push({
    id: `${robot.id}-image`,
    robotId: robot.id,
    title: robot.name,
    type: 'image',
    url: robot.image,
    description: robot.summary
  });
}

const columnMapping = {
  buildTime: 'build_time',
  mainSensor: 'main_sensor',
  robotId: 'robot_id',
  componentId: 'component_id',
  stepOrder: 'step_order'
};

function toSqlLiteral(value) {
  if (value === null) {
    return 'NULL';
  }

  if (typeof value === 'number') {
    return String(value);
  }

  const content =
    typeof value === 'object'
      ? JSON.stringify(value)
      : value;

  const hex = Buffer
    .from(content)
    .toString('hex');

  return `CONVERT(X'${hex}' USING utf8mb4)`;
}

function buildInsert(table, row) {
  const columns = Object.keys(row)
    .map(key => `\`${columnMapping[key] || key}\``)
    .join(', ');

  const values = Object.values(row)
    .map(toSqlLiteral)
    .join(', ');

  return `INSERT INTO ${table} (${columns}) VALUES (${values});`;
}

const seedData = [
  ['components', components],
  ['robots', robots],
  ['robot_components', relations],
  ['assembly_steps', steps],
  ['library_resources', library]
];

const sqlLines = [
  '-- Generated by database/build-seed.cjs',
  'SET NAMES utf8mb4;',
  'START TRANSACTION;'
];

for (const [table, rows] of seedData) {
  for (const row of rows) {
    sqlLines.push(buildInsert(table, row));
  }
}

sqlLines.push('COMMIT;');

fs.writeFileSync(
  path.join(__dirname, 'seed.sql'),
  `${sqlLines.join('\n')}\n`
);

const migrationsPath = path.join(__dirname, 'migrations');

const migrations = fs
  .readdirSync(migrationsPath)
  .filter(name => name.endsWith('.sql'))
  .sort();

const schema = migrations
  .map(name =>
    fs
      .readFileSync(path.join(migrationsPath, name), 'utf8')
      .replaceAll('\r\n', '\n')
      .trimEnd()
  )
  .join('\n\n');

fs.writeFileSync(
  path.join(__dirname, 'schema.sql'),
  `${schema}\n`
);

const fixture = {
  robots,
  components,
  relations,
  steps,
  library
};

fs.writeFileSync(
  path.join(root, 'tests/content/seed-fixture.json'),
  `${JSON.stringify(fixture, null, 2)}\n`
);
