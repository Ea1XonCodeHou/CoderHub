lian<template>
  <div class="tutorial-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <!-- Logo区域 -->
        <div class="nav-logo" @click="goToHome">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="8" fill="url(#gradient)" />
            <path d="M12 14L20 10L28 14V26L20 30L12 26V14Z" stroke="white" stroke-width="2" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="40" y2="40">
                <stop offset="0%" stop-color="#2c3e50" />
                <stop offset="100%" stop-color="#34495e" />
              </linearGradient>
            </defs>
          </svg>
          <span class="logo-text">CoderHub</span>
        </div>

        <!-- 导航菜单 -->
        <ul class="nav-menu">
          <li @click="goToHome">
            <a href="#">博客首页</a>
          </li>
          <li class="active">
            <a href="#">教程</a>
          </li>
          <li>
            <a href="#">项目</a>
          </li>
          <li>
            <a href="#">智能体</a>
          </li>
        </ul>

        <!-- 搜索框 -->
        <div class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 21L16.65 16.65M19 11C19 15.4183 15.4183 19 11 19C6.58172 19 3 15.4183 3 11C3 6.58172 6.58172 3 11 3C15.4183 3 19 6.58172 19 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <input 
            type="text" 
            placeholder="搜索教程..." 
            v-model="searchKeyword"
            @input="handleSearch"
          />
        </div>

        <!-- 右侧操作区 -->
        <div class="nav-right">
          <!-- 用户头像 -->
          <div class="user-avatar" @click="toggleUserMenu">
            <img :src="userInfo.avatar" alt="avatar" />
            <div v-if="showUserMenu" class="user-menu">
              <a href="#" class="menu-item" @click.prevent="goToProfile">个人主页</a>
              <a href="#" class="menu-item">我的学习</a>
              <a href="#" class="menu-item">设置</a>
              <a href="#" class="menu-item" @click.prevent="handleLogout">退出登录</a>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero区域 - 大图横幅 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <svg class="title-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 14L21 9L12 4L3 9L12 14Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M12 14L21 9V15L12 20L3 15V9L12 14Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            </svg>
            开启你的编程之旅
          </h1>
          <p class="hero-subtitle">
            从零基础到高级进阶，精心打造的系统化学习路径
          </p>
          <div class="hero-stats">
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M2 3H8C9.06087 3 10.0783 3.42143 10.8284 4.17157C11.5786 4.92172 12 5.93913 12 7V21C12 20.2044 11.6839 19.4413 11.1213 18.8787C10.5587 18.3161 9.79565 18 9 18H2V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 3H16C14.9391 3 13.9217 3.42143 13.1716 4.17157C12.4214 4.92172 12 5.93913 12 7V21C12 20.2044 12.3161 19.4413 12.8787 18.8787C13.4413 18.3161 14.2044 18 15 18H22V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <div>
                <span class="stat-number">{{ mockData.totalCourses }}+</span>
                <span class="stat-label">精品教程</span>
              </div>
            </div>
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <div>
                <span class="stat-number">{{ mockData.totalStudents }}k+</span>
                <span class="stat-label">学习人数</span>
              </div>
            </div>
            <div class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <div>
                <span class="stat-number">{{ mockData.avgRating }}</span>
                <span class="stat-label">平均评分</span>
              </div>
            </div>
          </div>
        </div>
        <div class="hero-illustration">
          <svg viewBox="0 0 500 400" fill="none" xmlns="http://www.w3.org/2000/svg">
            <!-- 代码编辑器窗口 -->
            <rect x="50" y="50" width="400" height="300" rx="10" fill="#2c3e50"/>
            <rect x="50" y="50" width="400" height="30" rx="10" fill="#34495e"/>
            <circle cx="70" cy="65" r="5" fill="#e74c3c"/>
            <circle cx="90" cy="65" r="5" fill="#f39c12"/>
            <circle cx="110" cy="65" r="5" fill="#2ecc71"/>
            
            <!-- 代码行 -->
            <rect x="70" y="100" width="180" height="8" rx="4" fill="#3498db" opacity="0.8"/>
            <rect x="70" y="120" width="240" height="8" rx="4" fill="#9b59b6" opacity="0.6"/>
            <rect x="90" y="140" width="200" height="8" rx="4" fill="#1abc9c" opacity="0.7"/>
            <rect x="90" y="160" width="160" height="8" rx="4" fill="#e67e22" opacity="0.6"/>
            <rect x="70" y="180" width="220" height="8" rx="4" fill="#3498db" opacity="0.8"/>
            
            <!-- 光标闪烁 -->
            <rect x="260" y="100" width="2" height="8" fill="#ecf0f1">
              <animate attributeName="opacity" values="1;0;1" dur="1s" repeatCount="indefinite"/>
            </rect>
            
            <!-- 悬浮图标 -->
            <g transform="translate(350, 150)">
              <circle cx="0" cy="0" r="30" fill="#3498db" opacity="0.2">
                <animateTransform attributeName="transform" type="scale" values="1;1.1;1" dur="2s" repeatCount="indefinite"/>
              </circle>
              <path d="M-10,-10 L10,-10 L10,10 L-10,10 Z" fill="#3498db"/>
            </g>
            
            <g transform="translate(380, 250)">
              <circle cx="0" cy="0" r="25" fill="#9b59b6" opacity="0.2">
                <animateTransform attributeName="transform" type="scale" values="1;1.2;1" dur="2.5s" repeatCount="indefinite"/>
              </circle>
              <circle cx="0" cy="0" r="8" fill="#9b59b6"/>
            </g>
          </svg>
        </div>
      </div>
    </section>

    <!-- 筛选栏 -->
    <section class="filter-section">
      <div class="filter-container">
        <!-- 分类筛选 -->
        <div class="filter-group">
          <label class="filter-label">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 6H20M4 12H20M4 18H20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            分类：
          </label>
          <div class="filter-chips">
            <span 
              v-for="category in categories" 
              :key="category.id"
              :class="['chip', { 'active': selectedCategory === category.id }]"
              @click="selectCategory(category.id)"
            >
              {{ category.name }}
            </span>
          </div>
        </div>

        <!-- 难度筛选 -->
        <div class="filter-group">
          <label class="filter-label">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            难度：
          </label>
          <div class="filter-chips">
            <span 
              v-for="level in difficultyLevels" 
              :key="level.id"
              :class="['chip', level.color, { 'active': selectedDifficulty === level.id }]"
              @click="selectDifficulty(level.id)"
            >
              <svg v-if="level.id !== 'all'" class="chip-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ level.name }}
            </span>
          </div>
        </div>

        <!-- 排序方式 -->
        <div class="filter-group sort-group">
          <label class="filter-label">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 6H21M3 12H15M3 18H9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            排序：
          </label>
          <select v-model="sortType" class="sort-select">
            <option value="hot">最热门</option>
            <option value="latest">最新发布</option>
            <option value="rating">评分最高</option>
            <option value="students">学习人数</option>
          </select>
        </div>
      </div>
    </section>

    <!-- 教程列表主体 -->
    <section class="content-section">
      <div class="content-container">
        <!-- 左侧教程卡片列表 -->
        <div class="courses-grid">
          <div 
            v-for="course in filteredCourses" 
            :key="course.id"
            class="course-card"
            @click="viewCourse(course.id)"
          >
            <!-- 课程缩略图 -->
            <div class="course-thumbnail">
              <img :src="course.thumbnail" :alt="course.title" />
              <div class="course-badge" :class="course.difficulty">
                {{ getDifficultyText(course.difficulty) }}
              </div>
              <div class="course-duration">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                {{ course.duration }}
              </div>
            </div>

            <!-- 课程信息 -->
            <div class="course-info">
              <div class="course-header">
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-category">{{ course.category }}</div>
              </div>

              <p class="course-description">{{ course.description }}</p>

              <!-- 讲师信息 -->
              <div class="instructor-info">
                <img :src="course.instructor.avatar" :alt="course.instructor.name" class="instructor-avatar" />
                <div class="instructor-details">
                  <span class="instructor-name">{{ course.instructor.name }}</span>
                  <span class="instructor-title">{{ course.instructor.title }}</span>
                </div>
              </div>

              <!-- 课程统计 -->
              <div class="course-stats">
                <div class="stat">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  {{ formatNumber(course.students) }}
                </div>
                <div class="stat">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                  </svg>
                  {{ course.rating }}
                </div>
                <div class="stat">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.7088 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ course.chapters }}章节
                </div>
              </div>

              <!-- 课程标签 -->
              <div class="course-tags">
                <span v-for="tag in course.tags" :key="tag" class="tag">{{ tag }}</span>
              </div>

              <!-- 底部操作栏 -->
              <div class="course-footer">
                <span class="course-price" v-if="course.price === 0">免费</span>
                <span class="course-price" v-else>¥{{ course.price }}</span>
                <button class="btn-enroll">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  立即学习
                </button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredCourses.length === 0" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>暂无匹配的教程</p>
            <button class="btn-reset" @click="resetFilters">重置筛选条件</button>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <aside class="sidebar">
          <!-- 学习路径推荐 -->
          <div class="sidebar-card">
            <h3 class="sidebar-title">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 16V8C20.9996 7.64927 20.9071 7.30481 20.7315 7.00116C20.556 6.69751 20.3037 6.44536 20 6.27L13 2.27C12.696 2.09446 12.3511 2.00205 12 2.00205C11.6489 2.00205 11.304 2.09446 11 2.27L4 6.27C3.69626 6.44536 3.44398 6.69751 3.26846 7.00116C3.09294 7.30481 3.00036 7.64927 3 8V16C3.00036 16.3507 3.09294 16.6952 3.26846 16.9988C3.44398 17.3025 3.69626 17.5546 4 17.73L11 21.73C11.304 21.9055 11.6489 21.9979 12 21.9979C12.3511 21.9979 12.696 21.9055 13 21.73L20 17.73C20.3037 17.5546 20.556 17.3025 20.7315 16.9988C20.9071 16.6952 20.9996 16.3507 21 16Z" stroke="currentColor" stroke-width="2"/>
                <path d="M3.27 6.96L12 12.01L20.73 6.96M12 22.08V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              学习路径推荐
            </h3>
            <div class="learning-paths">
              <div 
                v-for="path in learningPaths" 
                :key="path.id"
                class="path-item"
              >
                <div class="path-icon" :style="{ background: path.color }">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.7088 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="white" stroke-width="2" stroke-linecap="round"/>
                    <path d="M22 4L12 14.01L9 11.01" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <div class="path-info">
                  <h4>{{ path.name }}</h4>
                  <p>{{ path.courses }}门课程</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 热门讲师 -->
          <div class="sidebar-card">
            <h3 class="sidebar-title">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              热门讲师
            </h3>
            <div class="instructors-list">
              <div 
                v-for="instructor in topInstructors" 
                :key="instructor.id"
                class="instructor-item"
              >
                <img :src="instructor.avatar" :alt="instructor.name" />
                <div class="instructor-data">
                  <h4>{{ instructor.name }}</h4>
                  <p>{{ instructor.students }}名学员</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 最新动态 -->
          <div class="sidebar-card">
            <h3 class="sidebar-title">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              最新动态
            </h3>
            <div class="activities-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-dot"></div>
                <div class="activity-content">
                  <p>{{ activity.text }}</p>
                  <span>{{ activity.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>CoderHub</h4>
            <p>专业的技术学习平台</p>
            <div class="social-links">
              <a href="#" aria-label="GitHub">
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.17 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.463-1.11-1.463-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.167 22 16.418 22 12c0-5.523-4.477-10-10-10z"/>
                </svg>
              </a>
              <a href="#" aria-label="Twitter">
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M23 3a10.9 10.9 0 01-3.14 1.53 4.48 4.48 0 00-7.86 3v1A10.66 10.66 0 013 4s-4 9 5 13a11.64 11.64 0 01-7 2c9 5 20 0 20-11.5a4.5 4.5 0 00-.08-.83A7.72 7.72 0 0023 3z"/>
                </svg>
              </a>
            </div>
          </div>
          <div class="footer-section">
            <h4>学习中心</h4>
            <a href="#">前端开发</a>
            <a href="#">后端开发</a>
            <a href="#">移动开发</a>
            <a href="#">数据库</a>
          </div>
          <div class="footer-section">
            <h4>关于我们</h4>
            <a href="#">关于CoderHub</a>
            <a href="#">联系方式</a>
            <a href="#">加入我们</a>
            <a href="#">用户协议</a>
          </div>
          <div class="footer-section">
            <h4>帮助支持</h4>
            <a href="#">使用帮助</a>
            <a href="#">常见问题</a>
            <a href="#">反馈建议</a>
          </div>
        </div>
        <div class="footer-copyright">
          <p>© 2024 CoderHub. All rights reserved.</p>
          <p>让学习成为一种习惯</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { getTutorialList } from '@/api/user'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const showUserMenu = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// Mock数据统计
const mockData = ref({
  totalCourses: 156,
  totalStudents: 89,
  avgRating: 4.8
})

// 分类列表
const categories = ref([
  { id: 'all', name: '全部' }
])

// 分类ID到名称的映射
const categoryMap = ref(new Map())

// 难度等级
const difficultyLevels = ref([
  { id: 'all', name: '全部难度', color: '' },
  { id: 'beginner', name: '入门', color: 'easy' },
  { id: 'intermediate', name: '进阶', color: 'medium' },
  { id: 'advanced', name: '高级', color: 'hard' }
])

// 选中的筛选条件
const selectedCategory = ref('all')
const selectedDifficulty = ref('all')
const sortType = ref('hot')

// 教程数据
const courses = ref([])
const loading = ref(false)
const total = ref(0)

// 学习路径推荐
const learningPaths = ref([
  { id: 1, name: '前端工程师', courses: 12, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, name: '后端工程师', courses: 15, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, name: '全栈开发', courses: 20, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { id: 4, name: '算法工程师', courses: 18, color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
])

// 热门讲师
const topInstructors = ref([
  { id: 1, name: '张晓明', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', students: 12580 },
  { id: 2, name: '李华', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', students: 15230 },
  { id: 3, name: '王芳', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', students: 8920 },
  { id: 4, name: '刘强', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', students: 11450 }
])

// 最新动态
const recentActivities = ref([
  { id: 1, text: '新增《AI大模型应用开发》课程', time: '2小时前' },
  { id: 2, text: '《Vue 3实战》课程更新至第10章', time: '5小时前' },
  { id: 3, text: '张晓明讲师直播答疑中', time: '1天前' },
  { id: 4, text: '618学习节优惠活动开启', time: '2天前' }
])

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 1 && res.data) {
      // 构建分类列表（只显示启用的一级分类）
      const activeCategories = res.data
        .filter(cat => cat.status === 1)
        .map(cat => ({
          id: cat.id,
          name: cat.name
        }))
      
      categories.value = [
        { id: 'all', name: '全部' },
        ...activeCategories
      ]
      
      // 构建分类映射
      categoryMap.value.clear()
      res.data.forEach(cat => {
        categoryMap.value.set(cat.id, cat.name)
      })
      
      console.log('加载分类成功：', categories.value.length - 1, '个')
    }
  } catch (error) {
    console.error('加载分类列表失败：', error)
  }
}

// 加载教程列表
const loadTutorials = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      pageSize: 100 // 先加载全部数据，前端自己做筛选和分页
    }
    
    const res = await getTutorialList(params)
    if (res.code === 1 && res.data) {
      // 转换后端数据格式为前端需要的格式
      courses.value = res.data.records.map(item => ({
        id: item.id,
        title: item.title,
        description: item.description,
        thumbnail: item.coverImage || 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=250&fit=crop',
        category: getCategoryName(item.categoryId),
        categoryId: item.categoryId,
        difficulty: getDifficultyKey(item.difficulty),
        duration: '待更新', // 后端暂无此字段
        rating: item.rating || 0,
        students: item.studentCount || 0,
        chapters: item.chapterCount || 0,
        price: item.price || 0,
        tags: [], // 后端暂无标签
        instructor: {
          name: item.instructorName || '匿名',
          title: item.instructorDesc || '讲师',
          avatar: item.instructorAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
        }
      }))
      total.value = res.data.total
      mockData.value.totalCourses = res.data.total
    }
  } catch (error) {
    console.error('加载教程列表失败：', error)
  } finally {
    loading.value = false
  }
}

// 将后端难度值转换为前端需要的 key
const getDifficultyKey = (difficulty) => {
  const map = {
    0: 'beginner',
    1: 'intermediate',
    2: 'advanced'
  }
  return map[difficulty] || 'beginner'
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  return categoryMap.value.get(categoryId) || '未分类'
}

// 过滤后的课程列表
const filteredCourses = computed(() => {
  let result = courses.value

  // 分类筛选
  if (selectedCategory.value !== 'all') {
    result = result.filter(c => c.categoryId === selectedCategory.value)
  }

  // 难度筛选
  if (selectedDifficulty.value !== 'all') {
    result = result.filter(c => c.difficulty === selectedDifficulty.value)
  }

  // 搜索关键词
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(c => 
      c.title.toLowerCase().includes(keyword) ||
      c.description.toLowerCase().includes(keyword) ||
      c.tags.some(tag => tag.toLowerCase().includes(keyword))
    )
  }

  // 排序
  if (sortType.value === 'hot') {
    result = result.sort((a, b) => b.students - a.students)
  } else if (sortType.value === 'latest') {
    result = result.sort((a, b) => b.id - a.id)
  } else if (sortType.value === 'rating') {
    result = result.sort((a, b) => b.rating - a.rating)
  } else if (sortType.value === 'students') {
    result = result.sort((a, b) => b.students - a.students)
  }

  return result
})

// 方法
const goToHome = () => {
  router.push('/home')
}

const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  showUserMenu.value = false
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

const handleSearch = () => {
  // 搜索会自动触发computed重新计算
}

const selectCategory = (id) => {
  selectedCategory.value = id
}

const selectDifficulty = (id) => {
  selectedDifficulty.value = id
}

const viewCourse = (id) => {
  console.log('查看课程:', id)
  router.push(`/tutorial/${id}`)
}

const resetFilters = () => {
  selectedCategory.value = 'all'
  selectedDifficulty.value = 'all'
  searchKeyword.value = ''
  sortType.value = 'hot'
}

const getDifficultyText = (difficulty) => {
  const map = {
    'beginner': '入门',
    'intermediate': '进阶',
    'advanced': '高级'
  }
  return map[difficulty] || difficulty
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 页面加载
onMounted(async () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
    // 先加载分类，再加载教程
    await loadCategories()
    await loadTutorials()
  } else {
    router.push('/')
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.tutorial-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* ==================== 导航栏 ==================== */
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
}

.nav-logo svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
}

.nav-menu {
  display: flex;
  gap: 32px;
  list-style: none;
  flex-shrink: 0;
}

.nav-menu li {
  position: relative;
  cursor: pointer;
}

.nav-menu li a {
  display: block;
  padding: 8px 0;
  font-size: 15px;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}

.nav-menu li.active a {
  color: #2c3e50;
  font-weight: 600;
}

.nav-menu li.active::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 0;
  right: 0;
  height: 3px;
  background: #2c3e50;
}

