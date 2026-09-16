'use strict';
const { fields, fail } = require('./validation.cjs');
const tables = { robots: 'robots', components: 'components', steps: 'assembly_steps', 'library-resources': 'library_resources' };
const columns = { buildTime: 'build_time', mainSensor: 'main_sensor', robotId: 'robot_id', stepOrder: 'step_order', componentId: 'component_id' };
const jsonFields = new Set(['wiring', 'specs', 'illustration']);
const column = key => columns[key] || key;
const encode = (key, value) => jsonFields.has(key) ? JSON.stringify(value) : value;
function projection(kind) { return Object.keys(fields[kind]).map(key => `\`${column(key)}\` AS \`${key}\``).join(', '); }
function decode(row) {
  for (const key of jsonFields) if (typeof row[key] === 'string') row[key] = JSON.parse(row[key]);
  return row;
}
function createRepository(pool) {
  async function list(kind, { page = 1, limit = 20, robotId } = {}) {
    const where = robotId ? ' WHERE robot_id = ?' : '';
    const args = robotId ? [robotId] : [];
    const [rows] = await pool.execute(`SELECT ${projection(kind)} FROM ${tables[kind]}${where} ORDER BY ${kind === 'steps' ? 'step_order, id' : 'id'} LIMIT ${limit} OFFSET ${(page - 1) * limit}`, args);
    const [[count]] = await pool.execute(`SELECT COUNT(*) AS total FROM ${tables[kind]}${where}`, args);
    return { data: rows.map(decode), meta: { page, limit, total: Number(count.total) } };
  }
  async function get(kind, id) {
    const [[row]] = await pool.execute(`SELECT ${projection(kind)} FROM ${tables[kind]} WHERE id = ?`, [id]);
    if (!row) throw fail('Không tìm thấy nội dung.', 404, 'NOT_FOUND');
    return decode(row);
  }
  async function create(kind, data) {
    const keys = Object.keys(data);
    await pool.execute(`INSERT INTO ${tables[kind]} (${keys.map(k => `\`${column(k)}\``).join(',')}) VALUES (${keys.map(() => '?').join(',')})`, keys.map(k => encode(k, data[k])));
    return get(kind, data.id);
  }
  async function update(kind, id, data) {
    const keys = Object.keys(data);
    const [result] = await pool.execute(`UPDATE ${tables[kind]} SET ${keys.map(k => `\`${column(k)}\` = ?`).join(',')} WHERE id = ?`, [...keys.map(k => encode(k, data[k])), id]);
    if (!result.affectedRows) await get(kind, id);
    return get(kind, id);
  }
  async function remove(kind, id) {
    const [result] = await pool.execute(`DELETE FROM ${tables[kind]} WHERE id = ?`, [id]);
    if (!result.affectedRows) throw fail('Không tìm thấy nội dung.', 404, 'NOT_FOUND');
  }
  async function parts(id) {
    await get('robots', id);
    const [rows] = await pool.execute('SELECT component_id AS componentId, quantity FROM robot_components WHERE robot_id = ? ORDER BY component_id', [id]);
    return rows;
  }
  async function setPart(id, data) {
    await pool.execute('INSERT INTO robot_components (robot_id, component_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = ?', [id, data.componentId, data.quantity, data.quantity]);
    return data;
  }
  async function removePart(id, componentId) {
    const [result] = await pool.execute('DELETE FROM robot_components WHERE robot_id = ? AND component_id = ?', [id, componentId]);
    if (!result.affectedRows) throw fail('Không tìm thấy quan hệ.', 404, 'NOT_FOUND');
  }
  return { list, get, create, update, remove, parts, setPart, removePart };
}
module.exports = { createRepository };
