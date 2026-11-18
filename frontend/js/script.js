// ============================================================
// CounterCheck Frontend JavaScript
// Connects HTML pages to Spring Boot REST API (http://localhost:8081)
// ============================================================

const API_BASE_URL = "http://localhost:8081/api";

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, function (character) {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[character];
    });
}

function safeImageUrl(value, fallback) {
    if (typeof value !== "string" || value.length > 15000000) return fallback;

    const candidate = value.trim();
    if (!candidate) return fallback;

    // Guest previews may use FileReader-generated raster image data URLs.
    if (/^data:image\/(?:png|jpeg|webp|gif);base64,[a-z0-9+/]*={0,2}$/i.test(candidate)) {
        return candidate;
    }

    try {
        const backendOrigin = new URL(API_BASE_URL).origin;
        const url = new URL(candidate, backendOrigin);
        if ((url.protocol === "https:" || url.protocol === "http:")
            && url.origin === backendOrigin
            && !url.username
            && !url.password) {
            return url.href;
        }
    } catch (error) {
        // Invalid or unexpected image URLs fall back to a local placeholder.
    }

    return fallback;
}

function isSupportedImageFile(file) {
    if (!file || typeof file.name !== "string") return false;

    const extension = file.name.toLowerCase().match(/\.[^.]+$/)?.[0];
    const supportedMimeTypes = {
        ".jpg": "image/jpeg",
        ".jpeg": "image/jpeg",
        ".png": "image/png",
        ".webp": "image/webp"
    };
    const expectedMimeType = supportedMimeTypes[extension];
    if (!expectedMimeType) return false;

    const actualMimeType = typeof file.type === "string" ? file.type.toLowerCase() : "";
    return !actualMimeType || actualMimeType === expectedMimeType;
}

function normalizePrediction(value) {
    const prediction = typeof value === "string" ? value.trim().toLowerCase() : "";
    if (prediction === "genuine") return "Genuine";
    if (prediction === "counterfeit") return "Counterfeit";
    return "Unknown";
}

function normalizeConfidence(value) {
    const numeric = Number(value);
    return Number.isFinite(numeric) ? Math.max(0, Math.min(numeric, 100)) : 0;
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Unknown date" : date.toLocaleString();
}


// ===============================
// MOBILE NAVIGATION
// ===============================

function toggleMenu() {
    const navLinks = document.getElementById("navLinks");
    if (navLinks) {
        navLinks.classList.toggle("show");
    }
}


// ===============================
// REGISTER FORM
// ===============================

const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = document.getElementById("name").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            alert("Password and Confirm Password do not match.");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, email, password })
            });

            const data = await response.json();

            if (response.ok) {
                alert("Registration successful! Please login.");
                window.location.href = "login.html";
            } else {
                alert(data.error || data.message || "Registration failed.");
            }
        } catch (error) {
            console.error("Register Error:", error);
            alert("Could not connect to Spring Boot backend. Please make sure server is running on http://localhost:8081");
        }
    });
}


// ===============================
// USER LOGIN FORM
// ===============================

const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem("counterCheckCurrentUser", JSON.stringify(data));
                alert("Login successful!");
                window.location.href = "upload.html";
            } else {
                alert(data.error || data.message || "Invalid email or password.");
            }
        } catch (error) {
            console.error("Login Error:", error);
            alert("Could not connect to Spring Boot backend.");
        }
    });
}


// ===============================
// IMAGE PREVIEW & DRAG-AND-DROP HANDLERS
// ===============================

let selectedFile = null;

function setupImageUploadHandlers() {
    const productImageInput = document.getElementById("productImage");
    const uploadDropZone = document.getElementById("uploadDropZone");
    const removeImageBtn = document.getElementById("removeImageBtn");

    if (!productImageInput || !uploadDropZone) return;

    // Click listener for dropzone label
    uploadDropZone.addEventListener("click", function (e) {
        if (e.target.closest('#removeImageBtn')) {
            e.preventDefault();
            return;
        }
        // Input value reset so picking the same file again triggers change event
        productImageInput.value = "";
    });

    // Keyboard activation (Enter / Space) for accessible dropzone focus
    uploadDropZone.addEventListener("keydown", function (e) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            productImageInput.click();
        }
    });

    // File input change listener
    productImageInput.addEventListener("change", function () {
        if (productImageInput.files && productImageInput.files.length > 0) {
            handleSelectedFile(productImageInput.files[0]);
        }
    });

    // Drag and Drop event listeners
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadDropZone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            uploadDropZone.classList.add('drag-active');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        uploadDropZone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            uploadDropZone.classList.remove('drag-active');
        }, false);
    });

    uploadDropZone.addEventListener('drop', function (e) {
        const dt = e.dataTransfer;
        if (dt && dt.files && dt.files.length > 0) {
            try {
                productImageInput.files = dt.files;
            } catch (err) {
                console.log("File assignment notice:", err);
            }
            handleSelectedFile(dt.files[0]);
        }
    }, false);

    // Remove / Change Image Button Listener
    if (removeImageBtn) {
        removeImageBtn.addEventListener("click", function (e) {
            e.preventDefault();
            e.stopPropagation();
            resetSelectedFile();
        });
    }
}

