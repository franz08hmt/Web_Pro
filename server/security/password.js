"use strict";

const crypto = require("node:crypto");
const { promisify } = require("node:util");

const scrypt = promisify(crypto.scrypt);
const KEY_LENGTH = 64;
const SCRYPT_OPTIONS = { N: 16384, r: 8, p: 1, maxmem: 64 * 1024 * 1024 };

function encodeHash(salt, derivedKey) {
  return [
    "scrypt",
    SCRYPT_OPTIONS.N,
    SCRYPT_OPTIONS.r,
    SCRYPT_OPTIONS.p,
    salt.toString("hex"),
    derivedKey.toString("hex")
  ].join("$");
}

async function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const derivedKey = await scrypt(password, salt, KEY_LENGTH, SCRYPT_OPTIONS);
  return encodeHash(salt, derivedKey);
}

async function verifyPassword(password, encoded) {
  const [algorithm, rawN, rawR, rawP, rawSalt, rawHash] = String(encoded).split("$");
  const N = Number(rawN);
  const r = Number(rawR);
  const p = Number(rawP);

  if (
    algorithm !== "scrypt" ||
    N !== SCRYPT_OPTIONS.N ||
    r !== SCRYPT_OPTIONS.r ||
    p !== SCRYPT_OPTIONS.p ||
    !/^[a-f0-9]{32}$/.test(rawSalt || "") ||
    !/^[a-f0-9]{128}$/.test(rawHash || "")
  ) {
    return false;
  }

  const expected = Buffer.from(rawHash, "hex");
  const actual = await scrypt(password, Buffer.from(rawSalt, "hex"), expected.length, SCRYPT_OPTIONS);
  return crypto.timingSafeEqual(actual, expected);
}

const DUMMY_PASSWORD_HASH = encodeHash(
  Buffer.alloc(16, 0),
  crypto.scryptSync("invalid-credentials", Buffer.alloc(16, 0), KEY_LENGTH, SCRYPT_OPTIONS)
);

module.exports = { DUMMY_PASSWORD_HASH, hashPassword, verifyPassword };
