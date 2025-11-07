<template>
  <div class="minio-test">
    <h2>MinIO大文件上传测试</h2>
    
    <div class="upload-section">
      <h3>上传视频文件</h3>
      <input 
        type="file" 
        ref="fileInput"
        accept="video/*"
        @change="handleFileSelect"
      />
      
      <div v-if="selectedFile" class="file-info">
        <p><strong>文件名：</strong>{{ selectedFile.name }}</p>
        <p><strong>文件大小：</strong>{{ formatFileSize(selectedFile.size) }}</p>
        <p><strong>文件类型：</strong>{{ selectedFile.type }}</p>
      </div>
      
      <button 
        @click="uploadToMinio" 
        :disabled="!selectedFile || uploading"
        class="upload-btn"
      >
        {{ uploading ? '上传中...' : '上传到MinIO' }}
      </button>
      
      <div v-if="uploading" class="progress-bar">
        <div class="progress" :style="{ width: uploadProgress + '%' }"></div>
        <span class="progress-text">{{ uploadProgress }}%</span>
      </div>
      
      <div v-if="uploadResult" class="result">
        <h4>上传成功！</h4>
        <p><strong>URL：</strong></p>
        <input 
          type="text" 
          :value="uploadResult" 
          readonly 
          class="url-input"
          @click="selectUrl"
        />
        <button @click="copyUrl" class="copy-btn">复制链接</button>
        
        <!-- 视频预览 -->
        <div class="video-preview">
          <h4>视频预览：</h4>
          <video 
            :src="uploadResult" 
            controls 
            class="preview-video"
          ></video>
        </div>
      </div>
      
      <div v-if="errorMsg" class="error">
        {{ errorMsg }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { uploadFileToMinio } from '@/api/admin'
import { ref } from 'vue'

const fileInput = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadResult = ref('')
const errorMsg = ref('')

// 文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  selectedFile.value = file
  uploadResult.value = ''
  errorMsg.value = ''
  uploadProgress.value = 0
  
  console.log('选择的文件：', file)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 上传到MinIO
const uploadToMinio = async () => {
  if (!selectedFile.value) return
  
  uploading.value = true
  errorMsg.value = ''
  uploadProgress.value = 0
  
  try {
    console.log('开始上传到MinIO...')
    
    const res = await uploadFileToMinio(selectedFile.value, (progressEvent) => {
      // 计算上传进度
      uploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      console.log('上传进度：', uploadProgress.value + '%')
    })
    
    console.log('上传成功：', res)
    uploadResult.value = res.data
    
  } catch (error) {
    console.error('上传失败：', error)
    errorMsg.value = '上传失败：' + (error.message || '未知错误')
  } finally {
    uploading.value = false
  }
}

// 选中URL文本
const selectUrl = (event) => {
  event.target.select()
}

// 复制URL
const copyUrl = () => {
  navigator.clipboard.writeText(uploadResult.value)
    .then(() => {
      alert('链接已复制到剪贴板！')
    })
    .catch(err => {
      console.error('复制失败：', err)
    })
}
</script>

<style scoped>
.minio-test {
  max-width: 800px;
  margin: 40px auto;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

h2 {
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

h3 {
  color: #34495e;
  margin-bottom: 20px;
}

.upload-section {
  margin-top: 20px;
}

input[type="file"] {
  display: block;
  margin-bottom: 20px;
  padding: 10px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  width: 100%;
  cursor: pointer;
}

input[type="file"]:hover {
  border-color: #42b983;
}

.file-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.file-info p {
  margin: 5px 0;
  color: #555;
}

.upload-btn {
  background: #42b983;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.upload-btn:hover:not(:disabled) {
  background: #33a06f;
}

.upload-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 30px;
  background: #f0f0f0;
  border-radius: 15px;
  margin-top: 20px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #42b983, #2c9d6f);
  transition: width 0.3s;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #333;
  font-weight: bold;
}

.result {
  margin-top: 30px;
  padding: 20px;
  background: #e7f5ef;
  border-radius: 4px;
  border-left: 4px solid #42b983;
}

.result h4 {
  color: #42b983;
  margin-bottom: 10px;
}

.url-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
  font-family: monospace;
  font-size: 14px;
}

.copy-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
}

.copy-btn:hover {
  background: #2980b9;
}

.video-preview {
  margin-top: 20px;
}

.preview-video {
  width: 100%;
  max-height: 400px;
  background: black;
  border-radius: 4px;
}

.error {
  margin-top: 20px;
  padding: 15px;
  background: #ffe6e6;
  color: #d63031;
  border-radius: 4px;
  border-left: 4px solid #d63031;
}
</style>
