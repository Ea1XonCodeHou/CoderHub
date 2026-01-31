/**
 * useMarkdownRenderer - Markdown 渲染 Composable
 * 
 * 功能：
 * 1. 使用 marked.js 解析 Markdown
 * 2. 使用 highlight.js 进行代码高亮
 * 3. 自定义渲染器：代码块、表格、引用等
 * 4. 支持复制代码功能
 * 
 * @author CoderHub
 */
import { ref, computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'

// 导入高亮主题
import 'highlight.js/styles/github-dark.css'

/**
 * 复制到剪贴板
 */
async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    return true
  }
}

/**
 * 创建自定义渲染器
 */
function createCustomRenderer() {
  const renderer = new marked.Renderer()

  // 代码块渲染 - 添加语言标签和复制按钮
  renderer.code = function(code, language) {
    // 处理 code 可能是对象的情况（marked 新版本）
    if (typeof code === 'object') {
      language = code.lang
      code = code.text
    }
    
    const validLanguage = language && hljs.getLanguage(language) ? language : 'plaintext'
    let highlighted
    
    try {
      highlighted = hljs.highlight(code, { language: validLanguage }).value
    } catch {
      highlighted = hljs.highlightAuto(code).value
    }
    
    const uniqueId = `code-${Math.random().toString(36).substr(2, 9)}`
    
    return `
      <div class="code-block-wrapper">
        <div class="code-block-header">
          <span class="code-language">${validLanguage.toUpperCase()}</span>
          <button class="copy-code-btn" data-code-id="${uniqueId}" onclick="window.__copyCode('${uniqueId}')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <span>复制</span>
          </button>
        </div>
        <pre class="code-block-content"><code id="${uniqueId}" class="hljs language-${validLanguage}">${highlighted}</code></pre>
      </div>
    `
  }

  // 表格渲染 - 添加滚动容器和斑马纹
  renderer.table = function(header, body) {
    // 处理新版本 marked 的对象参数
    if (typeof header === 'object') {
      const tableData = header
      let headerHtml = '<tr>'
      let bodyHtml = ''
      
      if (tableData.header) {
        tableData.header.forEach(cell => {
          headerHtml += `<th>${cell.text}</th>`
        })
        headerHtml += '</tr>'
      }
      
      if (tableData.rows) {
        tableData.rows.forEach(row => {
          bodyHtml += '<tr>'
          row.forEach(cell => {
            bodyHtml += `<td>${cell.text}</td>`
          })
          bodyHtml += '</tr>'
        })
      }
      
      return `
        <div class="table-wrapper">
          <table class="markdown-table">
            <thead>${headerHtml}</thead>
            <tbody>${bodyHtml}</tbody>
          </table>
        </div>
      `
    }
    
    return `
      <div class="table-wrapper">
        <table class="markdown-table">
          <thead>${header}</thead>
          <tbody>${body}</tbody>
        </table>
      </div>
    `
  }

  // 引用块渲染
  renderer.blockquote = function(quote) {
    const text = typeof quote === 'object' ? quote.text : quote
    return `
      <blockquote class="markdown-blockquote">
        ${text}
      </blockquote>
    `
  }

  // 链接渲染 - 新标签页打开
  renderer.link = function(href, title, text) {
    // 处理新版本参数
    if (typeof href === 'object') {
      title = href.title
      text = href.text
      href = href.href
    }
    const titleAttr = title ? ` title="${title}"` : ''
    return `<a href="${href}"${titleAttr} target="_blank" rel="noopener noreferrer" class="markdown-link">${text}</a>`
  }

  // 图片渲染
  renderer.image = function(href, title, text) {
    if (typeof href === 'object') {
      title = href.title
      text = href.text
      href = href.href
    }
    const titleAttr = title ? ` title="${title}"` : ''
    const altAttr = text ? ` alt="${text}"` : ''
    return `<img src="${href}"${altAttr}${titleAttr} class="markdown-image" loading="lazy" />`
  }

  // 行内代码
  renderer.codespan = function(code) {
    const text = typeof code === 'object' ? code.text : code
    return `<code class="inline-code">${text}</code>`
  }

  // 分隔线
  renderer.hr = function() {
    return '<hr class="markdown-hr" />'
  }

  return renderer
}

/**
 * 配置 marked
 */
function configureMarked() {
  marked.setOptions({
    renderer: createCustomRenderer(),
    breaks: true,
    gfm: true,
    headerIds: false,
    mangle: false
  })
}

/**
 * 注册全局复制函数
 */
if (typeof window !== 'undefined') {
  window.__copyCode = async function(codeId) {
    const codeElement = document.getElementById(codeId)
    if (codeElement) {
      const success = await copyToClipboard(codeElement.textContent)
      
      // 更新按钮状态
      const btn = document.querySelector(`[data-code-id="${codeId}"]`)
      if (btn && success) {
        const originalHtml = btn.innerHTML
        btn.innerHTML = `
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>已复制</span>
        `
        btn.classList.add('copied')
        
        setTimeout(() => {
          btn.innerHTML = originalHtml
          btn.classList.remove('copied')
        }, 2000)
      }
    }
  }
}

/**
 * useMarkdownRenderer Composable
 */
export function useMarkdownRenderer() {
  const isInitialized = ref(false)

  /**
   * 初始化
   */
  function init() {
    if (!isInitialized.value) {
      configureMarked()
      isInitialized.value = true
    }
  }

  /**
   * 渲染 Markdown 文本
   * @param {string} text - Markdown 文本
   * @returns {string} - HTML 字符串
   */
  function render(text) {
    if (!text) return ''
    
    if (!isInitialized.value) {
      init()
    }

    try {
      return marked(text)
    } catch (error) {
      console.error('Markdown 渲染错误:', error)
      // 降级处理：简单替换换行
      return text.replace(/\n/g, '<br>')
    }
  }

  /**
   * 渲染为纯文本（去除 HTML 标签）
   * @param {string} text - Markdown 文本
   * @returns {string} - 纯文本
   */
  function renderToPlainText(text) {
    if (!text) return ''
    const html = render(text)
    const div = document.createElement('div')
    div.innerHTML = html
    return div.textContent || div.innerText || ''
  }

  /**
   * 复制文本到剪贴板
   */
  async function copyText(text) {
    return copyToClipboard(text)
  }

  // 自动初始化
  init()

  return {
    render,
    renderToPlainText,
    copyText,
    isInitialized
  }
}

export default useMarkdownRenderer