function handleSelectedFile(file) {
    if (!file) return;

    // Accept only the raster formats supported by the preview policy.
    // This is a client-side convenience check; the backend must validate file content.
    if (!isSupportedImageFile(file)) {
        alert("Invalid file format. Please upload a JPG, JPEG, PNG, or WEBP image with a matching MIME type.");
        resetSelectedFile();
        return;
    }

    // Validate file size (10MB max)
    if (file.size > 10 * 1024 * 1024) {
        alert("File size exceeds 10MB maximum limit. Please select a smaller image.");
        resetSelectedFile();
        return;
    }

    selectedFile = file;

    // Update File Metadata Display
    const fileNameDisplay = document.getElementById("fileNameDisplay");
    const fileSizeDisplay = document.getElementById("fileSizeDisplay");
    if (fileNameDisplay) fileNameDisplay.innerText = file.name;
    if (fileSizeDisplay) fileSizeDisplay.innerText = `(${(file.size / (1024 * 1024)).toFixed(2)} MB)`;

    // Display Preview Image using FileReader Data URI for 100% reliable browser rendering
    const previewImage = document.getElementById("previewImage");
    const imagePreview = document.getElementById("imagePreview");
    
    const reader = new FileReader();
    reader.onload = function (event) {
        const previewUrl = safeImageUrl(event && event.target ? event.target.result : "", "");
        if (!previewUrl) {
            alert("The selected file could not be previewed as a supported raster image.");
            resetSelectedFile();
            return;
        }

        if (previewImage) {
            previewImage.src = previewUrl;
        }
        if (imagePreview) {
            imagePreview.style.display = "block";
        }
    };
    reader.onerror = function () {
        alert("The selected image could not be read. Please choose another file.");
        resetSelectedFile();
    };
    reader.readAsDataURL(file);

    // Hide previous result section if a new image is picked
    const resultSection = document.getElementById("analysisResultSection");
    if (resultSection) resultSection.style.display = "none";
}

function resetSelectedFile() {
    selectedFile = null;
    const productImageInput = document.getElementById("productImage");
    const imagePreview = document.getElementById("imagePreview");
    const previewImage = document.getElementById("previewImage");
    const fileNameDisplay = document.getElementById("fileNameDisplay");
    const fileSizeDisplay = document.getElementById("fileSizeDisplay");

    if (productImageInput) productImageInput.value = "";
    if (previewImage) previewImage.src = "";
    if (imagePreview) imagePreview.style.display = "none";
    if (fileNameDisplay) fileNameDisplay.innerText = "";
    if (fileSizeDisplay) fileSizeDisplay.innerText = "";
}


// ===============================
// PRODUCT ANALYSIS FORM SUBMISSION
// ===============================

