/* STEditor - tiny dependency free rich text editor (bold / italic / underline / headings / lists / link).
   API: new STEditor("#id")  ->  getHTMLCode(), setHTMLCode(html)                                     */
class STEditor {
    constructor(selector) {
        this.host = typeof selector === "string" ? document.querySelector(selector) : selector;
        if (!this.host) return;
        this.host.classList.add("st-editor");
        this.host.innerHTML =
            '<div class="bar" role="toolbar" aria-label="Formatting">' +
            this.btn("bold", "<b>B</b>", "Bold") + this.btn("italic", "<i>I</i>", "Italic") + this.btn("underline", "<u>U</u>", "Underline") +
            '<span class="sep"></span>' +
            this.btn("h3", "H", "Heading") + this.btn("p", "&para;", "Paragraph") +
            '<span class="sep"></span>' +
            this.btn("insertUnorderedList", '<i class="fa-regular fa-list-ul"></i>', "Bullet list") +
            this.btn("insertOrderedList", '<i class="fa-regular fa-list-ol"></i>', "Numbered list") +
            this.btn("link", '<i class="fa-regular fa-link"></i>', "Insert link") +
            this.btn("removeFormat", '<i class="fa-regular fa-eraser"></i>', "Clear formatting") +
            '</div><div class="area" contenteditable="true" data-placeholder="Describe the phone: condition, accessories, warranty..."></div>';
        this.area = this.host.querySelector(".area");

        this.host.querySelector(".bar").addEventListener("mousedown", (e) => e.preventDefault()); // keep the selection
        this.host.querySelector(".bar").addEventListener("click", (e) => {
            const b = e.target.closest("button");
            if (b) this.run(b.dataset.cmd);
        });
        this.area.addEventListener("paste", (e) => { // paste as plain text
            e.preventDefault();
            document.execCommand("insertText", false, (e.clipboardData || window.clipboardData).getData("text/plain"));
        });
    }

    btn(cmd, label, title) {
        return '<button type="button" data-cmd="' + cmd + '" title="' + title + '" aria-label="' + title + '">' + label + "</button>";
    }

    run(cmd) {
        if (!this.area) return;
        this.area.focus();
        if (cmd === "h3") document.execCommand("formatBlock", false, "h3");
        else if (cmd === "p") document.execCommand("formatBlock", false, "p");
        else if (cmd === "link") {
            const url = prompt("Link address (https://...)");
            if (url && /^https?:\/\//i.test(url.trim())) document.execCommand("createLink", false, url.trim());
        } else document.execCommand(cmd, false, null);
    }

    getHTMLCode() {
        if (!this.area) return "";
        const html = this.area.innerHTML.trim();
        return this.area.textContent.trim() || /<(ul|ol|li|img)/i.test(html) ? (window.ST ? ST.sanitize(html) : html) : "";
    }

    setHTMLCode(html) {
        if (!this.area) return;
        this.area.innerHTML = html && window.ST ? ST.sanitize(html) : html || "";
    }
}
