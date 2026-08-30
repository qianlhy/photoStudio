<template>
  <div>
    <!-- 头像模式 -->
    <el-upload
        v-if="avatar"
        ref="upload"
        class="avatar-uploader"
        :action="getActionUrl"
        :show-file-list="false"
        :multiple="false"
        :limit="1"
        :headers="myHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadErr"
        :before-upload="handleBeforeUpload"
    >
      <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar-img" alt="">
      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
    </el-upload>

    <!-- 视频模式：不走 picture-card，避免 mp4 裂图 -->
    <div v-else-if="isVideoMode" class="video-uploader">
      <el-upload
          ref="upload"
          :action="getActionUrl"
          :show-file-list="false"
          :multiple="false"
          :limit="1"
          :headers="myHeaders"
          :accept="accept || 'video/*,.mp4,.mov,.avi,.mkv,.webm'"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadErr"
          :before-upload="handleBeforeUpload"
      >
        <el-button size="small" type="primary" icon="el-icon-upload2">选择视频</el-button>
        <div slot="tip" class="el-upload__tip tip-text">{{ tip || '支持 mp4 等常见视频格式' }}</div>
      </el-upload>
      <div v-if="fileUrlList.length" class="video-card">
        <div class="video-card-main">
          <i class="el-icon-video-camera-solid video-ico"></i>
          <div class="video-meta">
            <div class="video-name">{{ displayName(fileUrlList[0]) }}</div>
            <div class="video-ok">上传成功</div>
          </div>
        </div>
        <div class="video-actions">
          <el-button type="text" @click="previewVideo">预览</el-button>
          <el-button type="text" style="color:#F56C6C" @click="clearVideo">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 默认图片卡片模式 -->
    <template v-else>
      <el-upload
          ref="upload"
          :action="getActionUrl"
          list-type="picture-card"
          :multiple="multiple"
          :limit="limit"
          :headers="myHeaders"
          :file-list="fileList"
          :on-exceed="handleExceed"
          :on-preview="handleUploadPreview"
          :on-remove="handleRemove"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadErr"
          :before-upload="handleBeforeUpload"
      >
        <i class="el-icon-plus"></i>
        <div slot="tip" class="el-upload__tip" style="color:#838fa1;">{{ tip }}</div>
      </el-upload>
      <el-dialog :visible.sync="dialogVisible" size="tiny" append-to-body>
        <img width="100%" :src="dialogImageUrl" alt>
      </el-dialog>
    </template>
    <div v-if="avatar && tip" class="el-upload__tip avatar-tip">{{ tip }}</div>
  </div>
</template>
<script>
import storage from "@/utils/storage";

