<template>
  <div class="parking-container">
    <div class="page-header">
      <h2>模拟停车</h2>
      <p>选择停车区域和停车位，登记停车记录</p>
    </div>

    <!-- 区域标签页 + 停车位网格 -->
    <div class="grid-section">
      <div class="section-title">
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7" rx="1"></rect>
          <rect x="14" y="3" width="7" height="7" rx="1"></rect>
          <rect x="14" y="14" width="7" height="7" rx="1"></rect>
          <rect x="3" y="14" width="7" height="7" rx="1"></rect>
        </svg>
        <span>区域停车位</span>
      </div>

      <el-tabs
        v-model="activeAreaId"
        type="border-card"
        class="area-tabs"
        @tab-change="handleTabChange"
      >
        <el-tab-pane
          v-for="area in areaList"
          :key="area.id"
          :label="area.areaCode"
          :name="area.id"
        />
      </el-tabs>

      <div class="grid-legend">
        <div class="legend-item">
          <span class="legend-dot free"></span>
          <span>空闲</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot occupied"></span>
          <span>已占用</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot fault"></span>
          <span>故障/维护</span>
        </div>
      </div>

      <div v-loading="loadingGridSpaces" class="grid-scroll-wrapper">
        <div class="space-grid">
          <div
            v-for="space in gridSpaceList"
            :key="space.id"
            class="space-cell"
            :class="getSpaceStatusClass(space.status)"
            @click="handleSpaceClick(space)"
          >
            {{ space.spaceCode }}
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧：选择表单 -->
      <div class="form-section">
        <div class="section-title">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
            <path d="M2 17l10 5 10-5"></path>
            <path d="M2 12l10 5 10-5"></path>
          </svg>
          <span>选择停车位</span>
        </div>

        <div class="form-item">
          <label>停车区域</label>
          <el-select
            v-model="parkingForm.areaId"
            placeholder="请选择停车区域"
            @change="handleAreaChange"
            :disabled="loadingAreas"
          >
            <el-option
              v-for="area in areaList"
              :key="area.id"
              :label="area.areaName + ' (' + area.areaCode + ')'"
              :value="area.id"
            />
          </el-select>
        </div>

        <div class="form-item">
          <label>停车位</label>
          <el-select
            v-model="parkingForm.spaceId"
            placeholder="请选择停车位"
            :disabled="!parkingForm.areaId || loadingSpaces"
          >
            <el-option
              v-for="space in availableSpaceList"
              :key="space.id"
              :label="space.spaceName + ' (' + space.spaceCode + ')'"
              :value="space.id"
            />
          </el-select>
        </div>

        <div class="form-item">
          <label>车牌号</label>
          <el-input
            v-model="parkingForm.carNumber"
            placeholder="请输入车牌号（如：京A12345）"
            maxlength="20"
          />
        </div>

        <div class="form-item">
          <label>当前用户</label>
          <div class="user-info">
            <svg class="user-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>{{ currentUsername }}</span>
          </div>
        </div>

        <div class="form-actions">
          <el-button
            type="primary"
            :loading="isParking"
            :disabled="!parkingForm.areaId || !parkingForm.spaceId"
            @click="handleStartParking"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path>
            </svg>
            确认停车
          </el-button>
        </div>
      </div>

      <!-- 右侧：动画展示区域 -->
      <div class="animation-section">
        <div class="section-title">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
          <span>停车信息预览</span>
        </div>

        <div class="animation-container">
          <!-- 车辆 -->
          <div class="animation-item car-item" :class="{ selected: true }">
            <div class="icon-wrapper">
              <svg t="1781176570972" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" class="car-svg">
                <path d="M207.8 566.7l-91.4 0.1c-8.2 0-14.8 6.7-14.8 14.9l0.3 241.5c0 8.2 6.7 14.8 14.9 14.8l91.4-0.1c8.2 0 14.8-6.7 14.8-14.9l-0.4-241.5c0-8.1-6.6-14.8-14.8-14.8zM818.6 565.8l91.4-0.1c8.2 0 14.9 6.6 14.9 14.8l0.4 241.5c0 8.2-6.6 14.9-14.8 14.9l-91.4 0.1c-8.2 0-14.9-6.6-14.9-14.8l-0.3-241.5c-0.1-8.2 6.5-14.8 14.7-14.9z" fill="#5B79FB"/>
                <path d="M237.1 368.9l275.8-0.4 275.8-0.4c21.2 0 41.7 5.1 59.9 14.4h0.1l-61.2-155.8c-3.5-8.8-8.6-16.9-15.4-23.5-17.9-17.6-43.7-28.8-71.8-30.7l-6.7-0.4c-50.4-3.4-115.3-5.3-181-5.2-65.6 0.1-130.6 2.2-180.9 5.7l-6.7 0.4c-28.1 2-53.8 13.2-71.7 30.9-6.7 6.6-11.9 14.7-15.3 23.5l-60.7 156h0.1c18.1-9.4 38.5-14.5 59.7-14.5z" fill="#A4BEFF"/>
                <path d="M805.5 331.7c6.9 11.4-2.2 25.2-16.5 25.2h-0.3l-551.6 0.8h-0.4c-14.2 0.1-23.4-13.7-16.5-25.2l27.1-84.7c6.6-20.7 22-38.1 42.8-47 11-4.7 23.2-7.6 36-8.5l6.7-0.4c50.9-3.4 114.8-5.3 179.9-5.4 65.1-0.1 129 1.6 179.9 4.9l6.7 0.4c11.8 0.8 23.1 3.3 33.5 7.3 22 8.6 38.6 26.4 45.6 48l27.1 84.6z" fill="#F0F5FF"/>
                <path d="M636.3 326.6l-246.8 0.4c-27.2 0-45.4-28-34.2-52.8l20.5-45.6c6-13.4 19.4-22.1 34.1-22.1l205.7-0.3c14.7 0 28.1 8.6 34.2 22l20.6 45.6c11.2 24.7-6.9 52.8-34.1 52.8z" fill="#FFFFFF"/>
                <path d="M474.5 357.4v-7.8c0-25.2-20.5-45.5-45.6-45.5h-21.3c7.2-3.2 12.3-10.5 12.3-18.9v-9c0-11.4-9.3-20.6-20.7-20.6l-45.1 0.1c-11.4 0-20.6 9.3-20.6 20.7v9c0 8.4 5.1 15.7 12.3 18.9h-21.3c-25.2 0-45.5 20.5-45.5 45.6v7.8l195.5-0.3zM746.9 357v-7.8c0-25.2-20.5-45.5-45.6-45.5H680c7.2-3.2 12.3-10.5 12.3-18.9v-9c0-11.4-9.3-20.6-20.7-20.6l-45.1 0.1c-11.4 0-20.6 9.3-20.6 20.7v9c0 8.4 5.1 15.7 12.3 18.9h-21.3c-25.2 0-45.5 20.5-45.5 45.6v7.8l195.5-0.3z" fill="#FF7E71"/>
                <path d="M512.9 368.5l275.8-0.4c41.3-0.1 80 19.2 104 51.6l10.4 20.5c7.1 14 14.6 27.8 23 41 19 30 14 45.1 14.1 86.8l0.2 107.5c0.1 35.7-28.9 64.8-64.6 64.8l-362.3 0.5-362.3 0.5c-35.7 0.1-64.8-28.9-64.8-64.6l-0.2-107.5c-0.1-41.6-5.1-56.7 13.8-86.8 8.3-13.3 15.8-27.1 22.9-41.1l10.3-20.5c23.9-32.5 62.6-51.8 103.8-51.9l275.9-0.4z" fill="#A4BEFF"/>
                <path d="M804.8 622.4c-17.1 3-33.3 9.8-47.4 20-19.4 14-42.8 21.6-66.7 21.6l-177.3 0.3-177.4 0.2c-24 0-47.3-7.5-66.8-21.5-14.1-10.1-30.3-16.9-47.5-19.8L144.2 610c-18-3.1-34.9-10.5-49.4-21.3l0.1 88c0 31 25.3 56.1 56.2 56.1l724.6-1c30.9 0 56.1-25.3 56-56.2l-0.1-88c-14.4 10.9-31.3 18.4-49.3 21.5l-77.5 13.3z" fill="#83A4FF"/>
                <path d="M100.8 518.3L98.3 553c-1.3 18.1 12.7 33.6 30.8 34.3l103.1 14.1c8 0.3 14.1-7.3 12-15.1l-10.8-41.2c-4.9-18.8-20.1-33.2-39.2-37.1l-63-12.8c-15-3.1-29.3 7.8-30.4 23.1z" fill="#5B79FB"/>
                <path d="M218.1 555.1c0 14.7-11.9 26.7-26.6 26.7-14.7 0-26.7-11.9-26.7-26.6 0-14.7 11.9-26.7 26.6-26.7 14.7-0.1 26.7 11.9 26.7 26.6z" fill="#E5ECFF"/>
                <path d="M925.5 517.1l2.6 34.7c1.4 18.1-12.6 33.7-30.7 34.3l-103.1 14.4c-8.1 0.3-14.1-7.3-12.1-15.1l10.6-41.2c4.9-18.8 20-33.3 39.1-37.2l63-13c15.1-3 29.4 7.8 30.6 23.1z" fill="#5B79FB"/>
                <path d="M808.3 554.2c0 14.7 12 26.6 26.7 26.6s26.6-12 26.6-26.7-12-26.6-26.7-26.6c-14.7 0.1-26.6 12-26.6 26.7z" fill="#E5ECFF"/>
                <path d="M773 697.6l-6.4-13.4c-9-18.8-22.6-29.7-37.1-29.7l-216.2 0.3-216.2 0.3c-14.4 0-28.1 11-37 29.8l-6.4 13.4c-4.6 9.8-0.4 24.2 7.1 24.1l505.1-0.7c7.6 0 11.8-14.3 7.1-24.1zM766.8 544.6l-121.8 12c-43.6 4.3-87.9 6.5-131.8 6.5-43.8 0.1-88.2-2-131.8-6.2l-121.9-11.6h-0.1c-3.5-0.3-6.2 2.7-5 5.7l9.7 23c12.3 29.1 41.9 49.5 76.3 52.5l120.1 10.7c35.1 3.1 70.4 3.1 105.4-0.2L686 626c34.4-3.2 63.9-23.6 76.1-52.7l9.7-23c1.3-3-1.4-6-5-5.7 0.1 0 0 0 0 0z" fill="#5B79FB"/>
                <path d="M542.5 600.6c0 16.1-13.1 29.3-29.2 29.3s-29.3-13-29.3-29.2 13.1-29.3 29.2-29.3c16.2-0.1 29.3 13 29.3 29.2z" fill="#F0F5FF"/>
                <path d="M177.3 383.4l10.7-18.8c-7.7 0.7-15.4-3.1-19-10.2l-6.4-12.3c-6.5-12.5-19.8-20.4-34.3-20.4h-25.4c-11.6 0-22.5 5.7-28.8 15.1l-2.7 4 101.5 44.9c1.4-0.7 2.9-1.5 4.4-2.3z" fill="#A4BEFF"/>
                <path d="M71.4 340.9l-3.6 5.4c-7.3 10.9-2.8 25.5 9.5 30.7l66.7 31.2c8.5-8.8 18.3-16.4 28.9-22.4L71.4 340.9z" fill="#5B79FB"/>
                <path d="M848.6 382.4l-10.8-18.8c7.7 0.7 15.4-3.2 19-10.2l6.4-12.3c6.5-12.5 19.7-20.4 34.2-20.5h25.4c11.6 0 22.5 5.7 28.9 15.1l2.7 4L853 384.8c-1.5-0.8-2.9-1.6-4.4-2.4z" fill="#A4BEFF"/>
                <path d="M954.3 339.6l3.6 5.4c7.3 10.8 2.9 25.5-9.4 30.8L882 407.1c-8.6-8.8-18.3-16.3-29-22.3l101.3-45.2z" fill="#5B79FB"/>
              </svg>
            </div>
            <span class="item-label">车辆</span>
          </div>

          <!-- 箭头1：车辆 -> 停车区域 -->
          <div class="arrow-container arrow-1" :class="{ active: parkingForm.areaId }">
            <svg class="arrow-svg" viewBox="0 0 200 60">
              <defs>
                <linearGradient id="arrowGradient1" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#1677ff" />
                  <stop offset="100%" stop-color="#0958d9" />
                </linearGradient>
              </defs>
              <path d="M10 30 L170 30" stroke="url(#arrowGradient1)" stroke-width="4" fill="none" marker-end="url(#arrowhead1)"/>
              <defs>
                <marker id="arrowhead1" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#0958d9"/>
                </marker>
              </defs>
              <circle r="6" fill="#1677ff">
                <animate attributeName="cx" from="10" to="170" dur="1.5s" repeatCount="indefinite"/>
              </circle>
            </svg>
          </div>

          <!-- 停车区域 -->
          <div class="animation-item pile-item" :class="{ selected: parkingForm.areaId }">
            <div class="icon-wrapper" v-if="parkingForm.areaId">
              <svg t="1781949669549" class="pile-svg" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5556">
                <path d="M535.27241 618.35664a25.134528 25.134528 0 0 1 30.254525 10.007266H605.090544v-23.272711h-69.818134zM628.363256 628.363906h37.236338a28.392708 28.392708 0 0 1 23.272711-11.636356 27.229072 27.229072 0 0 1 9.774539 2.094544V605.091195H628.363256zM543.650586 418.909503a90.996302 90.996302 0 0 0 29.556344-19.549077 95.883571 95.883571 0 0 0 20.247258-29.090889 88.203576 88.203576 0 0 0 7.447268-35.839976 84.479942 84.479942 0 0 0-7.21454-35.374521 80.989036 80.989036 0 0 0-19.549078-27.694527 91.694483 91.694483 0 0 0-29.090889-18.618169 98.210842 98.210842 0 0 0-36.538157-6.749086h-128.930821v180.130786h128.930821a86.341759 86.341759 0 0 0 35.141794-7.214541z" fill="#5C88FF" p-id="5557"></path>
                <path d="M511.999699 116.364255a325.81796 325.81796 0 1 0 325.817959 325.81796A325.81796 325.81796 0 0 0 511.999699 116.364255z m-132.421728 558.545074h-50.734511V200.844198h179.665332a151.039897 151.039897 0 0 1 61.672685 11.869082 142.196267 142.196267 0 0 1 44.916333 31.185434 127.069004 127.069004 0 0 1 27.229072 43.51997 133.352636 133.352636 0 0 1 9.309085 48.40724 139.636268 139.636268 0 0 1-9.541812 50.269056 135.912635 135.912635 0 0 1-27.694526 43.519971 131.258092 131.258092 0 0 1-44.916333 30.487252 157.323529 157.323529 0 0 1-60.974504 11.403628h-128.930821z m343.970674-29.090889h-6.050905a29.090889 29.090889 0 0 1-58.181778 0h-87.272668a29.090889 29.090889 0 0 1-58.181779 0h-6.050905a14.196354 14.196354 0 0 1-12.799991-20.712713l14.894535-29.556344a13.963627 13.963627 0 0 1 12.567265-7.912722h27.461799a6.749086 6.749086 0 0 1-6.981813-6.981813 80.756309 80.756309 0 0 1 53.527236-22.109076h38.399974a168.959885 168.959885 0 0 1 53.527236 22.109076 6.981813 6.981813 0 0 1-6.981813 6.981813h27.229072a13.963627 13.963627 0 0 1 12.567264 7.912722l14.894535 29.556344a14.196354 14.196354 0 0 1-12.567264 20.712713z" fill="#5C88FF" p-id="5558"></path>
                <path d="M511.999699 0.000698a442.181517 442.181517 0 0 0-116.363558 869.003044A289.745257 289.745257 0 0 1 511.999699 1024a289.745257 289.745257 0 0 1 116.363557-154.996258A442.181517 442.181517 0 0 0 511.999699 0.000698z m0 814.544899a372.363382 372.363382 0 1 1 372.363382-372.363382 372.363382 372.363382 0 0 1-372.363382 372.363382z" fill="#5C88FF" p-id="5559"></path>
              </svg>
            </div>
            <div class="icon-wrapper placeholder" v-else>
              <svg viewBox="0 0 100 100" class="placeholder-svg">
                <rect width="100" height="100" fill="#f3f4f6" rx="10"/>
                <text x="50" y="55" text-anchor="middle" fill="#9ca3af" font-size="12">选择停车区域</text>
              </svg>
            </div>
            <span class="item-label">{{ parkingForm.areaId ? selectedAreaName : '停车区域' }}</span>
          </div>

          <!-- 箭头2：停车区域 -> 停车位 -->
          <div class="arrow-container arrow-2" :class="{ active: parkingForm.spaceId }">
            <svg class="arrow-svg" viewBox="0 0 200 60">
              <defs>
                <linearGradient id="arrowGradient2" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#3b82f6" />
                  <stop offset="100%" stop-color="#2563eb" />
                </linearGradient>
              </defs>
              <path d="M10 30 L170 30" stroke="url(#arrowGradient2)" stroke-width="4" fill="none"/>
              <defs>
                <marker id="arrowhead2" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#2563eb"/>
                </marker>
              </defs>
              <path d="M10 30 L170 30" stroke="url(#arrowGradient2)" stroke-width="4" fill="none" marker-end="url(#arrowhead2)"/>
              <circle r="6" fill="#3b82f6">
                <animate attributeName="cx" from="10" to="170" dur="1.2s" repeatCount="indefinite"/>
              </circle>
            </svg>
          </div>

          <!-- 停车位 -->
          <div class="animation-item gun-item" :class="{ selected: parkingForm.spaceId }">
            <div class="icon-wrapper" v-if="parkingForm.spaceId">
              <svg t="1781949864317" class="gun-svg" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10140">
                <path d="M944.128 824.32c-1.536-13.312-2.048-26.624-2.56-39.936 0-6.144-2.048-9.728-9.216-10.24-9.216-0.512-18.432-3.072-27.136-4.608-8.704-1.536-14.336-8.704-12.8-15.872 1.536-7.68 8.704-11.776 17.408-10.24 9.216 1.536 18.944 3.584 28.16 5.12 2.048-8.704-0.512-14.848-9.728-20.992-26.112-17.408-54.784-29.696-84.992-37.888-33.792-9.216-68.608-16.384-102.4-24.576-3.584-1.024-7.68-2.56-10.752-5.12-38.4-32.256-80.384-59.904-125.44-81.92-39.424-19.456-81.92-25.6-124.928-28.672-46.592-3.072-92.672 0-138.24 6.656-21.504 3.072-41.472 11.264-59.392 22.528-28.16 17.408-56.32 35.328-83.456 54.272-14.848 10.752-29.696 14.848-47.104 10.24-8.192-2.048-16.384-2.56-24.576-4.096-10.752-1.536-17.92 2.048-19.456 10.752-1.536 9.216 3.584 15.872 14.336 17.92 2.048 0.512 4.096 1.024 6.144 1.024 0 0.512 0 1.024 0.512 1.536-8.704 3.072-16.896 6.656-25.6 9.216-18.944 6.656-21.504 9.216-22.016 29.696v9.216h35.328c9.728 0 16.384 5.12 16.384 12.8 0.512 8.192-6.656 14.336-16.384 14.336h-35.328V813.056c0 5.12-1.536 8.704-5.632 12.288s-6.144 10.24-8.192 15.872c-1.024 2.56 0 5.632 0 8.704-1.536 23.552 9.216 28.16 26.624 30.208 6.656 0.512 13.824 1.536 21.504 2.56-3.072-44.544 10.24-81.92 43.52-110.592 24.576-20.992 53.248-30.72 85.504-29.696 24.064 1.024 46.08 8.192 65.536 22.016 19.968 13.824 34.816 32.256 44.032 54.784 9.216 22.016 11.776 45.056 8.192 69.12H660.48c-5.632-48.64 9.216-89.6 47.616-120.32 26.624-20.992 57.344-28.672 91.136-25.088 60.416 6.656 121.856 66.048 107.008 151.04 12.8-2.048 25.088-4.096 37.376-6.144 2.56-0.512 5.12-3.072 7.168-5.12 9.216-9.728 7.68-39.424-2.048-48.64-2.56-1.536-4.608-5.632-4.608-9.728z" fill="#008AFF" p-id="10141"></path>
                <path d="M277.504 675.328c28.672-41.472 63.488-70.656 113.152-79.36 2.048 29.696 4.608 59.392 6.656 89.088-39.936-3.072-79.36-6.656-119.808-9.728zM456.704 746.496c-5.632 0.512-10.752 0-16.384 0h-17.92c-7.68-1.024-13.824-7.68-13.312-14.848 0.512-6.656 6.656-12.288 14.336-12.288 10.752 0 22.016-0.512 32.768 0 8.192 0 14.336 5.632 14.336 13.312 0.512 7.68-5.12 13.312-13.824 13.824zM427.008 688.128c-2.56-32.256-4.608-63.488-6.656-94.72 102.912-11.776 189.952 23.04 268.288 89.6-88.064 19.968-174.592 9.728-261.632 5.12z" fill="#B7D3FF" p-id="10142"></path>
                <path d="M911.36 430.592c0 2.56 0 4.608 0 0zM911.36 434.176V430.08 434.176zM911.36 430.08v-3.584V430.08zM911.36 423.424v0z" fill="#59A2EC" p-id="10143"></path>
                <path d="M601.088 475.136c17.92-1.024 36.352-0.512 54.272-0.512h44.032c18.432 0 36.864 0.512 55.296 0.512h35.84c31.232 0 62.976 0.512 94.208 0 12.8 0 25.6-1.024 37.376-10.24 14.336-11.776 20.992-26.624 20.992-43.52 0.512-95.744 0.512-191.488 0-287.232 0-24.576-10.752-45.056-38.912-52.224-3.072-1.024-6.144-1.024-9.216-1.024h-296.96c-23.552 0-47.616 22.016-48.128 47.104-0.512 28.672 0 57.856 0 86.528 0 38.4-0.512 76.8-0.512 115.712 0 32.256-1.024 64 0 96.256 0.512 29.184 27.648 49.664 51.712 48.64z" fill="#B7D3FF" p-id="10144"></path>
                <path d="M911.36 428.032v0zM911.36 430.08c0 0.512 0 0.512 0 0v-1.024 1.024z" fill="#59A2EC" p-id="10145"></path>
                <path d="M239.104 776.192c-49.664 0-90.624 39.936-91.648 87.04-1.536 54.784 43.008 96.768 91.136 95.232 51.2 0 90.624-39.936 90.624-92.16 0-49.664-40.448-90.112-90.112-90.112z" fill="#008AFF" p-id="10146"></path>
                <path d="M239.104 901.632c-17.92 0.512-34.816-15.872-34.816-34.304-0.512-17.92 15.872-34.816 34.304-34.816 17.92 0 34.304 16.384 34.304 34.304 0 18.432-15.872 34.304-33.792 34.816z" fill="#B7D3FF" p-id="10147"></path>
                <path d="M787.456 776.192c-53.248-1.536-94.72 41.984-94.72 90.112 0.512 51.712 39.424 92.16 89.6 92.16 52.224 0 91.648-39.424 91.648-91.648 1.024-48.64-39.424-89.6-86.528-90.624z" fill="#008AFF" p-id="10148"></path>
                <path d="M783.872 901.632c-18.432-0.512-34.304-15.872-34.304-34.816 0-18.432 16.384-34.304 34.816-33.792 17.92 0.512 33.792 16.384 33.792 34.304 0 18.432-16.384 34.816-34.304 34.304z" fill="#B7D3FF" p-id="10149"></path>
                <path d="M705.536 330.752h10.752c18.944-0.512 38.4 0.512 56.832-2.048 72.192-11.264 95.744-77.312 71.168-131.584-15.872-34.816-47.104-48.128-83.456-49.152-34.816-1.024-70.144-0.512-104.96 0-2.56 0-5.12 0.512-7.68 0.512v260.096h57.344V330.752z" fill="#008AFF" p-id="10150"></path>
                <path d="M706.048 197.632c19.456 1.024 38.4 0.512 56.832 3.072 20.992 3.072 32.256 19.456 31.232 40.96-0.512 18.944-13.824 33.28-33.28 36.864l-10.752 1.536h-44.544c0.512-27.648 0.512-54.272 0.512-82.432z" fill="#B7D3FF" p-id="10151"></path>
              </svg>
            </div>
            <div class="icon-wrapper placeholder" v-else>
              <svg viewBox="0 0 100 100" class="placeholder-svg">
                <rect width="100" height="100" fill="#f3f4f6" rx="10"/>
                <text x="50" y="55" text-anchor="middle" fill="#9ca3af" font-size="12">选择停车位</text>
              </svg>
            </div>
            <span class="item-label">{{ parkingForm.spaceId ? selectedSpaceInfo : '停车位' }}</span>
          </div>
        </div>

        <!-- 停车信息卡片 -->
        <div class="info-card" v-if="parkingForm.areaId || parkingForm.spaceId">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">停车区域</span>
              <span class="info-value">{{ selectedAreaName || '--' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">停车位</span>
              <span class="info-value">{{ selectedSpaceInfo || '--' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">面积</span>
              <span class="info-value">{{ selectedSpaceSize || '--' }} ㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span class="info-value status-active">空闲</span>
            </div>
          </div>
        </div>

        <div class="tips-card">
          <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="15" x2="12" y2="9"></line>
            <line x1="9" y1="12" x2="15" y2="12"></line>
          </svg>
          <div class="tip-content">
            <p><strong>温馨提示：</strong></p>
            <ul>
              <li>只能选择状态为"空闲"的停车位</li>
              <li>停车登记后请勿随意离开停车位</li>
              <li>结束停车后请及时取车</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 网格登记弹窗 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="登记停车"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="register-info">
        <p><span class="register-label">停车区域：</span>{{ selectedGridAreaName }}</p>
        <p><span class="register-label">停车位：</span>{{ selectedGridSpace?.spaceCode }}</p>
      </div>
      <div class="form-item">
        <label>车牌号</label>
        <el-input
          v-model="registerCarNumber"
          placeholder="请输入车牌号（如：京A12345）"
          maxlength="20"
        />
      </div>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="registerLoading"
            @click="handleRegisterConfirm"
          >
            确认登记
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showSuccessDialog"
      title="停车登记成功"
      :show-close="false"
      width="400px"
    >
      <div class="success-content">
        <div class="success-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>
        <h3>停车记录已创建</h3>
        <p class="order-no">记录编号：{{ orderInfo.recordNo }}</p>
        <p class="order-time">开始时间：{{ formatDateTime(orderInfo.startTime) }}</p>
      </div>
      <div class="dialog-actions">
        <el-button @click="showSuccessDialog = false">关闭</el-button>
        <el-button type="primary" @click="goToOrderList">查看记录</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import parkApi from '@/api/park'
import recordApi from '@/api/record'

const router = useRouter()

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})

service.interceptors.request.use(
  (config) => {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        const role = parseInt(user.role)
        config.headers['X-User-Id'] = user.id
        config.headers['X-User-Role'] = isNaN(role) ? '' : role.toString()
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    if (response.data && response.data.code === 200) {
      return response.data
    } else {
      ElMessage.error(response.data?.msg || '请求失败')
      return Promise.reject(new Error(response.data?.msg || '请求失败'))
    }
  },
  (error) => {
    console.error('响应错误:', error)
    let message = error.response?.data?.msg || '网络错误，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

const parkingForm = reactive({
  areaId: null,
  spaceId: null,
  carNumber: ''
})

const areaList = ref([])
const spaceList = ref([])
const currentUsername = ref('')
const loadingAreas = ref(false)
const loadingSpaces = ref(false)
const isParking = ref(false)
const showSuccessDialog = ref(false)
const orderInfo = ref({})

// 网格化相关状态
const activeAreaId = ref(null)
const gridSpaceList = ref([])
const loadingGridSpaces = ref(false)
const selectedGridSpace = ref(null)
const registerDialogVisible = ref(false)
const registerCarNumber = ref('')
const registerLoading = ref(false)

const selectedAreaName = computed(() => {
  const area = areaList.value.find(p => p.id === parkingForm.areaId)
  return area ? area.areaName : ''
})

const selectedSpaceInfo = computed(() => {
  const space = spaceList.value.find(g => g.id === parkingForm.spaceId)
  return space ? space.spaceName : ''
})

const selectedSpaceSize = computed(() => {
  const space = spaceList.value.find(g => g.id === parkingForm.spaceId)
  return space ? space.areaSize : null
})

// 左侧表单只展示空闲停车位
const availableSpaceList = computed(() => {
  return spaceList.value.filter(space => space.status === 0)
})

const selectedGridArea = computed(() => {
  return areaList.value.find(area => area.id === activeAreaId.value)
})

const selectedGridAreaName = computed(() => {
  return selectedGridArea.value ? selectedGridArea.value.areaName : ''
})

const loadAreas = async () => {
  loadingAreas.value = true
  try {
    const response = await service.get('/area/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    if (response.data && response.data.records) {
      areaList.value = response.data.records.filter(area => area.status === 1)
      if (areaList.value.length > 0) {
        activeAreaId.value = areaList.value[0].id
        await loadGridSpaces(activeAreaId.value)
      }
    }
  } catch (error) {
    console.error('加载停车区域列表失败:', error)
    ElMessage.error('加载停车区域列表失败: ' + error.message)
  } finally {
    loadingAreas.value = false
  }
}

const loadSpacesByAreaId = async (areaId) => {
  if (!areaId) {
    spaceList.value = []
    parkingForm.spaceId = null
    return
  }
  loadingSpaces.value = true
  try {
    const response = await service.get(`/space/area/${areaId}`)
    if (response.data) {
      // 改造后不再过滤状态，所有状态均返回，由使用方自行筛选
      spaceList.value = response.data
    }
    parkingForm.spaceId = null
  } catch (error) {
    console.error('加载停车位列表失败:', error)
    ElMessage.error('加载停车位列表失败: ' + error.message)
  } finally {
    loadingSpaces.value = false
  }
}

const loadGridSpaces = async (areaId) => {
  if (!areaId) {
    gridSpaceList.value = []
    return
  }
  loadingGridSpaces.value = true
  try {
    const res = await parkApi.spaceApi.getSpacesByAreaId(areaId)
    gridSpaceList.value = res.data || []
  } catch (error) {
    console.error('加载网格停车位失败:', error)
    ElMessage.error('加载网格停车位失败: ' + error.message)
  } finally {
    loadingGridSpaces.value = false
  }
}

const handleAreaChange = (areaId) => {
  loadSpacesByAreaId(areaId)
}

const handleTabChange = (areaId) => {
  activeAreaId.value = areaId
  loadGridSpaces(areaId)
}

const getSpaceStatusClass = (status) => {
  if (status === 0) return 'free'
  if (status === 1) return 'occupied'
  return 'fault'
}

const handleSpaceClick = (space) => {
  if (space.status !== 0) {
    ElMessage.warning('该停车位不可使用，请选择空闲车位')
    return
  }
  selectedGridSpace.value = space
  registerCarNumber.value = ''
  registerDialogVisible.value = true
}

const handleRegisterConfirm = async () => {
  if (!selectedGridSpace.value) return

  if (!registerCarNumber.value || registerCarNumber.value.trim() === '') {
    ElMessage.warning('请输入车牌号')
    return
  }

  registerLoading.value = true
  try {
    const res = await recordApi.simulateCreateRecord({
      spaceId: selectedGridSpace.value.id,
      carNumber: registerCarNumber.value.trim()
    })
    registerDialogVisible.value = false
    await loadGridSpaces(activeAreaId.value)
    if (res.data) {
      orderInfo.value = res.data
      showSuccessDialog.value = true
    }
  } catch (error) {
    console.error('网格登记停车失败:', error)
    ElMessage.error(error.message || '停车登记失败')
  } finally {
    registerLoading.value = false
  }
}

const handleStartParking = async () => {
  if (!parkingForm.areaId || !parkingForm.spaceId) {
    ElMessage.warning('请先选择停车区域和停车位')
    return
  }

  if (!parkingForm.carNumber || parkingForm.carNumber.trim() === '') {
    ElMessage.warning('请输入车牌号')
    return
  }

  isParking.value = true
  try {
    const requestData = {
      spaceId: parkingForm.spaceId,
      carNumber: parkingForm.carNumber.trim()
    }
    console.log('创建停车记录请求数据:', requestData)
    const response = await service.post('/record/simulateCreate', requestData)
    console.log('创建停车记录响应数据:', response)
    if (response.data) {
      orderInfo.value = response.data
      showSuccessDialog.value = true
      await loadGridSpaces(activeAreaId.value)
    }
  } catch (error) {
    console.error('创建停车记录失败:', error)
    ElMessage.error('创建停车记录失败: ' + error.message)
  } finally {
    isParking.value = false
  }
}

const goToOrderList = () => {
  showSuccessDialog.value = false
  router.push('/system/order')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const initUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      currentUsername.value = user.username || user.name || '用户'
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

onMounted(() => {
  initUserInfo()
  loadAreas()
})
</script>

<style scoped>
.parking-container {
  padding: 24px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 网格化区域 */
.grid-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.area-tabs {
  margin-bottom: 16px;
}

.grid-legend {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4b5563;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

.legend-dot.free {
  background: #1677ff;
}

.legend-dot.occupied {
  background: #f56c6c;
}

.legend-dot.fault {
  background: #c0c4cc;
}

.grid-scroll-wrapper {
  overflow-x: auto;
}

.space-grid {
  display: grid;
  grid-template-columns: repeat(10, minmax(56px, 1fr));
  gap: 8px;
  min-width: 640px;
}

.space-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 8px 4px;
  border-radius: 6px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  transition: all 0.2s ease;
  user-select: none;
}

.space-cell.free {
  background: #1677ff;
  cursor: pointer;
}

.space-cell.free:hover {
  background: #4096ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.space-cell.occupied {
  background: #f56c6c;
  cursor: not-allowed;
}

.space-cell.fault {
  background: #c0c4cc;
  cursor: not-allowed;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 24px;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.section-title .icon {
  width: 20px;
  height: 20px;
  color: #1677ff;
}

.form-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.user-info .user-icon {
  width: 24px;
  height: 24px;
  color: #1677ff;
}

.user-info span {
  font-size: 14px;
  color: #374151;
}

.form-actions {
  margin-top: 24px;
}

.form-actions .el-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.btn-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.animation-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.animation-container {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
  min-height: 200px;
}

.animation-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.6;
  transform: scale(0.8);
}

.animation-item.selected {
  opacity: 1;
  transform: scale(1);
}

.icon-wrapper {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.animation-item.selected .icon-wrapper {
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.1) 0%, rgba(22, 119, 255, 0.05) 100%);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.15);
  transform: scale(1.05);
}

.icon-wrapper.placeholder {
  background: #f3f4f6;
}

.car-svg, .pile-svg, .gun-svg {
  width: 100px;
  height: 100px;
  transition: all 0.3s ease;
}

.placeholder-svg {
  width: 80px;
  height: 80px;
}

.item-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.arrow-container {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.arrow-container.active {
  opacity: 1;
  transform: scale(1);
}

.arrow-svg {
  width: 80px;
  height: 30px;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-label {
  font-size: 14px;
  color: #6b7280;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.status-active {
  color: #1677ff;
}

.tips-card {
  background: linear-gradient(135deg, #f0f5ff 0%, #dce7ff 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
}

.tip-icon {
  width: 24px;
  height: 24px;
  color: #1677ff;
  flex-shrink: 0;
}

.tip-content p {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #0c4a6e;
}

.tip-content ul {
  margin: 0;
  padding-left: 20px;
}

.tip-content li {
  font-size: 13px;
  color: #0958d9;
  margin-bottom: 4px;
}

.register-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.register-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #374151;
}

.register-info p:last-child {
  margin-bottom: 0;
}

.register-label {
  color: #6b7280;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  animation: successPulse 0.5s ease-out;
}

.success-icon svg {
  width: 40px;
  height: 40px;
}

@keyframes successPulse {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.order-no {
  font-size: 14px;
  color: #374151;
  margin: 8px 0;
  font-family: 'Courier New', monospace;
}

.order-time {
  font-size: 14px;
  color: #6b7280;
  margin: 8px 0;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.lightning-bolt {
  animation: lightningPulse 1.5s ease-in-out infinite;
}

@keyframes lightningPulse {
  0%, 100% {
    filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5));
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(255, 215, 0, 0.8));
  }
}
</style>
