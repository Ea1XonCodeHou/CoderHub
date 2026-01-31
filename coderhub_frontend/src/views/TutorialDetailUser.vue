<template>
  <div class="tutorial-detail-container">
    <!-- 顶部导航栏 - 使用统一的NavBar组件 -->
    <NavBar :showWriteBtn="false" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 主内容区 -->
    <div v-else-if="tutorial" class="main-content">
      <div class="content-grid">
        <!-- 左侧主要内容 -->
        <div class="left-column">
          <!-- 课程信息卡片 -->
          <div class="tutorial-header">
        <div class="header-content">
          <div class="header-left">
            <img :src="tutorial.coverImage" :alt="tutorial.title" class="cover-image" />
          </div>
          <div class="header-right">
            <h1 class="tutorial-title">{{ tutorial.title }}</h1>
            <p class="tutorial-description">{{ tutorial.description }}</p>
            
            <!-- 讲师信息 -->
            <div class="instructor-info">
              <img :src="tutorial.instructorAvatar" :alt="tutorial.instructorName" class="instructor-avatar" />
              <div class="instructor-details">
                <span class="instructor-name">{{ tutorial.instructorName }}</span>
                <span class="instructor-title">{{ tutorial.instructorDesc }}</span>
              </div>
            </div>

            <!-- 课程统计 -->
            <div class="tutorial-stats">
              <div class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
                  <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                </svg>
                <span>{{ formatNumber(tutorial.studentCount) }} 学员</span>
              </div>
              <div class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                </svg>
                <span>{{ tutorial.rating }} 评分</span>
              </div>
              <div class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M2 3H8C9.06087 3 10.0783 3.42143 10.8284 4.17157C11.5786 4.92172 12 5.93913 12 7V21C12 20.2044 11.6839 19.4413 11.1213 18.8787C10.5587 18.3161 9.79565 18 9 18H2V3Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M22 3H16C14.9391 3 13.9217 3.42143 13.1716 4.17157C12.4214 4.92172 12 5.93913 12 7V21C12 20.2044 12.3161 19.4413 12.8787 18.8787C13.4413 18.3161 14.2044 18 15 18H22V3Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                <span>{{ tutorial.chapterCount }} 章节</span>
              </div>
              <div class="stat-item difficulty" :class="getDifficultyClass(tutorial.difficulty)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                <span>{{ getDifficultyText(tutorial.difficulty) }}</span>
              </div>
            </div>

            <!-- 价格和操作 -->
            <div class="tutorial-actions">
              <div class="price-info">
                <span v-if="tutorial.price === 0" class="price free">免费</span>
                <span v-else class="price">¥{{ tutorial.price }}</span>
              </div>
              <button class="btn-start" @click="scrollToChapters">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M5 3L19 12L5 21V3Z" fill="currentColor"/>
                </svg>
                开始学习
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 章节列表区域 -->
      <div class="chapters-section" ref="chaptersRef">
        <div class="section-header">
          <h2>
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 6H20M4 12H20M4 18H20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            课程章节
          </h2>
          <span class="chapter-count">共 {{ chapters.length }} 个章节</span>
        </div>

        <!-- 章节列表 -->
        <div class="chapters-list">
          <div 
            v-for="(chapter, index) in chapters" 
            :key="chapter.id"
            class="chapter-item"
            :class="{ 'expanded': expandedChapters.includes(chapter.id) }"
          >
            <!-- 章节标题栏 -->
            <div 
              class="chapter-header" 
              @click="toggleChapter(chapter.id)"
              :class="{ 'locked': !isChapterFree(chapter) }"
            >
              <div class="chapter-left">
                <span class="chapter-number">第 {{ index + 1 }} 章</span>
                <h3 class="chapter-title">{{ chapter.chapterTitle }}</h3>
                <span v-if="isChapterFree(chapter)" class="free-badge">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M5 13L9 17L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  免费试看
                </span>
                <span v-else class="locked-badge">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  未解锁
                </span>
              </div>
              <div class="chapter-right">
                <span class="chapter-duration" v-if="chapter.duration">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  {{ chapter.duration }}
                </span>
                <svg class="expand-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>

            <!-- 章节内容（展开时显示） -->
            <transition name="slide">
              <div v-show="expandedChapters.includes(chapter.id)" class="chapter-content">
                <div class="loading-resources" v-if="loadingResources[chapter.id]">
                  <div class="mini-spinner"></div>
                  <span>加载资源中...</span>
                </div>

                <div v-else class="resources-container">
                  <!-- 文档列表 -->
                  <div v-if="chapterDocuments[chapter.id]?.length > 0" class="resource-section">
                    <h4 class="resource-title">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2"/>
                        <path d="M14 2V8H20" stroke="currentColor" stroke-width="2"/>
                      </svg>
                      课程文档
                    </h4>
                    <div class="resource-list">
                      <div 
                        v-for="doc in chapterDocuments[chapter.id]" 
                        :key="doc.id"
                        class="resource-item"
                        :class="{ 'locked': !isChapterFree(chapter) }"
                      >
                        <div class="resource-icon document">
                          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2"/>
                          </svg>
                        </div>
                        <div class="resource-info">
                          <span class="resource-name">{{ doc.documentTitle }}</span>
                          <span class="resource-meta">{{ doc.documentType?.toUpperCase() }} · {{ formatFileSize(doc.fileSize) }}</span>
                        </div>
                        <div class="resource-actions">
                          <button
                            v-if="isChapterFree(chapter)"
                            class="action-btn view-btn"
                            @click.stop="viewDocument(doc, chapter)"
                            title="预览文档"
                          >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2"/>
                              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            查看
                          </button>
                          <button
                            v-if="isChapterFree(chapter)"
                            class="action-btn download-btn"
                            @click.stop="downloadDocument(doc)"
                            title="下载文档"
                          >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15M7 10L12 15M12 15L17 10M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            下载
                          </button>
                          <div v-if="!isChapterFree(chapter)" class="locked-icon">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <rect x="5" y="11" width="14" height="10" rx="1" stroke="currentColor" stroke-width="2"/>
                              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2"/>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 视频列表 -->
                  <div v-if="chapterVideos[chapter.id]?.length > 0" class="resource-section">
                    <h4 class="resource-title">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 3L19 12L5 21V3Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                      </svg>
                      课程视频
                    </h4>
                    <div class="resource-list">
                      <div 
                        v-for="video in chapterVideos[chapter.id]" 
                        :key="video.id"
                        class="resource-item video-item"
                        :class="{ 'locked': !isChapterFree(chapter) }"
                      >
                        <div class="resource-icon video">
                          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M5 3L19 12L5 21V3Z" fill="currentColor"/>
                          </svg>
                        </div>
                        <div class="resource-info">
                          <span class="resource-name">{{ video.videoTitle }}</span>
                          <span class="resource-meta">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                              <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            {{ video.duration }} · {{ formatFileSize(video.fileSize) }}
                          </span>
                        </div>
                        <div class="resource-actions">
                          <template v-if="isChapterFree(chapter)">
                            <button class="btn-play" @click.stop="playVideo(video, chapter)" title="播放视频">
                              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M5 3L19 12L5 21V3Z" fill="currentColor"/>
                              </svg>
                              播放
                            </button>
                            <button class="btn-download" @click.stop="downloadVideo(video)" title="下载视频">
                              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 5V13M12 13L9 10M12 13L15 10M5 16L5 17C5 18.1046 5.89543 19 7 19L17 19C18.1046 19 19 18.1046 19 17V16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                              </svg>
                              下载
                            </button>
                          </template>
                          <div v-else class="locked-badge">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <rect x="5" y="11" width="14" height="10" rx="1" stroke="currentColor" stroke-width="2"/>
                              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            <span>已锁定</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 无资源提示 -->
                  <div v-if="!chapterDocuments[chapter.id]?.length && !chapterVideos[chapter.id]?.length" class="no-resources">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                      <path d="M12 16V12M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <p>该章节暂无学习资源</p>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <div class="section-header">
          <h2>
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            课程评论
          </h2>
          <span class="comment-count">{{ comments.length }} 条评论</span>
        </div>
        
        <!-- 评论输入框 -->
        <div class="comment-input-area">
          <div class="comment-input-wrapper">
            <img 
              :src="userInfo?.avatar || 'https://i.pravatar.cc/150?img=0'" 
              alt="用户头像" 
              class="comment-user-avatar"
            />
            <div class="comment-input-box">
              <textarea 
                v-model="commentContent"
                :placeholder="isLoggedIn ? '发表你的看法...' : '请登录后发表评论'"
                :disabled="!isLoggedIn"
                class="comment-textarea"
                rows="3"
              ></textarea>
              <div class="comment-input-actions">
                <span class="char-count">{{ commentContent.length }}/500</span>
                <button 
                  class="btn-submit-comment"
                  @click="submitComment"
                  :disabled="!commentContent.trim() || submittingComment || !isLoggedIn"
                >
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ submittingComment ? '发布中...' : '发布评论' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 评论加载状态 -->
        <div v-if="commentsLoading" class="comments-loading">
          <div class="mini-spinner"></div>
          <span>加载评论中...</span>
        </div>
        
        <!-- 评论列表 -->
        <div v-else-if="comments.length > 0" class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <!-- 主评论 -->
            <div class="comment-main">
              <img 
                :src="comment.userAvatar || 'https://i.pravatar.cc/150?img=0'" 
                :alt="comment.username"
                class="comment-avatar"
              />
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.username || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
                <div class="comment-actions">
                  <button class="action-btn-comment" @click="startReply(comment.id)">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    回复
                  </button>
                  <button 
                    v-if="isMyComment(comment)" 
                    class="action-btn-comment delete"
                    @click="handleDeleteComment(comment.id)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M3 6H5H21M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    删除
                  </button>
                </div>
                
                <!-- 回复输入框 -->
                <div v-if="replyingTo === comment.id" class="reply-input-area">
                  <textarea 
                    v-model="replyContent"
                    :placeholder="'回复 @' + comment.username"
                    class="reply-textarea"
                    rows="2"
                  ></textarea>
                  <div class="reply-actions">
                    <button class="btn-cancel-reply" @click="cancelReply">取消</button>
                    <button 
                      class="btn-submit-reply"
                      @click="submitReply(comment.id)"
                      :disabled="!replyContent.trim() || submittingComment"
                    >
                      {{ submittingComment ? '提交中...' : '提交回复' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 子回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <img 
                  :src="reply.userAvatar || 'https://i.pravatar.cc/150?img=0'" 
                  :alt="reply.username"
                  class="reply-avatar"
                />
                <div class="reply-body">
                  <div class="reply-header">
                    <span class="reply-author">{{ reply.username || '匿名用户' }}</span>
                    <span v-if="reply.replyToUsername" class="reply-to">
                      回复 <span class="reply-to-name">@{{ reply.replyToUsername }}</span>
                    </span>
                    <span class="reply-time">{{ formatCommentTime(reply.createTime) }}</span>
                  </div>
                  <p class="reply-text">{{ reply.content }}</p>
                  <div class="reply-actions-bar">
                    <button class="action-btn-comment" @click="startReply(comment.id)">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2"/>
                      </svg>
                      回复
                    </button>
                    <button 
                      v-if="isMyComment(reply)" 
                      class="action-btn-comment delete"
                      @click="handleDeleteComment(reply.id)"
                    >
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 6H5H21M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空评论状态 -->
        <div v-else class="comments-empty">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2"/>
          </svg>
          <p>快来发表第一条评论吧！</p>
        </div>
      </div>
    </div>

    <!-- 右侧边栏 -->
    <aside class="right-sidebar">
      <!-- 人气教程推荐 -->
      <div class="sidebar-card">
        <h3 class="sidebar-title">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17.657 18.657L13.414 14.414C12.2426 15.3158 10.7906 15.7925 9.30884 15.7645C7.82708 15.7366 6.39417 15.2057 5.25783 14.2617C4.12149 13.3178 3.34817 12.0164 3.06351 10.5688C2.77886 9.12122 2.9989 7.61532 3.69056 6.30453C4.38223 4.99374 5.50433 3.95569 6.87204 3.35538C8.23976 2.75507 9.77028 2.62668 11.2212 3.0012C12.6721 3.37573 13.9542 4.23738 14.8541 5.44682C15.754 6.65626 16.2172 8.14213 16.169 9.66C16.1554 10.4067 16.0165 11.1446 15.758 11.843L20 16.086L17.657 18.657ZM9.5 13.5C10.4283 13.5 11.3185 13.1313 11.9749 12.4749C12.6313 11.8185 13 10.9283 13 10C13 9.07174 12.6313 8.1815 11.9749 7.52513C11.3185 6.86875 10.4283 6.5 9.5 6.5C8.57174 6.5 7.6815 6.86875 7.02513 7.52513C6.36875 8.1815 6 9.07174 6 10C6 10.9283 6.36875 11.8185 7.02513 12.4749C7.6815 13.1313 8.57174 13.5 9.5 13.5Z" fill="currentColor"/>
          </svg>
          人气推荐
        </h3>
        <div class="recommended-courses">
          <div 
            v-for="course in mockRecommendedCourses" 
            :key="course.id"
            class="mini-course-card"
            @click="viewCourse(course.id)"
          >
            <img :src="course.thumbnail" :alt="course.title" />
            <div class="mini-course-info">
              <h4>{{ course.title }}</h4>
              <div class="mini-course-meta">
                <span class="students">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  {{ formatNumber(course.students) }}
                </span>
                <span class="rating">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                  </svg>
                  {{ course.rating }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Coder推荐语 -->
      <div class="sidebar-card">
        <h3 class="sidebar-title">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
          Coder推荐
        </h3>
        <div class="coder-testimonials">
          <div 
            v-for="testimonial in mockTestimonials" 
            :key="testimonial.id"
            class="testimonial-card"
          >
            <div class="testimonial-header">
              <img :src="testimonial.avatar" :alt="testimonial.name" />
              <div>
                <h4>{{ testimonial.name }}</h4>
                <p>{{ testimonial.title }}</p>
              </div>
            </div>
            <p class="testimonial-text">"{{ testimonial.text }}"</p>
            <div class="testimonial-footer">
              <span>{{ testimonial.company }}</span>
            </div>
          </div>
        </div>
      </div>
    </aside>
  </div>
</div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p>课程不存在或已下架</p>
      <button class="btn-back" @click="goBack">返回教程列表</button>
    </div>
  </div>
</template>

<script setup>
import { getChapterDocuments, getChapterVideos, getTutorialChapters, getTutorialDetail, getTutorialComments, publishTutorialComment, deleteTutorialComment } from '@/api/user'
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({})
const tutorial = ref(null)
const chapters = ref([])
const loading = ref(true)
const chaptersRef = ref(null)

// 章节展开状态
const expandedChapters = ref([])
// 章节资源加载状态
const loadingResources = ref({})
// 章节文档数据
const chapterDocuments = ref({})
// 章节视频数据
const chapterVideos = ref({})

// ==================== 评论相关状态 ====================
const comments = ref([])
const commentsLoading = ref(false)
const commentContent = ref('')
const submittingComment = ref(false)
const replyingTo = ref(null) // 当前回复的评论ID
const replyContent = ref('')

// 计算登录状态
const isLoggedIn = computed(() => {
  return !!userInfo.value?.id
})

// Mock 推荐课程数据
const mockRecommendedCourses = ref([
  {
    id: 'rec-1',
    title: 'Vue 3 全栈开发实战',
    thumbnail: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=250&fit=crop',
    students: 15234,
    rating: 4.8
  },
  {
    id: 'rec-2',
    title: 'Spring Boot 微服务架构',
    thumbnail: 'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=400&h=250&fit=crop',
    students: 12890,
    rating: 4.9
  },
  {
    id: 'rec-3',
    title: 'Docker 容器化实践',
    thumbnail: 'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=400&h=250&fit=crop',
    students: 9876,
    rating: 4.7
  }
])

// Mock Coder推荐语数据
const mockTestimonials = ref([
  {
    id: 'test-1',
    name: '张三',
    avatar: 'https://i.pravatar.cc/150?img=1',
    title: '全栈工程师',
    company: '阿里巴巴',
    text: '这个课程系统且全面，从基础到进阶都讲得非常清楚，强烈推荐给想要进阶的同学！'
  },
  {
    id: 'test-2',
    name: 'Lisa Wang',
    avatar: 'https://i.pravatar.cc/150?img=5',
    title: '资深架构师',
    company: '腾讯',
    text: '老师讲解深入浅出，项目实战性很强，学完后能独立完成企业级项目开发。'
  },
  {
    id: 'test-3',
    name: '李明',
    avatar: 'https://i.pravatar.cc/150?img=8',
    title: '技术总监',
    company: '字节跳动',
    text: '内容编排合理，代码规范性强，是我见过最好的实战教程之一！'
  }
])

// 加载教程详情
const loadTutorialDetail = async () => {
  try {
    const tutorialId = route.params.id
    const res = await getTutorialDetail(tutorialId)
    if (res.code === 1 && res.data) {
      tutorial.value = res.data
    }
  } catch (error) {
    console.error('加载教程详情失败：', error)
  }
}

// 加载章节列表
const loadChapters = async () => {
  try {
    const tutorialId = route.params.id
    const res = await getTutorialChapters(tutorialId)
    if (res.code === 1 && res.data) {
      chapters.value = res.data
    }
  } catch (error) {
    console.error('加载章节列表失败：', error)
  }
}

// 判断章节是否免费
const isChapterFree = (chapter) => {
  return chapter.isFree === 1
}

// 切换章节展开/折叠
const toggleChapter = async (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    // 折叠
    expandedChapters.value.splice(index, 1)
  } else {
    // 展开
    expandedChapters.value.push(chapterId)
    // 加载该章节的资源
    await loadChapterResources(chapterId)
  }
}

// 加载章节资源（文档和视频）
const loadChapterResources = async (chapterId) => {
  if (loadingResources.value[chapterId]) return
  
  loadingResources.value[chapterId] = true
  
  try {
    // 并行加载文档和视频
    const [docsRes, videosRes] = await Promise.all([
      getChapterDocuments(chapterId),
      getChapterVideos(chapterId)
    ])
    
    if (docsRes.code === 1) {
      chapterDocuments.value[chapterId] = docsRes.data || []
    }
    
    if (videosRes.code === 1) {
      chapterVideos.value[chapterId] = videosRes.data || []
    }
  } catch (error) {
    console.error('加载章节资源失败：', error)
  } finally {
    loadingResources.value[chapterId] = false
  }
}

// 查看文档
const viewDocument = (doc, chapter) => {
  if (!isChapterFree(chapter)) {
    alert('该章节需要解锁后才能查看')
    return
  }
  
  // 跳转到文档预览页面，使用 query 传递数据
  router.push({
    path: '/document/viewer',
    query: {
      documentId: doc.id,
      chapterId: chapter.id,
      tutorialId: tutorial.value.id
    }
  })
}

// 下载文档
const downloadDocument = (doc) => {
  // 在新标签页打开文档URL，触发浏览器下载
  window.open(doc.documentUrl, '_blank')
}

// 播放视频
const playVideo = (video, chapter) => {
  if (!isChapterFree(chapter)) {
    alert('该章节需要解锁后才能观看')
    return
  }
  
  // 跳转到视频播放页面
  router.push({
    path: '/video/player',
    query: {
      videoId: video.id,
      chapterId: chapter.id,
      tutorialId: tutorial.value.id
    }
  })
}

// 下载视频
const downloadVideo = async (video) => {
  try {
    // 创建一个隐藏的a标签来触发下载
    const link = document.createElement('a')
    link.href = video.videoUrl
    link.download = video.videoTitle + '.mp4'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error('下载失败：', error)
    alert('下载失败，请重试')
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const map = { 0: '入门', 1: '进阶', 2: '高级' }
  return map[difficulty] || '未知'
}

// 获取难度样式类
const getDifficultyClass = (difficulty) => {
  const map = { 0: 'easy', 1: 'medium', 2: 'hard' }
  return map[difficulty] || 'easy'
}

// 滚动到章节列表
const scrollToChapters = () => {
  chaptersRef.value?.scrollIntoView({ behavior: 'smooth' })
}

// 返回教程列表
const goBack = () => {
  router.push('/tutorial')
}

// 查看推荐课程
const viewCourse = (courseId) => {
  router.push(`/tutorial/${courseId}`)
  // 刷新页面数据
  loadTutorialDetail()
  loadChapters()
}

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// ==================== 评论相关方法 ====================

// 加载评论列表
const loadComments = async () => {
  const tutorialId = route.params.id
  if (!tutorialId) return
  
  commentsLoading.value = true
  try {
    const res = await getTutorialComments(tutorialId)
    if (res.code === 1) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('加载评论失败：', error)
  } finally {
    commentsLoading.value = false
  }
}

// 发布评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    alert('请输入评论内容')
    return
  }
  
  if (!isLoggedIn.value) {
    alert('请先登录后再评论')
    router.push('/')
    return
  }
  
  const tutorialId = route.params.id
  submittingComment.value = true
  
  try {
    const res = await publishTutorialComment(tutorialId, {
      content: commentContent.value.trim(),
      parentId: null
    })
    
    if (res.code === 1) {
      commentContent.value = ''
      await loadComments() // 重新加载评论
    } else {
      alert(res.msg || '评论发布失败')
    }
  } catch (error) {
    console.error('发布评论失败：', error)
    alert('评论发布失败，请重试')
  } finally {
    submittingComment.value = false
  }
}

// 开始回复
const startReply = (commentId) => {
  replyingTo.value = commentId
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) {
    alert('请输入回复内容')
    return
  }
  
  if (!isLoggedIn.value) {
    alert('请先登录后再回复')
    router.push('/')
    return
  }
  
  const tutorialId = route.params.id
  submittingComment.value = true
  
  try {
    const res = await publishTutorialComment(tutorialId, {
      content: replyContent.value.trim(),
      parentId: parentId
    })
    
    if (res.code === 1) {
      replyContent.value = ''
      replyingTo.value = null
      await loadComments()
    } else {
      alert(res.msg || '回复失败')
    }
  } catch (error) {
    console.error('回复失败：', error)
    alert('回复失败，请重试')
  } finally {
    submittingComment.value = false
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  if (!confirm('确定要删除这条评论吗？')) {
    return
  }
  
  try {
    const res = await deleteTutorialComment(commentId)
    if (res.code === 1) {
      await loadComments()
    } else {
      alert(res.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败：', error)
    alert('删除失败，请重试')
  }
}

// 判断是否为当前用户的评论
const isMyComment = (comment) => {
  return userInfo.value?.id === comment.userId
}

// 格式化评论时间
const formatCommentTime = (time) => {
  if (!time) return ''
  
  // 处理Java LocalDateTime数组格式
  if (Array.isArray(time)) {
    const [year, month, day, hour = 0, minute = 0] = time
    const date = new Date(year, month - 1, day, hour, minute)
    return formatTimeAgo(date)
  }
  
  const date = new Date(time)
  if (isNaN(date.getTime())) return ''
  return formatTimeAgo(date)
}

// 格式化为多久前
const formatTimeAgo = (date) => {
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 30) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } else if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

// 页面加载
onMounted(async () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }

  loading.value = true
  await Promise.all([loadTutorialDetail(), loadChapters()])
  loading.value = false
  
  // 加载评论（不阻塞主内容显示）
  loadComments()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&family=JetBrains+Mono&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

:global(:root) {
  --primary: #c2410c;
  --accent: #d97706;
  --background: #fdfaf6;
  --surface: #f7f2eb;
  --text-main: #2d2a26;
  --text-muted: #7c7267;
  --border-warm: #eaddd3;
  --golden-glow: rgba(245, 158, 11, 0.25);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.tutorial-detail-container {
  min-height: 100vh;
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

/* ==================== 两栏布局 ==================== */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 32px;
  max-width: 1440px;
  margin: 0 auto;
  padding: 40px;
}

.left-column {
  min-width: 0;
}

/* ==================== 加载和错误状态 ==================== */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 72px);
  padding: 80px 40px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--border-warm);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p,
.error-container p {
  margin-top: 20px;
  font-size: 16px;
  font-family: 'Crimson Pro', serif;
  color: var(--text-muted);
}

.error-container svg {
  width: 64px;
  height: 64px;
  color: #dc2626;
}

.btn-back {
  margin-top: 24px;
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: var(--primary);
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.btn-back:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

/* ==================== 主内容区 ==================== */
.main-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 40px;
}

/* 课程头部 */
.tutorial-header {
  background: white;
  border-radius: 26px;
  padding: 40px;
  margin-bottom: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.header-content {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 40px;
}

.cover-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 20px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 4px 12px rgba(45, 42, 38, 0.1);
}

.header-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tutorial-title {
  font-family: 'Crimson Pro', serif;
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.tutorial-description {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: var(--text-muted);
  line-height: 1.7;
}

/* 讲师信息 */
.instructor-info {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--surface);
  border-radius: 16px;
  border: 1px solid var(--border-warm);
}

.instructor-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid var(--border-warm);
  object-fit: cover;
}

