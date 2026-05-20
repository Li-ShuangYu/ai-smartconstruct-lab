<template>
  <div class="pipeline-create-wrapper">
    <aside class="material-sidebar" 
           :class="{ 
             'is-collapsed': isSidebarCollapsed,
             'is-drag-over-delete': isHoveringDelete 
           }"
           @dragover.prevent="onSidebarDragOver"
           @dragleave="onSidebarDragLeave"
           @drop="onSidebarDrop">
      
      <div class="sidebar-inner" v-show="!isSidebarCollapsed">
        <h1 class="panel-main-title">实训节点库</h1>
        <div class="category-container">
          <div v-for="cat in MATERIAL_CATEGORIES" :key="cat.type" class="cat-group">
            <div class="cat-header" @click="toggleCategory(cat.type)">
              <span class="cat-label">{{ cat.label }}</span>
              <i class="pure-chevron" :class="{ 'is-rotated': expandedCats.includes(cat.type) }"></i>
            </div>
            <div class="drawer-animator" :class="{ 'is-open': expandedCats.includes(cat.type) }">
              <div class="drawer-inner">
                <div class="node-list">
                  <div v-for="item in filteredCatItems(cat)" :key="item.type" class="draggable-node"
                    draggable="true" 
                    @dragstart="onDragStartLibrary($event, item)"
                    @dragend="onDragEnd"
                    @click="addNodeToCanvas(item.type, item.label)">
                    <span class="node-icon">{{ item.icon }}</span>
                    <span class="node-label">{{ item.label }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="delete-overlay" v-show="isHoveringDelete">
        <div class="trash-icon">🗑️</div>
        <span>松开鼠标删除该节点</span>
      </div>

      <div class="sidebar-toggle" @click="isSidebarCollapsed = !isSidebarCollapsed">
        {{ isSidebarCollapsed ? '▶' : '◀' }}
      </div>
    </aside>

    <main class="canvas-container"
          @dragover.prevent
          @drop="onDropCanvas"
          @dragleave="onCanvasDragLeave">
      <div class="canvas-top-toolbar">
        <span class="pipeline-title">实训流程编排</span>
        <div class="flex-spacer"></div>
        <button class="secondary-action-btn" @click="handlePublish">🚀 发布模板</button>
      </div>
      
      <div class="pipeline-scroll-area">
        <div class="pipeline-queue">
          <transition-group name="list-anim">
            <div v-for="(node, index) in orchestrationList" :key="node.id" 
                 class="pipeline-item-wrapper"
                 @dragover.prevent="onNodeDragOver($event, index)">
              
              <div class="pipeline-node" 
                   :draggable="!['start', 'end'].includes(node.type)"
                   :class="{ 
                     'is-active': selectedNodeId === node.id,
                     'is-fixed': node.type === 'start' || node.type === 'end',
                     'is-dragging': dragState.item?.id === node.id
                   }"
                   @dragstart="onDragStartNode($event, index, node)"
                   @dragend="onDragEnd"
                   @click="selectNode(node)">
                <div class="node-index">{{ index + 1 }}</div>
                <div class="node-content">
                  <div class="node-title">
                    <span class="node-icon">{{ getNodeIcon(node.type) }}</span>
                    {{ node.name }}
                  </div>
                  <div class="node-type-tag">{{ getCategoryLabel(node.type) }}</div>
                </div>
                <div class="node-actions" v-if="!['start', 'end'].includes(node.type)">
                  <button class="icon-btn" title="上移" @click.stop="moveNodeUp(index)" :disabled="index <= 1">↑</button>
                  <button class="icon-btn" title="下移" @click.stop="moveNodeDown(index)" :disabled="index >= orchestrationList.length - 2">↓</button>
                  <button class="icon-btn danger" title="删除" @click.stop="deleteNode(index)">✕</button>
                </div>
              </div>

              <div class="pipeline-connector" 
                   v-if="index !== orchestrationList.length - 1"
                   :class="{ 'is-drag-target': dropIndicatorIndex === index }">
                
                <template v-if="dropIndicatorIndex === index">
                  <div class="drop-placeholder-box">✨ 释放到此处</div>
                </template>
                <template v-else>
                  <div class="line"></div>
                  <div class="arrow">▼</div>
                </template>
              </div>

              <template v-if="index === 0 && orchestrationList.length === 2 && dropIndicatorIndex === -1">
                <div class="pipeline-node is-empty-placeholder">
                  请从左侧拖拽或点击添加节点
                </div>
                <div class="pipeline-connector">
                  <div class="line"></div>
                  <div class="arrow">▼</div>
                </div>
              </template>

            </div>
          </transition-group>
        </div>
      </div>
    </main>

    <transition name="panel-slide">
      <aside v-if="activeNode" class="config-panel" :style="{ width: configPanelWidth + 'px' }">
        <div class="resize-handle" @mousedown="startPanelResize"></div>

        <div class="config-header">
          <h3>{{ activeNode.name }} 配置</h3>
          <button class="close-btn" @click="selectedNodeId = null">✕</button>
        </div>
        
        <div class="config-body">
          <div class="section-title">基础配置</div>
          
          <template v-if="activeNode.type === 'start'">
            <div class="config-field">
              <label><span class="required">*</span>实训整体说明(场景)</label>
              <textarea v-model.lazy="activeNode.config.desc" class="config-textarea" placeholder="设置实训的场景"></textarea>
            </div>
            <div class="config-field">
              <label>实训学生背景</label>
              <textarea v-model.lazy="activeNode.config.student_bg" class="config-textarea" placeholder="介绍一下学生的背景，基础"></textarea>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'end'">
            <div class="config-field">
              <label>结束页说明文字</label>
              <input v-model.lazy="activeNode.config.end_desc" class="config-input" />
            </div>
          </template>

          <template v-else-if="activeNode.type === 'resource_read'">
            <div class="config-field list-container">
              <label><span class="required">*</span>文件列表 (pdf, txt, word, ppt, md)</label>
              <div class="table-wrapper">
                <table class="resizable-table">
                  <thead><tr><th style="width: 40px">序号</th><th style="width: 120px">上传文件</th><th class="resizable-th">展示名</th><th class="resizable-th">进度%</th><th style="width: 40px">操作</th></tr></thead>
                  <tbody>
                    <tr v-for="(file, i) in activeNode.config.files" :key="i">
                      <td class="text-center">{{ Number(i) + 1 }}</td>
                      <td><button class="upload-btn-sm" @click="openDocUpload(Number(i))">{{ file.original_name || '选择文件' }}</button></td>
                      <td><input v-model.lazy="file.display_name" class="config-input borderless" placeholder="展示名" /></td>
                      <td><input type="number" v-model.lazy="file.min_progress" class="config-input borderless" min="0" max="100" /></td>
                      <td class="text-center"><button class="icon-btn-small" @click="activeNode.config.files.splice(i, 1)">✕</button></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <button class="add-btn" @click="activeNode.config.files.push({ original_name: '', display_name: '', min_progress: 100 })">+ 添加阅读文件</button>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'video_watch'">
            <div class="config-field"><label><span class="required">*</span>上传视频文件</label><button class="upload-btn" @click="openVideoUpload()">{{ activeNode.config.video_file || '选择视频文件' }}</button></div>
            <div class="config-field"><label>视频展示标题</label><input v-model.lazy="activeNode.config.video_title" class="config-input" /></div>
            <div class="config-field"><label>最少播放进度(%)</label><input type="number" v-model.lazy="activeNode.config.min_progress" class="config-input" min="0" max="100" /></div>
            <div class="config-field flex-row"><label>是否允许拖拽进度条</label><input type="checkbox" v-model="activeNode.config.allow_drag" /></div>
            <div class="config-field">
              <label>允许的倍速范围</label>
              <div class="checkbox-group">
                <label v-for="speed in [1.25, 1.5, 2, 3, 5]" :key="speed"><input type="checkbox" :value="speed" v-model="activeNode.config.allowed_speeds" /> {{ speed }}x</label>
              </div>
            </div>
          </template>

          <template v-else-if="['mindmap_preview', 'mindmap_draw'].includes(activeNode.type)">
            <div class="config-field flex-row"><label>是否由 AI 自动生成导图</label><input type="checkbox" v-model="activeNode.config.ai_generated" /></div>
            <template v-if="activeNode.config.ai_generated">
              <div class="config-field"><label><span class="required">*</span>导图主题</label><input v-model.lazy="activeNode.config.topic" class="config-input" /></div>
              <div class="config-field"><label>最大节点数</label><input type="number" v-model.lazy="activeNode.config.max_nodes" class="config-input" /></div>
            </template>
            <template v-else>
              <div class="config-field"><label>初始导图结构</label><button class="secondary-action-btn w-full">进入编排实时手绘</button></div>
            </template>
          </template>

          <template v-else-if="activeNode.type === 'survey'">
            <div class="config-field list-container">
              <label>调查内容列表</label>
              <div class="table-wrapper">
                <table class="resizable-table">
                  <thead><tr><th style="width: 40px">#</th><th class="resizable-th">题目内容</th><th style="width: 40px">操作</th></tr></thead>
                  <tbody>
                    <tr v-for="(q, i) in activeNode.config.questions" :key="i">
                      <td class="text-center">{{ Number(i) + 1 }}</td>
                      <td><input v-model.lazy="q.content" class="config-input borderless" placeholder="输入题目" /></td>
                      <td class="text-center"><button class="icon-btn-small" @click="activeNode.config.questions.splice(i, 1)">✕</button></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <button class="add-btn" @click="activeNode.config.questions.push({ content: '' })">+ 添加题目</button>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'ai_practice'">
            <div class="config-field"><label>考核知识点列表</label><textarea v-model.lazy="activeNode.config.knowledge_points" class="config-textarea" placeholder="不填则由AI生成"></textarea></div>
            <div class="config-field"><label>通关标准描述</label><textarea v-model.lazy="activeNode.config.pass_criteria" class="config-textarea" placeholder="不填则由AI生成"></textarea></div>
            <div class="config-field"><label>最大对话轮数</label><input type="number" v-model.lazy="activeNode.config.max_rounds" class="config-input" placeholder="不填则由AI生成" /></div>
          </template>

          <template v-else-if="activeNode.type === 'theory_class'">
            <div class="config-field"><label>理论主题</label><input v-model.lazy="activeNode.config.topic" class="config-input" placeholder="不填则由AI生成" /></div>
            <div class="config-field"><label>覆盖知识点列表</label><textarea v-model.lazy="activeNode.config.knowledge_points" class="config-textarea" placeholder="不填则由AI生成"></textarea></div>
            <div class="config-field"><label>最大学习卡片数量</label><input type="number" v-model.lazy="activeNode.config.max_cards" class="config-input" placeholder="不填则由AI生成" /></div>
          </template>

          <template v-else-if="activeNode.type === 'task_distribute'">
            <div class="config-field"><label>任务标题</label><input v-model.lazy="activeNode.config.title" class="config-input" placeholder="不填则由AI生成" /></div>
            <div class="config-field"><label>任务具体要求</label><textarea v-model.lazy="activeNode.config.requirement" class="config-textarea"></textarea></div>
            <div class="config-field"><label>任务截止时间</label><input type="date" v-model.lazy="activeNode.config.deadline" class="config-input" /></div>
            <div class="config-field list-container">
              <label>附件列表</label>
              <div class="table-wrapper">
                <table class="resizable-table">
                  <thead><tr><th style="width: 40px">序号</th><th style="width: 120px">上传文件</th><th class="resizable-th">展示名</th><th style="width: 40px">操作</th></tr></thead>
                  <tbody>
                    <tr v-for="(file, i) in activeNode.config.files" :key="i">
                      <td class="text-center">{{ Number(i) + 1 }}</td>
                      <td><button class="upload-btn-sm" @click="openDocUpload(Number(i))">{{ file.original_name || '选择文件' }}</button></td>
                      <td><input v-model.lazy="file.display_name" class="config-input borderless" placeholder="展示名" /></td>
                      <td class="text-center"><button class="icon-btn-small" @click="activeNode.config.files.splice(i, 1)">✕</button></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <button class="add-btn" @click="activeNode.config.files.push({ original_name: '', display_name: '' })">+ 添加附件</button>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'req_upload'">
            <div class="config-field"><label>场景描述</label><textarea v-model.lazy="activeNode.config.scenario" class="config-textarea"></textarea></div>
            <div class="config-field"><label>最低字数要求</label><input type="number" v-model.lazy="activeNode.config.min_words" class="config-input" /></div>
          </template>

          <template v-else-if="activeNode.type === 'plan_upload'">
            <div class="config-field">
              <label>允许的文件格式</label>
              <div class="checkbox-group">
                <label v-for="fmt in ['pdf', 'txt', 'word', 'ppt', 'md']" :key="fmt"><input type="checkbox" :value="fmt" v-model="activeNode.config.allowed_formats" /> {{ fmt.toUpperCase() }}</label>
              </div>
            </div>
            <div class="config-field"><label>最大文件大小(MB)</label><input type="number" v-model.lazy="activeNode.config.max_size" class="config-input" /></div>
            <div class="config-field"><label>上传具体要求</label><textarea v-model.lazy="activeNode.config.requirement" class="config-textarea"></textarea></div>
          </template>

          <template v-else-if="activeNode.type === 'homework'">
            <div class="config-field"><label>答题限时(分钟)</label><input type="number" v-model.lazy="activeNode.config.time_limit" class="config-input" /></div>
            <div class="config-field">
              <label>题目来源</label>
              <select v-model="activeNode.config.source_mode" class="config-input">
                <option value="ai">AI 生成</option><option value="bank">从题库中抽取</option>
              </select>
            </div>
            <template v-if="activeNode.config.source_mode === 'ai'">
              <div class="config-field flex-row"><label>单选数量</label><input type="number" v-model.lazy="activeNode.config.count_single" class="config-input w-24"/></div>
              <div class="config-field flex-row"><label>多选数量</label><input type="number" v-model.lazy="activeNode.config.count_multi" class="config-input w-24"/></div>
              <div class="config-field flex-row"><label>填空数量</label><input type="number" v-model.lazy="activeNode.config.count_fill" class="config-input w-24"/></div>
              <div class="config-field flex-row"><label>判断数量</label><input type="number" v-model.lazy="activeNode.config.count_judge" class="config-input w-24"/></div>
              <div class="config-field flex-row"><label>简答数量</label><input type="number" v-model.lazy="activeNode.config.count_essay" class="config-input w-24"/></div>
              <div class="config-field flex-row"><label>难度</label>
                <select v-model="activeNode.config.difficulty" class="config-input w-24">
                  <option value="easy">易</option><option value="medium">中</option><option value="hard">难</option>
                </select>
              </div>
              <div class="config-field flex-row"><label>生成后是否保存到题库</label><input type="checkbox" v-model="activeNode.config.save_to_bank" /></div>
            </template>
            <template v-else>
              <div class="config-field flex-row"><label>选题方式</label>
                <select v-model="activeNode.config.select_mode" class="config-input w-32">
                  <option value="manual">手动 manual</option><option value="random">随机 random</option>
                </select>
              </div>
              <div class="config-field" v-if="activeNode.config.select_mode === 'random'">
                <label>随机抽取题数</label><input type="number" v-model.lazy="activeNode.config.random_count" class="config-input" />
              </div>
              <div class="config-field" v-if="activeNode.config.select_mode === 'manual'">
                <button class="secondary-action-btn">打开题库，多选题目</button>
              </div>
            </template>
          </template>

          <template v-else-if="activeNode.type === 'coding_class'">
            <div class="config-field"><label>初始代码模板</label><textarea v-model.lazy="activeNode.config.template" class="config-textarea font-mono" placeholder="可为空"></textarea></div>
          </template>

          <template v-else-if="activeNode.type === 'student_peer_review'">
            <div class="config-field"><label>选择要评价的产出物</label>
              <select v-model="activeNode.config.target_node" class="config-input">
                <option value="">关联前置产出节点...</option>
                <option v-for="n in getEvaluableNodes()" :key="n.id" :value="n.id">{{ n.name }}</option>
              </select>
            </div>
            <div class="config-field"><label>每人需评份数</label><input type="number" v-model.lazy="activeNode.config.count" class="config-input" min="3" max="5" /></div>
            <div class="config-field list-container">
              <label>评价维度列表</label>
              <div class="table-wrapper">
                <table class="resizable-table">
                  <thead><tr><th style="width: 40px">编号</th><th class="resizable-th">维度名称</th><th class="resizable-th">分数范围</th><th style="width: 40px">操作</th></tr></thead>
                  <tbody>
                    <tr v-for="(dim, i) in activeNode.config.dimensions" :key="i">
                      <td class="text-center">{{ Number(i) + 1 }}</td>
                      <td><input v-model.lazy="dim.name" class="config-input borderless" placeholder="名称" /></td>
                      <td><input v-model.lazy="dim.score_range" class="config-input borderless" placeholder="例如 1-10分" /></td>
                      <td class="text-center"><button class="icon-btn-small" @click="activeNode.config.dimensions.splice(i, 1)">✕</button></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <button class="add-btn" @click="activeNode.config.dimensions.push({ name: '', score_range: '1-10分' })">+ 添加维度</button>
            </div>
            <div class="config-field"><label>评价权重(成绩占比%)</label><input type="number" v-model.lazy="activeNode.config.weight" class="config-input" /></div>
          </template>

          <template v-else-if="activeNode.type === 'teacher_eval'">
            <div class="config-field"><label>选择要评价的产出物</label>
              <select v-model="activeNode.config.target_node" class="config-input">
                <option value="">关联前置产出节点...</option>
                <option v-for="n in getEvaluableNodes()" :key="n.id" :value="n.id">{{ n.name }}</option>
              </select>
            </div>
          </template>

          <div class="ai-config-wrapper" v-if="hasAIConfig(activeNode.type)">
            <div class="ai-config-header" @click="toggleAIConfig">
              <span class="ai-title">✨ AI 增强配置</span>
              <i class="pure-chevron" :class="{ 'is-rotated': isAIExpanded }"></i>
            </div>
            
            <div class="drawer-animator" :class="{ 'is-open': isAIExpanded }">
              <div class="drawer-inner">
                <div class="ai-config-body">
                  <div class="config-field flex-row" v-if="'ai_qa' in activeNode.ai_config">
                    <label>允许 AI 随时问答</label>
                    <input type="checkbox" v-model="activeNode.ai_config.ai_qa" />
                  </div>
                  
                  <template v-if="activeNode.type === 'start'">
                    <div class="config-field flex-row"><label>是否开启 AI 个性化欢迎</label><input type="radio" :value="true" v-model="activeNode.ai_config.ai_welcome_enabled" /><label class="ml-2">是</label><input type="radio" :value="false" v-model="activeNode.ai_config.ai_welcome_enabled" /><label class="ml-2">否</label></div>
                    <div class="config-field" v-if="activeNode.ai_config.ai_welcome_enabled">
                      <label>AI 欢迎语模板</label><textarea v-model.lazy="activeNode.ai_config.ai_welcome_tpl" class="config-textarea"></textarea>
                    </div>
                  </template>

                  <template v-if="activeNode.type === 'resource_read'">
                    <div class="config-field flex-row"><label>是否开启 AI 提炼摘要</label><input type="checkbox" v-model="activeNode.ai_config.ai_summary" /></div>
                    <div class="config-field flex-row"><label>是否开启 AI 提炼重点</label><input type="checkbox" v-model="activeNode.ai_config.ai_focus" /></div>
                    <div class="config-field flex-row"><label>是否开启 AI 提炼快捷导航</label><input type="checkbox" v-model="activeNode.ai_config.ai_nav" /></div>
                  </template>

                  <template v-if="activeNode.type === 'video_watch'">
                    <div class="config-field flex-row"><label>是否开启 AI 字幕</label><input type="checkbox" v-model="activeNode.ai_config.ai_subtitles" /></div>
                    <div class="config-field flex-row"><label>是否开启 AI 知识点切片</label><input type="checkbox" v-model="activeNode.ai_config.ai_chapters" /></div>
                  </template>

                  <template v-if="activeNode.type === 'ai_practice'">
                    <div class="config-field flex-row"><label>是否开启难度自适应</label><input type="checkbox" v-model="activeNode.ai_config.ai_adaptive" /></div>
                    <div class="config-field flex-row"><label>是否开启核心概念检测</label><input type="checkbox" v-model="activeNode.ai_config.ai_core_concept" /></div>
                  </template>

                  <template v-if="activeNode.type === 'theory_class'">
                    <div class="config-field flex-row"><label>是否开启 AI 错题本</label><input type="checkbox" v-model="activeNode.ai_config.ai_mistake_book" /></div>
                  </template>

                  <template v-if="activeNode.type === 'task_distribute'">
                    <div class="config-field flex-row"><label>是否开启 AI 任务拆解指引</label><input type="checkbox" v-model="activeNode.ai_config.ai_task_breakdown" /></div>
                  </template>

                  <template v-if="activeNode.type === 'plan_upload'">
                    <div class="config-field flex-row"><label>是否开启 AI 方案初评</label><input type="checkbox" v-model="activeNode.ai_config.ai_pre_eval" /></div>
                  </template>

                  <template v-if="activeNode.type === 'coding_class'">
                    <div class="config-field flex-row"><label>是否允许 AI 辅助</label><input type="checkbox" v-model="activeNode.ai_config.ai_assist" /></div>
                    <div class="config-field flex-row" v-if="activeNode.ai_config.ai_assist"><label>AI 辅助模式</label>
                      <select v-model="activeNode.ai_config.ai_mode" class="config-input w-24">
                        <option value="guide">Guide</option><option value="hint">Hint</option><option value="full">Full</option>
                      </select>
                    </div>
                    <div class="config-field flex-row"><label>是否开启 AI 代码审查</label><input type="checkbox" v-model="activeNode.ai_config.ai_review" /></div>
                  </template>

                </div>
              </div>
            </div>
          </div>

        </div>
      </aside>
    </transition>

    <!-- Hidden file inputs -->
    <input ref="docFileInput" type="file" accept=".pdf,.txt,.doc,.docx,.ppt,.pptx,.md" style="display:none" @change="onDocFileSelected" />
    <input ref="videoFileInput" type="file" accept=".wmv,.mp4,.m4v,.mov,.avi,.mkv,.mp3" style="display:none" @change="onVideoFileSelected" />

    <!-- 发布模板弹窗 -->
    <div v-if="showPublishModal" class="modal-overlay" @click.self="showPublishModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>发布实训模板</h3>
          <button class="modal-close" @click="showPublishModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>模板名称</label>
            <input v-model="templateName" type="text" class="modal-input" placeholder="请输入模板名称" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showPublishModal = false">取消</button>
          <button class="btn btn-primary" @click="confirmPublish">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getActiveNodes } from '@/services/modules/admin.service'

