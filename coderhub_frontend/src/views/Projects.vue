<template>
  <div class="projects-container">
    <!-- 公用导航栏 -->
    <NavBar :showWriteBtn="false" @search="handleNavSearch" />

    <!-- Hero区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <svg class="title-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 21V5C16 4.46957 15.7893 3.96086 15.4142 3.58579C15.0391 3.21071 14.5304 3 14 3H10C9.46957 3 8.96086 3.21071 8.58579 3.58579C8.21071 3.96086 8 4.46957 8 5V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            分享你的精彩项目
          </h1>
          <p class="hero-subtitle">
            探索优秀开源项目，展示个人作品，与全球开发者共同成长
          </p>
          <div class="hero-actions">
            <button class="btn-primary" @click="showUploadModal = true">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              上传项目
            </button>
            <button class="btn-secondary" @click="scrollToSection('showcase')">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              浏览项目
            </button>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ mockStats.totalProjects }}</span>
              <span class="stat-label">项目总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ mockStats.totalStars }}k</span>
              <span class="stat-label">获赞数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ mockStats.totalForks }}</span>
              <span class="stat-label">Fork数</span>
            </div>
          </div>
        </div>
        <div class="hero-illustration">
          <svg viewBox="0 0 500 400" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="50" y="60" width="400" height="280" rx="12" fill="url(#grad1)"/>
            <rect x="50" y="60" width="400" height="40" rx="12" fill="#34495e"/>
            <circle cx="70" cy="80" r="6" fill="#e74c3c"/>
            <circle cx="90" cy="80" r="6" fill="#f39c12"/>
            <circle cx="110" cy="80" r="6" fill="#2ecc71"/>
            <rect x="80" y="130" width="340" height="180" rx="8" fill="#ecf0f1" opacity="0.9"/>
            <rect x="100" y="150" width="150" height="60" rx="6" fill="#3498db"/>
            <rect x="270" y="150" width="130" height="60" rx="6" fill="#9b59b6"/>
            <rect x="100" y="230" width="100" height="60" rx="6" fill="#e67e22"/>
            <rect x="220" y="230" width="180" height="60" rx="6" fill="#1abc9c"/>
            <defs>
              <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#2c3e50;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#34495e;stop-opacity:1" />
              </linearGradient>
            </defs>
          </svg>
        </div>
      </div>
    </section>

    <!-- Tab切换 -->
    <section class="tabs-section">
      <div class="tabs-container">
        <div class="tabs">
          <div 
            :class="['tab-item', { 'active': activeTab === 'showcase' }]" 
            @click="switchToShowcaseTab"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>优秀开源项目</span>
            <span class="tab-count">{{ showcaseProjects.length }}</span>
          </div>
          <div 
            :class="['tab-item', { 'active': activeTab === 'community' }]" 
            @click="switchToCommunityTab"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>个人项目分享</span>
            <span class="tab-count">{{ communityProjects.length }}</span>
          </div>
        </div>
        <!-- 筛选和排序 -->
        <div class="filter-controls">
          <div class="search-box">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索项目..." 
              @input="handleSearch"
            />
          </div>
          <select v-model="sortBy" class="sort-select">
            <option value="hot">最热门</option>
            <option value="latest">最新</option>
            <option value="stars">最多Star</option>
          </select>
        </div>
      </div>
    </section>

    <!-- 优秀开源项目展示 -->
    <section v-if="activeTab === 'showcase'" class="showcase-section" id="showcase">
      <div class="section-header">
        <h2>平台推荐项目</h2>
        <p>精心挑选的优质开源项目，值得学习与借鉴</p>
      </div>
      
      <div class="projects-grid">
        <div 
          v-for="project in filteredShowcaseProjects" 
          :key="project.id" 
          class="project-card showcase-card"
          @click="viewProjectDetail(project)"
        >
          <!-- 项目预览图 -->
          <div class="project-preview">
            <img :src="project.coverImage" :alt="project.projectName" />
            <div class="project-overlay">
              <button class="btn-view">查看详情</button>
            </div>
            <!-- 推荐标签 -->
            <div class="badge-recommended">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
              </svg>
              推荐
            </div>
          </div>
          
          <!-- 项目信息 -->
          <div class="project-info">
            <div class="project-header">
              <h3 class="project-name">{{ project.projectName }}</h3>
              <a 
                :href="project.gitUrl" 
                target="_blank" 
                class="git-link"
                @click.stop
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 19C4 20.5 4 16.5 2 16M16 22V18.13C16.0375 17.6532 15.9731 17.1738 15.811 16.7238C15.6489 16.2738 15.3929 15.8634 15.06 15.52C18.2 15.17 21.5 13.98 21.5 8.52C21.4997 7.12383 20.9627 5.7812 20 4.77C20.4559 3.54851 20.4236 2.19835 19.91 1C19.91 1 18.73 0.650001 16 2.48C13.708 1.85882 11.292 1.85882 9 2.48C6.27 0.650001 5.09 1 5.09 1C4.57638 2.19835 4.54414 3.54851 5 4.77C4.03013 5.7887 3.49252 7.14346 3.5 8.55C3.5 13.97 6.8 15.16 9.94 15.55C9.611 15.89 9.35726 16.2954 9.19531 16.7399C9.03335 17.1844 8.96681 17.6581 9 18.13V22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                GitHub
              </a>
            </div>
            
            <p class="project-description">{{ project.shortDescription }}</p>
            
            <!-- 技术栈标签 -->
            <div class="tech-stack">
              <span 
                v-for="tech in project.techStacks" 
                :key="tech.id" 
                class="tech-tag"
                :style="{ background: getTechColor(tech.techName) }"
              >
                {{ tech.techName }}
              </span>
            </div>
            
            <!-- 作者信息 -->
            <div class="author-info">
              <img :src="project.author?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" :alt="project.author?.username" class="author-avatar" />
              <div class="author-details">
                <span class="author-name">{{ project.author?.username || '匿名用户' }}</span>
                <span class="author-role">{{ project.author?.role || '开发者' }}</span>
              </div>
            </div>
            
            <!-- 项目统计 -->
            <div class="project-stats">
              <div class="stat" v-if="project.githubStars">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                {{ formatNumber(project.githubStars) }} Stars
              </div>
              <div class="stat">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 16V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                {{ formatNumber(project.viewCount) }} 浏览
              </div>
              <div class="stat" v-if="project.categoryName">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M4 7V4C4 3.46957 4.21071 2.96086 4.58579 2.58579C4.96086 2.21071 5.46957 2 6 2H18C18.5304 2 19.0391 2.21071 19.4142 2.58579C19.7893 2.96086 20 3.46957 20 4V7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <rect x="3" y="7" width="18" height="15" rx="2" stroke="currentColor" stroke-width="2"/>
                </svg>
                {{ project.categoryName }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 个人项目分享 -->
    <section v-if="activeTab === 'community'" class="community-section">
      <div class="section-header">
        <h2>社区项目展示</h2>
        <p>开发者们的精彩作品与实战经验分享</p>
      </div>
      
      <div class="projects-masonry">
        <div 
          v-for="project in filteredCommunityProjects" 
          :key="project.id" 
          class="project-card community-card"
          @click="viewProjectDetail(project)"
        >
          <!-- 项目封面 -->
          <div class="project-screenshots">
            <img :src="project.coverImage" :alt="project.projectName" class="main-screenshot" />
            <div v-if="project.screenshots && project.screenshots.length > 0" class="screenshot-count">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
                <path d="M21 15L16 10L5 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ project.screenshots.length }}
            </div>
          </div>
          
          <!-- 项目内容 -->
          <div class="project-content">
            <div class="project-title-row">
              <h3 class="project-title">{{ project.projectName }}</h3>
              <span v-if="project.isOpenSource" class="badge-open">开源</span>
            </div>
            
            <p class="project-intro">{{ project.shortDescription }}</p>
            
            <!-- 技术栈 -->
            <div class="tech-tags-wrap">
              <span 
                v-for="tech in project.techStacks.slice(0, 4)" 
                :key="tech.id" 
                class="tech-badge"
              >
                {{ tech.techName }}
              </span>
              <span v-if="project.techStacks.length > 4" class="tech-more">
                +{{ project.techStacks.length - 4 }}
              </span>
            </div>
            
            <!-- 项目链接 -->
            <div class="project-links">
              <a 
                v-if="project.gitUrl" 
                :href="project.gitUrl" 
                target="_blank" 
                class="link-btn"
                @click.stop
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 19C4 20.5 4 16.5 2 16M16 22V18.13C16.0375 17.6532 15.9731 17.1738 15.811 16.7238C15.6489 16.2738 15.3929 15.8634 15.06 15.52C18.2 15.17 21.5 13.98 21.5 8.52C21.4997 7.12383 20.9627 5.7812 20 4.77C20.4559 3.54851 20.4236 2.19835 19.91 1C19.91 1 18.73 0.650001 16 2.48C13.708 1.85882 11.292 1.85882 9 2.48C6.27 0.650001 5.09 1 5.09 1C4.57638 2.19835 4.54414 3.54851 5 4.77C4.03013 5.7887 3.49252 7.14346 3.5 8.55C3.5 13.97 6.8 15.16 9.94 15.55C9.611 15.89 9.35726 16.2954 9.19531 16.7399C9.03335 17.1844 8.96681 17.6581 9 18.13V22" stroke="currentColor" stroke-width="2"/>
                </svg>
                源码
              </a>
              <a 
                v-if="project.demoUrl" 
                :href="project.demoUrl" 
                target="_blank" 
                class="link-btn"
                @click.stop
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18 13V19C18 19.5304 17.7893 20.0391 17.4142 20.4142C17.0391 20.7893 16.5304 21 16 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V8C3 7.46957 3.21071 6.96086 3.58579 6.58579C3.96086 6.21071 4.46957 6 5 6H11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M15 3H21V9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M10 14L21 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                演示
              </a>
            </div>
            
            <!-- 作者和互动 -->
            <div class="project-footer">
              <div class="author-mini">
                <img :src="project.author?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" :alt="project.author?.username" />
                <span>{{ project.author?.username || '匿名用户' }}</span>
              </div>
              <div class="interaction-stats">
                <span class="stat-mini">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  {{ project.viewCount || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 上传项目模态框 -->
    <Transition name="modal">
      <div v-if="showUploadModal" class="modal-overlay" @click="showUploadModal = false">
        <div class="modal-content upload-modal-large" @click.stop>
          <div class="modal-header">
            <h3>🚀 上传你的项目</h3>
            <button class="close-btn" @click="showUploadModal = false">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div class="modal-body">
            <form class="upload-form">
              <!-- 项目名称 -->
              <div class="form-group">
                <label>项目名称 <span class="required">*</span></label>
                <input type="text" placeholder="输入你的项目名称" v-model="uploadForm.projectName" maxlength="100" />
              </div>
              
              <!-- 简短描述 -->
              <div class="form-group">
                <label>简短描述 <span class="required">*</span></label>
                <input type="text" placeholder="一句话介绍你的项目（200字以内）" v-model="uploadForm.shortDescription" maxlength="200" />
              </div>
              
              <!-- 项目分类 -->
              <div class="form-group">
                <label>项目分类 <span class="required">*</span></label>
                <div class="category-selector">
                  <div 
                    v-for="cat in categories" 
                    :key="cat.id" 
                    :class="['category-chip', { active: uploadForm.categoryId === cat.id }]"
                    @click="selectCategory(cat)"
                  >
                    {{ cat.categoryName }}
                  </div>
                </div>
              </div>
              
              <!-- 技术栈选择 -->
              <div class="form-group" v-if="uploadForm.categoryId">
                <label>技术栈 <span class="required">*</span> <span class="hint">（最多选择10个）</span></label>
                <div class="tech-selector">
                  <div 
                    v-for="tech in availableTechStacks" 
                    :key="tech.id" 
                    :class="['tech-chip', { active: isTechSelected(tech.id), popular: tech.isPopular }]"
                    @click="toggleTechStack(tech.id)"
                  >
                    {{ tech.techName }}
                    <span v-if="tech.isPopular" class="hot-badge">热</span>
                  </div>
                </div>
                <div v-if="uploadForm.techStackIds.length > 0" class="selected-count">
                  已选择 {{ uploadForm.techStackIds.length }} 项
                </div>
              </div>
              
              <!-- README 详细描述 -->
              <div class="form-group">
                <label>详细描述 <span class="required">*</span></label>
                <div class="readme-upload">
                  <button type="button" class="btn-upload-readme" @click="triggerReadmeInput">
                    📄 上传 README.md
                  </button>
                  <input 
                    type="file" 
                    ref="readmeInput" 
                    accept=".md"
                    style="display: none"
                    @change="handleReadmeUpload"
                  />
                  <span class="readme-hint">或直接在下方编辑Markdown</span>
                </div>
                <textarea 
                  placeholder="项目详细描述（支持Markdown格式），将展示在项目详情页..." 
                  rows="6"
                  v-model="uploadForm.detailedDescription"
                ></textarea>
              </div>
              
              <!-- 封面图 -->
              <div class="form-group">
                <label>封面图 <span class="required">*</span></label>
                <div class="cover-upload">
                  <div v-if="!uploadForm.coverImage" class="upload-zone cover-zone" @click="triggerCoverInput">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>点击上传封面图</p>
                    <span class="upload-hint">建议 16:9 比例，JPG/PNG</span>
                  </div>
                  <div v-else class="cover-preview">
                    <img :src="uploadForm.coverImage" alt="cover" />
                    <button type="button" class="change-cover" @click="triggerCoverInput">更换</button>
                  </div>
                  <input 
                    type="file" 
                    ref="coverInput" 
                    accept="image/*" 
                    style="display: none"
                    @change="handleCoverUpload"
                  />
                </div>
              </div>
              
              <!-- 项目截图 -->
              <div class="form-group">
                <label>项目截图 <span class="hint">（可选，最多5张）</span></label>
                <div class="upload-area">
                  <div class="upload-zone" @click="triggerFileInput">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>点击上传项目截图</p>
                  </div>
                  <input 
                    type="file" 
                    ref="fileInput" 
                    accept="image/*" 
                    multiple 
                    style="display: none"
                    @change="handleFileUpload"
                  />
                  <div v-if="uploadForm.screenshots.length > 0" class="preview-list">
                    <div v-for="(img, index) in uploadForm.screenshots" :key="index" class="preview-item">
                      <img :src="img" alt="preview" />
                      <button type="button" class="remove-img" @click="removeScreenshot(index)">×</button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 链接区域 -->
              <div class="form-row">
                <div class="form-group half">
                  <label>Git仓库地址</label>
                  <input type="url" placeholder="https://github.com/..." v-model="uploadForm.gitUrl" />
                </div>
                <div class="form-group half">
                  <label>在线演示地址</label>
                  <input type="url" placeholder="https://demo.example.com" v-model="uploadForm.demoUrl" />
                </div>
              </div>
              
              <!-- 项目文件 -->
              <div class="form-group">
                <label>项目压缩包 <span class="hint">（可选，最大100MB）</span></label>
                <div class="file-upload">
                  <button type="button" class="btn-upload-file" @click="triggerZipInput">
                    📦 上传项目压缩包
                  </button>
                  <input 
                    type="file" 
                    ref="zipInput" 
                    accept=".zip,.rar,.7z" 
                    style="display: none"
                    @change="handleZipUpload"
                  />
                  <span v-if="uploadForm.projectFileUrl" class="file-name">
                    ✅ 已上传 ({{ (uploadForm.fileSize / 1024 / 1024).toFixed(2) }} MB)
                  </span>
                  <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
                    <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
                    <span>{{ uploadProgress }}%</span>
                  </div>
                </div>
              </div>
              
              <!-- 是否开源 -->
              <div class="form-group">
                <label>项目可见性 <span class="required">*</span></label>
                <div class="visibility-options">
                  <label class="visibility-option" :class="{ active: uploadForm.isOpenSource === 1 }">
                    <input type="radio" name="visibility" :value="1" v-model="uploadForm.isOpenSource" />
                    <div class="option-content">
                      <span class="option-icon">🌐</span>
                      <div class="option-text">
                        <strong>开源项目</strong>
                        <span class="option-desc">展示在个人项目分享区，所有人可见</span>
                      </div>
                    </div>
                  </label>
                  <label class="visibility-option" :class="{ active: uploadForm.isOpenSource === 0 }">
                    <input type="radio" name="visibility" :value="0" v-model="uploadForm.isOpenSource" />
                    <div class="option-content">
                      <span class="option-icon">🔒</span>
                      <div class="option-text">
                        <strong>私有项目</strong>
                        <span class="option-desc">仅自己可见，不会出现在社区列表</span>
                      </div>
                    </div>
                  </label>
                </div>
              </div>
            </form>
          </div>
          
          <div class="modal-footer">
            <button class="btn-cancel" @click="showUploadModal = false">取消</button>
            <button class="btn-submit" :disabled="!canSubmit || isSubmitting" @click="submitProject">
              <span v-if="isSubmitting">提交中...</span>
              <span v-else>🚀 提交项目</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { projectApi } from '@/api/projectApi'

const router = useRouter()

// ==================== 响应式数据 ====================
const activeTab = ref('showcase') // 当前标签页：showcase | community
const searchQuery = ref('') // 搜索关键词
const sortBy = ref('hot') // 排序方式：hot | latest | stars
const showUploadModal = ref(false) // 显示上传模态框
const isLoading = ref(false) // 加载状态
const isSubmitting = ref(false) // 提交状态

// 统计数据
const mockStats = ref({
  totalProjects: 0,
  totalStars: 0,
  totalForks: 0
})

// 分类和技术栈数据（从Redis缓存获取）
const categories = ref([])
const selectedCategoryId = ref(null)
const availableTechStacks = ref([])

// 上传表单
const uploadForm = ref({
  projectName: '',
  shortDescription: '',
  detailedDescription: '',
  categoryId: null,
  techStackIds: [],
  difficultyLevel: 2,
  gitUrl: '',
  demoUrl: '',
  videoUrl: '',
  coverImage: '',
  screenshots: [],
  projectFileUrl: '',
  fileSize: 0,
  isOpenSource: 1
})

const fileInput = ref(null) // 截图文件输入
const zipInput = ref(null) // ZIP文件输入
const readmeInput = ref(null) // README文件输入
const coverInput = ref(null) // 封面图输入
const uploadProgress = ref(0) // 上传进度

// ==================== 数据 ====================
// 优秀开源项目（暂时保留Mock，后续由管理员添加）
const showcaseProjects = ref([])

// 个人项目列表（从API获取）
const communityProjects = ref([])
const totalProjects = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)


// ==================== 计算属性 ====================
// 筛选后的优秀项目（前端不做过滤，数据从后端获取）
const filteredShowcaseProjects = computed(() => showcaseProjects.value)

// 筛选后的个人项目（数据从后端获取，已做分页）
const filteredCommunityProjects = computed(() => communityProjects.value)

// 已选择的分类名称
const selectedCategoryName = computed(() => {
  const cat = categories.value.find(c => c.id === selectedCategoryId.value)
  return cat ? cat.categoryName : '请选择分类'
})

// 表单验证状态
const canSubmit = computed(() => {
  return uploadForm.value.projectName.trim() &&
    uploadForm.value.shortDescription.trim() &&
    uploadForm.value.detailedDescription.trim() &&
    uploadForm.value.categoryId &&
    uploadForm.value.techStackIds.length > 0 &&
    uploadForm.value.coverImage
})

// ==================== 工具方法 ====================
// 格式化数字
function formatNumber(num) {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取技术栈颜色
function getTechColor(tech) {
  const colorMap = {
    'Vue': 'linear-gradient(135deg, #42b883 0%, #35495e 100%)',
    'Vue 3': 'linear-gradient(135deg, #42b883 0%, #35495e 100%)',
    'React': 'linear-gradient(135deg, #61dafb 0%, #20232a 100%)',
    'React Native': 'linear-gradient(135deg, #61dafb 0%, #20232a 100%)',
    'Spring Boot': 'linear-gradient(135deg, #6db33f 0%, #4a7c2f 100%)',
    'Spring Cloud': 'linear-gradient(135deg, #6db33f 0%, #4a7c2f 100%)',
    'Node.js': 'linear-gradient(135deg, #68a063 0%, #3c6e2f 100%)',
    'Python': 'linear-gradient(135deg, #3776ab 0%, #ffd343 100%)',
    'TypeScript': 'linear-gradient(135deg, #3178c6 0%, #235a97 100%)',
    'MySQL': 'linear-gradient(135deg, #4479a1 0%, #00758f 100%)',
    'MongoDB': 'linear-gradient(135deg, #4db33d 0%, #3f9a2b 100%)',
    'Redis': 'linear-gradient(135deg, #dc382d 0%, #a02e24 100%)',
    'Docker': 'linear-gradient(135deg, #2496ed 0%, #1d78b5 100%)',
    'Flutter': 'linear-gradient(135deg, #02569b 0%, #0468d7 100%)',
    'LangChain': 'linear-gradient(135deg, #ff6b6b 0%, #c92a2a 100%)',
    'FastAPI': 'linear-gradient(135deg, #009688 0%, #00695c 100%)',
  }
  
  // 模糊匹配
  for (const [key, color] of Object.entries(colorMap)) {
    if (tech.includes(key) || key.includes(tech)) {
      return color
    }
  }
  
  // 默认渐变色（与 Stitch 暖色系一致）
  return 'linear-gradient(135deg, #d97706 0%, #c2410c 100%)'
}

// 搜索处理(防抖)
let searchTimer = null
function handleSearch() {
  console.log('搜索:', searchQuery.value)
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadShowcaseProjects()
    loadCommunityProjects()
  }, 500)
}

// 导航搜索处理
function handleNavSearch(query) {
  searchQuery.value = query
}

// 滚动到指定区域
function scrollToSection(id) {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 查看项目详情
function viewProjectDetail(project) {
  console.log('查看项目:', project.projectName, 'ID:', project.id)
  router.push(`/project/${project.id}`)
}

// ==================== 数据加载 ====================
// 加载分类和技术栈
async function loadCategories() {
  try {
    const data = await projectApi.getCategories()
    categories.value = data || []
    console.log('分类加载成功:', categories.value.length)
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

// 根据分类加载技术栈
async function loadTechStacks(categoryId) {
  if (!categoryId) {
    availableTechStacks.value = []
    return
  }
  try {
    const data = await projectApi.getTechStacksByCategory(categoryId)
    availableTechStacks.value = data || []
  } catch (e) {
    console.error('加载技术栈失败:', e)
  }
}

// 切换到优秀项目标签
function switchToShowcaseTab() {
  console.log('切换到优秀开源项目标签')
  activeTab.value = 'showcase'
  console.log('当前activeTab:', activeTab.value)
}

// 切换到个人项目标签
function switchToCommunityTab() {
  console.log('切换到个人项目分享标签')
  activeTab.value = 'community'
  console.log('当前activeTab:', activeTab.value)
}

// 加载优秀开源项目
async function loadShowcaseProjects() {
  try {
    const params = {
      categoryId: selectedCategoryId.value || undefined,
      keyword: searchQuery.value || undefined,
      sortBy: sortBy.value === 'stars' ? 'views' : sortBy.value,
      page: 1,
      size: 50 // 优秀项目显示更多
    }
    console.log('🔍 请求优秀开源项目，参数:', params)
    const data = await projectApi.getShowcaseProjects(params)
    console.log('✅ 优秀开源项目响应数据:', data)
    console.log('📊 响应数据中的list:', data?.list)
    showcaseProjects.value = data?.list || []
    console.log('✨ 已加载优秀项目数量:', showcaseProjects.value.length)
    console.log('💾 showcaseProjects.value:', showcaseProjects.value)
  } catch (e) {
    console.error('❌ 加载优秀项目失败:', e)
    showcaseProjects.value = []
  }
}

// 加载个人项目
async function loadCommunityProjects() {
  isLoading.value = true
  try {
    const params = {
      categoryId: selectedCategoryId.value,
      keyword: searchQuery.value || undefined,
      sortBy: sortBy.value === 'stars' ? 'views' : sortBy.value,
      page: currentPage.value,
      size: pageSize.value
    }
    console.log('🔍 请求个人项目，参数:', params)
    const data = await projectApi.getCommunityProjects(params)
    console.log('✅ 个人项目响应数据:', data)
    communityProjects.value = data?.list || []
    totalProjects.value = data?.total || 0
    mockStats.value.totalProjects = totalProjects.value
    console.log('✨ 已加载个人项目数量:', communityProjects.value.length)
    console.log('💾 communityProjects.value:', communityProjects.value)
  } catch (e) {
    console.error('❌ 加载个人项目失败:', e)
    communityProjects.value = []
  } finally {
    isLoading.value = false
  }
}

// ==================== 上传功能 ====================
// 选择分类
function selectCategory(category) {
  uploadForm.value.categoryId = category.id
  uploadForm.value.techStackIds = []
  loadTechStacks(category.id)
}

// 切换技术栈选择
function toggleTechStack(techId) {
  const index = uploadForm.value.techStackIds.indexOf(techId)
  if (index > -1) {
    uploadForm.value.techStackIds.splice(index, 1)
  } else if (uploadForm.value.techStackIds.length < 10) {
    uploadForm.value.techStackIds.push(techId)
  }
}

// 是否已选择该技术栈
function isTechSelected(techId) {
  return uploadForm.value.techStackIds.includes(techId)
}

// 触发文件选择
function triggerFileInput() {
  fileInput.value?.click()
}

// 触发ZIP选择
function triggerZipInput() {
  zipInput.value?.click()
}

// 触发README选择
function triggerReadmeInput() {
  readmeInput.value?.click()
}

// 触发封面图选择
function triggerCoverInput() {
  coverInput.value?.click()
}

// 上传封面图
async function handleCoverUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  if (file.size > 5 * 1024 * 1024) {
    alert('封面图大小不能超过5MB')
    return
  }
  
  try {
    const url = await projectApi.uploadImage(file)
    uploadForm.value.coverImage = url
  } catch (e) {
    alert('封面图上传失败')
  }
  event.target.value = ''
}

// 处理截图上传
async function handleFileUpload(event) {
  const files = Array.from(event.target.files)
  if (files.length + uploadForm.value.screenshots.length > 5) {
    alert('最多只能上传5张截图')
    return
  }
  
  for (const file of files) {
    try {
      const url = await projectApi.uploadImage(file)
      uploadForm.value.screenshots.push(url)
    } catch (e) {
      console.error('截图上传失败:', e)
    }
  }
  event.target.value = ''
}

// 移除截图
function removeScreenshot(index) {
  uploadForm.value.screenshots.splice(index, 1)
}

// 处理README上传
async function handleReadmeUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  try {
    const result = await projectApi.uploadReadme(file)
    uploadForm.value.detailedDescription = result.content
  } catch (e) {
    alert('README解析失败')
  }
  event.target.value = ''
}

// 处理ZIP上传
async function handleZipUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  if (file.size > 100 * 1024 * 1024) {
    alert('文件大小不能超过100MB')
    return
  }
  
  try {
    uploadProgress.value = 0
    const result = await projectApi.uploadProjectFile(file, (percent) => {
      uploadProgress.value = percent
    })
    uploadForm.value.projectFileUrl = result.url
    uploadForm.value.fileSize = result.size
  } catch (e) {
    alert('文件上传失败')
  } finally {
    uploadProgress.value = 0
  }
  event.target.value = ''
}

// 提交项目
async function submitProject() {
  if (!canSubmit.value) {
    alert('请填写必填信息')
    return
  }
  
  isSubmitting.value = true
  try {
    const data = {
      projectName: uploadForm.value.projectName,
      shortDescription: uploadForm.value.shortDescription,
      detailedDescription: uploadForm.value.detailedDescription,
      categoryId: uploadForm.value.categoryId,
      techStackIds: uploadForm.value.techStackIds,
      difficultyLevel: uploadForm.value.difficultyLevel,
      gitUrl: uploadForm.value.gitUrl || null,
      demoUrl: uploadForm.value.demoUrl || null,
      videoUrl: uploadForm.value.videoUrl || null,
      coverImage: uploadForm.value.coverImage,
      screenshots: uploadForm.value.screenshots,
      projectFileUrl: uploadForm.value.projectFileUrl || null,
      fileSize: uploadForm.value.fileSize || 0,
      isOpenSource: uploadForm.value.isOpenSource
    }
    
    const projectId = await projectApi.createProject(data)
    alert('项目创建成功！')
    resetUploadForm()
    showUploadModal.value = false
    // 跳转到项目详情页
    router.push(`/project/${projectId}`)
  } catch (e) {
    alert('项目创建失败：' + e.message)
  } finally {
    isSubmitting.value = false
  }
}

// 重置上传表单
function resetUploadForm() {
  uploadForm.value = {
    projectName: '',
    shortDescription: '',
    detailedDescription: '',
    categoryId: null,
    techStackIds: [],
    difficultyLevel: 2,
    gitUrl: '',
    demoUrl: '',
    videoUrl: '',
    coverImage: '',
    screenshots: [],
    projectFileUrl: '',
    fileSize: 0,
    isOpenSource: 1
  }
  availableTechStacks.value = []
  uploadProgress.value = 0
}

// ==================== 监听器 ====================
// 监听排序方式变化
watch(sortBy, () => {
  console.log('排序方式变化:', sortBy.value)
  loadShowcaseProjects()
  loadCommunityProjects()
})

// ==================== 生命周期 ====================
onMounted(async () => {
  console.log('项目页面加载完成')
  await loadCategories()
  await loadShowcaseProjects() // 加载优秀开源项目
  await loadCommunityProjects() // 加载个人项目
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&family=JetBrains+Mono&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

:global(:root) {
  --primary: #c2410c;
  --accent: #d97706;
  --background: #faf7f2;
  --surface: #f3eee5;
  --text-main: #2d2a26;
  --text-muted: #7c7267;
  --border-warm: #e8e2d9;
  --golden-glow: rgba(251, 191, 36, 0.25);
}

:global(.material-symbols-outlined) {
  font-variation-settings: 'FILL' 0, 'wght' 500, 'GRAD' 0, 'opsz' 24;
}

/* ==================== 基础容器 ==================== */
.projects-container {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
}

/* ==================== Hero 区域 ==================== */
.hero-section {
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  padding: 80px 20px;
  color: white;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.3;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 60px;
  position: relative;
  z-index: 1;
}

.hero-text {
  flex: 1;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 16px;
  line-height: 1.2;
}

.title-icon {
  width: 56px;
  height: 56px;
  stroke-width: 2.5;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.95;
  margin: 0 0 40px 0;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 50px;
}

.btn-primary,
.btn-secondary {
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 184, 148, 0.4);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.3);
}

.btn-primary svg {
  width: 20px;
  height: 20px;
}

.btn-secondary {
  background: rgba(255,255,255,0.15);
  color: white;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.3);
}