.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instructor-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
}

.instructor-title {
  font-size: 13px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

/* 课程统计 */
.tutorial-stats {
  display: flex;
  gap: 28px;
  padding: 20px 0;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.stat-item svg {
  width: 20px;
  height: 20px;
  color: var(--accent);
}

.stat-item.difficulty svg {
  color: currentColor;
}

.stat-item.difficulty.easy {
  color: #10b981;
}

.stat-item.difficulty.medium {
  color: #f59e0b;
}

.stat-item.difficulty.hard {
  color: #ef4444;
}

/* 价格和操作 */
.tutorial-actions {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-warm);
}

.price-info .price {
  font-family: 'Crimson Pro', serif;
  font-size: 36px;
  font-weight: 700;
  color: #dc2626;
}

.price-info .price.free {
  color: #10b981;
}

.btn-start {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 36px;
  font-size: 16px;
  font-weight: 700;
  color: white;
  background: var(--primary);
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
  font-family: 'Inter', sans-serif;
}

.btn-start:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

.btn-start svg {
  width: 20px;
  height: 20px;
}

/* ==================== 章节区域 ==================== */
.chapters-section {
  background: white;
  border-radius: 26px;
  padding: 40px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid var(--border-warm);
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Crimson Pro', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.section-header h2 svg {
  width: 28px;
  height: 28px;
  color: var(--primary);
}

.chapter-count {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 章节项 */
.chapter-item {
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid var(--border-warm);
}

.chapter-item.expanded {
  background: white;
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.1);
  border-color: var(--primary);
}

/* 章节标题栏 */
.chapter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.chapter-header:hover {
  background: rgba(44, 62, 80, 0.02);
}

.chapter-header.locked {
  cursor: not-allowed;
  opacity: 0.6;
}

.chapter-header.locked:hover {
  background: transparent;
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.chapter-number {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-muted);
  padding: 6px 14px;
  background: white;
  border-radius: 10px;
  white-space: nowrap;
  border: 1px solid var(--border-warm);
  font-family: 'Inter', sans-serif;
}

.chapter-item.expanded .chapter-number {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.chapter-title {
  font-family: 'Crimson Pro', serif;
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  flex: 1;
}

.chapter-header.locked .chapter-title {
  color: var(--text-muted);
}

.free-badge,
.locked-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  white-space: nowrap;
  font-family: 'Inter', sans-serif;
}

.free-badge {
  color: #10b981;
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.free-badge svg {
  width: 14px;
  height: 14px;
}

.locked-badge {
  color: var(--text-muted);
  background: rgba(124, 114, 103, 0.1);
  border: 1px solid var(--border-warm);
}

.locked-badge svg {
  width: 14px;
  height: 14px;
}

.chapter-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.chapter-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.chapter-duration svg {
  width: 16px;
  height: 16px;
  color: var(--accent);
}

.expand-icon {
  width: 20px;
  height: 20px;
  color: var(--primary);
  transition: transform 0.3s;
}

.chapter-item.expanded .expand-icon {
  transform: rotate(180deg);
}

/* 章节内容 */
.chapter-content {
  padding: 0 28px 28px;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.slide-enter-to,
.slide-leave-from {
  max-height: 1000px;
  opacity: 1;
}

/* 资源加载状态 */
.loading-resources {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: #64748b;
}

.mini-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-warm);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 资源容器 */
.resources-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resource-section {
  background: var(--surface);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid var(--border-warm);
}

.resource-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14px;
  font-family: 'Inter', sans-serif;
}

.resource-title svg {
  width: 18px;
  height: 18px;
  color: var(--primary);
}

/* 资源列表 */
.resource-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-warm);
}

