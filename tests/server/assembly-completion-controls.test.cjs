"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const source = fs.readFileSync(
  path.join(__dirname, "..", "..", "assets", "js", "assembly-3d.js"),
  "utf8"
);

function loadCompletionControls(updateStatus) {
  // Execute the application functions, with only DOM and API dependencies replaced.
  const names = [
    "setPartInputsDisabled", "setCompleteStatus", "showCompletionResult", "completeAssembly"
  ];
  const functions = names.map((name) => {
    const match = source.match(new RegExp(`^  (?:async )?function ${name}\\([^]*?^  }`, "m"));
    assert.ok(match, `Application function ${name} must exist`);
    return match[0];
  }).join("\n");
  const inputs = [{ disabled: false }, { disabled: false }];
  let celebrations = 0;
  const context = {
    activeSession: { id: "42", status: "IN_PROGRESS" },
    sessionApi: { updateStatus },
    partsContainer: { querySelectorAll: () => inputs },
    resetButton: { disabled: false },
    completeButton: { disabled: false, hidden: false },
    completeStatus: { textContent: "", dataset: {} },
    completeResult: { hidden: true },
    viewReceiptLink: {},
    retryLink: {},
    model: { id: "line-follower" },
    window: { RobotConfetti: { celebrate: () => { celebrations++; } } },
    syncDriveAvailability: () => {},
    updateCompleteAvailability: () => { context.completeButton.disabled = false; }
  };
  vm.createContext(context);
  vm.runInContext(functions, context);
  return { context, inputs, celebrations: () => celebrations };
}

test("successful completion immediately locks reset and part inputs while retaining result actions", async () => {
  const calls = [];
  const fixture = loadCompletionControls(async (id, status) => {
    calls.push([id, status]);
    return { id, status };
  });
  const { context, inputs } = fixture;

  await context.completeAssembly();

  assert.deepEqual(calls, [["42", "COMPLETED"]]);
  assert.equal(context.resetButton.disabled, true);
  assert.ok(inputs.every((input) => input.disabled));
  assert.equal(context.completeButton.hidden, true);
  assert.equal(context.completeResult.hidden, false);
  assert.equal(context.viewReceiptLink.href, "../assembly-receipt?session=42");
  assert.equal(context.retryLink.href, "lap-rap.html?model=line-follower");
  assert.equal(fixture.celebrations(), 1);
});

test("showing an existing completed result locks editing without repeating celebration", () => {
  const fixture = loadCompletionControls(async () => { throw new Error("Unexpected mutation"); });
  const { context, inputs } = fixture;
  context.activeSession.status = "COMPLETED";

  context.showCompletionResult(false);

  assert.equal(context.resetButton.disabled, true);
  assert.ok(inputs.every((input) => input.disabled));
  assert.equal(context.completeResult.hidden, false);
  assert.equal(fixture.celebrations(), 0);
});

test("a rejected completion keeps the in-progress editing controls available", async () => {
  const fixture = loadCompletionControls(async () => { throw new Error("Not yet complete"); });
  const { context, inputs } = fixture;

  await context.completeAssembly();

  assert.equal(context.activeSession.status, "IN_PROGRESS");
  assert.equal(context.resetButton.disabled, false);
  assert.ok(inputs.every((input) => !input.disabled));
  assert.equal(context.completeButton.hidden, false);
  assert.equal(context.completeResult.hidden, true);
  assert.match(context.completeStatus.textContent, /Not yet complete/);
  assert.equal(fixture.celebrations(), 0);
});
