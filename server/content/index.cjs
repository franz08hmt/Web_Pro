'use strict';
const { validate, identifier, pagination, fail } = require('./validation.cjs');
const { createRepository } = require('./repository.cjs');
// Dependencies are injected so this module does not own app.js, auth or the pool.
function createContentRouter({ Router, pool, requireAdmin, protectMutation, repository }) {
  if (typeof requireAdmin !== 'function' || typeof protectMutation !== 'function') throw new Error('Cần requireAdmin và protectMutation (CSRF) từ backend tích hợp.');
  const repo = repository || createRepository(pool);
  const router = Router();
  const run = handler => (req, res, next) => Promise.resolve().then(() => handler(req, res)).catch(next);
  router.use('/admin', requireAdmin);
  router.use('/admin', (req, res, next) => ['GET', 'HEAD', 'OPTIONS'].includes(req.method) ? next() : protectMutation(req, res, next));
  for (const kind of ['robots', 'components', 'steps', 'library-resources']) {
    const list = run(async (req, res) => {
      const options = pagination(req.query);
      if (req.query.robotId !== undefined) {
        if (!['steps', 'library-resources'].includes(kind)) throw fail('Không hỗ trợ robotId ở tài nguyên này.');
        options.robotId = identifier(req.query.robotId);
      }
      res.json(await repo.list(kind, options));
    });
    const detail = run(async (req, res) => res.json({ data: await repo.get(kind, identifier(req.params.id)) }));
    router.get(`/${kind}`, list);
    router.get(`/${kind}/:id`, detail);
    router.get(`/admin/${kind}`, list);
    router.get(`/admin/${kind}/:id`, detail);
    router.post(`/admin/${kind}`, run(async (req, res) => res.status(201).json({ data: await repo.create(kind, validate(kind, req.body)) })));
    router.patch(`/admin/${kind}/:id`, run(async (req, res) => res.json({ data: await repo.update(kind, identifier(req.params.id), validate(kind, req.body, true)) })));
    router.delete(`/admin/${kind}/:id`, run(async (req, res) => { await repo.remove(kind, identifier(req.params.id)); res.status(204).end(); }));
  }
  router.get('/robots/:id/components', run(async (req, res) => res.json({ data: await repo.parts(identifier(req.params.id)) })));
  router.get('/robots/:id/steps', run(async (req, res) => {
    const robotId = identifier(req.params.id);
    await repo.get('robots', robotId);
    res.json(await repo.list('steps', { ...pagination(req.query), robotId }));
  }));
  router.put('/admin/robots/:id/components/:componentId', run(async (req, res) => {
    if (!req.body || Object.keys(req.body).some(k => k !== 'quantity')) throw fail('Body chỉ chứa quantity.');
    const data = validate('relation', { componentId: req.params.componentId, quantity: req.body.quantity });
    res.json({ data: await repo.setPart(identifier(req.params.id), data) });
  }));
  router.delete('/admin/robots/:id/components/:componentId', run(async (req, res) => {
    await repo.removePart(identifier(req.params.id), identifier(req.params.componentId)); res.status(204).end();
  }));
  router.use((err, req, res, next) => {
    if (res.headersSent) return next(err);
    if (err.code === 'ER_DUP_ENTRY') err = fail('ID hoặc thứ tự bước đã tồn tại.', 409, 'CONFLICT');
    if (['ER_ROW_IS_REFERENCED_2', 'ER_NO_REFERENCED_ROW_2'].includes(err.code)) err = fail('Quan hệ dữ liệu không hợp lệ hoặc nội dung đang được sử dụng.', 409, 'RELATION_CONFLICT');
    if (!err.status || !err.code) return next(err);
    res.status(err.status).json({ error: { code: err.code, message: err.message, ...(req.requestId ? { requestId: req.requestId } : {}) } });
  });
  return router;
}
module.exports = { createContentRouter };
