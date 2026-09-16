'use strict';

const { ApiError } = require('../core/api-error');

const userColumns =
  'CAST(id AS CHAR) AS id, display_name AS fullName, email, UPPER(role) AS role, created_at AS createdAt';

const sessionColumns =
  'CAST(id AS CHAR) AS id, CAST(user_id AS CHAR) AS userId, robot_id AS robotId, status, created_at AS createdAt, updated_at AS updatedAt';

const authColumns =
  'CAST(id AS CHAR) AS id, CAST(user_id AS CHAR) AS userId, token_hash AS tokenHash, expires_at AS expiresAt, revoked_at AS revokedAt';

function error(status, code, message) {
  return new ApiError(status, code, message);
}

function databaseId(value) {
  const pattern = /^[1-9][0-9]{0,19}$/;

  if (
    typeof value !== 'string' ||
    !pattern.test(value) ||
    BigInt(value) > 18446744073709551615n
  ) {
    throw error(
      422,
      'VALIDATION_ERROR',
      'ID phải là chuỗi số nguyên dương BIGINT.'
    );
  }

  return value;
}

function createBackendRepositories(pool) {
  async function transaction(work) {
    const connection = await pool.getConnection();

    try {
      await connection.beginTransaction();

      const result = await work(connection);

      await connection.commit();

      return result;
    } catch (err) {
      await connection.rollback();
      throw err;
    } finally {
      connection.release();
    }
  }

  async function first(db, sql, args) {
    const [[row]] = await db.execute(sql, args);
    return row || null;
  }

  const users = {
    findByEmail: email =>
      first(
        pool,
        `SELECT ${userColumns}, password_hash AS passwordHash
         FROM users
         WHERE email = ?`,
        [email]
      ),

    findPublicById: id =>
      first(
        pool,
        `SELECT ${userColumns}
         FROM users
         WHERE id = ?`,
        [databaseId(id)]
      ),

    async create({ fullName, email, passwordHash, role }) {
      if (!['USER', 'ADMIN'].includes(role)) {
        throw error(
          422,
          'VALIDATION_ERROR',
          'Vai trò không hợp lệ.'
        );
      }

      const normalizedEmail = email.trim().toLowerCase();

      try {
        await pool.execute(
          `INSERT INTO users (
            display_name,
            email,
            password_hash,
            role
          ) VALUES (?, ?, ?, ?)`,
          [
            fullName,
            normalizedEmail,
            passwordHash,
            role.toLowerCase()
          ]
        );
      } catch (err) {
        if (err.code === 'ER_DUP_ENTRY') {
          throw error(
            409,
            'EMAIL_ALREADY_EXISTS',
            'Email đã được sử dụng.'
          );
        }

        throw err;
      }

      return users.findByEmail(normalizedEmail);
    }
  };

  const authSessions = {
    async create({ userId, tokenHash, expiresAt }) {
      await pool.execute(
        `INSERT INTO auth_sessions (
          user_id,
          token_hash,
          expires_at
        ) VALUES (?, ?, ?)`,
        [
          databaseId(userId),
          tokenHash,
          expiresAt
        ]
      );

      return first(
        pool,
        `SELECT ${authColumns}
         FROM auth_sessions
         WHERE token_hash = ?`,
        [tokenHash]
      );
    },

    findActiveByTokenHash: hash =>
      first(
        pool,
        `SELECT ${authColumns}
         FROM auth_sessions
         WHERE token_hash = ?
           AND revoked_at IS NULL
           AND expires_at > UTC_TIMESTAMP(3)`,
        [hash]
      ),

    async revokeByTokenHash(hash) {
      await pool.execute(
        `UPDATE auth_sessions
         SET revoked_at = UTC_TIMESTAMP(3)
         WHERE token_hash = ?
           AND revoked_at IS NULL`,
        [hash]
      );
    },

    async revokeAllForUser(userId) {
      await pool.execute(
        `UPDATE auth_sessions
         SET revoked_at = UTC_TIMESTAMP(3)
         WHERE user_id = ?
           AND revoked_at IS NULL`,
        [databaseId(userId)]
      );
    }
  };

  async function hydrate(db, row) {
    if (!row) {
      return null;
    }

    const [components] = await db.execute(
      `SELECT
        component_id AS componentId,
        prepared AS isPrepared,
        updated_at AS updatedAt
       FROM session_components
       WHERE session_id = ?
       ORDER BY component_id`,
      [row.id]
    );

    const [steps] = await db.execute(
      `SELECT
        step_id AS stepId,
        completed,
        updated_at AS updatedAt
       FROM session_steps
       WHERE session_id = ?
       ORDER BY step_id`,
      [row.id]
    );

    return {
      ...row,

      components: components.map(part => ({
        ...part,
        isPrepared: Boolean(part.isPrepared)
      })),

      steps: steps.map(({ completed, ...step }) => ({
        ...step,
        status: completed ? 'COMPLETED' : 'PENDING'
      }))
    };
  }

  async function owned(db, { sessionId, userId }, lock = false) {
    return first(
      db,
      `SELECT ${sessionColumns}
       FROM assembly_sessions
       WHERE id = ?
         AND user_id = ?${lock ? ' FOR UPDATE' : ''}`,
      [
        databaseId(sessionId),
        databaseId(userId)
      ]
    );
  }

  async function change(input, work) {
    return transaction(async connection => {
      const row = await owned(connection, input, true);

      if (!row) {
        throw error(
          404,
          'NOT_FOUND',
          'Không tìm thấy phiên lắp ráp.'
        );
      }

      await work(connection, row);

      await connection.execute(
        `UPDATE assembly_sessions
         SET updated_at = CURRENT_TIMESTAMP
         WHERE id = ?`,
        [row.id]
      );

      const updatedRow = await owned(connection, input);

      return hydrate(connection, updatedRow);
    });
  }

  const assemblySessions = {
    async createOrResume({ userId, robotId }) {
      databaseId(userId);

      return transaction(async connection => {
        // Lock user before creating a session.
        const user = await first(
          connection,
          'SELECT id FROM users WHERE id = ? FOR UPDATE',
          [userId]
        );

        if (!user) {
          throw error(
            404,
            'NOT_FOUND',
            'Không tìm thấy người dùng.'
          );
        }

        const robot = await first(
          connection,
          'SELECT id FROM robots WHERE id = ?',
          [robotId]
        );

        if (!robot) {
          throw error(
            422,
            'VALIDATION_ERROR',
            'Robot không tồn tại.'
          );
        }

        let row = await first(
          connection,
          `SELECT ${sessionColumns}
           FROM assembly_sessions
           WHERE user_id = ?
             AND robot_id = ?
             AND status IN ('PREPARING', 'READY', 'IN_PROGRESS')
           ORDER BY created_at DESC, id DESC
           LIMIT 1
           FOR UPDATE`,
          [userId, robotId]
        );

        if (!row) {
          await connection.execute(
            `INSERT INTO assembly_sessions (
              user_id,
              robot_id
            ) VALUES (?, ?)`,
            [userId, robotId]
          );

          row = await first(
            connection,
            `SELECT ${sessionColumns}
             FROM assembly_sessions
             WHERE id = LAST_INSERT_ID()`,
            []
          );
        }

        return hydrate(connection, row);
      });
    },

    async listByUser({
      userId,
      page = 1,
      pageSize = 20,
      status
    }) {
      databaseId(userId);

      const invalidPagination =
        !Number.isSafeInteger(page) ||
        page < 1 ||
        page > 100000 ||
        !Number.isInteger(pageSize) ||
        pageSize < 1 ||
        pageSize > 100;

      if (invalidPagination) {
        throw error(
          422,
          'VALIDATION_ERROR',
          'Phân trang không hợp lệ.'
        );
      }

      const filter =
        status === undefined
          ? ''
          : ' AND status = ?';

      const args =
        status === undefined
          ? [userId]
          : [userId, status];

      const offset = (page - 1) * pageSize;

      return transaction(async connection => {
        const [rows] = await connection.execute(
          `SELECT ${sessionColumns}
           FROM assembly_sessions
           WHERE user_id = ?${filter}
           ORDER BY created_at DESC, id DESC
           LIMIT ${pageSize} OFFSET ${offset}`,
          args
        );

        const count = await first(
          connection,
          `SELECT COUNT(*) AS total
           FROM assembly_sessions
           WHERE user_id = ?${filter}`,
          args
        );

        const items = [];

        for (const row of rows) {
          items.push(
            await hydrate(connection, row)
          );
        }

        return {
          items,
          total: Number(count.total)
        };
      });
    },

    findOwnedById: input =>
      transaction(async connection =>
        hydrate(
          connection,
          await owned(connection, input)
        )
      ),

    async updateStatus(input) {
      const statuses = [
        'PREPARING',
        'READY',
        'IN_PROGRESS',
        'COMPLETED',
        'ABANDONED'
      ];

      if (!statuses.includes(input.status)) {
        throw error(
          422,
          'VALIDATION_ERROR',
          'Trạng thái không hợp lệ.'
        );
      }

      return change(
        input,
        connection =>
          connection.execute(
            `UPDATE assembly_sessions
             SET status = ?
             WHERE id = ?`,
            [input.status, input.sessionId]
          )
      );
    },

    async upsertComponentProgress(input) {
      if (typeof input.isPrepared !== 'boolean') {
        throw error(
          422,
          'VALIDATION_ERROR',
          'isPrepared phải là boolean.'
        );
      }

      return change(input, async (connection, row) => {
        const component = await first(
          connection,
          `SELECT component_id
           FROM robot_components
           WHERE robot_id = ?
             AND component_id = ?`,
          [row.robotId, input.componentId]
        );

        if (!component) {
          throw error(
            422,
            'VALIDATION_ERROR',
            'Linh kiện không thuộc robot của phiên.'
          );
        }

        await connection.execute(
          `INSERT INTO session_components (
            session_id,
            robot_id,
            component_id,
            prepared
          ) VALUES (?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE prepared = ?`,
          [
            row.id,
            row.robotId,
            input.componentId,
            input.isPrepared,
            input.isPrepared
          ]
        );
      });
    },

    async upsertStepProgress(input) {
      if (!['PENDING', 'COMPLETED'].includes(input.status)) {
        throw error(
          422,
          'VALIDATION_ERROR',
          'Trạng thái bước không hợp lệ.'
        );
      }

      return change(input, async (connection, row) => {
        const step = await first(
          connection,
          `SELECT id
           FROM assembly_steps
           WHERE robot_id = ?
             AND id = ?`,
          [row.robotId, input.stepId]
        );

        if (!step) {
          throw error(
            422,
            'VALIDATION_ERROR',
            'Bước không thuộc robot của phiên.'
          );
        }

        const completed =
          input.status === 'COMPLETED';

        await connection.execute(
          `INSERT INTO session_steps (
            session_id,
            robot_id,
            step_id,
            completed
          ) VALUES (?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE completed = ?`,
          [
            row.id,
            row.robotId,
            input.stepId,
            completed,
            completed
          ]
        );
      });
    }
  };

  return {
    users,
    authSessions,
    assemblySessions
  };
}

module.exports = { createBackendRepositories };