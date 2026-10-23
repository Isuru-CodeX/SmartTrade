/* Seller tools : add product (brand / model / specs / images) and "My listings" */
const loadingOptions = {clickToClose: false};

/* small built-in rich text editor (assets/js/editor.js) for the description */
const editor1 = new STEditor("#description");
const editEditor = new STEditor("#editDescription");

let specificationsLoaded = false;

document.getElementById("product-anchor").addEventListener("shown.bs.tab", async () => {
    if (specificationsLoaded) return;
    Notiflix.Loading.pulse("Loading form data...", loadingOptions);
    try {
        await loadBrands();
        await loadProductSpecifications();
        specificationsLoaded = true;
    } finally {
        Notiflix.Loading.remove();
    }
});

/* Dynamic product photos (capped at 8) */
let selectedProductImages = [];

function triggerPhotoUpload() {
    const input = document.getElementById("productImagesInput");
    if (input) {
        input.click();
    }
}

function handleProductImageSelection(input) {
    if (!input.files || !input.files.length) return;
    const remainingSlots = 8 - selectedProductImages.length;
    if (remainingSlots <= 0) {
        Notiflix.Notify.warning("Maximum of 8 photos reached.");
        input.value = "";
        return;
    }

    const filesToAdd = Array.from(input.files).slice(0, remainingSlots);
    if (input.files.length > remainingSlots) {
        Notiflix.Notify.warning(`Only ${remainingSlots} photo(s) added. Maximum limit is 8 photos.`);
    }

    let addedCount = 0;
    filesToAdd.forEach((file) => {
        if (!file.type.startsWith("image/")) {
            Notiflix.Notify.failure("Only image files are allowed: " + file.name);
            return;
        }
        selectedProductImages.push(file);
        addedCount++;
    });

    input.value = "";
    renderImageGrid();
    if (addedCount > 0 && selectedProductImages.length === addedCount) {
        Notiflix.Notify.info("1st photo automatically designated as Main Cover photo.");
    }
}

function setAsCoverImage(index) {
    if (index <= 0 || index >= selectedProductImages.length) return;
    const [chosen] = selectedProductImages.splice(index, 1);
    selectedProductImages.unshift(chosen);
    renderImageGrid();
    Notiflix.Notify.success("Main cover photo updated!");
}

function removeProductImage(index) {
    selectedProductImages.splice(index, 1);
    renderImageGrid();
}

function renderImageGrid() {
    const grid = document.getElementById("imageUploaderGrid");
    const addBtn = document.getElementById("addImageBtn");
    const countBadge = document.getElementById("photoCountBadge");
    const limitNotice = document.getElementById("imageLimitNotice");

    if (!grid || !addBtn) return;

    // Remove old preview cards
    grid.querySelectorAll(".preview-card").forEach((el) => el.remove());

    if (countBadge) countBadge.textContent = `${selectedProductImages.length} / 8 photos`;

    // Render each selected photo
    selectedProductImages.forEach((file, idx) => {
        const card = document.createElement("div");
        card.className = "image-upload-card preview-card" + (idx === 0 ? " is-cover" : "");

        const img = document.createElement("img");
        img.src = URL.createObjectURL(file);
        img.alt = `Photo ${idx + 1}`;
        card.appendChild(img);

        if (idx === 0) {
            const cover = document.createElement("span");
            cover.className = "cover-badge";
            cover.innerHTML = '<i class="fa-solid fa-star me-1"></i>Main Cover';
            card.appendChild(cover);
        } else {
            const setCoverBtn = document.createElement("button");
            setCoverBtn.type = "button";
            setCoverBtn.className = "btn-set-cover";
            setCoverBtn.title = "Make this the main cover photo";
            setCoverBtn.innerHTML = '<i class="fa-solid fa-star me-1"></i>Set as Cover';
            setCoverBtn.onclick = (e) => {
                e.stopPropagation();
                setAsCoverImage(idx);
            };
            card.appendChild(setCoverBtn);
        }

        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.className = "btn-remove-img";
        removeBtn.title = "Remove photo";
        removeBtn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
        removeBtn.onclick = (e) => {
            e.stopPropagation();
            removeProductImage(idx);
        };
        card.appendChild(removeBtn);

        grid.insertBefore(card, addBtn);
    });

    if (selectedProductImages.length >= 8) {
        addBtn.style.display = "none";
        if (limitNotice) limitNotice.hidden = false;
    } else {
        addBtn.style.display = "flex";
        if (limitNotice) limitNotice.hidden = true;
    }
}

