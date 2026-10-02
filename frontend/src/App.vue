<template>
  <div id="app" class="app-shell">
    <aside class="sidebar">
      <div class="brand">销售洞察系统</div>
      <el-menu :default-active="activePath" class="menu" router>
        <el-menu-item index="/dashboard">销售分析看板</el-menu-item>
        <el-menu-item index="/data-governance">数据接入与治理</el-menu-item>
        <el-menu-item index="/alerts">预警中心</el-menu-item>
        <el-menu-item index="/templates">模板与导出</el-menu-item>
      </el-menu>
    </aside>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  computed: {
    activePath() {
      return this.$route.path;
    }
  }
};
</script>

<style>
:root {
  --bg-dark: #0f1b2d;
  --panel: #ffffff;
  --accent: #e96f2c;
  --accent-2: #2a9d8f;
  --line: #dfe6ef;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif;
  background: radial-gradient(circle at top left, #0f1b2d, #152c46 60%, #1d3a58);
}

.app-shell {
  min-height: 100vh;
  display: flex;
  color: #172b4d;
}

.sidebar {
  width: 240px;
  background: rgba(10, 20, 36, 0.88);
  backdrop-filter: blur(6px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  padding: 18px 12px;
}

.brand {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 4px 12px 18px;
  letter-spacing: 0.4px;
}

.menu {
  background: transparent;
  border-right: 0;
}

.menu .el-menu-item {
  color: #d0d9e6;
  border-radius: 8px;
  margin-bottom: 4px;
}

.menu .el-menu-item.is-active {
  color: #fff;
  background: linear-gradient(90deg, var(--accent), #ff9d57);
}

.content {
  flex: 1;
  padding: 20px;
}

.page-card {
  background: var(--panel);
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(5, 20, 40, 0.12);
  padding: 16px;
}

.page-title {
  margin: 0 0 14px;
  font-size: 22px;
}

@media (max-width: 960px) {
  .app-shell {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    padding: 10px;
  }

  .brand {
    margin-bottom: 8px;
  }
}
</style>

