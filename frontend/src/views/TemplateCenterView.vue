<template>
  <div class="page-card">
    <h2 class="page-title">图表模板与报告导出</h2>

    <el-row :gutter="14">
      <el-col :span="10">
        <el-card shadow="never">
          <h3>{{ editingId ? "编辑模板" : "新建模板" }}</h3>
          <el-form :model="form" label-width="100px">
            <el-form-item label="模板名称"><el-input v-model="form.templateName" /></el-form-item>
            <el-form-item label="图表类型">
              <el-select v-model="form.chartType">
                <el-option label="折线图" value="LINE" />
                <el-option label="柱状图" value="BAR" />
                <el-option label="饼图" value="PIE" />
                <el-option label="地图图表" value="MAP" />
              </el-select>
            </el-form-item>
            <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
            <el-form-item label="配色 JSON"><el-input v-model="form.colorScheme" type="textarea" rows="3" /></el-form-item>
            <el-form-item label="坐标 JSON"><el-input v-model="form.axisConfig" type="textarea" rows="2" /></el-form-item>
            <el-form-item label="格式 JSON"><el-input v-model="form.dataFormat" type="textarea" rows="2" /></el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveTemplate">保存</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <div class="row-head">
            <h3>模板列表</h3>
            <div>
              <el-select v-model="recommendDataType" size="mini" style="width: 120px">
                <el-option label="趋势" value="TREND" />
                <el-option label="排行" value="RANK" />
                <el-option label="占比" value="PROPORTION" />
                <el-option label="地理分布" value="GEO" />
              </el-select>
              <el-button size="mini" @click="recommend">智能推荐</el-button>
            </div>
          </div>
          <el-alert v-if="recommendResult" :title="recommendResult" type="info" show-icon style="margin-bottom: 10px" />
          <el-table :data="templates" size="small">
            <el-table-column prop="templateName" label="名称" min-width="130" />
            <el-table-column prop="chartType" label="类型" width="90" />
            <el-table-column prop="title" label="标题" min-width="140" />
            <el-table-column prop="createdBy" label="创建人" width="100" />
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="edit(scope.row)">编辑</el-button>
                <el-button type="text" @click="remove(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" style="margin-top: 12px">
          <h3>报告导出</h3>
          <el-form :inline="true" :model="exportForm">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="exportRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
            <el-form-item label="导出格式">
              <el-select v-model="exportForm.format" style="width: 120px">
                <el-option label="Excel" value="excel" />
                <el-option label="PDF" value="pdf" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="exportData">导出</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { saveAs } from "file-saver";
import {
  listTemplates,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  recommendTemplate,
  exportReport
} from "../api";

export default {
  name: "TemplateCenterView",
  data() {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);
    return {
      templates: [],
      editingId: null,
      form: {
        templateName: "",
        chartType: "LINE",
        title: "月度销售复盘",
        colorScheme: '["#355C7D", "#F67280"]',
        axisConfig: '{"x":"日期","y":"销售额"}',
        dataFormat: '{"currency":"CNY"}',
        createdBy: "admin"
      },
      recommendDataType: "TREND",
      recommendResult: "",
      exportForm: {
        format: "excel"
      },
      exportRange: [this.formatDate(start), this.formatDate(end)]
    };
  },
  mounted() {
    this.loadTemplates();
  },
  methods: {
    formatDate(date) {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return `${yyyy}-${mm}-${dd}`;
    },
    async loadTemplates() {
      const res = await listTemplates();
      this.templates = res.data;
    },
    async saveTemplate() {
      if (this.editingId) {
        await updateTemplate(this.editingId, this.form);
      } else {
        await createTemplate(this.form);
      }
      this.$message.success("模板保存成功");
      this.resetForm();
      this.loadTemplates();
    },
    edit(row) {
      this.editingId = row.id;
      this.form = {
        templateName: row.templateName,
        chartType: row.chartType,
        title: row.title,
        colorScheme: row.colorScheme || "",
        axisConfig: row.axisConfig || "",
        dataFormat: row.dataFormat || "",
        createdBy: row.createdBy || "admin"
      };
    },
    resetForm() {
      this.editingId = null;
      this.form = {
        templateName: "",
        chartType: "LINE",
        title: "",
        colorScheme: "",
        axisConfig: "",
        dataFormat: "",
        createdBy: "admin"
      };
    },
    async remove(id) {
      await deleteTemplate(id);
      this.$message.success("模板删除成功");
      this.loadTemplates();
    },
    async recommend() {
      const res = await recommendTemplate({ dataType: this.recommendDataType });
      this.recommendResult = `推荐图表：${res.data.chartType}，推荐理由：${res.data.reason}`;
    },
    async exportData() {
      const payload = {
        format: this.exportForm.format,
        fileName: `销售分析报告-${Date.now()}`,
        filter: {
          startDate: this.exportRange[0],
          endDate: this.exportRange[1],
          granularity: "DAY",
          paymentStatuses: ["PAID"]
        }
      };
      const blob = await exportReport(payload);
      const ext = this.exportForm.format === "pdf" ? "pdf" : "xlsx";
      saveAs(blob, `${payload.fileName}.${ext}`);
      this.$message.success("报告导出成功");
    }
  }
};
</script>

<style scoped>
h3 {
  margin: 0 0 12px;
}

.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
</style>

