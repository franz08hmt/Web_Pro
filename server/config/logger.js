"use strict";

function createLogger(sink = console) {
  function write(level, event) {
    const entry = { timestamp: new Date().toISOString(), level, ...event };
    const target = level === "error" ? sink.error : sink.log;
    target.call(sink, JSON.stringify(entry));
  }

  return {
    error(event) { write("error", event); },
    info(event) { write("info", event); }
  };
}

module.exports = { createLogger };
