"use strict";

const { SESSION_COOKIE } = require("../middleware/auth");

function createAuthController(options) {
  const { authService, isProduction } = options;
  const cookieBase = {
    httpOnly: true,
    sameSite: "lax",
    secure: isProduction,
    path: "/api"
  };

  function setSessionCookie(response, session) {
    response.cookie(SESSION_COOKIE, session.token, { ...cookieBase, expires: session.expiresAt });
  }

  return {
    async register(request, response, next) {
      try {
        const result = await authService.register(request.body);
        setSessionCookie(response, result.session);
        response.status(201).json({ data: { user: result.user } });
      } catch (error) {
        next(error);
      }
    },

    async login(request, response, next) {
      try {
        const result = await authService.login(request.body);
        setSessionCookie(response, result.session);
        response.status(200).json({ data: { user: result.user } });
      } catch (error) {
        next(error);
      }
    },

    me(request, response) {
      response.status(200).json({
        data: { user: request.user },
        csrfToken: request.auth.csrfToken
      });
    },

    async logout(request, response, next) {
      try {
        await authService.logout(request.auth?.token);
        response.cookie(SESSION_COOKIE, "", { ...cookieBase, maxAge: 0 });
        response.status(204).end();
      } catch (error) {
        next(error);
      }
    }
  };
}

module.exports = { createAuthController };