.resource-item:hover {
  background: var(--surface);
  transform: translateX(4px);
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(194, 65, 12, 0.1);
}

.resource-item.locked {
  cursor: not-allowed;
  opacity: 0.5;
}

.resource-item.locked:hover {
  background: white;
  transform: none;
}

.resource-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.resource-icon.document {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.resource-icon.video {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.resource-icon svg {
  width: 20px;
  height: 20px;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.resource-item.locked .resource-name {
  color: #94a3b8;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.resource-meta svg {
  width: 12px;
  height: 12px;
}

.resource-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 视频播放和下载按钮 */
.btn-play,
.btn-download {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-play svg,
.btn-download svg {
  width: 14px;
  height: 14px;
}

.btn-play {
  background: var(--primary);
  color: white;
  border: none;
}

.btn-play:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.3);
}

.btn-download {
  background: white;
  color: var(--text-muted);
  border: 1.5px solid var(--border-warm);
}

.btn-download:hover {
  background: var(--surface);
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.15);
}

.locked-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f8fafc;
  border-radius: 6px;
  color: #94a3b8;
  font-size: 13px;
}

.locked-badge svg {
  width: 14px;
  height: 14px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.view-btn {
  background: #2c3e50;
  color: white;
}

.view-btn:hover {
  background: #1a252f;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.15);
}

.download-btn {
  background: white;
  color: #64748b;
  border: 1.5px solid #e2e8f0;
}

.download-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #475569;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.1);
}

