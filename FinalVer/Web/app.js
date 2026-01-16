const $ = (s, el = document) => el.querySelector(s);

const stage = $("#stage");
const workspaceCard = $("#workspaceCard");

const codeA = $("#codeA");
const codeB = $("#codeB");
const boxA = $("#boxA");
const boxB = $("#boxB");

const fileA = $("#fileA");
const fileB = $("#fileB");

const btnCompute = $("#btnCompute");
const btnComputeHero = $("#btnComputeHero");
const btnSwap = $("#btnSwap");

const ring = $("#ring");
const scoreText = $("#scoreText");
const verdict = $("#verdict");
const verdictHint = $("#verdictHint");
const badgeLevel = $("#badgeLevel");

let lastScore = null;

function clamp(n, a = 0, b = 1) {
  return Math.min(b, Math.max(a, n));
}

function lerp(a, b, t) {
  return a + (b - a) * t;
}

function animateNumber(el, from, to, duration = 900) {
  const t0 = performance.now();
  const easeOut = (t) => 1 - Math.pow(1 - t, 3);
  function tick(t) {
    const p = clamp((t - t0) / duration);
    const v = lerp(from, to, easeOut(p));
    el.textContent = Math.round(v).toString();
    if (p < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}

function animateRing(toP, duration = 1100) {
  const from = parseFloat(getComputedStyle(ring).getPropertyValue("--p")) || 0;
  const t0 = performance.now();
  const ease = (t) => 1 - Math.pow(1 - t, 4);
  function tick(t) {
    const p = clamp((t - t0) / duration);
    const v = lerp(from, toP, ease(p));
    ring.style.setProperty("--p", v);
    if (p < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}

function verdictFrom(score) {
  if (score >= 0.88) return { t: "几乎一致", hint: "结构和表达高度相近。", badge: "Very High" };
  if (score >= 0.7) return { t: "高度相似", hint: "核心结构相似，可能存在局部改写。", badge: "High" };
  if (score >= 0.45) return { t: "中度相似", hint: "存在部分共享逻辑或相同片段。", badge: "Medium" };
  if (score >= 0.22) return { t: "低相似", hint: "相同元素较少，更多是局部巧合。", badge: "Low" };
  return { t: "几乎无关", hint: "两份代码的重叠非常少。", badge: "Very Low" };
}

function setScanning(on) {
  boxA.classList.toggle("scanning", on);
  boxB.classList.toggle("scanning", on);
  btnCompute.disabled = on;
  btnCompute.style.opacity = on ? 0.92 : 1;
}

function resetResult(soft = false) {
  ring.style.setProperty("--p", 0);
  scoreText.textContent = soft ? "—" : "0";
  lastScore = null;

  verdict.textContent = "等待输入";
  verdictHint.textContent = "填入两段代码后点击“计算相似度”。";
  badgeLevel.textContent = "Idle";
}

function animateResult(score01) {
  stage.dataset.layout = "focus";

  const score = clamp(score01);
  lastScore = score;

  const pct = Math.round(score * 100);
  const overshoot = clamp(score + (score > 0.5 ? 0.03 : 0.02), 0, 1);
  animateRing(overshoot, 820);
  setTimeout(() => animateRing(score, 520), 520);

  const from = scoreText.textContent === "—" ? 0 : parseInt(scoreText.textContent || "0", 10);
  animateNumber(scoreText, from, pct, 980);

  const v = verdictFrom(score);
  verdict.textContent = v.t;
  verdictHint.textContent = v.hint;
  badgeLevel.textContent = v.badge;
}

async function computeSimilarity(a, b) {
  const res = await fetch("/api/similarity", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code1: a, code2: b })
  });

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }

  const data = await res.json();
  if (typeof data.similarity !== "number") {
    throw new Error("Invalid response: missing similarity");
  }

  return data.similarity;
}

async function readFileInto(file, textarea) {
  const text = await file.text();
  textarea.value = text;
  textarea.dispatchEvent(new Event("input"));
}

async function runCompute() {
  const a = codeA.value.trim();
  const b = codeB.value.trim();

  if (!a || !b) {
    workspaceCard.classList.remove("shake");
    void workspaceCard.offsetWidth;
    workspaceCard.classList.add("shake");
    verdict.textContent = "缺少输入";
    verdictHint.textContent = "请同时提供 Code A 与 Code B。";
    badgeLevel.textContent = "Input";
    return;
  }

  setScanning(true);

  try {
    const score = await computeSimilarity(a, b);
    setScanning(false);
    animateResult(score);
  } catch (err) {
    setScanning(false);
    verdict.textContent = "计算失败";
    verdictHint.textContent = err instanceof Error ? err.message : "请求失败";
    badgeLevel.textContent = "Error";
  }
}

btnCompute.addEventListener("click", runCompute);
if (btnComputeHero) {
  btnComputeHero.addEventListener("click", () => {
    workspaceCard.scrollIntoView({ behavior: "smooth", block: "start" });
    setTimeout(runCompute, 200);
  });
}

btnSwap.addEventListener("click", () => {
  const a = codeA.value;
  codeA.value = codeB.value;
  codeB.value = a;
  codeA.dispatchEvent(new Event("input"));
  resetResult(true);
});

Array.from(document.querySelectorAll("[data-clear]"), (btn) => {
  btn.addEventListener("click", () => {
    const which = btn.dataset.clear;
    if (which === "A") codeA.value = "";
    if (which === "B") codeB.value = "";
    codeA.dispatchEvent(new Event("input"));
    resetResult(true);
  });
});


fileA.addEventListener("change", async () => {
  if (fileA.files?.[0]) await readFileInto(fileA.files[0], codeA);
  fileA.value = "";
  resetResult(true);
});

fileB.addEventListener("change", async () => {
  if (fileB.files?.[0]) await readFileInto(fileB.files[0], codeB);
  fileB.value = "";
  resetResult(true);
});

codeA.addEventListener("input", () => resetResult(true));
codeB.addEventListener("input", () => resetResult(true));

stage.dataset.layout = "idle";
resetResult(true);