.btn-secondary:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-2px);
}

.btn-secondary svg {
  width: 20px;
  height: 20px;
}

.hero-stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.hero-illustration {
  flex: 0 0 500px;
}

.hero-illustration svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 10px 30px rgba(0,0,0,0.2));
}

/* ==================== Tabs 区域 ==================== */
.tabs-section {
  background: white;
  border-bottom: 1px solid #e9ecef;
  position: sticky;
  top: 64px;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.tabs-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid #f1f3f5;
  margin-bottom: 20px;
}

.tab-item {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  color: #6c757d;
  font-weight: 600;
  position: relative;
  top: 2px;
}

.tab-item svg {
  width: 20px;
  height: 20px;
}

.tab-item:hover {
  color: #00b894;
  background: #f8f9fa;
}

.tab-item.active {
  color: #00b894;
  border-bottom-color: #00b894;
}

.tab-count {
  background: #e9ecef;
  color: #6c757d;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.tab-item.active .tab-count {
  background: #00b894;
  color: white;
}

.filter-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  gap: 16px;
}

.search-box {
  flex: 1;
  max-width: 400px;
  position: relative;
  display: flex;
  align-items: center;
}

.search-box svg {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  color: #adb5bd;
}

.search-box input {
  width: 100%;
  padding: 12px 16px 12px 48px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #00b894;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.sort-select {
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.sort-select:hover {
  border-color: #00b894;
}

.sort-select:focus {
  outline: none;
  border-color: #00b894;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* ==================== 区块标题 ==================== */
.section-header {
  max-width: 1200px;
  margin: 60px auto 40px auto;
  padding: 0 20px;
  text-align: center;
}

.section-header h2 {
  font-size: 36px;
  font-weight: 800;
  color: #212529;
  margin: 0 0 12px 0;
}

.section-header p {
  font-size: 18px;
  color: #6c757d;
  margin: 0;
}

/* ==================== 优秀开源项目 Grid ==================== */
.showcase-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 80px 20px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 32px;
}

.showcase-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.showcase-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.project-preview {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: #f8f9fa;
}

.project-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.showcase-card:hover .project-preview img {
  transform: scale(1.05);
}

.project-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 24px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.showcase-card:hover .project-overlay {
  opacity: 1;
}

.btn-view {
  padding: 10px 24px;
  background: white;
  color: #00b894;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-view:hover {
  background: #00b894;
  color: white;
  transform: scale(1.05);
}

.badge-recommended {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.badge-recommended svg {
  width: 14px;
  height: 14px;
}

.project-info {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.project-name {
  font-size: 20px;
  font-weight: 700;
  color: #212529;
  margin: 0;
  flex: 1;
  line-height: 1.3;
}

.git-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #f8f9fa;
  color: #495057;
  text-decoration: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.git-link svg {
  width: 16px;
  height: 16px;
}

.git-link:hover {
  background: #212529;
  color: white;
  transform: scale(1.05);
}

.project-description {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  white-space: nowrap;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f3f5;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #212529;
}

.author-role {
  font-size: 12px;
  color: #6c757d;
}

.project-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f1f3f5;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #6c757d;
}

.stat svg {
  width: 16px;
  height: 16px;
}

/* ==================== 个人项目瀑布流 ==================== */
.community-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 80px 20px;
}

.projects-masonry {
  column-count: 3;
  column-gap: 24px;
}

.community-card {
  break-inside: avoid;
  margin-bottom: 24px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.community-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.12);
}

