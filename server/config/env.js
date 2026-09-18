"use strict";

class ConfigurationError extends Error {
  constructor(message) {
    super(message);
    this.name = "ConfigurationError";
    this.code = "CONFIGURATION_ERROR";
  }
}

const LOCAL_ORIGINS = [
  "http://127.0.0.1:4173",
  "http://localhost:4173",
  "http://127.0.0.1:63342",
  "http://localhost:63342"
];

function positiveInteger(value, fallback, name, maximum = Number.MAX_SAFE_INTEGER) {
  if (value === undefined || value === "") return fallback;
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed < 1 || parsed > maximum) {
    throw new ConfigurationError(`${name} phải là số nguyên dương hợp lệ.`);
  }
  return parsed;
}

function parseOrigins(value) {
  if (value === undefined || value.trim() === "") return LOCAL_ORIGINS;
  const origins = value.split(",").map((origin) => origin.trim()).filter(Boolean);
  if (origins.length === 0) throw new ConfigurationError("CORS_ORIGINS không được để trống.");

  return origins.map((origin) => {
    let parsed;
    try {
      parsed = new URL(origin);
    } catch {
      throw new ConfigurationError("CORS_ORIGINS phải gồm các URL hợp lệ.");
    }
    if (!/^https?:$/.test(parsed.protocol) || parsed.origin !== origin) {
      throw new ConfigurationError("CORS_ORIGINS chỉ chấp nhận origin HTTP/HTTPS.");
    }
    return origin;
  });
}

function loadConfig(environment = process.env) {
  const nodeEnv = environment.NODE_ENV || "development";
  if (!new Set(["development", "test", "production"]).has(nodeEnv)) {
    throw new ConfigurationError("NODE_ENV phải là development, test hoặc production.");
  }

  const databaseValues = [
    environment.DB_HOST,
    environment.DB_PORT,
    environment.DB_NAME,
    environment.DB_USER,
    environment.DB_PASSWORD
  ];
  const hasDatabaseValue = databaseValues.some((value) => value !== undefined && value !== "");
  const hasCompleteDatabaseConfig = databaseValues.every((value) => value !== undefined && value !== "");
  if (hasDatabaseValue && !hasCompleteDatabaseConfig) {
    throw new ConfigurationError("Cấu hình MySQL phải có đủ DB_HOST, DB_PORT, DB_NAME, DB_USER và DB_PASSWORD.");
  }

  return {
    nodeEnv,
    isProduction: nodeEnv === "production",
    port: positiveInteger(environment.PORT, 3000, "PORT", 65535),
    corsOrigins: parseOrigins(environment.CORS_ORIGINS),
    rateLimit: {
      windowMs: positiveInteger(environment.RATE_LIMIT_WINDOW_MS, 15 * 60 * 1000, "RATE_LIMIT_WINDOW_MS"),
      max: positiveInteger(environment.RATE_LIMIT_MAX, 120, "RATE_LIMIT_MAX")
    },
    auth: {
      sessionTokenTtlMs: positiveInteger(
        environment.SESSION_TOKEN_TTL_MS,
        7 * 24 * 60 * 60 * 1000,
        "SESSION_TOKEN_TTL_MS"
      )
    },
    database: {
      isConfigured: hasCompleteDatabaseConfig,
      host: environment.DB_HOST,
      port: hasCompleteDatabaseConfig ? positiveInteger(environment.DB_PORT, 3306, "DB_PORT", 65535) : 3306,
      name: environment.DB_NAME,
      user: environment.DB_USER,
      password: environment.DB_PASSWORD
    }
  };
}

module.exports = { ConfigurationError, loadConfig };