.locked-icon {
  display: flex;
  align-items: center;
  color: #94a3b8;
}

.locked-icon svg {
  width: 18px;
  height: 18px;
}

.resource-action {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #2c3e50;
}

.resource-item.locked .resource-action {
  color: #94a3b8;
}

.resource-action svg {
  width: 16px;
  height: 16px;
}

/* 无资源提示 */
.no-resources {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #94a3b8;
}

.no-resources svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.no-resources p {
  font-size: 14px;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .header-content {
    grid-template-columns: 1fr;
  }
}

/* ==================== 评论区 ==================== */
.comments-section {
  background: white;
  border-radius: 26px;
  padding: 40px;
  margin-top: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.comments-section .section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid var(--border-warm);
}

.comments-section .section-header h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Crimson Pro', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.comments-section .section-header svg {
  width: 28px;
  height: 28px;
  color: var(--primary);
}

.comment-count {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  background: var(--surface);
  padding: 8px 18px;
  border-radius: 999px;
  font-family: 'Inter', sans-serif;
}

.comments-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-muted);
  text-align: center;
  background: var(--surface);
  border-radius: 20px;
  border: 1px dashed var(--border-warm);
}

.comments-placeholder svg {
  width: 64px;
  height: 64px;
  margin-bottom: 20px;
  opacity: 0.4;
  color: var(--primary);
}