function setupAnalysisForm() {
    const uploadForm = document.getElementById("uploadForm");
    if (!uploadForm) return;

    uploadForm.addEventListener("submit", async function (event) {
        event.preventDefault(); // Prevent default page reload

        const categorySelect = document.getElementById("category");
        const category = categorySelect ? categorySelect.value : "";

        // 1. Category Validation
        if (!category) {
            alert("Please select a product category before analyzing.");
            if (categorySelect) categorySelect.focus();
            return;
        }

        // 2. Image Selection Validation
        const fileInput = document.getElementById("productImage");
        const fileToUpload = selectedFile || (fileInput && fileInput.files && fileInput.files[0]);

        if (!fileToUpload) {
            alert("Please upload or drag a product image to analyze.");
            return;
        }

        // 3. UI Loading State
        const loading = document.getElementById("loading");
        const analyzeButton = document.getElementById("analyzeButton");
        const resultSection = document.getElementById("analysisResultSection");

        if (loading) loading.style.display = "block";
        if (resultSection) resultSection.style.display = "none";
        if (analyzeButton) {
            analyzeButton.disabled = true;
            analyzeButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Analyzing with AI...';
        }

        // 4. Construct FormData
        const currentUser = JSON.parse(localStorage.getItem("counterCheckCurrentUser")) || {};
        const formData = new FormData();
        formData.append("file", fileToUpload);
        formData.append("category", category);
        if (currentUser.id) {
            formData.append("userId", currentUser.id);
        }

        try {
            const response = await fetch(`${API_BASE_URL}/predictions/analyze`, {
                method: "POST",
                body: formData // Do not manually set Content-Type header for FormData!
            });

            const data = await response.json();

            if (response.ok) {
                // Construct complete static image URL
                const imageUrl = safeImageUrl(data.imagePath, "https://via.placeholder.com/500x400?text=Product+Image");

                const previewImgElem = document.getElementById("previewImage");
                const resultRecord = {
                    image: imageUrl,
                    previewUrl: (previewImgElem && previewImgElem.src && previewImgElem.src.length > 20 && previewImgElem.src.length < 100000) ? previewImgElem.src : imageUrl,
                    prediction: data.prediction,
                    confidence: data.confidence,
                    category: data.productCategory || category,
                    model: data.modelUsed || "Experimental image heuristic",
                    date: new Date(data.predictedAt || Date.now()).toLocaleString()
                };

                // Safely save to localStorage for persistence & result page compatibility
                try {
                    localStorage.setItem("counterCheckLatestResult", JSON.stringify(resultRecord));
                } catch (err) {
                    console.warn("localStorage quota overflow fallback:", err);
                    const compactRecord = Object.assign({}, resultRecord, { previewUrl: imageUrl });
                    try { localStorage.setItem("counterCheckLatestResult", JSON.stringify(compactRecord)); } catch (e) {}
                }

                // Render result inline AND automatically redirect to result.html page
                renderInlineResult(resultRecord);
                window.location.href = "result.html";
            } else {
                alert(`Analysis Error: ${data.error || data.message || "Failed to analyze image."}`);
            }
        } catch (error) {
            console.error("Analysis Request Error:", error);
            alert("Could not connect to Spring Boot AI backend server. Please make sure the backend is active on http://localhost:8081");
        } finally {
            if (loading) loading.style.display = "none";
            if (analyzeButton) {
                analyzeButton.disabled = false;
                analyzeButton.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles btn-icon"></i> <span>Analyze Product</span> <i class="fa-solid fa-arrow-right btn-arrow"></i>';
            }
        }
    });
}

function renderInlineResult(result) {
    const resultSection = document.getElementById("analysisResultSection");
    
    // If not on upload.html, navigate to result.html
    if (!resultSection) {
        window.location.href = "result.html";
        return;
    }

    const predictionLabel = normalizePrediction(result.prediction);
    const isGenuine = predictionLabel === "Genuine";
    const isCounterfeit = predictionLabel === "Counterfeit";

    // Verdict Banner & Icon
    const verdictBanner = document.getElementById("verdictBanner");
    const verdictIcon = document.getElementById("verdictIcon");
    const verdictText = document.getElementById("verdictText");

    if (verdictBanner && verdictText) {
        verdictBanner.className = `verdict-banner ${isGenuine ? 'verdict-genuine' : isCounterfeit ? 'verdict-counterfeit' : ''}`;
        verdictText.innerText = isGenuine ? "Lower-risk image pattern" : isCounterfeit ? "Higher-risk image pattern" : "Unclassified image pattern";
    }

    if (verdictIcon) {
        verdictIcon.className = `fa-solid ${isGenuine ? 'fa-shield-check' : isCounterfeit ? 'fa-triangle-exclamation' : 'fa-circle-question'}`;
    }

    // Images
    const resultCardImage = document.getElementById("resultCardImage");
    const resultImage = document.getElementById("resultImage");
    const previewImage = document.getElementById("previewImage");

    [resultCardImage, resultImage].forEach(imgElem => {
        if (imgElem) {
            imgElem.onerror = function() {
                this.onerror = null;
                this.src = safeImageUrl(result.image, "https://via.placeholder.com/500x400?text=Product+Image");
            };
            if (previewImage && previewImage.src && previewImage.src.length > 20) {
                imgElem.src = safeImageUrl(previewImage.src, "https://via.placeholder.com/500x400?text=Product+Image");
            } else if (result.previewUrl && result.previewUrl.length > 20) {
                imgElem.src = safeImageUrl(result.previewUrl, "https://via.placeholder.com/500x400?text=Product+Image");
            } else {
                imgElem.src = safeImageUrl(result.image, "https://via.placeholder.com/500x400?text=Product+Image");
            }
        }
    });

    // Confidence Score
    const formattedConf = normalizeConfidence(result.confidence).toFixed(2) + "%";
    ["resultConfidence", "confidenceScore"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedConf;
    });

    // Product Category
    const formattedCat = (result.category || "General").toUpperCase();
    ["resultCategory"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedCat;
    });

    // Model Info
    const formattedModel = result.model || "Experimental image heuristic";
    ["resultModel", "modelInfo"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedModel;
    });

    // Timestamp
    const formattedDate = result.date || new Date().toLocaleString();
    ["resultTimestamp", "predictionDate"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedDate;
    });

    // Details breakdown
    const detectionDetailsText = document.getElementById("detectionDetailsText");
    if (detectionDetailsText) {
        detectionDetailsText.innerHTML = `<strong>Experimental heuristic result</strong><br>The prototype combines generic image-model output with image-quality heuristics. This is not a validated counterfeit classifier and does not verify product authenticity.`;
    }

    resultSection.style.display = "block";
    resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function resetAnalysisForm() {
    resetSelectedFile();
    const categorySelect = document.getElementById("category");
    if (categorySelect) categorySelect.value = "";
    const resultSection = document.getElementById("analysisResultSection");
    if (resultSection) resultSection.style.display = "none";
    window.scrollTo({ top: 0, behavior: 'smooth' });
}


