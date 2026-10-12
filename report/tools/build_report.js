const fs = require("fs");
const path = require("path");
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell, WidthType, BorderStyle,
  AlignmentType, HeadingLevel, PageBreak, TableOfContents, Footer, PageNumber, LevelFormat, VerticalAlign,
} = require("docx");

const ROOT = path.resolve(__dirname, "..");
const SRC = path.join(ROOT, "REPORT_SOURCE_AR.md");
const OUT = path.join(ROOT, "BAIT_IWB303_HW_F25_CX_Name_ID.docx");
const FONT = "Times New Roman";
const CODE_FONT = "Consolas";
const CONTENT_W = 9070;
const BODY = 26;
const AR = /[\u0600-\u06FF]/;

const run = (text, o = {}) => new TextRun({
  text, font: o.code ? CODE_FONT : FONT,
  size: o.size || (o.code ? 21 : BODY), sizeComplexScript: o.size || (o.code ? 21 : BODY),
  bold: !!o.bold, boldComplexScript: !!o.bold,
  rightToLeft: !o.code && AR.test(text),
  language: { value: "en-US", bidirectional: "ar-SY" },
});

function inline(text, o = {}) {
  const out = [];
  for (const part of text.split(/(\*\*[^*]+\*\*|`[^`]+`)/)) {
    if (!part) continue;
    if (part.startsWith("**") && part.endsWith("**")) {
      for (const p of part.slice(2, -2).split(/(`[^`]+`)/)) {
        if (!p) continue;
        if (p.startsWith("`")) out.push(run(p.slice(1, -1), { ...o, code: true, bold: true }));
        else out.push(run(p, { ...o, bold: true }));
      }
    } else if (part.startsWith("`") && part.endsWith("`")) out.push(run(part.slice(1, -1), { ...o, code: true }));
    else out.push(run(part, o));
  }
  return out;
}

const para = (children, o = {}) => new Paragraph({
  children, bidirectional: true,
  alignment: o.align || AlignmentType.BOTH,
  spacing: { after: o.after ?? 120, before: o.before ?? 0, line: o.line || 300 },
  keepNext: !!o.keepNext, heading: o.heading, numbering: o.numbering,
  border: o.border, indent: o.indent,
});

const B = { style: BorderStyle.SINGLE, size: 6, color: "000000" };
const BORDERS = { top: B, bottom: B, left: B, right: B, insideHorizontal: B, insideVertical: B };