.comments-placeholder p {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: var(--text-muted);
}

/* ==================== 评论输入区 ==================== */
.comment-input-area {
  margin-bottom: 32px;
}

.comment-input-wrapper {
  display: flex;
  gap: 16px;
}

.comment-user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-warm);
  flex-shrink: 0;
}

.comment-input-box {
  flex: 1;
  background: var(--surface);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid var(--border-warm);
  transition: all 0.2s;
}

.comment-input-box:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--golden-glow);
}

.comment-textarea {
  width: 100%;
  border: none;
  background: transparent;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: var(--text-main);
  resize: none;
  outline: none;
  line-height: 1.6;
}

.comment-textarea::placeholder {
  color: var(--text-muted);
}

.comment-textarea:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.comment-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-warm);
}

.char-count {
  font-size: 12px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.btn-submit-comment {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
}

.btn-submit-comment:hover:not(:disabled) {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.3);
}

.btn-submit-comment:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-submit-comment svg {
  width: 16px;
  height: 16px;
}

/* ==================== 评论加载状态 ==================== */
.comments-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: var(--text-muted);
}

/* ==================== 评论列表 ==================== */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  background: var(--surface);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid var(--border-warm);
}

.comment-main {
  display: flex;
  gap: 16px;
}

.comment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-warm);
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

