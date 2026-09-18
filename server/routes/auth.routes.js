"use strict";

const { Router } = require("express");
const { createAuthController } = require("../controllers/auth.controller");

function createAuthRouter(options) {
  const router = Router();
  const controller = createAuthController(options);
  const { requireAuth } = options.middleware;

  router.post("/register", controller.register);
  router.post("/login", controller.login);
  router.post("/logout", controller.logout);
  router.get("/me", requireAuth, controller.me);

  return router;
}

module.exports = { createAuthRouter };
