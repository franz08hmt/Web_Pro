"use strict";

const { getDatabaseStatus } = require("../database/pool");
const { sendSuccess } = require("../utils/api-response");

async function getHealth(request, response, next) {
  try {
    const database = await getDatabaseStatus(request.app.locals.database);
    return sendSuccess(response, {
      service: "robot-assembly-lab-api",
      status: "ok",
      database
    });
  } catch (error) {
    return next(error);
  }
}

module.exports = { getHealth };