// ===============================
// DISPLAY RESULT PAGE
// ===============================

function displayResult() {
    const resultImage = document.getElementById("resultImage");
    const resultCardImage = document.getElementById("resultCardImage");
    const resultGrid = document.getElementById("resultGrid");
    const noResultState = document.getElementById("noResultState");

    const targetImageElem = resultImage || resultCardImage;
    if (!targetImageElem) return;

    const result = JSON.parse(localStorage.getItem("counterCheckLatestResult"));
    
    if (!result) {
        if (resultGrid) resultGrid.style.display = "none";
        if (noResultState) noResultState.style.display = "block";
        return;
    }

    if (resultGrid) resultGrid.style.display = "grid";
    if (noResultState) noResultState.style.display = "none";

    const predictionLabel = normalizePrediction(result.prediction);
    const isGenuine = predictionLabel === "Genuine";
    const isCounterfeit = predictionLabel === "Counterfeit";

    // Update Images
    [resultImage, resultCardImage].forEach(imgElem => {
        if (imgElem) {
            imgElem.onerror = function() {
                this.onerror = null;
                this.src = "https://via.placeholder.com/500x400?text=Product+Image";
            };
            if (result.previewUrl && result.previewUrl.length > 20) {
                imgElem.src = safeImageUrl(result.previewUrl, "https://via.placeholder.com/500x400?text=Product+Image");
            } else if (result.dataUrl && result.dataUrl.length > 20) {
                imgElem.src = safeImageUrl(result.dataUrl, "https://via.placeholder.com/500x400?text=Product+Image");
            } else {
                imgElem.src = safeImageUrl(result.image, "https://via.placeholder.com/500x400?text=Product+Image");
            }
        }
    });

    // Update Verdict Banners & Text
    const verdictBanner = document.getElementById("verdictBanner");
    const verdictIcon = document.getElementById("verdictIcon");
    const verdictText = document.getElementById("verdictText");

    if (verdictBanner && verdictText) {
        verdictBanner.className = `verdict-banner ${isGenuine ? 'verdict-genuine' : isCounterfeit ? 'verdict-counterfeit' : ''}`;
        verdictText.innerText = isGenuine ? "Lower-risk image pattern" : isCounterfeit ? "Higher-risk image pattern" : "Unclassified image pattern";
    }

    if (verdictIcon) {
        verdictIcon.className = `fa-solid ${isGenuine ? 'fa-shield-check' : isCounterfeit ? 'fa-triangle-exclamation' : 'fa-circle-question'}`;
    }

    const predictionStatus = document.getElementById("predictionStatus");
    if (predictionStatus) {
        predictionStatus.innerText = isGenuine ? "Lower-risk pattern" : isCounterfeit ? "Higher-risk pattern" : "Unclassified";
        predictionStatus.className = `prediction-status ${isGenuine ? 'genuine' : isCounterfeit ? 'counterfeit' : ''}`;
    }

    // Confidence Score
    const formattedConf = normalizeConfidence(result.confidence).toFixed(2) + "%";
    ["confidenceScore", "resultConfidence"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedConf;
    });

    // Category
    const formattedCat = (result.category || "General").toUpperCase();
    ["resultCategory"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedCat;
    });

    // Model Info
    const formattedModel = result.model || "Experimental image heuristic";
    ["modelInfo", "resultModel"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedModel;
    });

    // Timestamp
    const formattedDate = result.date || new Date().toLocaleString();
    ["predictionDate", "resultTimestamp"].forEach(id => {
        const elem = document.getElementById(id);
        if (elem) elem.innerText = formattedDate;
    });

    // Breakdown Details
    const detectionDetailsText = document.getElementById("detectionDetailsText");
    if (detectionDetailsText) {
        detectionDetailsText.innerHTML = `<strong>Experimental heuristic result</strong><br>The prototype combines generic image-model output with image-quality heuristics. This is not a validated counterfeit classifier and does not verify product authenticity.`;
    }
}