// ==================== 文件上传 ====================
const docFileInput = ref<HTMLInputElement | null>(null)
const videoFileInput = ref<HTMLInputElement | null>(null)
const pendingDocIndex = ref(-1)

// 发布模板弹窗相关
const showPublishModal = ref(false)
const templateName = ref('')

const openDocUpload = (index: number) => {
  pendingDocIndex.value = index
  docFileInput.value?.click()
}

const openVideoUpload = () => {
  videoFileInput.value?.click()
}

const onDocFileSelected = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || pendingDocIndex.value < 0) return
  const active = activeNode.value
  if (!active) return
  const files = active.config.files
  if (files && files[pendingDocIndex.value]) {
    files[pendingDocIndex.value].original_name = file.name
  }
  input.value = ''
  pendingDocIndex.value = -1
}

const onVideoFileSelected = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const active = activeNode.value
  if (!active) return
  active.config.video_file = file.name
  input.value = ''
}

// ==================== 0. 节点活跃状态过滤 ====================
const activeNodeTypes = ref<Set<string>>(new Set())

onMounted(async () => {
  try {
    const res = await getActiveNodes()
    if (res.code === 200) {
      activeNodeTypes.value = new Set(res.data.map((n: any) => n.nodeType))
    }
  } catch (e) {
    console.warn('获取活跃节点失败，默认显示全部', e)
  }
})