function table(rows, o = {}) {
  const n = rows[0].length;
  const weights = [...Array(n).keys()].map(c => Math.max(...rows.map(r => Math.min((r[c] || "").replace(/[`*]/g, "").length, 60))) + 6);
  const total = weights.reduce((a, b) => a + b, 0);
  let widths = weights.map(w => Math.floor(CONTENT_W * w / total));
  if (o.widths) widths = o.widths;
  widths[widths.length - 1] += CONTENT_W - widths.reduce((a, b) => a + b, 0);
  const size = o.size || 22;
  return new Table({
    width: { size: CONTENT_W, type: WidthType.DXA }, columnWidths: widths, visuallyRightToLeft: true, borders: BORDERS,
    rows: rows.map((r, i) => new TableRow({
      tableHeader: i === 0, cantSplit: true,
      children: r.map((cell, c) => new TableCell({
        width: { size: widths[c], type: WidthType.DXA }, verticalAlign: VerticalAlign.CENTER,
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [para(inline(cell, { size, bold: i === 0 || (o.boldFirstCol && c === 0) }), { align: i === 0 ? AlignmentType.CENTER : AlignmentType.RIGHT, after: 0, line: 260 })],
      })),
    })),
  });
}

function pngSize(file) {
  const b = fs.readFileSync(file);
  return [b.readUInt32BE(16), b.readUInt32BE(20), b];
}

let figure = 0;
function image(file, caption, o = {}) {
  const [w, h, data] = pngSize(file);
  const maxW = o.maxW || 600, maxH = o.maxH || 820;
  let dw = Math.min(maxW, w / 2), dh = h * dw / w;
  if (dh > maxH) { dh = maxH; dw = w * dh / h; }
  figure += 1;
  return [
    para([new ImageRun({ type: "png", data, transformation: { width: Math.round(dw), height: Math.round(dh) } })], { align: AlignmentType.CENTER, keepNext: true, after: 60, before: 120 }),
    para(inline(`الشكل ${figure}: ${caption}`, { size: 21 }), { align: AlignmentType.CENTER, after: 200 }),
  ];
}

function screenshot(name, caption) {
  const file = path.join(ROOT, "screenshots", `${name}.png`);
  if (fs.existsSync(file)) return image(file, caption, { maxW: 300, maxH: 560 });
  figure += 1;
  const box = { style: BorderStyle.SINGLE, size: 8, color: "000000", space: 12 };
  return [
    para([run("مكان لقطة الشاشة", { bold: true }), run(`  (report/screenshots/${name}.png)`, { code: true })], { align: AlignmentType.CENTER, keepNext: true, before: 200, after: 0, border: { top: box, bottom: box, left: box, right: box } }),
    para(inline(`الشكل ${figure}: ${caption}`, { size: 21 }), { align: AlignmentType.CENTER, after: 200, before: 160 }),
  ];
}

function cover() {
  const c = (t, size, bold, after = 60) => para([run(t, { size, bold })], { align: AlignmentType.CENTER, after });
  const info = [
    ["البيان", "التفاصيل"],
    ["اسم الطالب", "[اسم الطالب]"],
    ["الرقم الجامعي", "[الرقم الجامعي]"],
    ["الشعبة", "[رقم الشعبة C?]"],
    ["أعضاء الفريق", "[أسماء أعضاء الفريق وأرقامهم]"],
    ["أستاذ المقرر", "د. محمد مازن المصطفى"],
    ["تاريخ التسليم", "2026/11/5"],
  ];
  const rubric = [
    ["المعيار", "الوصف", "الدرجة", "العلامة المستحقة"],
    ["جودة وقوة الكود (Code Quality)", "خلو الكود من الأخطاء، صحة تضمين الـ DB مسبقة الصنع بكفاءة، وسرعة استجابة استعلامات البحث الفوري", "30", ""],
    ["عمليات CRUD والواجهات", "عمل شريط البحث الديناميكي بكفاءة، إمكانية إضافة وتعديل أرقام الطوارئ المخصصة، وتناسق واجهات الطوارئ", "20", ""],
    ["التقرير: دليل المبرمج", "دقة الشرح التقني، وضوح الـ ERD، والتحليل التأملي للمشاكل البرمجية التي واجهت الفريق أثناء التطوير وكيفية التغلب عليها", "25", ""],
    ["التقرير: دليل المستخدم/المستثمر", "وضوح لقطات الشاشة الإرشادية، وقوة الحجة التسويقية والتحليلية لتجربة المستخدم في الظروف الحرجة", "15", ""],
    ["النظام", "معالجة الأخطاء واستقرار اختبار التطبيق ضد المدخلات الخاطئة (Input Validation) وضمان عدم انهياره (Zero Crashes)", "10", ""],
    ["المجموع الكلي", "", "100", ""],
  ];
  return [
    c("الجمهورية العربية السورية", 26, true, 0),
    c("وزارة التعليم العالي", 26, true, 0),
    c("الجامعة الافتراضية السورية", 26, true, 240),
    c("وظيفة مقرر BAIT_IWB303 — الفصل F25", 26, false, 120),
    c("تطبيق دليل الإسعافات الأولية للطوارئ (FirstAid Hero)", 34, true, 60),
    c("التقرير التأملي: دليل المبرمج ودليل المستخدم والمستثمر", 26, false, 200),
    table(info, { widths: [3000, 6070], size: 22, boldFirstCol: true }),
    para([run("سلم تصحيح الوظيفة", { bold: true, size: 24 })], { align: AlignmentType.CENTER, before: 240, after: 80 }),
    table(rubric, { widths: [2900, 4300, 770, 1100], size: 19 }),
  ];
}

const numberingConfig = [];
function newList(kind) {
  const ref = `list${numberingConfig.length}`;
  numberingConfig.push({
    reference: ref,
    levels: [{
      level: 0, format: kind === "ol" ? LevelFormat.DECIMAL : LevelFormat.BULLET,
      text: kind === "ol" ? "%1." : "\u2022", alignment: AlignmentType.START,
      style: { paragraph: { indent: { start: 540, hanging: 360 } }, run: { font: FONT } },
    }],
  });
  return ref;
}

function build() {
  const lines = fs.readFileSync(SRC, "utf8").replace(/\r\n/g, "\n").split("\n");
  const out = [];
  let i = 0;
  while (i < lines.length) {
    const l = lines[i];
    const s = l.trim();
    if (!s) { i++; continue; }
    if (s === "[[COVER]]") { out.push(...cover()); i++; continue; }
    if (s === "[[PAGEBREAK]]") { out.push(new Paragraph({ children: [new PageBreak()] })); i++; continue; }
    if (s === "[[TOC]]") { out.push(new TableOfContents("فهرس المحتويات", { hyperlink: true, headingStyleRange: "1-3" })); i++; continue; }
    let m;
    if ((m = s.match(/^\[\[SHOT:([^|]+)\|(.+)\]\]$/))) { out.push(...screenshot(m[1], m[2])); i++; continue; }
    if ((m = s.match(/^!\[(.+)\]\((.+)\)$/))) { out.push(...image(path.join(ROOT, m[2]), m[1])); i++; continue; }
    if ((m = s.match(/^(#{1,3}) (.+)$/))) {
      const level = m[1].length, text = m[2];
      if (text === "فهرس المحتويات") { out.push(para([run(text, { bold: true, size: 32 })], { align: AlignmentType.CENTER, after: 240 })); i++; continue; }
      const size = [0, 34, 30, 27][level];
      out.push(para(inline(text, { bold: true, size }), {
        heading: [null, HeadingLevel.HEADING_1, HeadingLevel.HEADING_2, HeadingLevel.HEADING_3][level],
        align: level === 1 ? AlignmentType.CENTER : AlignmentType.RIGHT, keepNext: true,
        before: level === 1 ? 120 : 240, after: 120,
      }));
      i++; continue;
    }
    if (s.startsWith("|")) {
      const rows = [];
      while (i < lines.length && lines[i].trim().startsWith("|")) {
        const cells = lines[i].trim().slice(1, -1).split("|").map(x => x.trim());
        if (!cells.every(x => /^:?-+:?$/.test(x))) rows.push(cells);
        i++;
      }
      out.push(table(rows));
      out.push(para([], { after: 120 }));
      continue;
    }
    if (/^(- |\d+\. )/.test(s)) {
      const kind = /^\d+\. /.test(s) ? "ol" : "ul";
      const ref = newList(kind);
      while (i < lines.length && /^(- |\d+\. )/.test(lines[i].trim())) {
        const t = lines[i].trim().replace(/^(- |\d+\. )/, "");
        out.push(para(inline(t), { numbering: { reference: ref, level: 0 }, after: 60 }));
        i++;
      }
      out.push(para([], { after: 60 }));
      continue;
    }
    out.push(para(inline(s)));
    i++;
  }
  return out;
}

const children = build();
const headingStyle = (id, name, size) => ({
  id, name, basedOn: "Normal", next: "Normal", quickFormat: true,
  run: { font: FONT, size, sizeComplexScript: size, bold: true, boldComplexScript: true, color: "000000", rightToLeft: true },
  paragraph: { spacing: { before: 240, after: 120 } },
});
const doc = new Document({
  creator: "FirstAid Hero Team",
  title: "FirstAid Hero — التقرير التأملي",
  features: { updateFields: true },
  styles: {
    default: { document: { run: { font: FONT, size: BODY, sizeComplexScript: BODY, rightToLeft: true } } },
    paragraphStyles: [headingStyle("Heading1", "Heading 1", 34), headingStyle("Heading2", "Heading 2", 30), headingStyle("Heading3", "Heading 3", 27)],
  },
  numbering: { config: numberingConfig },
  sections: [{
    properties: {
      titlePage: true,
      page: { margin: { top: 1418, bottom: 1418, left: 1418, right: 1418 } },
    },
    footers: {
      default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 22 })] })] }),
      first: new Footer({ children: [new Paragraph({ children: [] })] }),
    },
    children,
  }],
});
Packer.toBuffer(doc).then(buf => { fs.writeFileSync(OUT, buf); console.log("wrote", OUT, "figures", figure); });
