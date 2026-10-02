<template>
  <div class="page-card">
    <h2 class="page-title">数据接入与治理</h2>

    <el-row :gutter="14">
      <el-col :span="12">
        <el-card shadow="never" class="card-block">
          <h3>多源数据接入</h3>
          <el-form :model="syncForm" label-width="120px">
            <el-form-item label="数据源类型">
              <el-select v-model="syncForm.sourceType">
                <el-option label="MySQL" value="MYSQL" />
                <el-option label="Oracle" value="ORACLE" />
              </el-select>
            </el-form-item>
            <el-form-item label="JDBC URL">
              <el-input v-model="syncForm.jdbcUrl" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="syncForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="syncForm.password" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="runSync">外部源同步</el-button>
              <el-button @click="runScheduled">手动执行定时同步</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <h4>定时同步频率</h4>
          <el-form :inline="true">
            <el-form-item label="频率">
              <el-select v-model="syncFrequency" style="width: 140px">
                <el-option label="每小时" value="HOURLY" />
                <el-option label="每天" value="DAILY" />
                <el-option label="每周" value="WEEKLY" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSyncFrequency">保存</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <h4>离线导入（CSV/XLSX）</h4>
          <el-upload
            action="#"
            :show-file-list="false"
            :http-request="uploadFile"
            :before-upload="beforeUpload"
          >
            <el-button type="success">上传并导入</el-button>
          </el-upload>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="card-block">
          <h3>数据生命周期管理</h3>

          <el-form :inline="true" :model="retentionForm">
            <el-form-item label="保留天数">
              <el-input-number v-model="retentionForm.retentionDays" :min="1" :max="3650" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveRetention">保存策略</el-button>
              <el-button type="warning" @click="archiveData">归档过期数据</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <h4>备份与恢复</h4>
          <el-button type="primary" plain @click="createNewBackup">创建备份</el-button>
          <el-table :data="backups" size="small" style="margin-top: 10px">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="fileName" label="备份文件" min-width="180" />
            <el-table-column prop="backupAt" label="创建时间" min-width="160" />
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button type="text" @click="restore(scope.row.id)">恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      v-if="message"
      :title="message"
      type="success"
      show-icon
      style="margin-top: 14px"
    />
  </div>
</template>

<script>
import {
  uploadOfflineFile,
  syncExternal,
  triggerScheduledSync,
  archiveExpiredData,
  createBackup,
  restoreBackup,
  listBackups,
  getRetention,
  setRetention,
  getSyncFrequency,
  setSyncFrequency
} from "../api";

export default {
  name: "DataGovernanceView",
  data() {
    return {
      message: "",
      syncFrequency: "DAILY",
      syncForm: {
        sourceType: "MYSQL",
        jdbcUrl: "jdbc:mysql://localhost:3306/external_sales?useSSL=false",
        username: "root",
        password: "admin"
      },
      retentionForm: {
        retentionDays: 180
      },
      backups: []
    };
  },
  mounted() {
    this.loadRetention();
    this.loadBackups();
    this.loadSyncFrequency();
  },
  methods: {
    beforeUpload(file) {
      const valid = /\.csv$|\.xlsx$/i.test(file.name);
      if (!valid) {
        this.$message.error("仅支持 CSV/XLSX 文件");
      }
      return valid;
    },
    async uploadFile(param) {
      try {
        const formData = new FormData();
        formData.append("file", param.file);
        const res = await uploadOfflineFile(formData);
        this.message = `${res.data.message}，成功 ${res.data.successCount} 条，失败 ${res.data.failedCount} 条`;
        this.$message.success("导入完成");
      } catch (error) {
        this.$message.error(error.message || "导入失败");
      }
    },
    async runSync() {
      try {
        const res = await syncExternal(this.syncForm);
        this.message = res.data;
        this.$message.success("外部同步完成");
      } catch (error) {
        this.$message.error(error.message || "同步失败");
      }
    },
    async runScheduled() {
      try {
        const res = await triggerScheduledSync();
        this.message = res.data;
        this.$message.success("手动定时同步完成");
      } catch (error) {
        this.$message.error(error.message || "定时同步失败");
      }
    },
    async archiveData() {
      try {
        const res = await archiveExpiredData();
        this.message = `归档完成，影响订单数：${res.data}`;
        this.$message.success("归档完成");
      } catch (error) {
        this.$message.error(error.message || "归档失败");
      }
    },
    async createNewBackup() {
      try {
        await createBackup({ comment: "手动备份" });
        this.$message.success("备份创建成功");
        this.loadBackups();
      } catch (error) {
        this.$message.error(error.message || "备份失败");
      }
    },
    async restore(id) {
      try {
        await restoreBackup(id);
        this.$message.success("恢复完成");
      } catch (error) {
        this.$message.error(error.message || "恢复失败");
      }
    },
    async loadBackups() {
      const res = await listBackups();
      this.backups = res.data;
    },
    async loadRetention() {
      const res = await getRetention();
      this.retentionForm.retentionDays = res.data;
    },
    async saveRetention() {
      try {
        await setRetention(this.retentionForm);
        this.$message.success("保留策略已更新");
      } catch (error) {
        this.$message.error(error.message || "保存失败");
      }
    },
    async loadSyncFrequency() {
      try {
        const res = await getSyncFrequency();
        this.syncFrequency = res.data;
      } catch (error) {
        this.syncFrequency = "DAILY";
      }
    },
    async saveSyncFrequency() {
      try {
        await setSyncFrequency({ frequency: this.syncFrequency });
        this.$message.success("同步频率已更新");
      } catch (error) {
        this.$message.error(error.message || "保存失败");
      }
    }
  }
};
</script>

<style scoped>
.card-block {
  min-height: 620px;
}

h3 {
  margin: 0 0 12px;
}

h4 {
  margin: 0 0 8px;
}
</style>