/** 将内部 type 名映射到 DB 中的 node_type 值 */
const typeToNodeType: Record<string, string> = {
  start: 'START',
  end: 'END',
  resource_read: 'RESOURCE_READ',
  video_watch: 'VIDEO_LEARN',
  mindmap_preview: 'MINDMAP_PREVIEW',
  survey: 'SEMESTER_SURVEY',
  mindmap_draw: 'MINDMAP_DRAW',
  ai_practice: 'AI_PRACTICE',
  theory_class: 'THEORY_CLASS',
  task_distribute: 'TASK_DISTRIBUTE',
  req_upload: 'REQ_UPLOAD',
  plan_upload: 'PLAN_UPLOAD',
  homework: 'HOMEWORK',
  coding_class: 'CODING_CLASS',
  student_peer_review: 'STUDENT_PEER_REVIEW',
  teacher_eval: 'TEACHER_COMMENT'
}

/** 过滤出活跃节点 */
const filteredCatItems = (cat: any) => {
  if (activeNodeTypes.value.size === 0) return cat.items // 没加载好时显示全部
  return cat.items.filter((item: any) => {
    const nodeType = typeToNodeType[item.type]
    return nodeType !== undefined && activeNodeTypes.value.has(nodeType)
  })
}

// ==================== 1. 数据结构与初始化 ====================
const MATERIAL_CATEGORIES = [
  {
    label: '预习环节', type: 'pre_learning',
    items: [
      { type: 'resource_read', label: '资源阅读', icon: '📄' },
      { type: 'video_watch', label: '视频观看', icon: '🎬' },
      { type: 'mindmap_preview', label: '导图预习', icon: '🗺️' },
      { type: 'survey', label: '学情调查', icon: '📊' }
    ]
  },
  {
    label: '理论实训', type: 'theory',
    items: [
      { type: 'mindmap_draw', label: '导图绘制', icon: '✍️' },
      { type: 'ai_practice', label: 'AI 对练', icon: '🤖' },
      { type: 'theory_class', label: '理论实训', icon: '📚' }
    ]
  },
  {
    label: '实践实训', type: 'practice',
    items: [
      { type: 'task_distribute', label: '任务下发', icon: '📋' },
      { type: 'req_upload', label: '需求上传', icon: '📤' },
      { type: 'plan_upload', label: '方案上传', icon: '📑' },
      { type: 'homework', label: '课后作业', icon: '📝' },
      { type: 'coding_class', label: '编码实训', icon: '💻' }
    ]
  },
  {
    label: '点评环节', type: 'evaluation',
    items: [
      { type: 'student_peer_review', label: '学生互评', icon: '👥' },
      { type: 'teacher_eval', label: '教师点评', icon: '👨‍🏫' }
    ]
  }
]