async function loadBrands() {
    try {
        const response = await fetch("api/data/brands");
        if (response.ok) {
            const data = await response.json();
            renderDropdowns(document.getElementById("brandSelect"), data.brands, "name");
        } else {
            Notiflix.Notify.failure("Brands loading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

async function loadModels() {
    const brandSelect = document.getElementById("brandSelect");
    const modelSelect = document.getElementById("model");
    modelSelect.innerHTML = '<option value="0">Select</option>';
    if (brandSelect.value === "0") return;

    Notiflix.Loading.pulse("Loading models...", loadingOptions);
    try {
        const response = await fetch("api/data/" + brandSelect.value + "/models");
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                renderDropdowns(modelSelect, data.models || [], "name");
                if (!data.models || !data.models.length) {
                    Notiflix.Notify.info("No models listed for this brand yet. You can add one using '+ Add Model'!");
                }
            } else {
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Models loading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

async function loadProductSpecifications() {
    try {
        const response = await fetch("api/data/specifications");
        if (response.ok) {
            const data = await response.json();
            renderDropdowns(document.getElementById("storage"), data.storages, "value");
            renderDropdowns(document.getElementById("color"), data.colors, "value");
            renderDropdowns(document.getElementById("condition"), data.qualities, "value");
        } else {
            Notiflix.Notify.failure("Product specifications loading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

function renderDropdowns(selector, list, key) {
    if (!selector) return;
    selector.innerHTML = '<option value="0">Select</option>';
    (list || []).forEach((item) => {
        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = item[key];
        selector.appendChild(option);
    });
}

async function saveProduct() {
    const brandSelect = document.getElementById("brandSelect");
    const modelSelect = document.getElementById("model");
    const title = document.getElementById("title");
    const storageSelect = document.getElementById("storage");
    const colorSelect = document.getElementById("color");
    const conditionSelect = document.getElementById("condition");
    const price = document.getElementById("price");
    const qty = document.getElementById("qty");

    if (brandSelect.value === "0") {
        Notiflix.Notify.warning("Please select a brand!");
        brandSelect.focus();
        return;
    }
    if (modelSelect.value === "0") {
        Notiflix.Notify.warning("Please select a model!");
        modelSelect.focus();
        return;
    }
    if (!title.value.trim()) {
        Notiflix.Notify.warning("Please enter a listing title!");
        title.focus();
        return;
    }
    if (storageSelect.value === "0") {
        Notiflix.Notify.warning("Please select storage!");
        storageSelect.focus();
        return;
    }
    if (colorSelect.value === "0") {
        Notiflix.Notify.warning("Please select colour!");
        colorSelect.focus();
        return;
    }
    if (conditionSelect.value === "0") {
        Notiflix.Notify.warning("Please select condition!");
        conditionSelect.focus();
        return;
    }
    if (!price.value || parseFloat(price.value) <= 0) {
        Notiflix.Notify.warning("Please enter a valid price!");
        price.focus();
        return;
    }
    if (!qty.value || parseInt(qty.value, 10) <= 0) {
        Notiflix.Notify.warning("Please enter a valid quantity!");
        qty.focus();
        return;
    }

    Notiflix.Loading.pulse("Publishing listing...", loadingOptions);

    const productDataObj = {
        brandId: brandSelect.value,
        modelId: modelSelect.value,
        title: title.value,
        description: editor1.getHTMLCode(),
        storageId: storageSelect.value,
        colorId: colorSelect.value,
        qualityId: conditionSelect.value,
        price: parseFloat(price.value),
        qty: parseInt(qty.value, 10)
    };

    const formData = new FormData();
    formData.append("product", JSON.stringify(productDataObj));

    try {
        const response = await fetch("api/products/save-product", {
            method: "POST",
            body: formData,
            credentials: "include"
        });
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                await uploadProductImages(data.productId);
            } else {
                Notiflix.Notify.failure(data.message || "Failed to save product details");
            }
        } else {
            Notiflix.Notify.failure("Product details adding failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

async function uploadProductImages(productId) {
    if (!selectedProductImages.length) {
        Notiflix.Report.success("SmartTrade", "Your listing was published successfully! (No photos attached)", "Okay");
        resetProductForm();
        return;
    }

    const formData = new FormData();
    // Index 0 in selectedProductImages is strictly the Main Cover Photo
    selectedProductImages.forEach((file) => {
        formData.append("images[]", file);
        formData.append("images", file);
    });

    try {
        const response = await fetch("api/products/" + productId + "/upload-images", {
            method: "POST",
            body: formData,
            credentials: "include"
        });
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success("SmartTrade", data.message || "Product published successfully with photos!", "Okay");
                resetProductForm();
            } else {
                Notiflix.Notify.failure(data.message || "Failed to upload images");
            }
        } else {
            Notiflix.Notify.failure("Product images uploading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

function resetProductForm() {
    ["title", "price", "qty"].forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.value = "";
    });
    ["brandSelect", "storage", "color", "condition"].forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.value = "0";
    });
    const modelEl = document.getElementById("model");
    if (modelEl) modelEl.innerHTML = '<option value="0">Select</option>';
    editor1.setHTMLCode("");
    selectedProductImages = [];
    renderImageGrid();
}

/* ---------- Quick Add Modals (Seller Side) ---------- */
function hideModalById(modalId) {
    const el = document.getElementById(modalId);
    if (!el) return;
    try {
        const inst = bootstrap.Modal.getInstance(el);
        if (inst) {
            inst.hide();
        } else {
            el.classList.remove("show");
            el.style.display = "none";
        }
    } catch (e) {
        el.classList.remove("show");
        el.style.display = "none";
    }
    setTimeout(() => {
        const anyOpen = document.querySelectorAll(".modal.show");
        if (!anyOpen.length) {
            document.body.classList.remove("modal-open");
            document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
        }
    }, 250);
}

function openAddBrandModal() {
    const el = document.getElementById("modalAddBrand");
    if (!el) return;
    const inp = document.getElementById("newBrandNameInput");
    if (inp) inp.value = "";
    const modal = bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
    setTimeout(() => {
        if (inp) inp.focus();
    }, 350);
}

async function submitNewBrand() {
    const input = document.getElementById("newBrandNameInput");
    const name = (input.value || "").trim();
    if (!name) {
        Notiflix.Notify.warning("Please enter a brand name!");
        input.focus();
        return;
    }

    Notiflix.Loading.pulse("Saving brand...", loadingOptions);
    try {
        const res = await fetch("api/data/brand", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ name: name })
        });
        const data = await res.json();
        if (data.status && data.brand) {
            hideModalById("modalAddBrand");
            await loadBrands();
            document.getElementById("brandSelect").value = String(data.brand.id);
            await loadModels();
            Notiflix.Notify.success(`Brand "${data.brand.name}" saved & selected!`);
        } else {
            Notiflix.Notify.failure(data.message || "Failed to add brand.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

function openAddModelModal() {
    const brandSelect = document.getElementById("brandSelect");
    if (!brandSelect || brandSelect.value === "0") {
        Notiflix.Report.info("Select a Brand First", "Please select a brand from the dropdown before adding a model, or click '+ Add Brand' if your brand is not listed yet.", "Got it");
        return;
    }

    const brandName = brandSelect.options[brandSelect.selectedIndex].text;
    const badge = document.getElementById("modalSelectedBrandBadge");
    if (badge) badge.textContent = brandName;
    const inp = document.getElementById("newModelNameInput");
    if (inp) inp.value = "";

    const el = document.getElementById("modalAddModel");
    const modal = bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
    setTimeout(() => {
        if (inp) inp.focus();
    }, 350);
}

async function submitNewModel() {
    const brandSelect = document.getElementById("brandSelect");
    const brandId = parseInt(brandSelect.value, 10);
    const input = document.getElementById("newModelNameInput");
    const name = (input.value || "").trim();

    if (!brandId || brandId <= 0) {
        Notiflix.Notify.warning("Please select a brand first!");
        return;
    }
    if (!name) {
        Notiflix.Notify.warning("Please enter a model name!");
        input.focus();
        return;
    }

    Notiflix.Loading.pulse("Saving model...", loadingOptions);
    try {
        const res = await fetch("api/data/model", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ brandId: brandId, name: name })
        });
        const data = await res.json();
        if (data.status && data.model) {
            hideModalById("modalAddModel");
            await loadModels();
            document.getElementById("model").value = String(data.model.id);
            Notiflix.Notify.success(`Model "${data.model.name}" saved & selected!`);
        } else {
            Notiflix.Notify.failure(data.message || "Failed to add model.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

function openAddStorageModal() {
    const el = document.getElementById("modalAddStorage");
    if (!el) return;
    const inp = document.getElementById("newStorageInput");
    if (inp) inp.value = "";
    const modal = bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
    setTimeout(() => {
        if (inp) inp.focus();
    }, 350);
}

async function submitNewStorage() {
    const input = document.getElementById("newStorageInput");
    const val = (input.value || "").trim();
    if (!val) {
        Notiflix.Notify.warning("Please enter a storage size (e.g. 1 TB)!");
        input.focus();
        return;
    }

    Notiflix.Loading.pulse("Saving storage...", loadingOptions);
    try {
        const res = await fetch("api/data/storage", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ value: val })
        });
        const data = await res.json();
        if (data.status && data.storage) {
            hideModalById("modalAddStorage");
            await loadProductSpecifications();
            document.getElementById("storage").value = String(data.storage.id);
            Notiflix.Notify.success(`Storage "${data.storage.value}" saved & selected!`);
        } else {
            Notiflix.Notify.failure(data.message || "Failed to add storage.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

function openAddColorModal() {
    const el = document.getElementById("modalAddColor");
    if (!el) return;
    const inp = document.getElementById("newColorInput");
    if (inp) inp.value = "";
    const modal = bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
    setTimeout(() => {
        if (inp) inp.focus();
    }, 350);
}

async function submitNewColor() {
    const input = document.getElementById("newColorInput");
    const val = (input.value || "").trim();
    if (!val) {
        Notiflix.Notify.warning("Please enter a colour name!");
        input.focus();
        return;
    }

    Notiflix.Loading.pulse("Saving colour...", loadingOptions);
    try {
        const res = await fetch("api/data/color", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ value: val })
        });
        const data = await res.json();
        if (data.status && data.color) {
            hideModalById("modalAddColor");
            await loadProductSpecifications();
            document.getElementById("color").value = String(data.color.id);
            Notiflix.Notify.success(`Colour "${data.color.value}" saved & selected!`);
        } else {
            Notiflix.Notify.failure(data.message || "Failed to add colour.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

/* ---------- My listings ---------- */
document.getElementById("product-listing-anchor").addEventListener("shown.bs.tab", async () => {
    Notiflix.Loading.pulse("Loading your listings...", loadingOptions);
    try {
        await loadAllProducts();
    } finally {
        Notiflix.Loading.remove();
    }
});

async function loadAllProducts() {
    const tbody = document.getElementById("product-listing-tbody");
    const empty = document.getElementById("listingEmpty");
    const wrap = document.getElementById("listingWrap");
    tbody.innerHTML = "";
    try {
        const response = await fetch("api/products/all", { credentials: "include" });
        if (!response.ok) {
            Notiflix.Notify.failure("Product data loading failed");
            return;
        }
        if (response.redirected) {
            window.location.href = response.url;
            return;
        }
        const data = await response.json();

        if (!data.status || !data.products || !data.products.length) {
            wrap.hidden = true;
            empty.hidden = false;
            empty.innerHTML = '<div class="empty-state"><div class="ic"><i class="fa-regular fa-box-open"></i></div><h4>No listings to show</h4><p>' +
                ST.esc(data.message || "You have not listed any phones yet.") +
                '</p><button class="st-btn" type="button" onclick="bootstrap.Tab.getOrCreateInstance(document.getElementById(\'product-anchor\')).show()">Sell a phone</button></div>';
            return;
        }

        wrap.hidden = false;
        empty.hidden = true;
        data.products.forEach((product) => {
            const thumb = ST.esc(ST.image(product, 0));
            (product.stockDTOList || [{ price: 0, qty: 0, createdAt: "-", status: product.status || "APPROVED" }]).forEach((stock) => {
                const statusStr = (stock.status || product.status || "APPROVED").toUpperCase();
                let statusBadge = '<span class="status-pill status-pill-approved"><i class="fa-solid fa-circle-check"></i> Approved</span>';
                if (statusStr === "REJECTED") {
                    statusBadge = '<span class="status-pill status-pill-rejected"><i class="fa-solid fa-circle-xmark"></i> Rejected</span>';
                } else if (statusStr === "PENDING") {
                    statusBadge = '<span class="status-pill status-pill-pending"><i class="fa-solid fa-clock"></i> Pending</span>';
                }

                const idCol = '<span class="pid-badge">#' + product.productId + '</span>';
                const rawTitle = product.title || "Untitled Product";
                const shortTitle = rawTitle.length > 28 ? rawTitle.substring(0, 25) + "..." : rawTitle;
                const titleCol = '<div class="d-flex align-items-center gap-2">' +
                    '<img src="' + thumb + '" alt="" class="rounded flex-shrink-0" style="width:38px;height:38px;object-fit:cover;background:#0d151a;border:1px solid var(--st-line-2)">' +
                    '<span class="fw-medium text-white table-product-title" title="' + ST.esc(rawTitle) + '">' + ST.esc(shortTitle) + '</span>' +
                    '</div>';
                const actionsCol =
                    '<div class="d-flex gap-2 align-items-center">' +
                    '<a href="single-product.html?productId=' + product.productId + '" class="btn-table-action btn-table-view" title="View product page">View</a>' +
                    '<button type="button" class="btn-table-action btn-table-edit" onclick="openEditListing(' + product.productId + ')" title="Edit listing">Edit</button>' +
                    '<button type="button" class="btn-table-action btn-table-danger" onclick="deleteListingConfirm(' + product.productId + ',\'' + ST.esc(rawTitle).replace(/'/g, "\\'") + '\')" title="Delete listing">Delete</button>' +
                    '</div>';
                tbody.insertAdjacentHTML("beforeend",
                    "<tr><th scope='row'>" + idCol + "</th><td>" + titleCol + "</td>" +
                    "<td>" + statusBadge + "</td>" +
                    "<td class='mono'>" + ST.rs(stock.price) + "</td><td class='mono'>" + stock.qty + "</td>" +
                    "<td>" + ST.esc(stock.createdAt) + "</td>" +
                    "<td>" + actionsCol + "</td></tr>");
            });
        });
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

/* ---------- View Listing (Redirects to product page) ---------- */
function openViewListing(productId) {
    if (productId) {
        window.location.href = "single-product.html?productId=" + productId;
    }
}

/* ---------- Edit Listing ---------- */
async function openEditListing(productId) {
    Notiflix.Loading.pulse("Loading product...", loadingOptions);
    try {
        const response = await fetch("api/products/" + productId, { credentials: "include" });
        if (!response.ok) {
            Notiflix.Notify.failure("Failed to load product details.");
            return;
        }
        const data = await response.json();
        if (!data.status || !data.product) {
            Notiflix.Notify.failure(data.message || "Failed to load product.");
            return;
        }

        const p = data.product;
        document.getElementById("editProductId").value = p.productId;
        document.getElementById("editTitle").value = p.title || "";
        if (editEditor) {
            editEditor.setHTMLCode(p.description || "");
        }
        document.getElementById("editPrice").value = p.price || 0;
        document.getElementById("editQty").value = p.qty || 1;

        // Show read-only meta info (brand, model, storage, color, condition)
        const metaEl = document.getElementById("editProductMeta");
        metaEl.innerHTML = "";
        const tags = [];
        if (p.brandName) tags.push('<span class="tag"><i class="fa-regular fa-tag me-1"></i>' + ST.esc(p.brandName) + '</span>');
        if (p.modelName) tags.push('<span class="tag"><i class="fa-regular fa-mobile me-1"></i>' + ST.esc(p.modelName) + '</span>');
        if (p.storageValue) tags.push('<span class="tag"><i class="fa-regular fa-hard-drive me-1"></i>' + ST.esc(p.storageValue) + '</span>');
        if (p.colorValue) tags.push('<span class="tag"><i class="fa-regular fa-palette me-1"></i>' + ST.esc(p.colorValue) + '</span>');
        if (p.qualityValue) tags.push('<span class="tag"><i class="fa-regular fa-star me-1"></i>' + ST.esc(p.qualityValue) + '</span>');
        metaEl.innerHTML = tags.join("");

        const el = document.getElementById("modalEditListing");
        const modal = bootstrap.Modal.getOrCreateInstance(el);
        modal.show();
        setTimeout(() => {
            document.getElementById("editTitle").focus();
        }, 350);
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

async function submitEditListing() {
    const productId = parseInt(document.getElementById("editProductId").value, 10);
    const title = document.getElementById("editTitle").value.trim();
    const description = editEditor ? editEditor.getHTMLCode() : "";
    const price = parseFloat(document.getElementById("editPrice").value);
    const qty = parseInt(document.getElementById("editQty").value, 10);

    if (!title) {
        Notiflix.Notify.warning("Please enter a listing title!");
        document.getElementById("editTitle").focus();
        return;
    }
    if (!price || price <= 0) {
        Notiflix.Notify.warning("Please enter a valid price!");
        document.getElementById("editPrice").focus();
        return;
    }
    if (!qty || qty <= 0) {
        Notiflix.Notify.warning("Please enter a valid quantity!");
        document.getElementById("editQty").focus();
        return;
    }

    Notiflix.Loading.pulse("Saving changes...", loadingOptions);
    try {
        const response = await fetch("api/products/update-product", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                productId: productId,
                title: title,
                description: description,
                price: price,
                qty: qty
            })
        });
        const data = await response.json();
        if (data.status) {
            hideModalById("modalEditListing");
            Notiflix.Notify.success(data.message || "Product updated successfully!");
            await loadAllProducts();
        } else {
            Notiflix.Notify.failure(data.message || "Failed to update product.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

/* ---------- Delete Listing ---------- */
function deleteListingConfirm(productId, productTitle) {
    Notiflix.Confirm.show(
        "Delete Listing",
        'Are you sure you want to delete "' + productTitle + '"? This action cannot be undone.',
        "Yes, Delete",
        "Cancel",
        async function () {
            Notiflix.Loading.pulse("Deleting listing...", loadingOptions);
            try {
                const response = await fetch("api/products/" + productId, {
                    method: "DELETE",
                    credentials: "include"
                });
                const data = await response.json();
                if (data.status) {
                    Notiflix.Notify.success(data.message || "Product deleted successfully!");
                    await loadAllProducts();
                } else {
                    Notiflix.Notify.failure(data.message || "Failed to delete product.");
                }
            } catch (e) {
                Notiflix.Notify.failure(e.message);
            } finally {
                Notiflix.Loading.remove();
            }
        },
        function () { /* cancelled */ },
    );
}

// Ensure functions are accessible in window scope
window.openViewListing = openViewListing;
window.openEditListing = openEditListing;
window.submitEditListing = submitEditListing;
window.deleteListingConfirm = deleteListingConfirm;
window.loadAllProducts = loadAllProducts;


