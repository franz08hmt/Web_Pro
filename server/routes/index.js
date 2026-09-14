"use strict";

const { Router } = require("express");
const { healthRouter } = require("./health.routes");

const apiRouter = Router();
apiRouter.use(healthRouter);

module.exports = { apiRouter };
