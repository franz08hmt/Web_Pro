"use strict";

require("dotenv").config();

const { createApp } = require("./app");
const { loadConfig } = require("./config/env");
const { createLogger } = require("./config/logger");
const { createDatabasePool } = require("./database/pool");

const config = loadConfig();
const logger = createLogger();
const database = createDatabasePool(config.database);
const app = createApp({ config, database, logger });
const server = app.listen(config.port, () => {
  logger.info({ event: "server_started", port: config.port, environment: config.nodeEnv });
});

function shutdown(signal) {
  logger.info({ event: "server_stopping", signal });
  server.close(async () => {
    if (database) await database.end();
    process.exit(0);
  });
}

process.once("SIGINT", () => shutdown("SIGINT"));
process.once("SIGTERM", () => shutdown("SIGTERM"));
