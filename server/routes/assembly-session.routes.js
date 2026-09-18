"use strict";

const { Router } = require("express");
const { createAssemblySessionController } = require("../controllers/assembly-session.controller");

function createAssemblySessionRouter({ service, middleware }) {
  const router = Router();
  const controller = createAssemblySessionController(service);

  router.use(middleware.requireAuth);
  router.post("/", middleware.protectMutation, controller.create);
  router.get("/", controller.list);
  router.get("/:sessionId", controller.get);
  router.patch("/:sessionId", middleware.protectMutation, controller.updateStatus);
  router.put("/:sessionId/components/:componentId", middleware.protectMutation, controller.setComponent);
  router.put("/:sessionId/steps/:stepId", middleware.protectMutation, controller.setStep);

  return router;
}

module.exports = { createAssemblySessionRouter };
