/**
 * 记住上传文件/文件夹句柄（File System Access API + IndexedDB）
 * - 多选单个 mp4：showOpenFilePicker → 存 FileSystemFileHandle，可一键续传
 * - 选文件夹：showDirectoryPicker → 按文件名匹配续传
 * Chrome / Edge 可用
 */
const DB_NAME = "hyMaterialUpload";
const STORE = "handles";
const DIR_KEY = "lastDir";
const FILES_KEY = "lastFiles"; // { name: FileSystemFileHandle }

const MEDIA_RE = /\.(mp4|mov|m4v|avi|mkv|webm|jpg|jpeg|png|gif|webp|bmp)$/i;

export function supportsFolderPicker() {
  return typeof window !== "undefined" && typeof window.showDirectoryPicker === "function";
}

export function supportsFilePicker() {
  return typeof window !== "undefined" && typeof window.showOpenFilePicker === "function";
}

function openDb() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, 1);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE)) db.createObjectStore(STORE);
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function idbPut(key, value) {
  const db = await openDb();
  await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).put(value, key);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

async function idbGet(key) {
  const db = await openDb();
  const value = await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readonly");
    const req = tx.objectStore(STORE).get(key);
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
  db.close();
  return value;
}

async function idbDel(key) {
  const db = await openDb();
  await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).delete(key);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

export async function saveDirHandle(handle) {
  if (!handle) return;
  await idbPut(DIR_KEY, handle);
}

export async function loadDirHandle() {
  return (await idbGet(DIR_KEY)) || null;
}

export async function clearDirHandle() {
  await idbDel(DIR_KEY);
}

/** 保存本批选中的文件句柄（按文件名） */
export async function saveFileHandles(handleMap) {
  if (!handleMap || !Object.keys(handleMap).length) {
    await idbDel(FILES_KEY);
    return;
  }
  await idbPut(FILES_KEY, handleMap);
}

export async function loadFileHandles() {
  const map = await idbGet(FILES_KEY);
  return map && typeof map === "object" ? map : {};
}

export async function mergeFileHandles(extraMap) {
  const cur = await loadFileHandles();
  Object.keys(extraMap || {}).forEach(k => { cur[k] = extraMap[k]; });
  await saveFileHandles(cur);
  return cur;
}

export async function clearFileHandles() {
  await idbDel(FILES_KEY);
}

export async function ensurePermission(handle) {
  if (!handle) return false;
  const opts = {mode: "read"};
  try {
    if (typeof handle.queryPermission === "function") {
      if ((await handle.queryPermission(opts)) === "granted") return true;
    }
    if (typeof handle.requestPermission === "function") {
      if ((await handle.requestPermission(opts)) === "granted") return true;
    }
    // 部分环境无 permission API，直接尝试 getFile
    if (typeof handle.getFile === "function") {
      await handle.getFile();
      return true;
    }
  } catch (e) {
    return false;
  }
  return false;
}

export const ensureDirPermission = ensurePermission;

function isMediaName(name) {
  return MEDIA_RE.test(name || "");
}

/** 弹出多选文件（可勾选单个/多个 mp4） */
export async function pickMediaFileHandles() {
  if (!supportsFilePicker()) {
    throw new Error("当前浏览器不支持文件句柄，请用 Chrome/Edge");
  }
  const handles = await window.showOpenFilePicker({
    multiple: true,
    types: [
      {
        description: "视频/图片",
        accept: {
          "video/*": [".mp4", ".mov", ".m4v", ".avi", ".mkv", ".webm"],
          "image/*": [".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"]
        }
      }
    ],
    excludeAcceptAllOption: false
  });
  const map = {};
  const files = [];
  for (const h of handles) {
    const file = await h.getFile();
    const name = file.name || h.name;
    map[name] = h;
    files.push(file);
  }
  return {handles: map, files};
}

/** 从已存文件句柄按文件名取回 File */
export async function restoreFilesFromHandles(names) {
  const map = await loadFileHandles();
  const found = {};
  const list = names || Object.keys(map);
  for (const name of list) {
    const h = map[name];
    if (!h) continue;
    const ok = await ensurePermission(h);
    if (!ok) continue;
    try {
      found[name] = await h.getFile();
    } catch (e) { /* skip */ }
  }
  return found;
}

/** 在文件夹内按文件名查找（含子目录，最多 3 层） */
export async function findFilesByNames(dirHandle, names, depth = 0) {
  const want = {};
  (names || []).forEach(n => { if (n) want[n] = true; });
  const found = {};
  if (!dirHandle || !Object.keys(want).length) return found;

  for await (const entry of dirHandle.values()) {
    if (entry.kind === "file" && want[entry.name] && !found[entry.name]) {
      try {
        found[entry.name] = await entry.getFile();
      } catch (e) { /* skip */ }
    } else if (entry.kind === "directory" && depth < 3) {
      const sub = await findFilesByNames(entry, Object.keys(want), depth + 1);
      Object.keys(sub).forEach(k => { if (!found[k]) found[k] = sub[k]; });
    }
  }
  return found;
}

/** 收集文件夹内媒体文件（含子目录，最多 2 层） */
export async function collectMediaFromDir(dirHandle, depth = 0) {
  const out = [];
  if (!dirHandle) return out;
  for await (const entry of dirHandle.values()) {
    if (entry.kind === "file" && isMediaName(entry.name)) {
      try {
        const file = await entry.getFile();
        out.push(file);
      } catch (e) { /* skip */ }
    } else if (entry.kind === "directory" && depth < 2) {
      const sub = await collectMediaFromDir(entry, depth + 1);
      out.push.apply(out, sub);
    }
  }
  return out;
}
