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
    const escaped = vm.runInContext('escapeHtml("<script>alert(1)</script> & " + String.fromCharCode(34) + "quoted" + String.fromCharCode(34) + " " + String.fromCharCode(39) + "single" + String.fromCharCode(39))', sandbox);
    assert.equal(escaped, "&lt;script&gt;alert(1)&lt;/script&gt; &amp; &quot;quoted&quot; &#39;single&#39;");
    assert.equal(
        vm.runInContext('escapeHtml("/uploads/" + String.fromCharCode(34) + " onerror=alert(1)")', sandbox),
        "/uploads/&quot; onerror=alert(1)"
    );
});

test("safeImageUrl accepts backend paths and raster previews only", () => {
    assert.equal(
        vm.runInContext('safeImageUrl("/uploads/item.png", "fallback")', sandbox),
        "http://localhost:8081/uploads/item.png"
    );
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


test("isSupportedImageFile accepts only supported raster extensions and matching MIME types", () => {
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.JPG", type: "image/jpeg" })', sandbox), true);
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.png", type: "image/png" })', sandbox), true);
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.webp", type: "" })', sandbox), true);
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.svg", type: "image/svg+xml" })', sandbox), false);
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.png", type: "image/svg+xml" })', sandbox), false);
    assert.equal(vm.runInContext('isSupportedImageFile({ name: "shoe.exe", type: "image/png" })', sandbox), false);
});

function createRenderingSandbox({ elements = {}, storage = {}, fetchImpl = async () => ({ ok: false, status: 500, json: async () => ({}) }) } = {}) {
    const createdRows = [];
    const document = {
        getElementById: id => Object.prototype.hasOwnProperty.call(elements, id) ? elements[id] : null,
        querySelector: () => null,
        querySelectorAll: () => [],
        addEventListener: () => {},
        createElement: tagName => {
            const element = { tagName, innerHTML: "", style: {}, appendChild() {} };
            if (tagName === "tr") createdRows.push(element);
            return element;
        }
    };
    const localStorage = {
        getItem: key => Object.prototype.hasOwnProperty.call(storage, key) ? storage[key] : null,
        setItem: () => {},
        removeItem: () => {}
    };
    const sandbox = {
        document,
        window: { addEventListener: () => {}, location: { pathname: "/test.html", href: "" } },
        localStorage,
        console,
        URL,
        fetch: fetchImpl,
        alert: () => {},
        confirm: () => true
    };

    vm.createContext(sandbox);
    vm.runInContext(fs.readFileSync(scriptPath, "utf8"), sandbox, { filename: scriptPath });
    return { sandbox, createdRows };
}

test("guest history renders hostile stored date as text and rejects an unsafe image URL", async () => {
    const tableBody = { innerHTML: "", appendChild() {} };
    const noHistoryMessage = { style: {} };
    const hostileDate = "<svg onload=alert(1)>";
    const { sandbox, createdRows } = createRenderingSandbox({
        elements: { historyTableBody: tableBody, noHistoryMessage },
        storage: {
            counterCheckLatestResult: JSON.stringify({
                prediction: "<img src=x onerror=alert(1)>",
                confidence: 45,
                date: hostileDate,
                dataUrl: "javascript:alert(1)",
                image: "https://attacker.example/image.png"
            })
        }
    });

    await sandbox.displayHistory();

    assert.equal(createdRows.length, 1);
    assert.ok(createdRows[0].innerHTML.includes("&lt;svg onload=alert(1)&gt;"));
    assert.ok(!createdRows[0].innerHTML.includes(hostileDate));
    assert.ok(createdRows[0].innerHTML.includes('src="https://via.placeholder.com/70?text=Product"'));
    assert.ok(createdRows[0].innerHTML.includes("Unknown"));
});

test("admin prediction rendering escapes hostile names and rejects unsafe IDs and image URLs", async () => {
    const createdRows = [];
    const tableBody = {
        innerHTML: "",
        appendChild: row => createdRows.push(row)
    };
    const elements = {
        totalUsers: { innerText: "" },
        totalPredictions: { innerText: "" },
        genuineProducts: { innerText: "" },
        counterfeitProducts: { innerText: "" },
        adminPredictionTableBody: tableBody
    };
    let requestCount = 0;
    const fetchImpl = async () => {
        requestCount += 1;
        if (requestCount === 1) {
            return {
                ok: true,
                status: 200,
                json: async () => ({ totalUsers: 1, totalPredictions: 1, genuineCount: 1, counterfeitCount: 0 })
            };
        }
        return {
            ok: true,
            status: 200,
            json: async () => [{
                id: "<img src=x onerror=alert(1)>",
                userName: "<img src=x onerror=alert(1)>",
                imagePath: "javascript:alert(1)",
                prediction: "genuine",
                confidence: 50,
                predictedAt: "<svg onload=alert(1)>"
            }]
        };
    };
    const { sandbox } = createRenderingSandbox({
        elements,
        storage: {
            counterCheckCurrentUser: JSON.stringify({ id: 1, role: "ADMIN" }),
            counterCheckAdmin: "true"
        },
        fetchImpl
    });

    await sandbox.initializeAdminDashboard();

    assert.equal(createdRows.length, 1);
    assert.ok(createdRows[0].innerHTML.includes("&lt;img src=x onerror=alert(1)&gt;"));
    assert.ok(!createdRows[0].innerHTML.includes("<img src=x onerror=alert(1)>"));
    assert.ok(createdRows[0].innerHTML.includes('src="https://via.placeholder.com/60?text=Product"'));
    assert.ok(createdRows[0].innerHTML.includes("Unknown date"));
    assert.ok(createdRows[0].innerHTML.includes("Delete unavailable"));
});
