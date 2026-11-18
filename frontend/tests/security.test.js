const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const { test } = require("node:test");

const scriptPath = path.join(__dirname, "..", "js", "script.js");
const sandbox = {
    document: {
        getElementById: () => null,
        querySelectorAll: () => [],
        addEventListener: () => {}
    },
    window: {
        addEventListener: () => {},
        location: { pathname: "/" }
    },
    localStorage: {
        getItem: () => null,
        setItem: () => {},
        removeItem: () => {}
    },
    console,
    URL
};

vm.createContext(sandbox);
vm.runInContext(fs.readFileSync(scriptPath, "utf8"), sandbox, { filename: scriptPath });

test("escapeHtml encodes text and attribute metacharacters", () => {
    const escaped = vm.runInContext('escapeHtml("<script>alert(1)</script> & \"quoted\" \'single\'")', sandbox);
    assert.equal(escaped, "&lt;script&gt;alert(1)&lt;/script&gt; &amp; &quot;quoted&quot; &#39;single&#39;");
});

test("safeImageUrl accepts backend-relative paths and same-origin URLs", () => {
    const fallback = "https://via.placeholder.com/70?text=Product";
    assert.equal(
        vm.runInContext('safeImageUrl("http://localhost:8081/uploads/item.png", "fallback")', sandbox),
        "http://localhost:8081/uploads/item.png"
    );
    assert.equal(
        vm.runInContext('safeImageUrl("data:image/png;base64,AAAA", "fallback")', sandbox),
        "data:image/png;base64,AAAA"
    );
    assert.equal(
        vm.runInContext('safeImageUrl("data:image/svg+xml;base64,AAAA", "fallback")', sandbox),
        "fallback"
    );
    assert.equal(
        vm.runInContext('safeImageUrl("javascript:alert(1)", "fallback")', sandbox),
        "fallback"
    );
    assert.equal(
        vm.runInContext('safeImageUrl("https://attacker.example/image.png", "fallback")', sandbox),
        "fallback"
    );
    assert.equal(
        vm.runInContext('safeImageUrl("/uploads/\\" onerror=alert(1)", "fallback")', sandbox),
        "fallback"
    );
});

test("prediction labels use a neutral fallback for unexpected values", () => {
    assert.equal(vm.runInContext('normalizePrediction("genuine")', sandbox), "Genuine");
    assert.equal(vm.runInContext('normalizePrediction("COUNTERFEIT")', sandbox), "Counterfeit");
    assert.equal(vm.runInContext('normalizePrediction("<img onerror=alert(1)>")', sandbox), "Unknown");
});

test("confidence values are finite and clamped to the display range", () => {
    assert.equal(vm.runInContext('normalizeConfidence(125)', sandbox), 100);
    assert.equal(vm.runInContext('normalizeConfidence(-5)', sandbox), 0);
    assert.equal(vm.runInContext('normalizeConfidence("invalid")', sandbox), 0);
});