.comment-time {
  font-size: 13px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.comment-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-main);
  margin-bottom: 12px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn-comment {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border-warm);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
}

.action-btn-comment:hover {
  background: white;
  border-color: var(--primary);
  color: var(--primary);
}

.action-btn-comment.delete:hover {
  border-color: #dc2626;
  color: #dc2626;
}

.action-btn-comment svg {
  width: 14px;
  height: 14px;
}

/* ==================== 回复输入区 ==================== */
.reply-input-area {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border-warm);
}

.reply-textarea {
  width: 100%;
  border: none;
  background: transparent;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: var(--text-main);
  resize: none;
  outline: none;
  line-height: 1.6;
}

.reply-textarea::placeholder {
  color: var(--text-muted);
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

.btn-cancel-reply {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-warm);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
}

.btn-cancel-reply:hover {
  background: var(--surface);
  border-color: var(--text-muted);
}

.btn-submit-reply {
  padding: 8px 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
}

.btn-submit-reply:hover:not(:disabled) {
  background: #9a3412;
}

.btn-submit-reply:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ==================== 子回复列表 ==================== */
.replies-list {
  margin-top: 20px;
  margin-left: 60px;
  padding-left: 20px;
  border-left: 2px solid var(--border-warm);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border-warm);
}

.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid var(--border-warm);
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.reply-author {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

.reply-to {
  font-size: 13px;
  color: var(--text-muted);
}

.reply-to-name {
  color: var(--primary);
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.reply-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main);
  margin-bottom: 8px;
  word-break: break-word;
}

