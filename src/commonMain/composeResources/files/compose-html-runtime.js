export const Env = (() => {
    const meta = new URL(import.meta.url).searchParams
    const container = document.querySelector(`#${meta.get("containerId")}`)
    const content = container.shadowRoot.querySelector(`#${meta.get("contentId")}`)

    return Object.freeze({
        container: container,
        content: content,
        emit: (ev) => container.dispatchEvent(ev),
        addListener: (type, listener) => container.addEventListener(type, listener),
        removeListener: (type, listener) => container.removeEventListener(type, listener),
    })
})();

// Event Proxy and accessibility
(() =>  {
    const container = Env.container
    const content = Env.content
    const canvas = document.querySelector("canvas")

    container.addEventListener("wheel", (ev) => canvas.dispatchEvent(new WheelEvent("wheel", ev)))
    const pointerEvents = ["pointerover", "pointerenter", "pointerdown", "pointermove", "pointerup", "pointercancel", "pointerout", "pointerleave", "gotpointercapture", "lostpointercapture"]
    pointerEvents.forEach((type) => container.addEventListener(type, (ev) => canvas.dispatchEvent(new PointerEvent(type, ev))))
    const touchEvents = ["touchstart", "touchend", "touchmove", "touchcancel"]
    touchEvents.forEach((type) => container.addEventListener(type, (ev) => canvas.dispatchEvent(new TouchEvent(type, ev))))
    const mouseEvents = ["click", "dblclick", "mousedown", "mouseenter", "mouseleave", "mouseout", "mouseover", "mouseup"]
    mouseEvents.forEach((type) => container.addEventListener(type, (ev) => canvas.dispatchEvent(new MouseEvent(type, ev))))

    const keyboardEvents = ["keyup", "keypress"]
    keyboardEvents.forEach((type) => container.addEventListener(type, (ev) => canvas.dispatchEvent(new KeyboardEvent(type, ev))))
    container.addEventListener("keydown", (ev) => {
        if (ev.key !== "Tab") {
             canvas.dispatchEvent(new KeyboardEvent("keydown", ev))
             return
        }

        const focusables = getFocusableElements(content)
        if (focusables.length > 0) {
            if (ev.shiftKey) {
                if (container.shadowRoot.activeElement === focusables[0]) {
                    container.dispatchEvent(new CustomEvent("compose-html-blur", {detail: {direction: "previous"}}))
                }
            } else if (container.shadowRoot.activeElement === focusables[focusables.length - 1]) {
                container.shadowRoot.activeElement.blur()
                container.dispatchEvent(new CustomEvent("compose-html-blur", {detail: {direction: "next"}}))
                ev.preventDefault()
            }
            return
        }
    })

    container.addEventListener("compose-html-focus", () => {
        const focusableElements = getFocusableElements(content)
        if (focusableElements.length === 0) return
        focusableElements[0].focus()
    })

    const observer = new ResizeObserver((ev) => {
        container.dispatchEvent(new CustomEvent("compose-html-resize", {detail: {height: content.scrollHeight, width: content.scrollWidth}}))
    })

    observer.observe(content)

    function getFocusableElements(root) {
        const selector = [
            "a[href]",
            "area[href]",
            "input:not([disabled])",
            "select:not([disabled])",
            "textarea:not([disabled])",
            "button:not([disabled])",
            "iframe",
            "object",
            "embed",
            "[contenteditable]",
            "[tabindex]"
        ].join(",")

        return Array.from(root.querySelectorAll(selector)).filter(el => el.tabIndex >= 0);
    }
})();