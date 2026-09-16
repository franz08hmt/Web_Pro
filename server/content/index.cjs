'use strict';

const {
  validate,
  identifier,
  pagination,
  fail
} = require('./validation.cjs');

const { createRepository } = require('./repository.cjs');

function createContentRouter({
  Router,
  pool,
  requireAdmin,
  protectMutation,
  repository
}) {
  if (
    typeof requireAdmin !== 'function' ||
    typeof protectMutation !== 'function'
  ) {
    throw new Error(
      'Cần requireAdmin và protectMutation (CSRF) từ backend tích hợp.'
    );
  }

  const repo = repository || createRepository(pool);
  const router = Router();

  const run = handler => (req, res, next) => {
    Promise.resolve()
      .then(() => handler(req, res))
      .catch(next);
  };

  router.use('/admin', requireAdmin);

  router.use('/admin', (req, res, next) => {
    const safeMethods = ['GET', 'HEAD', 'OPTIONS'];

    if (safeMethods.includes(req.method)) {
      return next();
    }

    return protectMutation(req, res, next);
  });

  const contentTypes = [
    'robots',
    'components',
    'steps',
    'library-resources'
  ];

  for (const kind of contentTypes) {
    const list = run(async (req, res) => {
      const options = pagination(req.query);

      if (req.query.robotId !== undefined) {
        if (!['steps', 'library-resources'].includes(kind)) {
          throw fail(
            'Không hỗ trợ robotId ở tài nguyên này.'
          );
        }

        options.robotId = identifier(req.query.robotId);
      }

      const result = await repo.list(kind, options);
      res.json(result);
    });

    const detail = run(async (req, res) => {
      const id = identifier(req.params.id);
      const data = await repo.get(kind, id);

      res.json({ data });
    });

    router.get(`/${kind}`, list);
    router.get(`/${kind}/:id`, detail);

    router.get(`/admin/${kind}`, list);
    router.get(`/admin/${kind}/:id`, detail);

    router.post(
      `/admin/${kind}`,
      run(async (req, res) => {
        const input = validate(kind, req.body);
        const data = await repo.create(kind, input);

        res.status(201).json({ data });
      })
    );

    router.patch(
      `/admin/${kind}/:id`,
      run(async (req, res) => {
        const id = identifier(req.params.id);
        const input = validate(kind, req.body, true);

        const data = await repo.update(
          kind,
          id,
          input
        );

        res.json({ data });
      })
    );

    router.delete(
      `/admin/${kind}/:id`,
      run(async (req, res) => {
        const id = identifier(req.params.id);

        await repo.remove(kind, id);

        res.status(204).end();
      })
    );
  }

  router.get(
    '/robots/:id/components',
    run(async (req, res) => {
      const robotId = identifier(req.params.id);
      const data = await repo.parts(robotId);

      res.json({ data });
    })
  );

  router.get(
    '/robots/:id/steps',
    run(async (req, res) => {
      const robotId = identifier(req.params.id);

      await repo.get('robots', robotId);

      const options = {
        ...pagination(req.query),
        robotId
      };

      const result = await repo.list(
        'steps',
        options
      );

      res.json(result);
    })
  );

  router.put(
    '/admin/robots/:id/components/:componentId',
    run(async (req, res) => {
      if (
        !req.body ||
        Object.keys(req.body).some(
          key => key !== 'quantity'
        )
      ) {
        throw fail('Body chỉ chứa quantity.');
      }

      const robotId = identifier(req.params.id);

      const data = validate('relation', {
        componentId: req.params.componentId,
        quantity: req.body.quantity
      });

      const result = await repo.setPart(
        robotId,
        data
      );

      res.json({ data: result });
    })
  );

  router.delete(
    '/admin/robots/:id/components/:componentId',
    run(async (req, res) => {
      const robotId = identifier(req.params.id);
      const componentId = identifier(
        req.params.componentId
      );

      await repo.removePart(
        robotId,
        componentId
      );

      res.status(204).end();
    })
  );

  router.use((err, req, res, next) => {
    if (res.headersSent) {
      return next(err);
    }

    if (err.code === 'ER_DUP_ENTRY') {
      err = fail(
        'ID hoặc thứ tự bước đã tồn tại.',
        409,
        'CONFLICT'
      );
    }

    if (
      [
        'ER_ROW_IS_REFERENCED_2',
        'ER_NO_REFERENCED_ROW_2'
      ].includes(err.code)
    ) {
      err = fail(
        'Quan hệ dữ liệu không hợp lệ hoặc nội dung đang được sử dụng.',
        409,
        'RELATION_CONFLICT'
      );
    }

    if (!err.status || !err.code) {
      return next(err);
    }

    const errorResponse = {
      code: err.code,
      message: err.message
    };

    if (req.requestId) {
      errorResponse.requestId = req.requestId;
    }

    return res
      .status(err.status)
      .json({ error: errorResponse });
  });

  return router;
}

module.exports = { createContentRouter };