.reply-actions-bar {
  display: flex;
  gap: 12px;
}

/* ==================== 空评论状态 ==================== */
.comments-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-muted);
  text-align: center;
  background: var(--surface);
  border-radius: 20px;
  border: 1px dashed var(--border-warm);
}

.comments-empty svg {
  width: 56px;
  height: 56px;
  margin-bottom: 16px;
  opacity: 0.4;
  color: var(--primary);
}

.comments-empty p {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: var(--text-muted);
}

/* ==================== 右侧边栏 ==================== */
.right-sidebar {
  position: sticky;
  top: 96px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.sidebar-card {
  background: white;
  border-radius: 26px;
  padding: 28px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Crimson Pro', serif;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-warm);
}

.sidebar-title svg {
  width: 22px;
  height: 22px;
  color: var(--primary);
}

/* 推荐课程 */
.recommended-courses {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mini-course-card {
  display: flex;
  gap: 14px;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--border-warm);
  transition: all 0.3s ease;
}

.mini-course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(194, 65, 12, 0.15);
  border-color: var(--primary);
}

.mini-course-card img {
  width: 100px;
  height: 60px;
  object-fit: cover;
  flex-shrink: 0;
}

.mini-course-info {
  flex: 1;
  padding: 8px 12px 8px 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.mini-course-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.mini-course-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.mini-course-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.mini-course-meta svg {
  width: 14px;
  height: 14px;
}

.mini-course-meta .students svg {
  color: #6366f1;
}

.mini-course-meta .rating {
  color: #f59e0b;
  font-weight: 600;
}

.mini-course-meta .rating svg {
  fill: #f59e0b;
}

/* Coder推荐语 */
.coder-testimonials {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.testimonial-card {
  background: var(--surface);
  border-radius: 16px;
  padding: 20px;
  border-left: 4px solid var(--primary);
  border: 1px solid var(--border-warm);
  border-left-width: 4px;
}

.testimonial-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.testimonial-header img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.testimonial-header h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.testimonial-header p {
  font-size: 12px;
  color: #64748b;
}

.testimonial-text {
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  margin-bottom: 10px;
  font-style: italic;
}

.testimonial-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
}

.testimonial-footer span {
  font-weight: 600;
  color: #3b82f6;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .right-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .content-grid {
    padding: 24px 20px;
    gap: 24px;
  }

  .main-content {
    padding: 24px 20px;
  }

  .tutorial-header {
    padding: 28px;
  }

  .header-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .tutorial-title {
    font-size: 28px;
  }

  .tutorial-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .tutorial-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .btn-start {
    justify-content: center;
    width: 100%;
  }

  .chapters-section {
    padding: 28px;
  }

  .sidebar-card {
    padding: 24px;
  }
}
</style>
