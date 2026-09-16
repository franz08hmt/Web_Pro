'use strict';
(() => {
  const $ = id => document.getElementById(id);
  const definitions = {
    robots: { id: 'ID', name: 'Tên', level: ['Cơ bản', 'Trung bình', 'Nâng cao'], summary: 'Mô tả', image: 'Đường dẫn ảnh', buildTime: 'Thời gian lắp', mainSensor: 'Cảm biến chính', skills: 'Kỹ năng', wiring: 'Sơ đồ chân (JSON)' },
    components: { id: 'ID', name: 'Tên', category: 'Danh mục', image: 'Đường dẫn ảnh', description: 'Mô tả', specs: 'Thông số (JSON)' },
    steps: { id: 'ID', robotId: 'ID robot', stepOrder: 'Thứ tự', title: 'Tiêu đề', instruction: 'Hướng dẫn', illustration: 'Minh họa 2D (JSON)' },
    'library-resources': { id: 'ID', robotId: 'ID robot (để trống nếu dùng chung)', title: 'Tiêu đề', type: ['image', 'document', 'link'], url: 'Đường dẫn tài nguyên', description: 'Mô tả' }
  };
  let selected = null, page = 1, csrfToken, generation = 0, partsGeneration = 0;
  const jsonKeys = ['wiring', 'specs', 'illustration'];
  const status = (message, error = false) => { $('status').textContent = message; $('status').dataset.error = String(error); };
  async function api(path, method = 'GET', body) {
    const response = await fetch(`/api${path}`, { method, credentials: 'same-origin', headers: { 'Content-Type': 'application/json', ...(csrfToken ? { 'X-CSRF-Token': csrfToken } : {}) }, ...(body === undefined ? {} : { body: JSON.stringify(body) }) });
    if (response.status === 204) return null;
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) throw Error(payload.error?.message || `Yêu cầu thất bại (${response.status}).`);
    return payload;
  }
  function edit(row = null) {
    partsGeneration++;
    $('parts').replaceChildren();
    selected = row?.id || null;
    $('fields').replaceChildren();
    $('editor-title').textContent = selected ? `Chỉnh sửa: ${selected}` : 'Thêm nội dung';
    $('delete').hidden = !selected;
    $('relations').hidden = !selected || $('kind').value !== 'robots';
    for (const [key, label] of Object.entries(definitions[$('kind').value])) {
      const field = document.createElement(Array.isArray(label) ? 'select' : jsonKeys.includes(key) || ['summary', 'instruction', 'description'].includes(key) ? 'textarea' : 'input');
      field.id = `field-${key}`; field.name = key;
      if (Array.isArray(label)) for (const value of label) field.add(new Option(value, value));
      if (key === 'stepOrder') { field.type = 'number'; field.min = '1'; field.max = '10000'; }
      field.required = !(key === 'robotId' && $('kind').value === 'library-resources');
      field.disabled = key === 'id' && Boolean(selected);
      field.value = jsonKeys.includes(key) ? JSON.stringify(row?.[key] ?? (key === 'wiring' ? [] : {}), null, 2) : row?.[key] ?? (Array.isArray(label) ? label[0] : '');
      const caption = document.createElement('label'); caption.htmlFor = field.id; caption.textContent = Array.isArray(label) ? key === 'level' ? 'Độ khó' : 'Loại tài nguyên' : label;
      $('fields').append(caption, field);
    }
    if (!$('relations').hidden) loadParts().catch(err => status(err.message, true));
  }
  async function load(targetPage = page) {
    const version = ++generation;
    const kind = $('kind').value;
    $('items').replaceChildren();
    $('paging').textContent = 'Đang tải…';
    $('previous').disabled = true; $('next').disabled = true;
    const result = await api(`/admin/${kind}?page=${targetPage}&limit=20`);
    if (version !== generation) return;
    const lastPage = Math.max(1, Math.ceil(result.meta.total / 20));
    if (targetPage > lastPage) return load(lastPage);
    page = targetPage;
    for (const row of result.data) {
      const li = document.createElement('li'), button = document.createElement('button'); button.type = 'button'; button.textContent = row.name || row.title || row.id; button.onclick = () => edit(row); li.append(button); $('items').append(li);
    }
    $('paging').textContent = `Trang ${page} · ${result.meta.total} mục`;
    $('previous').disabled = page === 1; $('next').disabled = page * 20 >= result.meta.total;
    status(result.data.length ? 'Đã tải nội dung.' : 'Chưa có nội dung ở trang này.');
  }
  async function loadParts() {
    const robot = selected;
    const version = ++partsGeneration;
    $('parts').replaceChildren();
    let result;
    try { result = await api(`/robots/${robot}/components`); }
    catch (err) {
      if (version === partsGeneration) throw err;
      return;
    }
    if (version !== partsGeneration || selected !== robot || $('kind').value !== 'robots') return;
    for (const part of result.data) {
      const li = document.createElement('li'), button = document.createElement('button');
      li.textContent = `${part.componentId} × ${part.quantity} `; button.type = 'button'; button.textContent = 'Gỡ';
      button.onclick = () => execute(async () => { if (selected !== robot || version !== partsGeneration) return; if (confirm(`Gỡ ${part.componentId}?`)) { await api(`/admin/robots/${robot}/components/${part.componentId}`, 'DELETE'); await loadParts(); } });
      li.append(button); $('parts').append(li);
    }
  }
  async function execute(work) {
    $('workspace').disabled = true;
    try { await work(); } catch (err) { status(err.message, true); } finally { $('workspace').disabled = false; }
  }
  $('kind').onchange = () => execute(async () => { page = 1; edit(); await load(); });
  $('new').onclick = () => edit();
  $('previous').onclick = () => execute(() => load(page - 1));
  $('next').onclick = () => execute(() => load(page + 1));
  $('editor').onsubmit = event => {
    event.preventDefault(); execute(async () => {
      const body = {};
      for (const key of Object.keys(definitions[$('kind').value])) {
        if (key === 'id' && selected) continue;
        const value = $(`field-${key}`).value.trim();
        body[key] = jsonKeys.includes(key) ? JSON.parse(value) : key === 'stepOrder' ? Number(value) : key === 'robotId' && !value ? null : value;
      }
      const result = await api(`/admin/${$('kind').value}${selected ? `/${selected}` : ''}`, selected ? 'PATCH' : 'POST', body);
      edit(result.data); await load(); status('Đã lưu nội dung.');
    });
  };
  $('delete').onclick = () => execute(async () => { if (!confirm(`Xóa ${selected}? Nội dung đang được sử dụng có thể không xóa được.`)) return; await api(`/admin/${$('kind').value}/${selected}`, 'DELETE'); edit(); await load(); status('Đã xóa nội dung.'); });
  $('part-form').onsubmit = event => { event.preventDefault(); execute(async () => { await api(`/admin/robots/${selected}/components/${encodeURIComponent($('component-id').value.trim())}`, 'PUT', { quantity: Number($('quantity').value) }); await loadParts(); status('Đã lưu linh kiện bắt buộc.'); }); };
  async function start() {
    try {
      const session = await api('/auth/me');
      if (session.data?.user?.role !== 'ADMIN') throw Error('Cần đăng nhập bằng tài khoản quản trị.');
      csrfToken = session.csrfToken;
      edit(); await load(); $('workspace').disabled = false;
    } catch (err) { status(`${err.message} Trang cần backend tích hợp của Tài; xem docs/content/INTEGRATION.md.`, true); }
  }
  start();
})();