.nav-menu li:hover a {
  color: #2c3e50;
}

/* 搜索框 */
.search-box {
  flex: 1;
  max-width: 400px;
  position: relative;
  margin: 0 24px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #94a3b8;
  pointer-events: none;
}

.search-box input {
  width: 100%;
  height: 40px;
  padding: 0 16px 0 42px;
  border: 2px solid transparent;
  border-radius: 20px;
  font-size: 14px;
  color: #2c3e50;
  background: #f5f7fa;
  transition: all 0.3s;
  outline: none;
}

.search-box input:focus {
  border-color: #2c3e50;
  background: white;
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

.search-box input::placeholder {
  color: #94a3b8;
}

/* 导航右侧 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
  margin-left: auto;
}

.user-avatar {
  position: relative;
  cursor: pointer;
}

.user-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
  transition: all 0.2s;
}

.user-avatar:hover img {
  border-color: #2c3e50;
  transform: scale(1.05);
}

.user-menu {
  position: absolute;
  top: 48px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 150px;
}

.menu-item {
  display: block;
  padding: 10px 16px;
  font-size: 14px;
  color: #2c3e50;
  text-decoration: none;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f5f7fa;
}

/* ==================== Hero区域 ==================== */
.hero-section {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  padding: 60px 24px;
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
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 120"><path d="M0,0V46.29c47.79,22.2,103.59,32.17,158,28,70.36-5.37,136.33-33.31,206.8-37.5C438.64,32.43,512.34,53.67,583,72.05c69.27,18,138.3,24.88,209.4,13.08,36.15-6,69.85-17.84,104.45-29.34C989.49,25,1113-14.29,1200,52.47V0Z" opacity=".1" fill="%23ffffff"></path></svg>') no-repeat bottom;
  background-size: cover;
  opacity: 0.1;
}

.hero-content {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-text {
  animation: fadeInLeft 0.8s ease;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  width: 56px;
  height: 56px;
  color: #fbbf24;
  filter: drop-shadow(0 4px 8px rgba(251, 191, 36, 0.3));
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
  line-height: 1.6;
}

.hero-stats {
  display: flex;
  gap: 40px;
}

.hero-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-stats .stat-item svg {
  width: 32px;
  height: 32px;
  color: #fbbf24;
}

.stat-number {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.hero-illustration {
  animation: fadeInRight 0.8s ease;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.hero-illustration svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.3));
}

/* ==================== 筛选栏 ==================== */
.filter-section {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 24px 0;
  position: sticky;
  top: 64px;
  z-index: 90;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.filter-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.filter-label svg {
  width: 18px;
  height: 18px;
  color: #64748b;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  font-size: 13px;
  color: #64748b;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.chip:hover {
  color: #2c3e50;
  background: #e2e8f0;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.chip.active {
  color: white;
  background: #2c3e50;
  border-color: #2c3e50;
  font-weight: 600;
}

.chip.easy.active {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-color: #10b981;
}

.chip.medium.active {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-color: #f59e0b;
}

.chip.hard.active {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border-color: #ef4444;
}

.chip-icon {
  width: 14px;
  height: 14px;
}

.sort-group {
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.sort-select {
  padding: 6px 16px;
  font-size: 13px;
  color: #2c3e50;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-select:hover {
  border-color: #cbd5e1;
  background: #e2e8f0;
}

.sort-select:focus {
  outline: none;
  border-color: #2c3e50;
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

/* ==================== 内容区域 ==================== */
.content-section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 24px;
}

.content-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

/* 课程卡片 */
.course-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.course-thumbnail {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f7fa;
}

.course-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.course-card:hover .course-thumbnail img {
  transform: scale(1.05);
}

.course-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  border-radius: 4px;
  backdrop-filter: blur(10px);
}

.course-badge.beginner {
  background: rgba(16, 185, 129, 0.9);
}

.course-badge.intermediate {
  background: rgba(245, 158, 11, 0.9);
}

.course-badge.advanced {
  background: rgba(239, 68, 68, 0.9);
}

.course-duration {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 6px;
  backdrop-filter: blur(10px);
}

.course-duration svg {
  width: 14px;
  height: 14px;
}

.course-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-category {
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.course-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 讲师信息 */
.instructor-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.instructor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.instructor-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.instructor-title {
  font-size: 12px;
  color: #94a3b8;
}

/* 课程统计 */
.course-stats {
  display: flex;
  gap: 20px;
  padding: 12px 0;
  border-top: 1px solid #f5f7fa;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.stat svg {
  width: 16px;
  height: 16px;
  color: #fbbf24;
}

/* 课程标签 */
.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 4px 10px;
  font-size: 12px;
  color: #64748b;
  background: #f5f7fa;
  border-radius: 4px;
  transition: all 0.2s;
}

.tag:hover {
  color: #2c3e50;
  background: #e2e8f0;
}

/* 课程底部 */
.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  margin-top: auto;
  border-top: 1px solid #f5f7fa;
}

.course-price {
  font-size: 20px;
  font-weight: 700;
  color: #ef4444;
}

.course-price:has-text("免费") {
  color: #10b981;
}

.btn-enroll {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-enroll:hover {
  transform: translateX(2px);
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.3);
}

.btn-enroll svg {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;
}

.btn-enroll:hover svg {
  transform: translateX(4px);
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-state svg {
  width: 120px;
  height: 120px;
  color: #cbd5e1;
  margin-bottom: 24px;
}

.empty-state p {
  font-size: 18px;
  color: #94a3b8;
  margin-bottom: 24px;
}

.btn-reset {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  background: white;
  border: 2px solid #2c3e50;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset:hover {
  color: white;
  background: #2c3e50;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.2);
}

/* ==================== 右侧边栏 ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 172px;
  height: fit-content;
}

.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f5f7fa;
}

.sidebar-title svg {
  width: 20px;
  height: 20px;
  color: #64748b;
}

/* 学习路径 */
.learning-paths {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.path-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  transition: all 0.2s;
  cursor: pointer;
}

.path-item:hover {
  background: #f5f7fa;
  transform: translateX(4px);
}

.path-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.path-icon svg {
  width: 20px;
  height: 20px;
}

.path-info {
  flex: 1;
}

.path-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}

.path-info p {
  font-size: 12px;
  color: #94a3b8;
}

/* 热门讲师 */
.instructors-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.instructor-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.instructor-item:hover {
  background: #f8f9fa;
}

.instructor-item img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.instructor-data {
  flex: 1;
}

.instructor-data h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}