.project-screenshots {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: #f8f9fa;
}

.main-screenshot {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.community-card:hover .main-screenshot {
  transform: scale(1.05);
}

.screenshot-count {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(10px);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.screenshot-count svg {
  width: 14px;
  height: 14px;
}

.project-content {
  padding: 20px;
}

.project-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.project-title {
  font-size: 18px;
  font-weight: 700;
  color: #212529;
  margin: 0;
  line-height: 1.3;
  flex: 1;
}

.badge-open {
  padding: 4px 10px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.project-intro {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tech-tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.tech-badge {
  padding: 4px 10px;
  background: #f8f9fa;
  color: #495057;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #e9ecef;
}

.tech-more {
  padding: 4px 10px;
  background: #e9ecef;
  color: #6c757d;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.project-links {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.link-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f8f9fa;
  color: #495057;
  text-decoration: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.link-btn svg {
  width: 16px;
  height: 16px;
}

.link-btn:hover {
  background: #00b894;
  color: white;
  transform: scale(1.02);
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f1f3f5;
}

.author-mini {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-mini img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.author-mini span {
  font-size: 13px;
  font-weight: 600;
  color: #495057;
}

.interaction-stats {
  display: flex;
  gap: 16px;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #6c757d;
}

.stat-mini svg {
  width: 16px;
  height: 16px;
}

/* ==================== 上传模态框 ==================== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.modal-content.upload-modal-large {
  max-width: 800px;
}

.modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #212529;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f8f9fa;
  color: #6c757d;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #e9ecef;
  transform: rotate(90deg);
}

.close-btn svg {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 32px;
  overflow-y: auto;
  flex: 1;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #495057;
}

.required {
  color: #e74c3c;
}

.form-group input,
.form-group textarea {
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #00b894;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.tech-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tech-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.tech-tag-item button {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s ease;
}

.tech-tag-item button:hover {
  background: rgba(255,255,255,0.2);
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-zone {
  border: 2px dashed #ced4da;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.upload-zone:hover {
  border-color: #00b894;
  background: rgba(102, 126, 234, 0.05);
}

.upload-zone svg {
  width: 48px;
  height: 48px;
  color: #adb5bd;
  margin-bottom: 16px;
}

.upload-zone p {
  font-size: 16px;
  font-weight: 600;
  color: #495057;
  margin: 0 0 8px 0;
}

.upload-hint {
  font-size: 13px;
  color: #6c757d;
}

.preview-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.preview-item {
  position: relative;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e9ecef;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(4px);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.preview-item:hover .remove-img {
  opacity: 1;
}

.remove-img svg {
  width: 12px;
  height: 12px;
}

.file-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-upload-file {
  padding: 10px 20px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #495057;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-upload-file:hover {
  background: #00b894;
  color: white;
  border-color: #00b894;
}

.btn-upload-file svg {
  width: 18px;
  height: 18px;
}

.file-name {
  font-size: 13px;
  color: #6c757d;
}

/* 分类选择器 */
.category-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-chip {
  padding: 10px 18px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-chip:hover {
  border-color: #00b894;
  color: #00b894;
}

.category-chip.active {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  border-color: transparent;
}

/* 技术栈选择器 */
.tech-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px;
}

.tech-chip {
  padding: 8px 14px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #495057;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.tech-chip:hover {
  border-color: #00b894;
}

.tech-chip.active {
  background: #00b894;
  color: white;
  border-color: #00b894;
}

.tech-chip.popular {
  border-color: #ffd43b;
}

.hot-badge {
  font-size: 10px;
  padding: 2px 5px;
  background: #ff6b6b;
  color: white;
  border-radius: 10px;
  margin-left: 4px;
}

.selected-count {
  font-size: 13px;
  color: #00b894;
  margin-top: 8px;
  font-weight: 500;
}

.hint {
  font-weight: 400;
  color: #adb5bd;
  font-size: 12px;
}

/* README上传 */
.readme-upload {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.btn-upload-readme {
  padding: 10px 20px;
  background: #f8f9fa;
  border: 2px dashed #dee2e6;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-upload-readme:hover {
  border-color: #00b894;
  color: #00b894;
}

.readme-hint {
  font-size: 13px;
  color: #adb5bd;
}

/* 封面图上传 */
.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-zone {
  height: 160px;
}

.cover-preview {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.change-cover {
  position: absolute;
  bottom: 10px;
  right: 10px;
  padding: 8px 16px;
  background: rgba(0,0,0,0.7);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.change-cover:hover {
  background: rgba(0,0,0,0.9);
}

/* 表单行 */
.form-row {
  display: flex;
  gap: 16px;
}

.form-group.half {
  flex: 1;
}

/* 上传进度 */
.upload-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  height: 6px;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

/* 可见性选项 */
.visibility-options {
  display: flex;
  gap: 12px;
}

.visibility-option {
  flex: 1;
  position: relative;
  cursor: pointer;
}

.visibility-option input[type="radio"] {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.visibility-option .option-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.visibility-option:hover .option-content {
  border-color: #00b894;
  background: #fff;
}

.visibility-option.active .option-content {
  border-color: #00b894;
  background: linear-gradient(135deg, rgba(0, 184, 148, 0.1) 0%, rgba(0, 160, 133, 0.1) 100%);
  box-shadow: 0 0 0 3px rgba(0, 184, 148, 0.1);
}

.visibility-option .option-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.visibility-option .option-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.visibility-option .option-text strong {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.visibility-option .option-desc {
  font-size: 12px;
  color: #6c757d;
  line-height: 1.4;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.checkbox-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #495057;
}

.checkbox-text svg {
  width: 18px;
  height: 18px;
  color: #11998e;
}

.modal-footer {
  padding: 20px 32px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 28px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-cancel {
  background: #f8f9fa;
  color: #6c757d;
}

.btn-cancel:hover {
  background: #e9ecef;
}

.btn-submit {
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 184, 148, 0.4);
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 184, 148, 0.5);
}

.btn-submit svg {
  width: 18px;
  height: 18px;
}

/* ==================== Stitch 暖色系覆盖 ==================== */
.projects-container {
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

.hero-section {
  background: linear-gradient(135deg, #fff7ee 0%, #f8eada 100%);
  color: #1f2937;
  border-bottom: 1px solid var(--border-warm);
}

.hero-section::before {
  opacity: 0.08;
}

.hero-content {
  max-width: 1440px;
}

.hero-title {
  font-family: 'Crimson Pro', serif;
  color: #1f2937;
}

.hero-subtitle {
  font-family: 'Crimson Pro', serif;
  color: #6b6359;
}

.title-icon {
  color: var(--primary);
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%);
  box-shadow: 0 8px 20px rgba(217, 119, 6, 0.25);
}

.btn-primary:hover {
  box-shadow: 0 10px 26px rgba(217, 119, 6, 0.35);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.85);
  color: var(--primary);
  border: 2px solid var(--border-warm);
  backdrop-filter: none;
}

.btn-secondary:hover {
  background: #ffffff;
}

.stat-number {
  color: var(--primary);
}

.stat-label {
  color: var(--text-muted);
}

.tabs-section {
  background: var(--background);
  border-bottom: 1px solid var(--border-warm);
  box-shadow: 0 2px 12px rgba(148, 163, 184, 0.12);
}

.tabs-container {
  max-width: 1440px;
}

.tabs {
  border-bottom-color: var(--border-warm);
}

.tab-item {
  color: var(--text-muted);
}

.tab-item:hover {
  color: var(--primary);
  background: var(--surface);
}

.tab-item.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.tab-count {
  background: var(--surface);
  color: var(--text-muted);
}

.tab-item.active .tab-count {
  background: var(--primary);
  color: #ffffff;
}

.search-box input {
  border-color: var(--border-warm);
  background: #ffffff;
}

.search-box input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.12);
}

.sort-select {
  border-color: var(--border-warm);
  color: #3f3a35;
}

.sort-select:hover,
.sort-select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.12);
}

.section-header h2 {
  font-family: 'Crimson Pro', serif;
  color: #1f2937;
}

.section-header p {
  color: var(--text-muted);
  font-family: 'Crimson Pro', serif;
}

.showcase-card,
.community-card {
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
  border-radius: 20px;
}

.showcase-card:hover,
.community-card:hover {
  box-shadow: 0 18px 36px rgba(194, 65, 12, 0.12);
}

.project-preview,
.project-screenshots {
  background: var(--surface);
}

.btn-view {
  background: #ffffff;
  color: var(--primary);
}

.btn-view:hover {
  background: var(--primary);
  color: #ffffff;
}

.badge-recommended {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 6px 14px rgba(217, 119, 6, 0.35);
}

.project-name,
.project-title {
  color: #2d2a26;
  font-family: 'Crimson Pro', serif;
}

.git-link {
  background: var(--surface);
  color: #6b6359;
}

.git-link:hover {
  background: #2d2a26;
  color: #ffffff;
}

.project-description,
.project-intro {
  color: var(--text-muted);
  font-family: 'Crimson Pro', serif;
}

.tech-tag {
  background: linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%);
}

.author-info,
.project-stats {
  border-top: 1px solid var(--border-warm);
}

.author-name {
  color: #2d2a26;
}

.author-role,
.stat,
.stat-mini {
  color: var(--text-muted);
}

.project-stats .stat svg,
.stat-mini svg {
  color: var(--accent);
}

.badge-open {
  background: linear-gradient(135deg, #d97706 0%, #c2410c 100%);
}

.tech-badge,
.link-btn {
  background: var(--surface);
  border: 1px solid var(--border-warm);
  color: #6b6359;
}

.tech-more {
  background: var(--border-warm);
  color: #6b6359;
}

.link-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: #ffffff;
}

.modal-content {
  border: 1px solid var(--border-warm);
}

.modal-header {
  border-bottom: 1px solid var(--border-warm);
}

.modal-header h3 {
  color: #1f2937;
  font-family: 'Crimson Pro', serif;
}

.close-btn {
  background: var(--surface);
  color: var(--text-muted);
}

.close-btn:hover {
  background: var(--border-warm);
}

.form-group label {
  color: #3f3a35;
}

.form-group input,
.form-group textarea {
  border-color: var(--border-warm);
  background: #ffffff;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.12);
}

.tech-tag-item {
  background: linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%);
}

.upload-zone {
  border-color: #d9cfc4;
  background: var(--surface);
}

.upload-zone:hover {
  border-color: var(--primary);
  background: rgba(217, 119, 6, 0.06);
}

.btn-upload-file,
.btn-upload-readme,
.btn-submit {
  background: linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%);
  box-shadow: 0 6px 16px rgba(217, 119, 6, 0.25);
}

.btn-upload-file:hover,
.btn-upload-readme:hover,
.btn-submit:hover {
  box-shadow: 0 8px 20px rgba(217, 119, 6, 0.35);
}

.btn-cancel {
  background: var(--surface);
  color: var(--text-muted);
}

.btn-cancel:hover {
  background: var(--border-warm);
}

.visibility-option {
  border-color: var(--border-warm);
}

.visibility-option.active {
  border-color: var(--primary);
  background: rgba(217, 119, 6, 0.08);
}

.category-chip {
  border-color: var(--border-warm);
}

.category-chip.active {
  border-color: var(--primary);
  background: rgba(217, 119, 6, 0.12);
  color: #3f3a35;
}

.tech-chip.active {
  border-color: var(--primary);
  background: rgba(217, 119, 6, 0.12);
}

/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9) translateY(20px);
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1024px) {
  .projects-masonry {
    column-count: 2;
  }
  
  .hero-content {
    flex-direction: column;
    text-align: center;
  }
  
  .hero-illustration {
    flex: 0 0 auto;
    max-width: 400px;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .hero-stats {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }
  
  .projects-masonry {
    column-count: 1;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    max-width: none;
  }
  
  .tabs {
    overflow-x: auto;
  }
}
</style>


