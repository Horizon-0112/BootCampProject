(() => {
  const root = document.querySelector(".mypage-container");
  if (!root) return;
  const api = root.dataset.api;
  const csrf = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
  let kind = "recipes", page = 0, generation = 0;
  const $ = id => document.getElementById(id);
  async function request(url, method = "GET", body) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    try {
      const response = await fetch(url, {
        method, credentials: "same-origin", signal: controller.signal,
        headers: { Accept: "application/json", ...(method !== "GET" ? { [csrfHeader]: csrf } : {}),
          ...(body && !(body instanceof FormData) ? { "Content-Type": "application/json" } : {}) },
        ...(body ? { body: body instanceof FormData ? body : JSON.stringify(body) } : {})
      });
      if (response.status === 401) { location.assign(root.dataset.login); throw new Error("다시 로그인해 주세요."); }
      if (!response.headers.get("content-type")?.includes("application/json")) throw new Error("서버 응답을 확인할 수 없습니다. 다시 시도해 주세요.");
      const data = await response.json();
      if (!response.ok) throw Object.assign(new Error(data.message || "요청을 처리하지 못했습니다."), { fields: data.fieldErrors || {} });
      return data;
    } catch (e) {
      if (e.name === "AbortError") throw new Error("응답 시간이 초과되었습니다. 처리 결과를 확인한 뒤 다시 시도해 주세요.");
      if (e instanceof TypeError) throw new Error("서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");
      throw e;
    } finally { clearTimeout(timer); }
  }
  async function profile(refreshForm = true) {
    $("retryProfile").hidden = true;
    $("pageMessage").textContent = "";
    try {
      const data = await request(api);
      $("profileNickname").textContent = data.nickName;
      $("profileEmail").textContent = data.email;
      $("profileAvatar").textContent = Array.from(data.nickName)[0] || "";
      hasProfileImage = data.hasProfileImage;
      $("btnAvatarDelete").disabled = !hasProfileImage;
      if (hasProfileImage) {
        const image = document.createElement("img");
        image.alt = "";
        image.src = api + "/profile-image?v=" + Date.now();
        image.addEventListener("error", () => {
          $("profileAvatar").textContent = Array.from(data.nickName)[0] || "";
          $("avatarMessage").textContent = "사진을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
        });
        $("profileAvatar").replaceChildren(image);
      }
      $("profileJoined").textContent = data.createdAt ? data.createdAt.slice(0, 10) + " 가입" : "";
      $("recipeCount").textContent = data.recipes;
      $("bookmarkCount").textContent = data.bookmarks;
      $("commentCount").textContent = data.comments;
      if (refreshForm) $("nickName").value = data.nickName;
      $("btnEditProfile").disabled = false;
    } catch (e) { $("pageMessage").textContent = e.message; $("retryProfile").hidden = false; }
  }
  function node(tag, className, text) {
    const el = document.createElement(tag); el.className = className; el.textContent = text; return el;
  }
  async function list() {
    const version = ++generation;
    $("activityList").replaceChildren();
    $("listMessage").textContent = "불러오는 중...";
    $("retryList").hidden = true;
    $("prevPage").disabled = $("nextPage").disabled = true;
    try {
      const data = await request(api + "/" + kind + "?page=" + page + "&size=10");
      if (version !== generation) return;
      $("listMessage").textContent = data.items.length ? "" : { recipes: "작성한 레시피가 없습니다.", bookmarks: "북마크한 레시피가 없습니다.", comments: "작성한 댓글이 없습니다." }[kind];
      $("listSummary").textContent = "최신순 · 총 " + data.total + "개";
      data.items.forEach(item => {
        const article = node("article", "activity-card", "");
        article.append(node("h2", "activity-title", item.title));
        article.append(node("p", "activity-content", item.content || "등록된 설명이 없습니다."));
        article.append(node("p", "activity-date", item.createdAt.slice(0, 10)));
        $("activityList").append(article);
      });
      $("pageNumber").textContent = (page + 1) + " / " + Math.max(1, Math.ceil(data.total / data.size));
      $("prevPage").disabled = page === 0;
      $("nextPage").disabled = (page + 1) * data.size >= data.total;
    } catch (e) {
      if (version !== generation) return;
      $("listMessage").textContent = e.message; $("retryList").hidden = false;
    }
  }
  function bindForm(id, method) {
    const form = $(id);
    form.addEventListener("submit", async event => {
      event.preventDefault();
      const button = form.querySelector('button[type="submit"]');
      if (button.disabled) return;
      if (method === "DELETE" && !window.confirm("작성한 콘텐츠를 포함해 계정을 영구 삭제하시겠습니까?")) return;
      const message = form.querySelector(".form-message");
      message.textContent = "";
      form.querySelectorAll("[data-error-for]").forEach(el => el.textContent = "");
      form.querySelectorAll("[aria-invalid]").forEach(el => el.removeAttribute("aria-invalid"));
      const data = Object.fromEntries(new FormData(form));
      if (method === "DELETE") data.confirmed = form.elements.confirmed.checked;
      button.disabled = true;
      try {
        const result = await request(api, method, data);
        if (result.redirectUrl) { location.assign(result.redirectUrl); return; }
        message.className = "form-message form-success"; message.textContent = result.message;
        form.querySelectorAll('input[type="password"]').forEach(el => el.value = "");
        await profile();
      } catch (e) {
        message.className = "form-message form-error"; message.textContent = e.message;
        let first;
        Object.entries(e.fields || {}).forEach(([name, text]) => {
          form.querySelectorAll("[data-error-for]").forEach(el => { if (el.dataset.errorFor === name) el.textContent = text; });
          const input = form.elements.namedItem(name);
          if (input) { input.setAttribute("aria-invalid", "true"); first ||= input; }
        });
        first?.focus();
      } finally { button.disabled = false; }
    });
  }
  let hasProfileImage = false, previewUrl = null, avatarBusy = false;
  function clearPreview() {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = null;
    $("avatarPreview").hidden = true;
    $("avatarPreview").removeAttribute("src");
  }
  $("btnAvatarEdit").addEventListener("click", () => {
    $("avatarSection").hidden = !$("avatarSection").hidden;
    $("btnAvatarEdit").setAttribute("aria-expanded", String(!$("avatarSection").hidden));
    if (!$("avatarSection").hidden) $("avatarFile").focus();
  });
  $("avatarFile").addEventListener("change", () => {
    clearPreview();
    const file = $("avatarFile").files[0];
    $("avatarMessage").textContent = "";
    $("btnAvatarSave").disabled = true;
    if (!file) return;
    if (!["image/jpeg", "image/png"].includes(file.type) || file.size > 5 * 1024 * 1024 || !file.size) {
      $("avatarMessage").className = "form-message form-error";
      $("avatarMessage").textContent = "5MB 이하의 JPG 또는 PNG 사진을 선택해 주세요.";
      return;
    }
    previewUrl = URL.createObjectURL(file);
    $("avatarPreview").src = previewUrl;
    $("avatarPreview").hidden = false;
    $("btnAvatarSave").disabled = false;
  });
  async function changeAvatar(remove) {
    if (avatarBusy) return;
    if (!remove && (!$("avatarFile").files[0] || $("btnAvatarSave").disabled)) return;
    avatarBusy = true;
    $("btnAvatarSave").disabled = $("btnAvatarDelete").disabled = $("avatarFile").disabled = true;
    $("avatarMessage").textContent = "처리 중...";
    try {
      const form = new FormData();
      if (!remove) form.append("file", $("avatarFile").files[0]);
      const result = await request(api + "/profile-image", remove ? "DELETE" : "POST", remove ? undefined : form);
      $("avatarMessage").className = "form-message form-success";
      $("avatarMessage").textContent = result.message;
      $("avatarFile").value = "";
      clearPreview();
      await profile(false);
    } catch (e) {
      $("avatarMessage").className = "form-message form-error";
      $("avatarMessage").textContent = e.fields?.file || e.message;
    } finally {
      avatarBusy = false;
      $("avatarFile").disabled = false;
      $("btnAvatarDelete").disabled = !hasProfileImage;
      $("btnAvatarSave").disabled = !$("avatarFile").files[0];
    }
  }
  $("avatarForm").addEventListener("submit", event => { event.preventDefault(); changeAvatar(false); });
  $("btnAvatarDelete").addEventListener("click", () => changeAvatar(true));
  window.addEventListener("pagehide", clearPreview);
  $("btnEditProfile").addEventListener("click", () => {
    $("editSection").hidden = !$("editSection").hidden;
    $("btnEditProfile").setAttribute("aria-expanded", String(!$("editSection").hidden));
    if (!$("editSection").hidden) $("nickName").focus();
  });
  document.querySelectorAll("[data-tab]").forEach(button => button.addEventListener("click", () => {
    kind = button.dataset.tab; page = 0;
    document.querySelectorAll("[data-tab]").forEach(tab => {
      tab.classList.toggle("active", tab === button); tab.setAttribute("aria-pressed", String(tab === button));
    });
    list();
  }));
  $("prevPage").addEventListener("click", () => { if (page > 0) { page--; list(); } });
  $("nextPage").addEventListener("click", () => { page++; list(); });
  $("retryList").addEventListener("click", list); $("retryProfile").addEventListener("click", profile);
  bindForm("profileForm", "PATCH"); bindForm("withdrawForm", "DELETE");
  profile(); list();
})();
