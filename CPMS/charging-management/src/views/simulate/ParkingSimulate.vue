<template>
  <div class="parking-container">
    <div class="page-header">
      <h2>模拟停车</h2>
      <p>选择停车区域和停车位，登记停车记录</p>
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
              v-for="space in spaceList"
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
                  <stop offset="0%" stop-color="#10b981" />
                  <stop offset="100%" stop-color="#059669" />
                </linearGradient>
              </defs>
              <path d="M10 30 L170 30" stroke="url(#arrowGradient1)" stroke-width="4" fill="none" marker-end="url(#arrowhead1)"/>
              <defs>
                <marker id="arrowhead1" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#059669"/>
                </marker>
              </defs>
              <circle r="6" fill="#10b981">
                <animate attributeName="cx" from="10" to="170" dur="1.5s" repeatCount="indefinite"/>
              </circle>
            </svg>
          </div>

          <!-- 停车区域 -->
          <div class="animation-item pile-item" :class="{ selected: parkingForm.areaId }">
            <div class="icon-wrapper" v-if="parkingForm.areaId">
              <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" class="pile-svg">
                <path d="M511.6928 514.2016m-450.816 0a450.816 450.816 0 1 0 901.632 0 450.816 450.816 0 1 0-901.632 0Z" fill="#E9F4FF"/>
                <path d="M789.7088 733.3376H742.4V256.8192a67.8912 67.8912 0 0 0-69.0688-66.56H349.9008a67.8912 67.8912 0 0 0-69.0688 66.56v476.5184h-47.1552a28.672 28.672 0 1 0 0 57.344h556.032a28.672 28.672 0 1 0 0-57.344z" fill="#2595E8"/>
                <path d="M789.7088 733.3376H742.4V256.8192a67.8912 67.8912 0 0 0-69.0688-66.56H349.9008a67.8912 67.8912 0 0 0-69.0688 66.56v476.5184h-47.1552a28.672 28.672 0 1 0 0 57.344h248.2176A452.0448 452.0448 0 0 0 742.4 549.2224z" fill="#3A9CED"/>
                <path d="M367.0528 297.3696a40.96 40.96 0 0 1 41.6768-40.192h205.9264a40.96 40.96 0 0 1 41.6768 40.192v87.9104a40.96 40.96 0 0 1-41.6768 40.192H408.7296a40.96 40.96 0 0 1-41.6768-40.192z" fill="#59ADF8"/>
                <path d="M426.496 314.5728h170.3424V368.128H426.496z" fill="#59ADF8"/>
                <g class="lightning-bolt">
                  <path d="M450.56 520a29.696 29.696 0 0 1-27.1872-17.152 27.904 27.904 0 0 1 5.12-30.72L497.664 416a30.72 30.72 0 0 1 42.0352-1.6384 28.0064 28.0064 0 0 1 1.7408 40.4992l-22.9888 24.1152h54.2208a29.7472 29.7472 0 0 1 27.4432 17.664 27.904 27.904 0 0 1-6.4 31.232l-112.1792 108.8a30.72 30.72 0 0 1-42.0352 0z" fill="#FFD700"/>
                </g>
                <circle cx="512" cy="680" r="20" fill="#10b981">
                  <animate attributeName="opacity" values="1;0.5;1" dur="1s" repeatCount="indefinite"/>
                </circle>
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
              <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" class="gun-svg">
                <path d="M213.333333 231.722667h72.106667a21.333333 21.333333 0 0 1 21.333333 21.333333v131.904a21.333333 21.333333 0 0 1-21.333333 21.333333H213.333333a21.333333 21.333333 0 0 1-21.333333-21.333333v-131.904a21.333333 21.333333 0 0 1 21.333333-21.333333z" fill="#1FA4FE"/>
                <path d="M294.506667 192h248.789333a21.333333 21.333333 0 0 1 19.136 11.904l175.424 356.245333a21.333333 21.333333 0 0 1-9.770667 28.586667l-125.653333 61.568a21.333333 21.333333 0 0 1-28.245333-9.130667l-102.016-191.68a21.333333 21.333333 0 0 0-18.837334-11.306666h-158.826666a21.333333 21.333333 0 0 1-21.333334-21.333334V213.333333a21.333333 21.333333 0 0 1 21.333334-21.333333z" fill="#1FA4FE"/>
                <path d="M703.36 232.021333a21.333333 21.333333 0 0 1 19.498667 12.650667l107.541333 241.28a21.333333 21.333333 0 0 1-19.477333 30.016h-57.92a21.333333 21.333333 0 0 1-19.626667-12.949333l-25.941333-60.8a21.333333 21.333333 0 0 0-21.248-12.906667l-63.509334 4.842667a21.333333 21.333333 0 0 1-21.226666-12.864l-68.416-159.530667a21.333333 21.333333 0 0 1 19.605333-29.738667h150.741333z" fill="#1FA4FE"/>
                <path d="M700.757333 685.44c65.6 101.034667 53.44 159.765333-38.08 161.749333l-4.906666 0.064v-21.824c59.136 0 76.928-23.637333 52.544-78.485333l-3.776-8.042667a303.786667 303.786667 0 0 0-6.72-12.885333l-5.205334-9.109333-5.781333-9.6-6.357333-10.005334 18.282666-11.882666z" fill="#1FA4FE"/>
                <circle cx="720" cy="700" r="15" fill="#10b981">
                  <animate attributeName="opacity" values="1;0.3;1" dur="0.8s" repeatCount="indefinite"/>
                </circle>
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
        <p class="order-no">记录编号：{{ orderInfo.orderNo }}</p>
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

const loadAreas = async () => {
  loadingAreas.value = true
  try {
    const response = await service.get('/area/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    if (response.data && response.data.records) {
      areaList.value = response.data.records.filter(area => area.status === 1)
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
      spaceList.value = response.data.filter(space => space.status === 0)
    }
    parkingForm.spaceId = null
  } catch (error) {
    console.error('加载停车位列表失败:', error)
    ElMessage.error('加载停车位列表失败: ' + error.message)
  } finally {
    loadingSpaces.value = false
  }
}

const handleAreaChange = (areaId) => {
  loadSpacesByAreaId(areaId)
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
  color: #10b981;
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
  color: #10b981;
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
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1) 0%, rgba(34, 197, 94, 0.05) 100%);
  box-shadow: 0 8px 24px rgba(34, 197, 94, 0.15);
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
  color: #10b981;
}

.tips-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
}

.tip-icon {
  width: 24px;
  height: 24px;
  color: #10b981;
  flex-shrink: 0;
}

.tip-content p {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #065f46;
}

.tip-content ul {
  margin: 0;
  padding-left: 20px;
}

.tip-content li {
  font-size: 13px;
  color: #059669;
  margin-bottom: 4px;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
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
