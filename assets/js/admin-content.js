'use strict';
(() => {
  const $ = id => document.getElementById(id);
  const definitions = {
    robots: { id: 'ID', name: 'Tên', level: ['Cơ bản', 'Trung bình', 'Nâng cao'], summary: 'Mô tả', image: 'Đường dẫn ảnh', buildTime: 'Thời gian lắp', mainSensor: 'Cảm biến chính', skills: 'Kỹ năng', wiring: 'Sơ đồ chân (JSON)' },
    components: { id: 'ID', name: 'Tên', category: 'Danh mục', image: 'Đường dẫn ảnh', description: 'Mô tả', specs: 'Thông số (JSON)' },
    steps: { id: 'ID', robotId: 'ID robot', stepOrder: 'Thứ tự', title: 'Tiêu đề', instruction: 'Hướng dẫn', illustration: 'Minh họa 2D (JSON)' },
    'library-resources': { id: 'ID', robotId: 'ID robot (để trống nếu dùng chung)', title: 'Tiêu đề', type: ['image', 'document', 'link'], url: 'Đường dẫn tài nguyên', description: 'Mô tả' }
  };
  // Mỗi danh mục có một trang công khai tương ứng để đối chiếu ngay sau khi lưu.
  const publicPages = {
    robots: { href: 'mau-robot.html', label: 'Kiểm chứng trên trang Mẫu robot' },
    components: { href: 'linh-kien.html', label: 'Kiểm chứng trên trang Linh kiện' },
    steps: { href: 'lap-rap.html', label: 'Kiểm chứng trên trang Lắp ráp' },
    'library-resources': { href: 'thu-vien.html', label: 'Kiểm chứng trên trang Thư viện' }
  };
  let selected = null, page = 1, csrfToken, generation = 0, partsGeneration = 0;
  const jsonKeys = ['wiring', 'specs', 'illustration'];

  const status = (message, state = '') => {
    $('status').textContent = message;
    $('status').dataset.state = state;
  };

  async function api(path, method = 'GET', body) {
    const response = await fetch(`/api${path}`, { method, credentials: 'same-origin', headers: { 'Content-Type': 'application/json', ...(csrfToken ? { 'X-CSRF-Token': csrfToken } : {}) }, ...(body === undefined ? {} : { body: JSON.stringify(body) }) });
    if (response.status === 204) return null;
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
      const error = Error(payload.error?.message || `Yêu cầu thất bại (${response.status}).`);
      error.status = response.status;
      throw error;
    }
    return payload;
  }

  function showSkeleton(rows = 5) {
    const list = document.createElement('div');
    list.className = 'skeleton-list';
    for (let index = 0; index < rows; index += 1) {
      const row = document.createElement('div');
      row.className = 'skeleton skeleton-row';
      list.append(row);
    }
    const holder = document.createElement('li');
    holder.append(list);
    $('items').replaceChildren(holder);
  }

  function markSelected() {
    for (const button of $('items').querySelectorAll('button[data-row-id]')) {
      button.setAttribute('aria-current', String(button.dataset.rowId === selected));
    }
  }

  function validateJsonField(field) {
    const error = document.getElementById(`${field.id}-error`);
    try {
      JSON.parse(field.value);
      field.setAttribute('aria-invalid', 'false');
      if (error) error.textContent = '';
      return true;
    } catch (err) {
      field.setAttribute('aria-invalid', 'true');
      if (error) error.textContent = `JSON không hợp lệ: ${err.message}`;
      return false;
    }
  }

  function edit(row = null) {
    partsGeneration++;
    $('parts').replaceChildren();
    selected = row?.id || null;
    $('fields').replaceChildren();
    $('editor-title').textContent = selected ? `Chỉnh sửa: ${selected}` : 'Thêm nội dung';
    $('delete').hidden = !selected;
    $('relations').hidden = !selected || $('kind').value !== 'robots';
    markSelected();
    for (const [key, label] of Object.entries(definitions[$('kind').value])) {
      const isJson = jsonKeys.includes(key);
      const field = document.createElement(Array.isArray(label) ? 'select' : isJson || ['summary', 'instruction', 'description'].includes(key) ? 'textarea' : 'input');
      field.id = `field-${key}`; field.name = key;
      if (Array.isArray(label)) for (const value of label) field.add(new Option(value, value));
      if (key === 'stepOrder') { field.type = 'number'; field.min = '1'; field.max = '10000'; }
      field.required = !(key === 'robotId' && $('kind').value === 'library-resources');
      field.disabled = key === 'id' && Boolean(selected);
      field.value = isJson ? JSON.stringify(row?.[key] ?? (key === 'wiring' ? [] : {}), null, 2) : row?.[key] ?? (Array.isArray(label) ? label[0] : '');
      const caption = document.createElement('label'); caption.htmlFor = field.id; caption.textContent = Array.isArray(label) ? key === 'level' ? 'Độ khó' : 'Loại tài nguyên' : label;
      $('fields').append(caption, field);
      if (!isJson) continue;
      const error = document.createElement('p');
      error.id = `${field.id}-error`;
      error.className = 'admin-field-error';
      error.setAttribute('role', 'status');
      field.setAttribute('aria-describedby', error.id);
      field.addEventListener('blur', () => validateJsonField(field));
      $('fields').append(error);
    }
    if (!$('relations').hidden) loadParts().catch(err => status(err.message, 'error'));
  }

  async function load(targetPage = page) {
    const version = ++generation;
    const kind = $('kind').value;
    showSkeleton();
    $('paging').textContent = 'Đang tải…';
    $('previous').disabled = true; $('next').disabled = true;
    status('Đang tải nội dung từ Content API…', 'loading');
    const result = await api(`/admin/${kind}?page=${targetPage}&limit=20`);
    if (version !== generation) return;
    const lastPage = Math.max(1, Math.ceil(result.meta.total / 20));
    if (targetPage > lastPage) return load(lastPage);
    page = targetPage;
    $('items').replaceChildren();
    for (const row of result.data) {
      const li = document.createElement('li'), button = document.createElement('button');
      button.type = 'button';
      button.dataset.rowId = row.id;
      button.textContent = row.name || row.title || row.id;
      button.onclick = () => edit(row);
      li.append(button); $('items').append(li);
    }
    markSelected();
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
      button.className = 'admin-danger';
      button.onclick = () => execute(async () => { if (selected !== robot || version !== partsGeneration) return; if (confirm(`Gỡ ${part.componentId}?`)) { await api(`/admin/robots/${robot}/components/${part.componentId}`, 'DELETE'); await loadParts(); status('Đã gỡ linh kiện khỏi robot.', 'saved'); } });
      li.append(button); $('parts').append(li);
    }
  }

  async function execute(work) {
    $('workspace').disabled = true;
    try { await work(); } catch (err) { status(err.message, 'error'); } finally { $('workspace').disabled = false; }
  }

  $('kind').onchange = () => execute(async () => {
    page = 1;
    const target = publicPages[$('kind').value];
    $('public-view').href = target.href;
    $('public-view').textContent = target.label;
    edit();
    await load();
  });
  $('new').onclick = () => edit();
  $('previous').onclick = () => execute(() => load(page - 1));
  $('next').onclick = () => execute(() => load(page + 1));
  $('editor').onsubmit = event => {
    event.preventDefault(); execute(async () => {
      const kind = $('kind').value;
      const invalid = jsonKeys
        .filter(key => key in definitions[kind])
        .map(key => $(`field-${key}`))
        .filter(field => field && !validateJsonField(field));
      if (invalid.length) {
        invalid[0].focus();
        throw Error('Hãy sửa các ô JSON được đánh dấu trước khi lưu.');
      }
      const body = {};
      for (const key of Object.keys(definitions[kind])) {
        if (key === 'id' && selected) continue;
        const value = $(`field-${key}`).value.trim();
        body[key] = jsonKeys.includes(key) ? JSON.parse(value) : key === 'stepOrder' ? Number(value) : key === 'robotId' && !value ? null : value;
      }
      const result = await api(`/admin/${kind}${selected ? `/${selected}` : ''}`, selected ? 'PATCH' : 'POST', body);
      edit(result.data); await load();
      status('Đã lưu nội dung. Mở trang công khai để đối chiếu.', 'saved');
    });
  };
  $('delete').onclick = () => execute(async () => { if (!confirm(`Xóa ${selected}? Nội dung đang được sử dụng có thể không xóa được.`)) return; await api(`/admin/${$('kind').value}/${selected}`, 'DELETE'); edit(); await load(); status('Đã xóa nội dung.', 'saved'); });
  $('part-form').onsubmit = event => { event.preventDefault(); execute(async () => { await api(`/admin/robots/${selected}/components/${encodeURIComponent($('component-id').value.trim())}`, 'PUT', { quantity: Number($('quantity').value) }); await loadParts(); status('Đã lưu linh kiện bắt buộc.', 'saved'); }); };
  $('admin-logout').onclick = () => execute(async () => {
    await api('/auth/logout', 'POST');
    window.location.assign('tai-khoan.html');
  });

  // Khối cổng đã mang đúng lý do và nút đăng nhập, nên dòng trạng thái được ẩn
  // để người dùng và trình đọc màn hình không nhận cùng một câu hai lần.
  function lockOut(message) {
    $('workspace').hidden = true;
    $('admin-session').hidden = true;
    $('status').hidden = true;
    $('gate-message').textContent = message;
    $('admin-gate').hidden = false;
  }

  async function start() {
    let session;
    try {
      session = await api('/auth/me');
    } catch (err) {
      lockOut(err.status === 401
        ? 'Khu vực này yêu cầu đăng nhập bằng tài khoản quản trị.'
        : `Không thể kiểm tra phiên đăng nhập. ${err.message}`);
      return;
    }
    if (session.data?.user?.role !== 'ADMIN') {
      lockOut(`Tài khoản ${session.data?.user?.email || ''} không có quyền quản trị nội dung. Hãy đăng nhập bằng tài khoản ADMIN.`.trim());
      return;
    }
    csrfToken = session.csrfToken;
    $('admin-email').textContent = session.data.user.email;
    $('admin-session').hidden = false;
    $('workspace').hidden = false;
    try {
      edit();
      await load();
      $('workspace').disabled = false;
    } catch (err) {
      status(err.message, 'error');
      $('workspace').disabled = false;
    }
  }

  start();
})();
