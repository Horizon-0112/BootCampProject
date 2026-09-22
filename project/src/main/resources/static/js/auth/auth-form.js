(() => {
  class AuthFormManager {
    constructor({ formSelector = "form[data-auth-form]" } = {}) {
      this.formSelector = formSelector;
      this.initialized = false;
    }

    setup() {
      if (this.initialized) return;
      this.initialized = true;

      document.querySelectorAll(this.formSelector).forEach(form => this.bindForm(form));
    }

    bindForm(form) {
      const message = form.querySelector(".form-message");
      const button = form.querySelector("button[type='submit']");
      const token = form.elements.namedItem("token");

      if (token) {
        token.value = new URLSearchParams(location.hash.slice(1)).get("token") || "";
        history.replaceState(null, "", location.pathname + location.search);

        if (!token.value && message) {
          message.textContent = "재설정 링크가 없습니다. 비밀번호 찾기에서 링크를 다시 받아 주세요.";
          message.className = "form-message form-error";
        }

        if (!token.value && button) {
          button.disabled = true;
        }
      }

      form.addEventListener("input", event => {
        const input = event.target;
        if (!(input instanceof HTMLElement)) return;

        input.removeAttribute("aria-invalid");
        form.querySelectorAll("[data-error-for]").forEach(el => {
          if (el.dataset.errorFor === input.name) el.textContent = "";
        });
      });

      form.addEventListener("submit", async event => {
        event.preventDefault();
        if (!button || button.disabled) return;

        if (message) message.textContent = "";
        form.querySelectorAll("[data-error-for]").forEach(el => el.textContent = "");
        form.querySelectorAll("[aria-invalid]").forEach(el => el.removeAttribute("aria-invalid"));

        const original = button.textContent;
        button.disabled = true;
        button.textContent = "처리 중...";

        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 15000);

        try {
          const response = await fetch(form.action, {
            method: "POST",
            credentials: "same-origin",
            headers: { "Accept": "application/json" },
            body: new URLSearchParams(new FormData(form)),
            signal: controller.signal
          });

          if (!response.headers.get("content-type")?.includes("application/json")) {
            throw new Error("INVALID_RESPONSE");
          }

          const data = await response.json();

          if (response.ok && data.success) {
            if (data.redirectUrl) {
              location.assign(data.redirectUrl);
              return;
            }

            if (message) {
              message.className = "form-message form-success";
              message.textContent = data.message;
            }
          } else {
            if (message) {
              message.className = "form-message form-error";
              message.textContent = data.message || "요청을 처리하지 못했습니다. 다시 시도해 주세요.";
            }

            let first;
            Object.entries(data.fieldErrors || {}).forEach(([name, error]) => {
              form.querySelectorAll("[data-error-for]").forEach(el => {
                if (el.dataset.errorFor === name) el.textContent = error;
              });

              const input = form.elements.namedItem(name);
              if (input && input.type !== "hidden") {
                input.setAttribute("aria-invalid", "true");
                first ||= input;
              }
            });
            first?.focus();
          }
        } catch (error) {
          if (message) {
            message.className = "form-message form-error";
            message.textContent = error.name === "AbortError"
              ? "응답 시간이 초과되었습니다. 처리 결과를 확인한 뒤 다시 시도해 주세요."
              : "서버에 연결할 수 없거나 응답이 올바르지 않습니다. 잠시 후 다시 시도해 주세요.";
          }
        } finally {
          clearTimeout(timeout);
          if (button) {
            button.disabled = false;
            button.textContent = original;
          }
        }
      });
    }
  }

  window.AuthFormManager = AuthFormManager;
  const manager = new AuthFormManager();
  window.authFormManager = manager;

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => manager.setup());
  } else {
    manager.setup();
  }
})();
