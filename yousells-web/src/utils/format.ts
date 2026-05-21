/**
 * 格式化日期时间 — 统一前端展示
 */

/** yyyy-MM-ddTHH:mm:ss → MM-DD */
export function shortDate(raw: string | null | undefined): string {
  if (!raw) return "-";
  const d = new Date(raw);
  if (isNaN(d.getTime())) return raw;
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${m}-${day}`;
}

/** yyyy-MM-ddTHH:mm:ss → M月D日 */
export function friendlyDate(raw: string | null | undefined): string {
  if (!raw) return "-";
  const d = new Date(raw);
  if (isNaN(d.getTime())) return raw;
  return `${d.getMonth() + 1}月${d.getDate()}日`;
}

/** 相对日期：今天/明天/昨天/逾期N天/M月D日 */
export function relativeDate(raw: string | null | undefined): string {
  if (!raw) return "-";
  const d = new Date(raw);
  if (isNaN(d.getTime())) return raw;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);

  const diff = Math.round((target.getTime() - today.getTime()) / 86400000);

  if (diff === 0) return "今天";
  if (diff === 1) return "明天";
  if (diff === -1) return "昨天";
  if (diff < -1) return `逾期${Math.abs(diff)}天`;
  return friendlyDate(raw);
}

/** yyyy-MM-ddTHH:mm:ss → MM-DD HH:mm */
export function datetime(raw: string | null | undefined): string {
  if (!raw) return "-";
  const d = new Date(raw);
  if (isNaN(d.getTime())) return raw;
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const h = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${m}-${d} ${h}:${min}`;
}
