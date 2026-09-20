'use strict';

/* Cấp tài khoản quản trị nội dung cho demo.
   Đăng ký qua website luôn tạo role USER (server/services/auth.service.js),
   nên tài khoản ADMIN phải được chuẩn bị trước bằng script này.

   Nâng quyền tài khoản đã có:
     npm run create-admin -- --email=ban@example.com --promote

   Tạo tài khoản quản trị mới (mật khẩu hỏi trực tiếp, không ghi vào lịch sử shell):
     npm run create-admin -- --email=admin@example.com --name="Quản trị nội dung"
*/

require('dotenv').config();

const readline = require('node:readline');
const { loadConfig } = require('../server/config/env');
const { createDatabasePool } = require('../server/database/pool');
const { hashPassword } = require('../server/security/password');

function readArguments(argv) {
  const options = { promote: false };
  for (const item of argv) {
    if (item === '--promote') {
      options.promote = true;
      continue;
    }
    const match = /^--(email|name)=(.*)$/.exec(item);
    if (match) options[match[1]] = match[2];
  }
  return options;
}

function normalizeEmail(value) {
  const email = String(value || '').trim().toLowerCase();
  if (email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    throw new Error('Email không hợp lệ.');
  }
  return email;
}

function validateFullName(value) {
  const fullName = String(value || '').trim();
  if (fullName.length < 2 || fullName.length > 100) {
    throw new Error('Họ tên phải dài từ 2 đến 100 ký tự.');
  }
  return fullName;
}

function validatePassword(value) {
  const password = String(value || '');
  if (password.length < 8 || password.length > 72) {
    throw new Error('Mật khẩu phải dài từ 8 đến 72 ký tự.');
  }
  return password;
}

// Tắt echo để mật khẩu không hiện trên màn hình khi giảng viên đang chiếu máy.
function askHiddenPassword(question) {
  return new Promise((resolve, reject) => {
    const input = process.stdin;
    if (!input.isTTY) {
      reject(new Error(
        'Không có bàn phím tương tác. Hãy đặt biến môi trường ADMIN_PASSWORD rồi chạy lại.'
      ));
      return;
    }
    const rl = readline.createInterface({ input, output: process.stdout, terminal: true });
    const onKeypress = () => { rl.output.write('[2K[200D' + question); };
    rl.output.write(question);
    input.on('data', onKeypress);
    rl.question('', (answer) => {
      input.off('data', onKeypress);
      rl.output.write('\n');
      rl.close();
      resolve(answer);
    });
  });
}

async function main() {
  const options = readArguments(process.argv.slice(2));
  const email = normalizeEmail(options.email);
  const config = loadConfig();
  const pool = createDatabasePool(config.database);

  if (!pool) {
    throw new Error('Chưa cấu hình MySQL trong .env nên không thể cấp quyền quản trị.');
  }

  try {
    const [[existing]] = await pool.execute(
      'SELECT id, email, role FROM users WHERE email = ?',
      [email]
    );

    if (options.promote) {
      if (!existing) {
        throw new Error(`Không tìm thấy tài khoản ${email}. Hãy đăng ký trên website trước khi nâng quyền.`);
      }
      if (existing.role === 'admin') {
        console.log(`Tài khoản ${email} đã có quyền quản trị.`);
        return;
      }
      await pool.execute('UPDATE users SET role = ? WHERE id = ?', ['admin', existing.id]);
      console.log(`Đã nâng quyền quản trị cho ${email}.`);
      return;
    }

    if (existing) {
      throw new Error(`Email ${email} đã tồn tại. Dùng --promote để nâng quyền tài khoản này.`);
    }

    const fullName = validateFullName(options.name);
    const password = validatePassword(
      process.env.ADMIN_PASSWORD || await askHiddenPassword('Mật khẩu quản trị: ')
    );
    const passwordHash = await hashPassword(password);
    await pool.execute(
      'INSERT INTO users (display_name, email, password_hash, role) VALUES (?, ?, ?, ?)',
      [fullName, email, passwordHash, 'admin']
    );
    console.log(`Đã tạo tài khoản quản trị ${email}.`);
  } finally {
    await pool.end();
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exitCode = 1;
});