// ===============================
// DISPLAY PREDICTION HISTORY (Real MySQL Fetch)
// ===============================

async function displayHistory() {
    const historyTableBody = document.getElementById("historyTableBody");
    if (!historyTableBody) return;

    const currentUser = JSON.parse(localStorage.getItem("counterCheckCurrentUser")) || {};
    const userId = currentUser.id;
    const noHistoryMessage = document.getElementById("noHistoryMessage");

    // If user is not logged in (guest mode), render latest local prediction or show empty history message cleanly
    if (!userId) {
        const latestLocal = JSON.parse(localStorage.getItem("counterCheckLatestResult"));
        historyTableBody.innerHTML = "";

        if (latestLocal) {
            if (noHistoryMessage) noHistoryMessage.style.display = "none";
            const predictionLabel = normalizePrediction(latestLocal.prediction);
            const isGenuine = predictionLabel === "Genuine";
            const isCounterfeit = predictionLabel === "Counterfeit";
            const badgeClass = isGenuine ? "badge-genuine" : isCounterfeit ? "badge-counterfeit" : "";
            const badgeIcon = isGenuine ? "fa-circle-check" : isCounterfeit ? "fa-triangle-exclamation" : "fa-circle-question";
            const fillClass = isGenuine ? "fill-genuine" : isCounterfeit ? "fill-counterfeit" : "";
            const confVal = normalizeConfidence(latestLocal.confidence);

            const row = document.createElement("tr");
            row.innerHTML = `
                <td class="id-cell">#Guest</td>
                <td>
                    <div class="table-img-container">
                        <img src="${escapeHtml(safeImageUrl(latestLocal.dataUrl || latestLocal.image, "https://via.placeholder.com/70?text=Product"))}" alt="Product Snapshot" onerror="this.onerror=null;this.src='https://via.placeholder.com/70?text=Product';">
                    </div>
                </td>
                <td>
                    <span class="badge ${badgeClass}">
                        <i class="fa-solid ${badgeIcon}"></i>
                        <span>${escapeHtml(predictionLabel)}</span>
                    </span>
                </td>
                <td>
                    <div class="confidence-cell">
                        <span class="confidence-num">${confVal.toFixed(2)}%</span>
                        <div class="confidence-mini-bar">
                            <div class="confidence-mini-fill ${fillClass}" style="width: ${confVal}%;"></div>
                        </div>
                    </div>
                </td>
                <td class="date-cell">
                    <i class="fa-regular fa-clock date-icon"></i>
                    <span>${escapeHtml(latestLocal.date)}</span>
                </td>
            `;
            historyTableBody.appendChild(row);
        } else {
            if (noHistoryMessage) noHistoryMessage.style.display = "block";
        }
        return;
    }

    try {
        const url = `${API_BASE_URL}/predictions/history/${userId}`;
            
        const response = await fetch(url, {
            headers: {
                "X-User-Id": currentUser.id || "",
                "X-User-Role": currentUser.role || "",
                "X-Admin-Role": currentUser.role || ""
            }
        });

        if (response.status === 403) {
            window.location.href = "access-denied.html";
            return;
        }

        const historyData = await response.json();

        historyTableBody.innerHTML = "";

        if (!response.ok || !historyData || historyData.length === 0) {
            if (noHistoryMessage) noHistoryMessage.style.display = "block";
            return;
        }

        if (noHistoryMessage) noHistoryMessage.style.display = "none";

        historyData.forEach(function (record) {
            const row = document.createElement("tr");

            const predictionLabel = normalizePrediction(record.prediction);
            const isGenuine = predictionLabel === "Genuine";
            const isCounterfeit = predictionLabel === "Counterfeit";
            const badgeClass = isGenuine ? "badge-genuine" : isCounterfeit ? "badge-counterfeit" : "";
            const badgeIcon = isGenuine ? "fa-circle-check" : isCounterfeit ? "fa-triangle-exclamation" : "fa-circle-question";
            const fillClass = isGenuine ? "fill-genuine" : isCounterfeit ? "fill-counterfeit" : "";
            const imageUrl = safeImageUrl(record.imagePath, "https://via.placeholder.com/70?text=Product");
            const formattedDate = formatDate(record.predictedAt);
            const confVal = normalizeConfidence(record.confidence);

            row.innerHTML = `
                <td class="id-cell">#${escapeHtml(record.id)}</td>
                <td>
                    <div class="table-img-container">
                        <img src="${escapeHtml(imageUrl)}" alt="Product Snapshot" onerror="this.onerror=null;this.src='https://via.placeholder.com/70?text=Product';">
                    </div>
                </td>
                <td>
                    <span class="badge ${badgeClass}">
                        <i class="fa-solid ${badgeIcon}"></i>
                        <span>${escapeHtml(predictionLabel)}</span>
                    </span>
                </td>
                <td>
                    <div class="confidence-cell">
                        <span class="confidence-num">${confVal.toFixed(2)}%</span>
                        <div class="confidence-mini-bar">
                            <div class="confidence-mini-fill ${fillClass}" style="width: ${confVal}%;"></div>
                        </div>
                    </div>
                </td>
                <td class="date-cell">
                    <i class="fa-regular fa-clock date-icon"></i>
                    <span>${escapeHtml(formattedDate)}</span>
                </td>
            `;

            historyTableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Fetch History Error:", error);
    }
}


// ===============================
// SEARCH USER HISTORY
// ===============================

function searchHistory() {
    const input = document.getElementById("historySearch");
    if (!input) return;

    const filter = input.value.toLowerCase();
    const table = document.getElementById("historyTable");
    const rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("tr");

    for (let i = 0; i < rows.length; i++) {
        const text = rows[i].innerText.toLowerCase();
        rows[i].style.display = text.includes(filter) ? "" : "none";
    }
}


// ===============================
// ADMIN LOGIN & AUTHENTICATION
// ===============================

const adminLoginForm = document.getElementById("adminLoginForm");

if (adminLoginForm) {
    adminLoginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("adminEmail").value.trim();
        const password = document.getElementById("adminPassword").value;
        const submitBtn = this.querySelector("button[type='submit']");
        const origText = submitBtn ? submitBtn.innerHTML : "Login as Admin";

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Authenticating...';
        }

        try {
            const response = await fetch(`${API_BASE_URL}/auth/admin/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok && data.role === "ADMIN") {
                localStorage.setItem("counterCheckCurrentUser", JSON.stringify(data));
                localStorage.setItem("counterCheckAdmin", "true");
                window.location.href = "admin-dashboard.html";
            } else {
                alert(data.message || data.error || "Access Denied: Invalid admin credentials.");
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = origText;
                }
            }
        } catch (error) {
            console.error("Admin Login Error:", error);
            alert("Error connecting to Spring Boot backend server.");
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = origText;
            }
        }
    });
}

function adminLogout() {
    localStorage.removeItem("counterCheckAdmin");
    localStorage.removeItem("counterCheckCurrentUser");
    window.location.href = "admin-login.html";
}


// ===============================
// SEARCH ADMIN PREDICTIONS
// ===============================

function searchAdminPredictions() {
    const input = document.getElementById("adminSearch");
    if (!input) return;

    const filter = input.value.toLowerCase();
    const table = document.getElementById("adminPredictionTable");
    if (!table) return;
    const rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("tr");

    for (let i = 0; i < rows.length; i++) {
        const text = rows[i].innerText.toLowerCase();
        rows[i].style.display = text.includes(filter) ? "" : "none";
    }
}


// ===============================
// DELETE ADMIN PREDICTION (SECURE WITH CONFIRMATION)
// ===============================

async function deletePrediction(predictionId, buttonElement) {
    const currentUser = JSON.parse(localStorage.getItem("counterCheckCurrentUser")) || {};
    const isAdmin = localStorage.getItem("counterCheckAdmin") === "true" && currentUser.role === "ADMIN";

    if (!isAdmin) {
        alert("Access Denied: Admin authorization required.");
        window.location.href = "admin-login.html";
        return;
    }

    const confirmDelete = confirm(`Are you sure you want to permanently delete prediction record #${predictionId}? This action cannot be undone.`);
    if (!confirmDelete) return;

    try {
        const response = await fetch(`${API_BASE_URL}/predictions/${predictionId}`, {
            method: "DELETE",
            headers: {
                "X-Admin-Role": currentUser.role || "ADMIN"
            }
        });

        if (response.ok) {
            const row = buttonElement.closest("tr");
            if (row) {
                row.style.transition = "all 0.3s ease";
                row.style.opacity = "0";
                row.style.transform = "translateX(20px)";
                setTimeout(() => row.remove(), 300);
            }
            initializeAdminDashboard(); // Refresh stats
        } else {
            const errData = await response.json();
            alert(errData.error || "Failed to delete prediction. Access Denied.");
        }
    } catch (error) {
        console.error("Delete Error:", error);
        alert("Error connecting to backend server.");
    }
}


// ===============================
// ADMIN DASHBOARD (REAL DATABASE & ROLE PROTECTION)
// ===============================

async function initializeAdminDashboard() {
    const totalUsersElem = document.getElementById("totalUsers");
    if (!totalUsersElem) return;

    // Strict Frontend Authorization Guard
    const currentUser = JSON.parse(localStorage.getItem("counterCheckCurrentUser")) || {};
    const isAdmin = localStorage.getItem("counterCheckAdmin") === "true" && currentUser.role === "ADMIN";

    if (!isAdmin) {
        alert("Security Alert: Access Denied. Admin login required.");
        window.location.href = "admin-login.html";
        return;
    }

    try {
        const authHeaders = {
            "X-Admin-Role": currentUser.role || "ADMIN"
        };

        // Fetch dashboard statistics from backend
        const statsResp = await fetch(`${API_BASE_URL}/predictions/stats`, { headers: authHeaders });
        if (statsResp.ok) {
            const stats = await statsResp.json();
            document.getElementById("totalUsers").innerText = stats.totalUsers || 0;
            document.getElementById("totalPredictions").innerText = stats.totalPredictions || 0;
            document.getElementById("genuineProducts").innerText = stats.genuineCount || 0;
            document.getElementById("counterfeitProducts").innerText = stats.counterfeitCount || 0;
        } else if (statsResp.status === 403) {
            alert("Session Expired or Unauthorized Role.");
            adminLogout();
            return;
        }

        // Fetch all predictions table from backend
        const tableBody = document.getElementById("adminPredictionTableBody");
        if (!tableBody) return;

        const allResp = await fetch(`${API_BASE_URL}/predictions/all`, { headers: authHeaders });
        if (allResp.ok) {
            const allData = await allResp.json();
            tableBody.innerHTML = "";

            if (!allData || allData.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: #64748b;">
                            <i class="fa-solid fa-inbox" style="font-size: 2rem; margin-bottom: 10px; display: block;"></i>
                            No prediction records available in the database.
                        </td>
                    </tr>
                `;
                return;
            }

            allData.forEach(function (record) {
                const row = document.createElement("tr");

                const predictionLabel = normalizePrediction(record.prediction);
                const isGenuine = predictionLabel === "Genuine";
                const isCounterfeit = predictionLabel === "Counterfeit";
                const badgeClass = isGenuine ? "badge-genuine" : isCounterfeit ? "badge-counterfeit" : "";
                const badgeIcon = isGenuine ? "fa-circle-check" : isCounterfeit ? "fa-triangle-exclamation" : "fa-circle-question";
                const fillClass = isGenuine ? "fill-genuine" : isCounterfeit ? "fill-counterfeit" : "";
                const imageUrl = safeImageUrl(record.imagePath, "https://via.placeholder.com/60?text=Product");
                const formattedDate = formatDate(record.predictedAt);
                const confVal = normalizeConfidence(record.confidence);
                const predictionId = Number(record.id);
                const canDelete = Number.isSafeInteger(predictionId) && predictionId > 0;
                const deleteAction = canDelete
                    ? `<button class="btn btn-danger btn-delete-row" onclick="deletePrediction(${predictionId}, this)">
                            <i class="fa-solid fa-trash-can"></i>
                            <span>Delete</span>
                        </button>`
                    : `<button class="btn btn-danger btn-delete-row" disabled>
                            <i class="fa-solid fa-trash-can"></i>
                            <span>Delete unavailable</span>
                        </button>`;

                row.innerHTML = `
                    <td class="id-cell">#${escapeHtml(record.id)}</td>
                    <td class="user-cell">
                        <div class="user-info-wrapper">
                            <div class="user-avatar-small"><i class="fa-solid fa-user"></i></div>
                            <span>${escapeHtml(record.userName || "Guest User")}</span>
                        </div>
                    </td>
                    <td>
                        <div class="table-img-container">
                            <img src="${escapeHtml(imageUrl)}" alt="Product Snapshot" onerror="this.onerror=null;this.src='https://via.placeholder.com/60?text=Product';">
                        </div>
                    </td>
                    <td>
                        <span class="badge ${badgeClass}">
                            <i class="fa-solid ${badgeIcon}"></i>
                            <span>${escapeHtml(predictionLabel)}</span>
                        </span>
                    </td>
                    <td>
                        <div class="confidence-cell">
                            <span class="confidence-num">${confVal.toFixed(2)}%</span>
                            <div class="confidence-mini-bar">
                                <div class="confidence-mini-fill ${fillClass}" style="width: ${confVal}%;"></div>
                            </div>
                        </div>
                    </td>
                    <td class="date-cell">
                        <i class="fa-regular fa-clock date-icon"></i>
                        <span>${escapeHtml(formattedDate)}</span>
                    </td>
                    <td>
                        ${deleteAction}
                    </td>
                `;

                tableBody.appendChild(row);
            });
        }
    } catch (error) {
        console.error("Admin Dashboard Error:", error);
    }
}


// ===============================
// DYNAMIC NAVIGATION & SECURITY GUARD
// ===============================

function updateNavigationAndSecurity() {
    const currentUser = JSON.parse(localStorage.getItem("counterCheckCurrentUser")) || null;
    const isAdmin = localStorage.getItem("counterCheckAdmin") === "true" || (currentUser && currentUser.role === "ADMIN");
    const currentPath = window.location.pathname.toLowerCase();

    // 1. Strict Route Security Guards
    if (currentPath.endsWith("admin-dashboard.html") || currentPath.endsWith("admin-analytics.html")) {
        if (!isAdmin) {
            window.location.href = "access-denied.html";
            return;
        }
    }

    // 2. Hide Admin Links for Non-Admins in Navbar & Footer
    const adminNavLinks = document.querySelectorAll("nav a[href*='admin-dashboard'], nav a[href*='admin-analytics']");
    adminNavLinks.forEach(link => {
        const li = link.closest("li");
        if (li) {
            li.style.display = isAdmin ? "inline-block" : "none";
        }
    });

    const adminFooterLinks = document.querySelectorAll("footer a[href*='admin-dashboard'], footer a[href*='admin-analytics']");
    adminFooterLinks.forEach(link => {
        const li = link.closest("li");
        if (li) {
            li.style.display = isAdmin ? "block" : "none";
        }
    });

    // 3. Dynamic User Login / Logout Navbar Toggle
    if (currentUser && currentUser.name) {
        const loginNavLinks = document.querySelectorAll("nav a[href='login.html'], nav a[href='register.html']");
        loginNavLinks.forEach(link => {
            const li = link.closest("li");
            if (li) {
                if (link.getAttribute("href") === "login.html") {
                    li.innerHTML = `
                        <a href="#" onclick="userLogout(); return false;" class="user-profile-badge" style="color: var(--accent-cyan); font-weight: 600;">
                            <i class="fa-solid fa-user-check"></i> ${escapeHtml(String(currentUser.name).trim().split(/\s+/)[0])} (Logout)
                        </a>
                    `;
                } else if (link.getAttribute("href") === "register.html") {
                    li.style.display = "none";
                }
            }
        });
    }
}

function userLogout() {
    localStorage.removeItem("counterCheckCurrentUser");
    localStorage.removeItem("counterCheckAdmin");
    window.location.href = "login.html";
}

// Re-evaluate security on page show (handles browser back button navigation)
window.addEventListener("pageshow", function () {
    updateNavigationAndSecurity();
});


// ===============================
// RUN FUNCTIONS WHEN PAGE LOADS
// ===============================

document.addEventListener("DOMContentLoaded", function () {
    updateNavigationAndSecurity();
    setupImageUploadHandlers();
    setupAnalysisForm();
    displayResult();
    displayHistory();
    initializeAdminDashboard();
});