export default {
  data() {
    return {
      dialogVisible: false,
      dialogImageUrl: "",
      fileList: [],
      fileUrlList: [],
      myHeaders: {}
    };
  },
  props: {
    tip: String,
    action: String,
    limit: { type: [Number, String], default: 1 },
    multiple: { type: Boolean, default: false },
    fileUrls: String,
    avatar: { type: Boolean, default: false },
    /** image | video —— video 用文件卡片预览，避免裂图 */
    mode: { type: String, default: "image" },
    accept: String
  },
  mounted() {
    this.init();
    this.myHeaders = {
      'Token': storage.get("Token")
    }
  },
  watch: {
    fileUrls() {
      this.init();
    }
  },
  computed: {
    getActionUrl() {
      return `/${this.$base.name}/` + this.action;
    },
    avatarPreviewUrl() {
      if (!this.fileUrlList.length) return '';
      return this.toDisplayUrl(this.fileUrlList[0]);
    },
    isVideoMode() {
      return this.mode === 'video';
    }
  },
  methods: {
    init() {
      if (this.fileUrls) {
        this.fileUrlList = this.fileUrls.split(',').map(s => this.normalizeUploadPath(s)).filter(Boolean);
        this.fileList = this.fileUrlList.map((rel, index) => ({
          name: this.displayName(rel) || String(index),
          url: this.toDisplayUrl(rel)
        }));
      } else {
        this.fileUrlList = [];
        this.fileList = [];
      }
    },
    displayName(path) {
      if (!path) return '';
      const s = String(path).split('?')[0];
      const i = s.lastIndexOf('/');
      return i >= 0 ? s.slice(i + 1) : s;
    },
    handleBeforeUpload(file) {
      if (this.avatar || this.isVideoMode) {
        this.fileUrlList = [];
        this.fileList = [];
      }
      if (this.isVideoMode) {
        const ok = /video\//i.test(file.type) || /\.(mp4|mov|avi|mkv|webm|m4v)$/i.test(file.name || '');
        if (!ok) {
          this.$message.error('请上传视频文件（如 mp4）');
          return false;
        }
      }
    },
    handleUploadSuccess(res, file, fileList) {
      if (res && res.code === 0) {
        const body = file.response || res || {};
        const fname = body.file || body.data || '';
        if (!fname) {
          this.$message.error('上传成功但未返回文件路径');
          return;
        }
        const rel = this.normalizeUploadPath(fname);
        if (this.avatar || this.isVideoMode) {
          this.fileUrlList = [rel];
          this.fileList = [{ name: this.displayName(rel), url: this.toDisplayUrl(rel) }];
          this.$emit('change', rel);
          return;
        }
        fileList[fileList.length - 1]['url'] = rel;
        this.setFileList(fileList);
        this.$emit('change', this.fileUrlList.join(','));
      } else {
        this.$message.error((res && res.msg) || '上传失败');
      }
    },
    handleUploadErr() {
      this.$message.error("文件上传失败");
    },
    handleRemove(file, fileList) {
      this.setFileList(fileList);
      this.$emit("change", this.fileUrlList.join(","));
    },
    clearVideo() {
      this.fileUrlList = [];
      this.fileList = [];
      this.$emit('change', '');
    },
    previewVideo() {
      if (!this.fileUrlList.length) return;
      window.open(this.toDisplayUrl(this.fileUrlList[0]));
    },
    handleUploadPreview(file) {
      this.dialogImageUrl = this.toDisplayUrl(file.url);
      this.dialogVisible = true;
    },
    handleExceed() {
      this.$message.warning(`最多上传${this.limit}个文件`);
    },
    setFileList(fileList) {
      const fileArray = [];
      const fileUrlArray = [];
      const _this = this;
      fileList.forEach(function (item) {
        const rel = _this.normalizeUploadPath(item.url);
        if (!rel) return;
        fileArray.push({ name: item.name || _this.displayName(rel), url: _this.toDisplayUrl(rel) });
        fileUrlArray.push(rel);
      });
      this.fileList = fileArray;
      this.fileUrlList = fileUrlArray;
    },
    normalizeUploadPath(url) {
      if (!url) return '';
      let s = String(url).split('?')[0].trim();
      if (!s || s.startsWith('blob:')) return '';
      const base = this.$base.url || '';
      if (base && s.indexOf(base) === 0) {
        s = s.slice(base.length);
      }
      if (/^https?:\/\//i.test(s)) {
        try {
          const u = new URL(s);
          const prefix = '/' + this.$base.name + '/';
          if (u.pathname.indexOf(prefix) === 0) {
            s = u.pathname.slice(prefix.length);
          } else {
            s = u.pathname.replace(/^\//, '');
          }
        } catch (e) { /* keep s */ }
      }
      if (s.startsWith('/' + this.$base.name + '/')) {
        s = s.slice(('/' + this.$base.name + '/').length);
      }
      if (s.startsWith('/')) s = s.slice(1);
      if (s.indexOf('upload/') === 0) return s;
      if (s.indexOf('/') < 0) return 'upload/' + s;
      return s;
    },
    toDisplayUrl(rel) {
      if (!rel) return '';
      const path = this.normalizeUploadPath(rel);
      if (!path) return '';
      if (/^https?:\/\//i.test(path)) return path.split('?')[0];
      return '/' + this.$base.name + '/' + path;
    }
  }
};
</script>
<style lang="scss" scoped>
.avatar-uploader ::v-deep .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-uploader ::v-deep .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.avatar-img {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
.avatar-tip {
  color: #838fa1;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 6px;
}
.tip-text {
  color: #838fa1;
  margin-left: 8px;
  display: inline;
}
.video-uploader {
  width: 100%;
}
.video-card {
  margin-top: 12px;
  padding: 12px 14px;
  background: #F5F8FF;
  border: 1px solid #D6E2FF;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.video-card-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.video-ico {
  font-size: 28px;
  color: #2F6BFF;
  flex-shrink: 0;
}
.video-meta {
  min-width: 0;
}
.video-name {
  font-size: 13px;
  color: #1F2733;
  font-weight: 600;
  word-break: break-all;
}
.video-ok {
  margin-top: 2px;
  font-size: 12px;
  color: #22B07D;
}
.video-actions {
  flex-shrink: 0;
}
</style>