.instructor-data p {
  font-size: 12px;
  color: #94a3b8;
}

/* 最新动态 */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 12px;
  position: relative;
}

.activity-item::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 20px;
  width: 1px;
  height: calc(100% + 16px);
  background: #e2e8f0;
}

.activity-item:last-child::before {
  display: none;
}

.activity-dot {
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.activity-content {
  flex: 1;
}

.activity-content p {
  font-size: 13px;
  color: #2c3e50;
  margin-bottom: 4px;
  line-height: 1.5;
}

.activity-content span {
  font-size: 12px;
  color: #94a3b8;
}

/* ==================== 页脚 ==================== */
.page-footer {
  background: #2c3e50;
  color: white;
  padding: 48px 0 24px;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: white;
}

.footer-section p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 16px;
  line-height: 1.6;
}

.footer-section a {
  display: block;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  margin-bottom: 10px;
  transition: all 0.2s;
}

.footer-section a:hover {
  color: white;
  transform: translateX(4px);
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-links a {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
}

.social-links a:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.social-links svg {
  width: 18px;
  height: 18px;
}

.footer-copyright {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-copyright p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 4px 0;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1200px) {
  .content-container {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
  }

  .hero-illustration {
    display: none;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }

  .nav-menu {
    display: none;
  }

  .search-box {
    max-width: none;
  }

  .filter-container {
    gap: 16px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

@media (max-width: 480px) {
  .nav-content {
    padding: 0 16px;
  }

  .hero-section {
    padding: 40px 16px;
  }

  .content-section {
    padding: 24px 16px;
  }

  .course-card {
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  }
}

/* ==================== 动画效果 ==================== */
.course-card {
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 为不同位置的卡片添加延迟 */
.course-card:nth-child(1) { animation-delay: 0.1s; }
.course-card:nth-child(2) { animation-delay: 0.15s; }
.course-card:nth-child(3) { animation-delay: 0.2s; }
.course-card:nth-child(4) { animation-delay: 0.25s; }
.course-card:nth-child(5) { animation-delay: 0.3s; }
.course-card:nth-child(6) { animation-delay: 0.35s; }

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 加载占位动画 */
@keyframes shimmer {
  0% {
    background-position: -468px 0;
  }
  100% {
    background-position: 468px 0;
  }
}

.loading-skeleton {
  animation: shimmer 1.2s ease-in-out infinite;
  background: linear-gradient(to right, #f5f7fa 0%, #e2e8f0 20%, #f5f7fa 40%, #f5f7fa 100%);
  background-size: 800px 104px;
}
</style>
