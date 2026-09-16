'use strict';
const fields = {
  robots: { id: 'id', name: 150, level: ['Cơ bản', 'Trung bình', 'Nâng cao'], summary: 5000, image: 'path', buildTime: 100, mainSensor: 255, skills: 2000, wiring: 'array' },
  components: { id: 'id', name: 150, category: 100, image: 'path', description: 5000, specs: 'object' },
  steps: { id: 'id', robotId: 'id', stepOrder: 'positive', title: 150, instruction: 10000, illustration: 'object' },
  'library-resources': { id: 'id', robotId: 'nullableId', title: 150, type: ['image', 'document', 'link'], url: 'path', description: 5000 },
  relation: { componentId: 'id', quantity: 'positive' }
};
function fail(message, status = 422, code = 'VALIDATION_ERROR') {
  return Object.assign(new Error(message), { status, code });
}
function identifier(value) {
  if (typeof value !== 'string' || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(value) || value.length > 64) throw fail('ID phải là slug chữ thường, tối đa 64 ký tự.');
  return value;
}
function validate(kind, body, partial = false) {
  if (!body || typeof body !== 'object' || Array.isArray(body)) throw fail('Body phải là object JSON.');
  const schema = fields[kind];
  if (!Object.keys(body).length) throw fail('Body không được rỗng.');
  for (const key of Object.keys(body)) if (!Object.hasOwn(schema, key) || (partial && key === 'id')) throw fail(`Field không được phép: ${key}`);
  const result = {};
  for (const [key, rule] of Object.entries(schema)) {
    if (!Object.hasOwn(body, key)) { if (partial) continue; throw fail(`Thiếu field: ${key}`); }
    let value = body[key];
    if (typeof value === 'string') value = value.trim();
    if (rule === 'id' || (rule === 'nullableId' && value !== null)) identifier(value);
    else if (typeof rule === 'number') { if (typeof value !== 'string' || !value || value.length > rule) throw fail(`${key}: chuỗi dài 1–${rule} ký tự.`); }
    else if (Array.isArray(rule)) { if (!rule.includes(value)) throw fail(`${key}: giá trị không hợp lệ.`); }
    else if (rule === 'positive') { if (!Number.isInteger(value) || value < 1 || value > 10000) throw fail(`${key}: số nguyên từ 1 đến 10000.`); }
    else if (rule === 'path') {
      if (typeof value !== 'string' || value.length > 2048 || !(/^(\/assets\/[a-zA-Z0-9_./-]+|https:\/\/[^\s]+)$/.test(value)) || value.includes('..')) throw fail(`${key}: cần /assets/... hoặc HTTPS URL.`);
      if (value.startsWith('https:')) { try { const url = new URL(value); if (!url.hostname || url.username || url.password) throw Error(); } catch { throw fail(`${key}: URL không hợp lệ.`); } }
    } else if (rule === 'object' || rule === 'array') {
      if (!value || typeof value !== 'object' || Array.isArray(value) !== (rule === 'array') || JSON.stringify(value).length > 20000) throw fail(`${key}: JSON không hợp lệ hoặc quá lớn.`);
    }
    result[key] = value;
  }
  return result;
}
function pagination(query) {
  const number = (value, fallback, max) => {
    if (value === undefined) return fallback;
    if (typeof value !== 'string' || !/^[1-9][0-9]*$/.test(value) || Number(value) > max) throw fail('Phân trang không hợp lệ.');
    return Number(value);
  };
  return { page: number(query.page, 1, 100000), limit: number(query.limit, 20, 100) };
}
module.exports = { fields, validate, identifier, pagination, fail };