const createNodeDefaultData = (type: string, name: string) => {
  const baseNode = {
    id: `node_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
    type,
    name,
    config: {} as Record<string, any>,
    ai_config: { ai_qa: true } as Record<string, any>
  }
  
  if (type === 'start') {
    baseNode.config = { desc: '', student_bg: '' }
    baseNode.ai_config.ai_welcome_enabled = true
    baseNode.ai_config.ai_welcome_tpl = '同学你好，欢迎来到本次实训！'
  } else if (type === 'end') {
    baseNode.config = { end_desc: '恭喜你完成了本次实训所有环节！' }
    delete baseNode.ai_config.ai_qa
  } else if (type === 'resource_read') {
    baseNode.config = { files: [{ original_name: '', display_name: '', min_progress: 100 }] }
    baseNode.ai_config = { ...baseNode.ai_config, ai_summary: true, ai_focus: true, ai_nav: true }
  } else if (type === 'video_watch') {
    baseNode.config = { video_file: '', video_title: '', min_progress: 100, allow_drag: false, allowed_speeds: [1.25, 1.5, 2] }
    baseNode.ai_config = { ...baseNode.ai_config, ai_subtitles: true, ai_chapters: true }
  } else if (type === 'mindmap_preview') {
    baseNode.config = { ai_generated: true, topic: '', max_nodes: 50, manual_structure: null }
  } else if (type === 'survey') {
    baseNode.config = { questions: [{ content: '' }] }
  } else if (type === 'mindmap_draw') {
    baseNode.config = { ai_generated: true, topic: '', max_nodes: 10, manual_structure: null }
  } else if (type === 'ai_practice') {
    baseNode.config = { knowledge_points: '', pass_criteria: '', max_rounds: '' }
    baseNode.ai_config = { ...baseNode.ai_config, ai_adaptive: true, ai_core_concept: true }
  } else if (type === 'theory_class') {
    baseNode.config = { topic: '', knowledge_points: '', max_cards: '' }
    baseNode.ai_config = { ...baseNode.ai_config, ai_mistake_book: true }
  } else if (type === 'task_distribute') {
    baseNode.config = { title: '', requirement: '', deadline: '', files: [] }
    baseNode.ai_config = { ...baseNode.ai_config, ai_task_breakdown: true }
  } else if (type === 'req_upload') {
    baseNode.config = { scenario: '', min_words: 10 }
  } else if (type === 'plan_upload') {
    baseNode.config = { allowed_formats: ['pdf', 'txt', 'word', 'ppt', 'md'], max_size: 100, requirement: '' }
    baseNode.ai_config = { ...baseNode.ai_config, ai_pre_eval: true }
  } else if (type === 'homework') {
    baseNode.config = { 
      source_mode: 'ai', time_limit: 30,
      count_single: 5, count_multi: 5, count_fill: 5, count_judge: 5, count_essay: 5, difficulty: 'medium', save_to_bank: true,
      select_mode: 'random', random_count: 10
    }
  } else if (type === 'coding_class') {
    baseNode.config = { template: '' }
    baseNode.ai_config = { ...baseNode.ai_config, ai_assist: true, ai_mode: 'guide', ai_review: true }
  } else if (type === 'student_peer_review') {
    baseNode.config = { target_node: '', count: 3, dimensions: [{ name: '', score_range: '1-10分' }], weight: 30 }
  } else if (type === 'teacher_eval') {
    baseNode.config = { target_node: '' }
  }
  return baseNode
}

// ==================== 2. 状态管理 ====================
const isSidebarCollapsed = ref(false)
const expandedCats = ref(MATERIAL_CATEGORIES.map(c => c.type))
const isAIExpanded = ref(false) 

const orchestrationList = ref<any[]>([
  createNodeDefaultData('start', '开始实训'),
  createNodeDefaultData('end', '结束实训')
])
const selectedNodeId = ref<string | null>(null)
const activeNode = computed(() => orchestrationList.value.find(n => n.id === selectedNodeId.value))

// 宽度需求3：侧边栏默认宽度 500
const configPanelWidth = ref(500)
let isResizing = false

const startPanelResize = () => {
  isResizing = true
  document.body.style.userSelect = 'none' 
  document.body.style.cursor = 'col-resize'
  window.addEventListener('mousemove', onPanelResize)
  window.addEventListener('mouseup', stopPanelResize)
}
const onPanelResize = (e: MouseEvent) => {
  if (!isResizing) return
  let newWidth = window.innerWidth - e.clientX
  if (newWidth < 300) newWidth = 300
  if (newWidth > 800) newWidth = 800
  configPanelWidth.value = newWidth
}
const stopPanelResize = () => {
  isResizing = false
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
  window.removeEventListener('mousemove', onPanelResize)
  window.removeEventListener('mouseup', stopPanelResize)
}

const getEvaluableNodes = () => {
  return orchestrationList.value.filter(n => ['req_upload', 'plan_upload', 'mindmap_draw'].includes(n.type))
}

// ==================== 3. 拖拽核心引擎 ====================
const dragState = ref<{ source: 'library' | 'list' | null, item: any, startIndex: number }>({
  source: null, item: null, startIndex: -1
})
const dropIndicatorIndex = ref(-1)
const isHoveringDelete = ref(false)

// [拖拽起点] - 从素材库
const onDragStartLibrary = (e: DragEvent, item: any) => {
  dragState.value = { source: 'library', item, startIndex: -1 }
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'copyMove'
    // 拖拽优化2：设置精准的幽灵影像(Ghost)，使得卡片能够完全“跟手”
    const target = e.currentTarget as HTMLElement
    e.dataTransfer.setDragImage(target, target.offsetWidth / 2, target.offsetHeight / 2)
  }
}

// [拖拽起点] - 从画布已有节点
const onDragStartNode = (e: DragEvent, index: number, node: any) => {
  dragState.value = { source: 'list', item: node, startIndex: index }
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    // 拖拽优化2：使用真实的 DOM 节点作为跟手残影
    const target = e.currentTarget as HTMLElement
    e.dataTransfer.setDragImage(target, target.offsetWidth / 2, target.offsetHeight / 2)
  }
  setTimeout(() => { if (e.target) (e.target as HTMLElement).style.opacity = '0.4' }, 0)
}

// [拖拽经过] - 计算插入空隙槽的位置 (拖拽优化2：监听绑定在了 wrapper 上，灵敏度大幅提升)
const onNodeDragOver = (e: DragEvent, index: number) => {
  if (dragState.value.startIndex === index && dragState.value.source === 'list') return
  
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  // 计算鼠标在包裹层内的Y轴百分比，上半部则插入其上，下半部则插入其下，极为顺滑
  const percentageY = (e.clientY - rect.top) / rect.height
  
  let targetInsertAfter = percentageY > 0.5 ? index : index - 1
  
  const maxIndex = orchestrationList.value.length - 2
  if (targetInsertAfter < 0) targetInsertAfter = 0
  if (targetInsertAfter > maxIndex) targetInsertAfter = maxIndex

  dropIndicatorIndex.value = targetInsertAfter
}

const onCanvasDragLeave = (e: DragEvent) => {
  if (!(e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) {
    dropIndicatorIndex.value = -1
  }
}

// [释放放置] - 画布区
const onDropCanvas = () => {
  if (dropIndicatorIndex.value === -1) { resetDrag(); return }

  const targetIndex = dropIndicatorIndex.value + 1
  
  if (dragState.value.source === 'library') {
    const newNode = createNodeDefaultData(dragState.value.item.type, getNextNodeName(dragState.value.item.type, dragState.value.item.label))
    orchestrationList.value.splice(targetIndex, 0, newNode)
    selectNode(newNode)
  } 
  else if (dragState.value.source === 'list') {
    const fromIndex = dragState.value.startIndex
    if (fromIndex !== targetIndex && fromIndex !== targetIndex - 1) {
      const [moved] = orchestrationList.value.splice(fromIndex, 1)
      const adjTarget = fromIndex < targetIndex ? targetIndex - 1 : targetIndex
      orchestrationList.value.splice(adjTarget, 0, moved)
    }
  }
  resetDrag()
}

const onSidebarDragOver = () => {
  if (isSidebarCollapsed.value) return
  if (dragState.value.source === 'list') isHoveringDelete.value = true
}
const onSidebarDragLeave = (e: DragEvent) => {
  if ((e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) return
  isHoveringDelete.value = false
}
const onSidebarDrop = () => {
  if (dragState.value.source === 'list') {
    deleteNode(dragState.value.startIndex)
  }
  resetDrag()
}

const onDragEnd = (e: DragEvent) => {
  if (e.target) (e.target as HTMLElement).style.opacity = '1'
  resetDrag()
}
const resetDrag = () => {
  dragState.value = { source: null, item: null, startIndex: -1 }
  dropIndicatorIndex.value = -1
  isHoveringDelete.value = false
}

// ==================== 4. 辅助与交互方法 ====================
const toggleCategory = (type: string) => {
  const idx = expandedCats.value.indexOf(type)
  if (idx > -1) expandedCats.value.splice(idx, 1); else expandedCats.value.push(type)
}
const toggleAIConfig = () => { isAIExpanded.value = !isAIExpanded.value }
const hasAIConfig = (type: string) => !['end'].includes(type)
const getNodeIcon = (type: string) => {
  if (type === 'start') return '🟢'; if (type === 'end') return '🔴'
  for (const cat of MATERIAL_CATEGORIES) {
    const it = cat.items.find(i => i.type === type); if (it) return it.icon
  }
  return '📦'
}
const getCategoryLabel = (type: string) => {
  if (['start', 'end'].includes(type)) return '通用流程'
  for (const cat of MATERIAL_CATEGORIES) {
    if (cat.items.some(i => i.type === type)) return cat.label
  }
  return '未分类'
}
const getNextNodeName = (type: string, baseLabel: string) => {
  const count = orchestrationList.value.filter(n => n.type === type).length
  return count === 0 ? baseLabel : `${baseLabel} (${count + 1})`
}

const addNodeToCanvas = (type: string, label: string) => {
  const newNode = createNodeDefaultData(type, getNextNodeName(type, label))
  orchestrationList.value.splice(orchestrationList.value.length - 1, 0, newNode)
  selectNode(newNode)
  setTimeout(() => {
    const scrollArea = document.querySelector('.pipeline-scroll-area')
    if (scrollArea) scrollArea.scrollTo({ top: scrollArea.scrollHeight, behavior: 'smooth' })
  }, 100)
}

const selectNode = (node: any) => {
  selectedNodeId.value = node.id
  isAIExpanded.value = false 
}
const deleteNode = (index: number) => {
  if (['start', 'end'].includes(orchestrationList.value[index].type)) return
  if (selectedNodeId.value === orchestrationList.value[index].id) selectedNodeId.value = null
  orchestrationList.value.splice(index, 1)
}
const moveNodeUp = (idx: number) => { onDragStartNode(null as any, idx, null); dropIndicatorIndex.value = idx - 2; onDropCanvas() }
const moveNodeDown = (idx: number) => { onDragStartNode(null as any, idx, null); dropIndicatorIndex.value = idx + 1; onDropCanvas() }

const NODE_TYPE_MAP: Record<string, string> = {
  start: 'START',
  end: 'END',
  resource_read: 'RESOURCE_READ',
  video_watch: 'VIDEO_LEARN',
  mindmap_preview: 'MINDMAP_PREVIEW',
  mindmap_draw: 'MINDMAP_DRAW',
  task_distribute: 'TASK_DISTRIBUTE',
  req_upload: 'REQ_UPLOAD',
  plan_upload: 'PLAN_UPLOAD',
  homework: 'HOMEWORK',
  student_peer_review: 'STUDENT_PEER_REVIEW',
  teacher_eval: 'TEACHER_COMMENT',
  ai_practice: 'AI_PRACTICE',
  theory_class: 'THEORY_CLASS',
  coding_class: 'CODING_CLASS'
}

const buildNodeConfig = (node: any) => {
  const { type, config, ai_config } = node
  const cfg: Record<string, any> = {}

  switch (type) {
    case 'start':
      cfg.desc = config.desc || ''
      cfg.student_bg = config.student_bg || ''
      cfg.enable_ai_welcome = ai_config.ai_welcome_enabled ?? true
      cfg.welcome_prompt = ai_config.ai_welcome_tpl || ''
      break

    case 'end':
      cfg.end_desc = config.end_desc || ''
      break

    case 'resource_read':
      cfg.resource_list = (config.files || []).map((f: any) => ({
        resource_id: f.original_name || '',
        resource_name: f.display_name || '',
        require_full_read: f.min_progress ?? 100
      }))
      cfg.enable_ai_summary = ai_config.ai_summary ?? true
      cfg.enable_ai_key_points = ai_config.ai_focus ?? true
      cfg.enable_ai_quick_nav = ai_config.ai_nav ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'video_watch':
      cfg.video_id = config.video_file || ''
      cfg.video_title = config.video_title || ''
      cfg.min_watch_progress = config.min_progress ?? 100
      cfg.allow_drag = config.allow_drag ?? false
      cfg.allow_speed = config.allowed_speeds || []
      cfg.enable_ai_subtitle = ai_config.ai_subtitles ?? true
      cfg.enable_ai_chapter = ai_config.ai_chapters ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'mindmap_preview':
    case 'mindmap_draw':
      cfg.enable_ai_generate_map = config.ai_generated ?? true
      if (config.ai_generated) {
        cfg.scenario = config.topic || ''
        cfg.max_nodes = config.max_nodes ?? (type === 'mindmap_preview' ? 50 : 10)
      } else {
        cfg.base_map_data = config.manual_structure || null
      }
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'task_distribute':
      cfg.task_title = config.title || ''
      cfg.task_desc = config.requirement || ''
      cfg.deadline = config.deadline || ''
      cfg.resource_list = (config.files || []).map((f: any) => ({
        resource_id: f.original_name || '',
        resource_name: f.display_name || ''
      }))
      cfg.enable_ai_task_split = ai_config.ai_task_breakdown ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'req_upload':
      cfg.scenario = config.scenario || ''
      cfg.min_length = config.min_words ?? 10
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'plan_upload':
      cfg.allowed_formats = config.allowed_formats || []
      cfg.max_size_mb = config.max_size ?? 100
      cfg.upload_requirements = config.requirement || ''
      cfg.enable_ai_pre_evaluation = ai_config.ai_pre_eval ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'homework':
      cfg.source_mode = config.source_mode === 'bank' ? 'db' : 'ai'
      cfg.time_limit_mins = config.time_limit ?? 30
      if (config.source_mode === 'bank') {
        cfg.db_selection_mode = config.select_mode || 'random'
        cfg.question_bank_ids = []
        cfg.question_ids = []
        cfg.random_count = config.random_count ?? 10
      } else {
        cfg.question_type_counts = {
          single_choice: config.count_single ?? 5,
          multi_choice: config.count_multi ?? 5,
          fill_blank: config.count_fill ?? 5,
          true_false: config.count_judge ?? 5,
          essay: config.count_essay ?? 5
        }
        cfg.difficulty_level = config.difficulty || 'medium'
        cfg.save_to_db = config.save_to_bank ?? true
      }
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'student_peer_review':
      cfg.target_node_id = config.target_node || ''
      cfg.review_count = config.count ?? 3
      cfg.evaluation_dimensions = (config.dimensions || []).map((d: any) => ({
        dimension_name: d.name || '',
        score_range: d.score_range || ''
      }))
      cfg.review_weight_percentage = config.weight ?? 30
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'teacher_eval':
      cfg.target_node_id = config.target_node || ''
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'ai_practice':
      cfg.knowledgePoint = config.knowledge_points || ''
      cfg.pass_criteria = config.pass_criteria || ''
      cfg.max_rounds = config.max_rounds || ''
      cfg.enable_adaptive_difficulty = ai_config.ai_adaptive ?? true
      cfg.enable_concept_check = ai_config.ai_core_concept ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'theory_class':
      cfg.topic = config.topic || ''
      cfg.knowledge_points = config.knowledge_points || ''
      cfg.card_count = config.max_cards || ''
      cfg.enable_ai_error_book = ai_config.ai_mistake_book ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break

    case 'coding_class':
      cfg.init_code = config.template || ''
      cfg.allow_ai_help = ai_config.ai_assist ?? true
      cfg.ai_help_mode = ai_config.ai_mode || 'guide'
      cfg.enable_code_review = ai_config.ai_review ?? true
      cfg.allow_ai_qa = ai_config.ai_qa ?? true
      break
  }

  return cfg
}

const validateRequired = (): string[] => {
  const errors: string[] = []

  for (const node of orchestrationList.value) {
    const { type, config, name } = node

    switch (type) {
      case 'start':
        if (!config.desc?.trim()) {
          errors.push(`"${name}"：实训整体说明(场景)为必填项`)
        }
        break

      case 'resource_read':
        if (!config.files || config.files.length === 0 || !config.files[0].display_name?.trim()) {
          errors.push(`"${name}"：至少添加一个阅读文件并填写展示名`)
        }
        break

      case 'video_watch':
        if (!config.video_file?.trim()) {
          errors.push(`"${name}"：上传视频文件为必填项`)
        }
        break

      case 'mindmap_preview':
      case 'mindmap_draw':
        if (config.ai_generated && !config.topic?.trim()) {
          errors.push(`"${name}"：AI生成模式下导图主题为必填项`)
        }
        break
    }
  }

  return errors
}

const handlePublish = () => {
  // 先显示弹窗让用户输入模板名称
  templateName.value = ''
  showPublishModal.value = true
}

const confirmPublish = () => {
  if (!templateName.value.trim()) {
    alert('请输入模板名称')
    return
  }

  const errors = validateRequired()
  if (errors.length > 0) {
    alert('请完善以下必填项：\n\n' + errors.join('\n'))
    return
  }

  const nodes = orchestrationList.value.map((node) => ({
    node_id: node.id,
    node_type: NODE_TYPE_MAP[node.type] || node.type.toUpperCase(),
    name: node.name,
    config: buildNodeConfig(node)
  }))

  const edges: { source: string; target: string }[] = []
  for (let i = 0; i < nodes.length - 1; i++) {
    const current = nodes[i]
    const next = nodes[i + 1]
    if (current && next) {
      edges.push({ source: current.node_id, target: next.node_id })
    }
  }

  const result = {
    templateName: templateName.value,
    orchestration_id: `flow_${Date.now().toString(36)}`,
    nodes,
    edges
  }

  console.log('=== 实训编排 JSON ===')
  console.log(JSON.stringify(result, null, 2))

  // 报告当前页面没有对应设置的字段
  console.warn('=== 以下字段在当前页面中没有对应设置，输出中使用了占位值 ===')
  console.warn('1. RESOURCE_READ.resource_list[].resource_id — 页面只有"上传文件名"和"展示名"，无"关联文件唯一标识"字段，暂用 original_name 填充')
  console.warn('2. VIDEO_LEARN.video_id — 页面只有"上传视频文件"文本输入，无"视频源唯一标识"字段，暂用 video_file 填充')
  console.warn('3. TASK_DISTRIBUTE.resource_list[].resource_id — 同资源阅读，无"关联文件唯一标识"字段')
  console.warn('4. HOMEWORK.question_bank_ids / question_ids — 页面无"题库来源集合"和"手动选取题目列表"字段，输出为空数组')
  console.warn('5. 学情调查(SURVEY) 节点 — 你提供的规范中未包含该类型映射，输出 node_type 为 SURVEY')

  showPublishModal.value = false
  alert(`实训模板「${templateName.value}」保存并发布成功！(请按F12查看控制台数据)`)
}
</script>

<style scoped>
/* =========== 1. 基础框架布局 =========== */
.pipeline-create-wrapper { display: flex; height: calc(100vh - 64px); background: #F1F5F9; overflow: hidden; box-sizing: border-box; font-family: system-ui, sans-serif; }
.flex-spacer { flex: 1; }

/* =========== 2. 左侧栏与回收站 =========== */
.material-sidebar { width: 300px; background: #FFF; border-right: 1px solid #E2E8F0; display: flex; flex-direction: column; position: relative; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); flex-shrink: 0; z-index: 10; }
.material-sidebar.is-collapsed { width: 0; border-right: none; }
.material-sidebar.is-drag-over-delete { background: #FEF2F2; border-color: #EF4444; }
.delete-overlay { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #EF4444; font-weight: bold; border: 4px dashed #FCA5A5; background: rgba(254, 242, 242, 0.9); z-index: 50; }
.trash-icon { font-size: 40px; margin-bottom: 10px; animation: bounce 1s infinite; }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }

.sidebar-inner { width: 300px; height: 100%; padding: 20px 16px; box-sizing: border-box; display: flex; flex-direction: column; }
.panel-main-title { font-size: 18px; font-weight: 800; color: #0F172A; margin-bottom: 16px; }
.sidebar-toggle { position: absolute; right: -16px; top: 50%; transform: translateY(-50%); width: 16px; height: 48px; background: #FFF; border: 1px solid #E2E8F0; border-left: none; border-radius: 0 6px 6px 0; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #94A3B8; font-size: 10px; z-index: 21; transition: 0.2s; }
.sidebar-toggle:hover { color: #4F46E5; }

.category-container { flex: 1; overflow-y: auto; padding-right: 4px; }
.category-container::-webkit-scrollbar { width: 4px; }
.category-container::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 4px; cursor: pointer; border-radius: 6px; transition: 0.2s; }
.cat-header:hover { background: #F8FAFC; }
.cat-label { font-weight: 600; color: #475569; font-size: 14px; }
.pure-chevron { width: 6px; height: 6px; border-right: 2px solid #94A3B8; border-bottom: 2px solid #94A3B8; transform: rotate(-45deg); transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.pure-chevron.is-rotated { transform: rotate(45deg); }

.drawer-animator { display: grid; grid-template-rows: 0fr; transition: grid-template-rows 0.3s ease-out; }
.drawer-animator.is-open { grid-template-rows: 1fr; }
.drawer-inner { min-height: 0; overflow: hidden; }

.node-list { display: flex; flex-direction: column; gap: 8px; padding: 4px 0 12px 0; }
.draggable-node { padding: 10px 14px; background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px; cursor: grab; display: flex; align-items: center; gap: 10px; transition: all 0.2s; font-size: 14px; color: #334155; }
.draggable-node:hover { background: #EEF2FF; border-color: #818CF8; color: #4F46E5; transform: translateX(4px); }
.draggable-node:active { cursor: grabbing; transform: scale(0.98); }

/* =========== 3. 中央画布区 =========== */
.canvas-container { flex: 1; display: flex; flex-direction: column; position: relative; }
.canvas-top-toolbar { display: flex; align-items: center; padding: 16px 24px; background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); border-bottom: 1px solid #E2E8F0; z-index: 5; }
.pipeline-title { font-size: 16px; font-weight: 700; color: #0F172A; }
.secondary-action-btn { padding: 8px 20px; background: #4F46E5; color: #FFF; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.2s; box-shadow: 0 4px 6px -1px rgba(79,70,229,0.2); box-sizing: border-box; }
.secondary-action-btn.w-full { width: 100%; }
.secondary-action-btn:hover { background: #4338CA; transform: translateY(-1px); }

.pipeline-scroll-area { flex: 1; overflow-y: auto; padding: 40px; display: flex; flex-direction: column; align-items: center; }
.pipeline-queue { width: 100%; max-width: 560px; display: flex; flex-direction: column; align-items: center; padding-bottom: 60px; position: relative; }

.list-anim-move, .list-anim-enter-active, .list-anim-leave-active { transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1); }
.list-anim-enter-from { opacity: 0; transform: translateX(-40px) scale(0.9); }
.list-anim-leave-to { opacity: 0; transform: scale(0.8) translateX(40px); }
.list-anim-leave-active { position: absolute; width: 100%; }

.pipeline-item-wrapper { width: 100%; display: flex; flex-direction: column; align-items: center; }

.pipeline-node { width: 100%; background: #FFF; border: 2px solid #E2E8F0; border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; cursor: grab; transition: all 0.25s cubic-bezier(0.25, 1, 0.5, 1); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); user-select: none; }
.pipeline-node:hover { border-color: #A5B4FC; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); transform: translateY(-2px); }
.pipeline-node:active { cursor: grabbing; }
.pipeline-node.is-active { border-color: #4F46E5; background: #EEF2FF; box-shadow: 0 0 0 4px rgba(79,70,229,0.1); transform: scale(1.02); }
.pipeline-node.is-fixed { border-style: dashed; cursor: pointer; }
.pipeline-node.is-fixed.is-active { border-style: solid; }
.pipeline-node.is-dragging { opacity: 0.5; transform: scale(0.98); border-color: #818CF8; }

.pipeline-node.is-empty-placeholder { background: transparent; border: 2px dashed #94A3B8; justify-content: center; color: #64748B; font-size: 14px; font-weight: 600; cursor: default; min-height: 78px; box-sizing: border-box; }
.pipeline-node.is-empty-placeholder:hover { transform: none; box-shadow: none; }

.node-index { width: 32px; height: 32px; background: #F1F5F9; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #64748B; flex-shrink: 0; }
.pipeline-node.is-active .node-index { background: #4F46E5; color: #FFF; }
.node-content { flex: 1; display: flex; flex-direction: column; gap: 4px; pointer-events: none; }
.node-title { font-size: 16px; font-weight: 600; color: #0F172A; display: flex; align-items: center; gap: 8px; }
.node-type-tag { font-size: 12px; color: #64748B; background: #F1F5F9; padding: 2px 8px; border-radius: 4px; width: fit-content; }

.node-actions { display: flex; gap: 6px; opacity: 0; transition: opacity 0.2s; }
.pipeline-node:hover .node-actions { opacity: 1; }
.icon-btn { width: 28px; height: 28px; border-radius: 6px; border: 1px solid #E2E8F0; background: #FFF; color: #475569; cursor: pointer; display: flex; align-items: center; justify-content: center; font-weight: bold; transition: 0.2s; }
.icon-btn:hover:not(:disabled) { background: #F1F5F9; border-color: #CBD5E1; color: #0F172A; }
.icon-btn.danger:hover { background: #FEF2F2; color: #EF4444; border-color: #FECACA; }
.icon-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.pipeline-connector { height: 32px; width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94A3B8; transition: height 0.3s cubic-bezier(0.25, 1, 0.5, 1); pointer-events: none; }
.pipeline-connector .line { width: 2px; height: 20px; background: #CBD5E1; }
.pipeline-connector .arrow { font-size: 10px; margin-top: -4px; color: #CBD5E1; }

.pipeline-connector.is-drag-target { height: 70px; }
.drop-placeholder-box { width: 100%; height: 50px; background: #EEF2FF; border: 2px dashed #4F46E5; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #4F46E5; font-size: 14px; font-weight: 700; box-shadow: 0 0 15px rgba(79,70,229,0.15); animation: pulseBox 1.5s infinite; pointer-events: none; }
@keyframes pulseBox { 0%, 100% { opacity: 0.8; transform: scale(0.98); } 50% { opacity: 1; transform: scale(1); } }

/* =========== 4. 右侧配置面板 =========== */
.config-panel { background: #FFF; border-left: 1px solid #E2E8F0; display: flex; flex-direction: column; flex-shrink: 0; z-index: 10; box-shadow: -4px 0 15px rgba(0,0,0,0.03); position: relative; will-change: width; }

.resize-handle { position: absolute; left: -3px; top: 0; bottom: 0; width: 6px; cursor: col-resize; z-index: 20; transition: background 0.2s; }
.resize-handle:hover, .resize-handle:active { background: #4F46E5; opacity: 0.5; }

.config-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #E2E8F0; background: #F8FAFC; }
.config-header h3 { font-size: 15px; font-weight: 700; color: #0F172A; margin: 0; }
.close-btn { background: none; border: none; font-size: 16px; cursor: pointer; color: #94A3B8; transition: 0.2s; }
.close-btn:hover { color: #EF4444; transform: rotate(90deg); }

.config-body { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; font-size: 13px; }
.config-body::-webkit-scrollbar { width: 6px; }
.config-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 6px; }

.section-title { font-size: 12px; font-weight: 800; color: #64748B; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
.config-field { display: flex; flex-direction: column; gap: 8px; width: 100%; box-sizing: border-box; }
.config-field label { font-size: 13px; font-weight: 600; color: #334155; }
.config-field.flex-row { flex-direction: row; align-items: center; justify-content: space-between; gap: 8px; }
.config-field.flex-row label { flex: 1; margin: 0; }
.config-field.flex-row input[type="checkbox"], .config-field.flex-row input[type="radio"] { width: 16px; height: 16px; cursor: pointer; accent-color: #4F46E5; margin: 0; }

.checkbox-group { display: flex; flex-wrap: wrap; gap: 12px; }
.checkbox-group label { display: flex; align-items: center; gap: 4px; font-weight: normal; font-size: 13px; cursor: pointer; }
.checkbox-group input { width: 14px; height: 14px; accent-color: #4F46E5; }

.ml-2 { margin-left: 4px; }
.w-24 { width: 96px; }
.w-32 { width: 128px; }
.font-mono { font-family: monospace; }

.required { color: #EF4444; margin-right: 4px; }
.config-input, .config-textarea, select.config-input { padding: 10px 12px; border: 1px solid #CBD5E1; border-radius: 6px; font-size: 13px; font-family: inherit; outline: none; background: #F8FAFC; transition: 0.2s; color: #0F172A; width: 100%; box-sizing: border-box; }
.config-input:focus, .config-textarea:focus, select.config-input:focus { border-color: #4F46E5; background: #FFF; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
.config-textarea { resize: vertical; min-height: 80px; line-height: 1.5; }

.list-container { width: 100%; }
.table-wrapper { width: 100%; overflow-x: auto; border: 1px solid #E2E8F0; border-radius: 6px; margin-bottom: 8px; }
.resizable-table { width: 100%; border-collapse: collapse; min-width: 300px; }
.resizable-table th { background: #F8FAFC; color: #64748B; font-size: 12px; font-weight: 600; padding: 8px; text-align: left; border-bottom: 1px solid #E2E8F0; position: relative; }
.resizable-th { resize: horizontal; overflow: hidden; border-right: 1px solid #CBD5E1; }
.resizable-table td { padding: 4px; border-bottom: 1px solid #F1F5F9; }
.resizable-table tr:last-child td { border-bottom: none; }
.text-center { text-align: center; }
.borderless { border: none; background: transparent; padding: 8px; box-shadow: none !important; }
.borderless:focus { background: #F8FAFC; border-radius: 4px; }

.icon-btn-small { width: 24px; height: 24px; border-radius: 4px; border: none; background: #FEF2F2; color: #EF4444; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 12px; transition: 0.2s; }
.icon-btn-small:hover { background: #FECACA; transform: scale(1.1); }
.add-btn { background: #F8FAFC; border: 1px dashed #CBD5E1; color: #4F46E5; font-size: 13px; padding: 10px; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.2s; width: 100%; }
.add-btn:hover { background: #EEF2FF; border-color: #A5B4FC; }

.upload-btn { display: block; width: 100%; padding: 10px 12px; background: #F8FAFC; border: 1px solid #CBD5E1; border-radius: 6px; color: #4F46E5; font-size: 13px; font-weight: 600; cursor: pointer; text-align: center; transition: 0.2s; box-sizing: border-box; }
.upload-btn:hover { background: #EEF2FF; border-color: #818CF8; }
.upload-btn-sm { display: inline-block; padding: 6px 10px; background: #F8FAFC; border: 1px solid #CBD5E1; border-radius: 4px; color: #4F46E5; font-size: 12px; font-weight: 600; cursor: pointer; text-align: center; transition: 0.2s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px; }
.upload-btn-sm:hover { background: #EEF2FF; border-color: #818CF8; }
.hint { text-align: center; color: #94A3B8; font-size: 13px; padding: 30px 0; background: #F8FAFC; border-radius: 8px; border: 1px dashed #E2E8F0; }

/* =========== AI 增强配置区 =========== */
.ai-config-wrapper { margin-top: 8px; border: 1px solid #E0E7FF; border-radius: 8px; overflow: hidden; background: #FAFAFF; transition: 0.3s; }
.ai-config-wrapper:hover { border-color: #C7D2FE; box-shadow: 0 4px 6px -1px rgba(67,56,202,0.05); }
.ai-config-header { padding: 12px 16px; background: #EEF2FF; display: flex; justify-content: space-between; align-items: center; cursor: pointer; user-select: none; }
.ai-title { font-size: 13px; font-weight: 700; color: #4338CA; }
.ai-config-header .pure-chevron { border-color: #4338CA; }
.ai-config-body { padding: 16px; display: flex; flex-direction: column; gap: 14px; border-top: 1px solid #E0E7FF; }

.panel-slide-enter-active, .panel-slide-leave-active { transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1); }
.panel-slide-enter-from, .panel-slide-leave-to { width: 0 !important; opacity: 0; transform: translateX(30px); margin-left: -1px; }

/* =========== 发布模板弹窗 =========== */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; animation: fadeIn 0.2s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-content { background: #FFF; border-radius: 12px; width: 90%; max-width: 450px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25); animation: slideUp 0.2s ease; }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #E2E8F0; }
.modal-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0F172A; }

.modal-close { width: 28px; height: 28px; border: none; background: #F1F5F9; border-radius: 6px; color: #64748B; font-size: 20px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s; }
.modal-close:hover { background: #E2E8F0; color: #334155; }

.modal-body { padding: 20px; }
.modal-body .form-group { margin-bottom: 0; }
.modal-body .form-group label { display: block; font-size: 13px; font-weight: 600; color: #334155; margin-bottom: 8px; }

.modal-input { width: 100%; padding: 12px 14px; border: 1px solid #CBD5E1; border-radius: 8px; font-size: 14px; font-family: inherit; outline: none; background: #F8FAFC; transition: 0.2s; color: #0F172A; box-sizing: border-box; }
.modal-input:focus { border-color: #4F46E5; background: #FFF; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
.modal-input::placeholder { color: #94A3B8; }

.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 20px; border-top: 1px solid #E2E8F0; }

.btn { padding: 10px 20px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.2s; border: none; }
.btn-secondary { background: #F1F5F9; color: #64748B; }
.btn-secondary:hover { background: #E2E8F0; color: #475569; }
.btn-primary { background: #4F46E5; color: #FFF; }
.btn-primary:hover { background: #4338CA; box-shadow: 0 4px 12px rgba(79,70,229,0.3); }
</style>